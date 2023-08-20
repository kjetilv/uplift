package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.file.Path

@Suppress("unused")
class UpliftPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.apply {
            register("uplift-init", UpliftInitTask::class.java) {
                it.configureFor(project) {
                    activeJar.set(project.cdkFile(it.stackbuilderJar.get()))
                    activePom.set(project.cdkFile("pom.xml"))
                }
            }
            register("uplift-bootstrap", UpliftBootstrapTask::class.java) {
                it.configureFor(project) {
                    template.set(project.templateFile(it))
                    dependsOn("uplift-init")
                }
            }
            register("uplift", UpliftDeployTask::class.java) {
                it.configureFor(project) {
                    dependsOn("uplift-bootstrap")
                }
            }
            register("uplift-ping", UpliftPingTask::class.java) {
                it.configureFor(project) {
                    clearDependencies()
                }
            }
            register("uplift-destroy", UpliftDestroyTask::class.java) {
                it.configureFor(project) {
                    clearDependencies()
                }
            }
        }
    }

    private fun UpliftTask.clearDependencies() =
        setDependsOn(emptyList<String>())

    private fun templateFile(project: Project, task: UpliftBootstrapTask): Path =
        project.cdkApp().resolve("cdk.out").resolve("${task.stack.get()}.template.json")

    private fun <T : UpliftTask> T.configureFor(project: Project, aob: T.() -> Unit = {}) {
        account %= "${project.propertyOrNull("account") ?: ""}"
        region %= "${project.propertyOrNull("region") ?: ""}"
        profile %= "${project.propertyOrNull("profile") ?: "default"}"
        stack %= "${project.propertyOrNull("stack") ?: composeName(project)}"
        arch %= resolveArchitecture()
        awsAuth %= resolveAwsAuth()
        if (this is UpliftCdkTask) {
            jarOutput(project)?.also {
                stackbuilderJar %= it
            }
            stackbuilderClass %= ""
        }
        aob.invoke(this)
    }

    private fun composeName(project: Project) = normalize(
        if (project.name.startsWith(project.group.toString())) project.name
        else "${project.group}-${project.name}"
    )

    private fun normalize(s: String) = s.toCharArray().joinToString("") { c -> if (Character.isLetterOrDigit(c)) "$c" else "-" }

    private fun jarOutput(project: Project) = project.tasks.findByName("jar")?.outputs?.files?.singleFile?.toPath()

    private fun resolveAwsAuth() = Path.of(System.getProperty("user.home")).resolve(".aws").toAbsolutePath().toString()

    private fun resolveArchitecture() = System.getProperty("os.arch").let {
        when (it) {
            "aarch64" -> "arm64v8"
            "x86_64" -> "amd64"
            else -> it
        }
    }
}
