import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

@CacheableTask
abstract class JFlexExec : JavaExec() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFile: RegularFileProperty

    @get:Classpath
    abstract val jflex: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val skeletonFile: RegularFileProperty

    @get:OutputDirectory
    abstract val destinationDirectory: DirectoryProperty

    @get:OutputFile
    abstract val destinationFile: RegularFileProperty

    @get:Internal
    abstract val logFile: RegularFileProperty

    @get:Inject
    abstract val fs: FileSystemOperations

    init {
        mainClass.set("jflex.Main")
    }

    override fun exec() {
        classpath = jflex

        args(
            "--skel", skeletonFile.get().asFile.absolutePath,
            "-d", destinationDirectory.get().asFile.absolutePath,
            sourceFile.get().asFile.absolutePath
        )

        fs.delete { delete(destinationDirectory) }

        val taskOutput = ByteArrayOutputStream()
        standardOutput = taskOutput
        errorOutput = taskOutput

        super.exec()

        val log = logFile.get().asFile
        log.parentFile.mkdirs()
        log.writeBytes(taskOutput.toByteArray())
    }
}
