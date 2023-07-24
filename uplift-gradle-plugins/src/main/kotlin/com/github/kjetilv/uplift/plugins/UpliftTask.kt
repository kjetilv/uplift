@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
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
import java.nio.file.Path


abstract class UpliftTask : DefaultTask() {

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

    protected fun upliftDir(): Path =
        project.buildDir.toPath().resolve("uplift").also(Files::createDirectories)

    protected fun clearCdkApp() = clearRecursive(cdkApp())

    protected fun runCdk(cmd: String): ExecResult = exe(
        upliftDir(),
        "docker run " +
                volumes(
                    awsAuth.get() to "/root/.aws",
                    cdkApp() to "/opt/app",
                    upliftDir() to "/lambdas"
                ) +
                environment(
                    env.get()
                ) +
                "${"cdk-site:latest"} $cmd"
    )

    fun configure(
        account: String? = null,
        region: String? = null,
        stack: String? = null,
        profile: String? = null
    ): UpliftTask = this.apply {
        account?.also(this.account::set)
        region?.also(this.region::set)
        stack?.also(this.stack::set)
        profile?.also(this.profile::set)
    }

    open fun stackWith(name: String) {}

    protected abstract fun perform()

    protected fun bootstrapCdk() {
        runCdk("cdk bootstrap ${profileOption()} aws://${account.get()}/${region.get()}")
    }

    protected fun profileOption() =
        profile.orNull?.let { "--profile=$it" }

    protected fun cdkApp(): Path = project.cdkApp()

    protected fun deploy() {
        runCdk("cdk deploy ${profileOption()} --require-approval=never ${stack.get()}")
    }

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

    protected fun ping() {
        lambdaClient {
            cloudFormationClient { cloudFormationClient ->
                cloudFormationClient.stacks()
                    .filter(::current)
                    .forEach { stack ->
                        cloudFormationClient.logStack(stack)
                    }
            }
        }
    }

    private fun CloudFormationClient.logStack(stack: Stack) {
        val stackResources = resources(stack)
        if (stackResources.isEmpty()) {
            logger.warn(
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
            logger.warn(
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
                logger.lifecycle("  ${resource.physicalResourceId()}:${resource.resourceType()}")
            }
            return
        }
        logger.lifecycle(
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
            lambdaClient { lambdaClient ->
                lambdaClient.functionConfigurations().firstOrNull { functionConfiguration ->
                    functionConfiguration.functionName().equals(stackResource.physicalResourceId())
                }?.let { func: FunctionConfiguration ->
                    lambdaClient.functionUrlConfigs(func).forEach { url ->
                        logger.lifecycle(
                            """
                            ##   [${f + 1}] ${func.functionName()}: ${func.description()?.takeUnless(String::isBlank) ?: ""}
                            ##    modified @ ${func.lastModified() ?: "<unknown>"}
                            ##    Function URL ${url.functionUrl()} 
                            ##      created @ ${url.creationTime()}
                            ##     modified @ ${url.lastModifiedTime()}
                            ##         cors : ${url.cors()}
                            ##         auth : ${url.authTypeAsString()}
                            ##
                            """.trimIndent()
                        )
                    }
                }
            }
        }
    }

    private fun CloudFormationClient.resources(stack: Stack) =
        describeStackResources { builder ->
            builder.stackName(stack.stackName())
        }.stackResources().toList()

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

    private fun lambdaClient(a: (LambdaClient) -> Unit) =
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
            upliftDir().resolve("Dockerfile"), renderResource("Dockerfile-cdk.st4", "arch" to arch.get())
        )
        exe(cwd = upliftDir(), cmd = "docker build --tag cdk-site:latest ${upliftDir()}")
    }

    private fun volumes(vararg vols: Pair<*, *>) =
        vols.joinToString("") { (local, contained) ->
            "-v $local:$contained "
        }

    private fun environment(env: Map<String, String>?) =
        env?.entries?.joinToString("") { (key, value) ->
            "-e $key=$value "
        } ?: ""
}
