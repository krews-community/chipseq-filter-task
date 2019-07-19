package testutil
import java.nio.file.*

fun getResourcePath(relativePath: String): Path {

    val url = TestCmdRunner::class.java.classLoader.getResource(relativePath)
     return Paths.get(url.toURI())
}

// Resource Directories
val testInputResourcesDir = getResourcePath("test-input-files")
val testOutputResourcesDir = getResourcePath("test-output-files")
// Test Working Directories
val testDir = Paths.get("/tmp/chipseq-test")!!
val testInputDir = testDir.resolve("input")!!
val testOutputDir = testDir.resolve("output")!!


val BAM = testInputDir.resolve("rep1_align_output.bam")
val PBAM = testInputDir.resolve("rep1_R1_rep1_R2.bam")
val SBAM = testInputDir.resolve("ENCFF000ASP.bam")
val CONTROLBAM = testInputDir.resolve("ENCFF000ARK.bam")