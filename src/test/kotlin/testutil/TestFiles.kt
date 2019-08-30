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
val PBAM = testInputDir.resolve("EpiGABA_H.276.GLU.27ac_hg19.bam")
val sponge = testInputDir.resolve("GRCh37_sponge.names")