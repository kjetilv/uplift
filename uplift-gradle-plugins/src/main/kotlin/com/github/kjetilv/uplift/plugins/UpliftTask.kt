@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.Stack
import software.amazon.awssdk.services.cloudformation.model.StackResource
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration
import software.amazon.awssdk.services.lambda.model.FunctionUrlConfig
import software.amazon.awssdk.services.lambda.model.ListFunctionUrlConfigsRequest
import java.nio.file.Files
import java.nio.file.Files.getLastModifiedTime
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.*
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@Suppress("unused")
abstract class UpliftTask @Inject constructor(private var execOperations: ExecOperations) : DefaultTask() {

    init {
        group = "uplift"
    }

    @get:Input
    abstract val arch: Property<String>

    @get:Input
    abstract val env: MapProperty<String, String>

    @get:Input
    abstract val awsAuth: Property<String>

    @get:Input
    abstract val account: Property<String>

    @get:Input
    abstract val profile: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val stack: Property<String>

    @TaskAction
    fun upliftPerform() {
        selfCheck()
        initialize()
        perform()
    }

    fun env(vararg envs: Pair<String, String>): UpliftTask = this.apply {
        env.set(mapOf(*envs))
    }

    open fun stackWith(name: String) {}

    fun configure(
        account: String? = null,
        region: String? = null,
        arch: String? = null,
        stack: String? = null,
        profile: String? = null
    ): UpliftTask = this.apply {
        account?.also(this.account::set)
        region?.also(this.region::set)
        arch?.also(this.arch::set)
        stack?.also(this.stack::set)
        profile?.also(this.profile::set)
    }

    protected fun runCdk(cmd: String): ExecResult = docker(
        upliftDir(),
        "run " +
                volumes(
                    awsAuth.get() to "/root/.aws",
                    cdkApp() to "/opt/app",
                    upliftDir() to "/lambdas"
                ) +
                environment(
                    env.get()
                ) +
                "${"cdk-site:latest"} $cmd",
        execOperations
    )

    protected fun upliftDir(): Path = project.buildSubDirectory("uplift")

    protected fun clearCdkApp() = clearRecursive(cdkApp())

    protected abstract fun perform()

    protected fun profileOption() =
        profile.orNull?.let { "--profile=$it" }

    protected fun cdkApp(): Path = project.cdkApp()

    protected fun clearRecursive(paths: List<Path>): Unit =
        paths.forEach { path ->
            path.takeIf(Files::isDirectory)?.also { dir ->
                dir.also {
                    Files.list(it).forEach { file ->
                        clearRecursive(file)
                    }
                }.let {
                    dir.also(Files::delete)
                }
            } ?: path.also(Files::delete)
        }

    protected fun clearRecursive(vararg paths: Path): Unit =
        clearRecursive(paths.toList())

    protected fun <T> nonEmpty(): (List<T>) -> Boolean = { it.isNotEmpty() }

    protected fun ping(sources: List<Path>? = null) {
        withLambdaClient {
            cloudFormationClient { cloudFormationClient ->
                cloudFormationClient.stacks()
                    .filter(::current)
                    .forEach { stack ->
                        logStack(cloudFormationClient, stack, sources)
                    }
            }
        }
    }

    private fun logStack(client: CloudFormationClient, stack: Stack, sources: List<Path>? = null) {
        val stackResources = client.describeStackResources { builder ->
            builder.stackName(stack.stackName())
        }.stackResources().toList()
        logStack(stack, stackResources, sources, logger::warn, logger::lifecycle)
    }

    private fun logStack(
        stack: Stack,
        stackResources: List<StackResource>,
        sources: List<Path>? = null,
        warner: (String) -> Unit,
        printer: (String) -> Unit
    ) {
        if (stackResources.isEmpty()) {
            warner(
                """
                    ##
                    ## No resources found for stack `${stack.stackName()}`
                    ##   Stack id : ${stack.stackId()}
                    ##   Created  : ${stack.creationTime()}
                    ##   Modified : ${stack.lastUpdatedTime()}
                    ##
                    """.trimIndent()
            )
            return
        }
        val functions = stackResources.filter { it.actualFunction() }
        if (functions.isEmpty()) {
            warner(
                """
                    ##
                    ## No functions found for stack `${stack.stackName()}`
                    ##   Stack id : ${stack.stackId()}
                    ##   Created  : ${stack.creationTime()}
                    ##   Modified : ${stack.lastUpdatedTime()}
                    ##
                    """.trimIndent()
            )
            stackResources.forEach { resource ->
                printer("  ${resource.physicalResourceId()}:${resource.resourceType()}")
            }
            return
        }
        printer(
            """
                ##
                ## uplifted `${stack.stackName()}`
                ##
                ##   Stack id  : ${stack.stackId()}
                ##   Created   : ${stack.creationTime()}
                ##   Modified  : ${stack.lastUpdatedTime()}
                ##   Resources : ${stackResources.size}
                ##
                ##  Lambdas:
                """.trimIndent()
        )
        functions.forEachIndexed { f, stackResource ->
            withLambdaClient {
                functionConfiguration(stackResource)?.let { func: FunctionConfiguration ->
                    functionUrlConfigs(func).forEach { url ->
                        printer(
                            """
                            ##   [${f + 1}] ${func.functionName()}: ${
                                func.description()?.takeUnless(String::isBlank) ?: ""
                            }
                            ##    modified @ ${func.lastModified() ?: "<unknown>"}
                            ##    Function URL ${url.functionUrl()} 
                            ##      created @ ${url.creationTime()}
                            ##     modified @ ${url.lastModifiedTime()}
                            ##         cors : ${url.cors()}
                            ##         auth : ${url.authTypeAsString()}
                            """.trimIndent()
                        )
                    }
                }
            }
        }
        if (sources?.isNotEmpty() == true) {
            printer(
                """
                ##
                ##  Zips/binaries/jars:
                """.trimIndent()
            )
        }
        sources?.flatMap { zip ->
            listOf(zip) +
                    (binarySourceFor(zip)?.let { listOf(it) } ?: emptyList()) +
                    (zip.parent?.let {
                        Files.find(
                            it,
                            1,
                            { file, _ -> file.fileName.toString().endsWith(".jar") })
                    }?.toList() ?: emptyList())
        }?.sortedBy { getLastModifiedTime(it) }
            ?.map { path ->
                path to getLastModifiedTime(path).toInstant().truncatedTo(ChronoUnit.SECONDS)
            }
            ?.forEachIndexed { count, (path, time) ->
                printer(
                    """
                    ##   [${count + 1}] ${time.local(ISO_LOCAL_TIME)} ${time.local(ISO_LOCAL_DATE)} → ${path.fileName} 
                    ##     full path : ${path.toAbsolutePath().toUri()}
                    ##     real time : ${time.utc(ISO_ZONED_DATE_TIME)}
                    ##     size      : ${path.printSize(4)}
                    """.trimIndent()
                )
            }
    }

    private fun Instant.local(format: DateTimeFormatter) = atZone(ZoneId.systemDefault())!!.format(format)

    private fun Instant.utc(format: DateTimeFormatter) = atZone(ZoneId.of("UTC"))!!.format(format)

    private fun binarySourceFor(zip: Path) =
        zip.fileName.toString()
            .takeIf { it.endsWith(".zip") }
            ?.dropLast(".zip".length)
            ?.let { zip.parent?.resolve(it) }

    private fun LambdaClient.functionConfiguration(stackResource: StackResource) =
        functionConfigurations().firstOrNull { functionConfiguration ->
            functionConfiguration.functionName().equals(stackResource.physicalResourceId())
        }

    private fun StackResource.actualFunction() =
        resourceType() == "AWS::Lambda::Function" && !physicalResourceId().contains("-LogRetention")

    private fun current(stack: Stack) =
        stack.stackName() == this.stack.get()

    private val awsRegion get() = Region.of(this.region.get())

    private val credentialsProvider get() = ProfileCredentialsProvider.create(this.profile.get())

    private fun CloudFormationClient.stacks() =
        this.describeStacks().stacks().toList()

    private fun LambdaClient.functionUrlConfigs(function: FunctionConfiguration): List<FunctionUrlConfig> =
        listFunctionUrlConfigs(listFunctionUrlConfigsRequest(function)).functionUrlConfigs().toList()

    private fun listFunctionUrlConfigsRequest(function: FunctionConfiguration) =
        ListFunctionUrlConfigsRequest.builder().functionName(function.functionName()).build()

    private fun LambdaClient.functionConfigurations() =
        listFunctions().functions().toList()

    private fun cloudFormationClient(a: (CloudFormationClient) -> Unit) =
        a.invoke(CloudFormationClient.builder().region(awsRegion).credentialsProvider(credentialsProvider).build())

    private fun withLambdaClient(a: LambdaClient.() -> Unit) =
        a.invoke(LambdaClient.builder().region(awsRegion).credentialsProvider(credentialsProvider).build())

    private fun selfCheck() {
        listOf(
            account, region, profile, stack
        ).filterNot { it.nonBlank != null }.takeIf(nonEmpty())?.also {
            throw IllegalStateException(
                "Missing config! Either use `configure` method, or provide gradle.properties:\n ${it.joinToString("\n ")}"
            )
        }
    }

    private fun initialize() {
        Files.write(
            upliftDir().resolve("Dockerfile"), "cdk-st4/Dockerfile".renderResource("arch" to arch.get())
        )
        docker(
            upliftDir(),
            "build --tag cdk-site:latest ${upliftDir()}",
            execOperations
        )
    }

    private fun volumes(vararg vols: Pair<*, *>) =
        vols.joinToString("") { (local, contained) ->
            "-v $local:$contained "
        }

    private fun environment(env: Map<String, String>?) =
        env?.entries?.joinToString("") { (key, value) ->
            "-e $key=$value "
        } ?: ""

    private fun Path.printSize(limit: Int) =
        Files.size(this).let {
            when {
                it > limit * 1_000_000 -> "${it / 1_000_000}Mb"
                it > limit * 1_000 -> "${it / 1_000}Kb"
                else -> "${it}b"
            }
        }
}


