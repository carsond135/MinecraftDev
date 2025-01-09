import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

typealias TaskDelegate<T> = RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, T>

fun Project.lexer(flex: String, pack: String): TaskDelegate<JFlexExec> {
    extensions.configure<PatternFilterable>("license") {
        exclude(pack.removeSuffix("/") + "/**")
    }

    return tasks.registering(JFlexExec::class) {
        sourceFile.set(layout.projectDirectory.file("src/main/grammars/$flex.flex"))
        destinationDirectory.set(layout.buildDirectory.dir("gen/$pack/lexer"))
        destinationFile.set(layout.buildDirectory.file("gen/$pack/lexer/$flex.java"))
        logFile.set(layout.buildDirectory.file("logs/generate$flex.log"))

        val jflex by project.configurations
        this.jflex.setFrom(jflex)

        val jflexSkeleton by project.configurations
        skeletonFile.set(jflexSkeleton.singleFile)
    }
}

fun Project.parser(bnf: String, pack: String): TaskDelegate<ParserExec> {
    extensions.configure<PatternFilterable>("license") {
        exclude(pack.removeSuffix("/") + "/**")
    }

    return tasks.registering(ParserExec::class) {
        val destRoot = project.layout.buildDirectory.dir("gen")
        val dest = destRoot.map { it.dir(pack) }
        sourceFile.set(project.layout.projectDirectory.file("src/main/grammars/$bnf.bnf"))
        destinationRootDirectory.set(destRoot)
        psiDirectory.set(dest.map { it.dir("psi") })
        parserDirectory.set(dest.map { it.dir("parser") })
        logFile.set(layout.buildDirectory.file("logs/generate$bnf.log"))

        val grammarKit by project.configurations
        this.grammarKit.setFrom(grammarKit)
    }
}
