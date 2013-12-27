import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.log.SystemLogChute
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class VelocityTask extends DefaultTask {

	@InputDirectory
	File inputDir

	@OutputDirectory
	File outputDir

	String filter = '**/*.java'

	File includeDir

	Map<String, Object> contextValues = [:]

	@TaskAction
	void run() {
		outputDir.deleteDir()
		outputDir.mkdirs()
		// println "Velocity: $inputDir -> $outputDir"

		VelocityEngine engine = new VelocityEngine()
		engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.name)
		engine.setProperty(VelocityEngine.RESOURCE_LOADER, "file")
		engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true")
		if (includeDir != null)
			engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, includeDir.getAbsolutePath())
		def inputFiles = project.fileTree(
			dir: inputDir,
			include: filter
		)
		inputFiles.visit { e ->
			if (e.file.isFile()) {
				File outputFile = e.relativePath.getFile(outputDir)
				VelocityContext context = new VelocityContext()
				contextValues.each { context.put(it.key, it.value) }
				context.put('project', project)
				context.put('package', e.relativePath.parent.segments.join('.'))
				context.put('class', e.relativePath.lastName.replaceFirst("\\.java\$", ""))
				// println "Parsing ${e.file}"
				e.file.withReader { reader ->
					outputFile.parentFile.mkdirs()
					outputFile.withWriter { writer ->
						engine.evaluate(context, writer, e.relativePath.toString(), reader)
					}
				}
			}
		}
	}
}

