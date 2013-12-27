import org.gradle.api.Plugin
import org.gradle.api.Project
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.log.SystemLogChute

class VelocityPluginExtension {
	String inputDir = "src/main/velocity"
	String outputDir = "build/generated-sources/velocity"
	Map<String, Object> contextValues = [:]
	def context(Closure closure) {
		contextValues.with closure
	}
}

class VelocityPlugin implements Plugin<Project> {
	void apply(Project project) {

		project.extensions.create("velocity", VelocityPluginExtension)

		project.task('velocityVpp', type: VelocityTask) {
			description "Preprocesses velocity template files."
			inputDir = project.file(project.velocity.inputDir)
			outputDir = project.file(project.velocity.outputDir)
			contextValues = project.velocity.contextValues
		}

		project.compileJava.dependsOn(project.velocityVpp)
		project.sourceSets.main.java.srcDir project.velocity.outputDir

	}
}

