import org.junit.jupiter.api.*
import step.*
import testutil.*
import mu.KotlinLogging
import testutil.cmdRunner
import testutil.setupTest
import util.*
import org.assertj.core.api.Assertions
private val log = KotlinLogging.logger {}

class FilterTests {
    @BeforeEach fun setup() = setupTest()
   @AfterEach fun cleanup() = cleanupTest()

     @Test fun `run filter step - pairedend nodupRemoval`() {

        cmdRunner.filter(PBAM,DupMarker.picard,30,true,true,1,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam.bai"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.flagstat.qc"))
    }
    @Test fun `run filter step- pairedend dupRemoval`() {

        cmdRunner.filter(PBAM,DupMarker.picard,30,true,false,0,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.mito_dup.txt"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.dup.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.pbc.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.flagstat.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam.bai"))
    }
     @Test fun `run filter step -  nodupRemoval`() {


        cmdRunner.filter(BAM,DupMarker.picard,30,false,true,0,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam.bai"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.flagstat.qc"))
    }
    @Test fun `run filter step-  dupRemoval`() {

        cmdRunner.filter(BAM,DupMarker.picard,30,false,false,0,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.mito_dup.txt"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.dup.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.pbc.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.flagstat.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam.bai"))
    }
    @Test fun `run filter step - sambamba pairedend nodupRemoval`() {

        cmdRunner.filter(PBAM,DupMarker.sambamba,30,true,true,1,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam.bai"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.flagstat.qc"))
    }
     @Test fun `run filter step- sambamba pairedend dupRemoval`() {

        cmdRunner.filter(PBAM,DupMarker.sambamba,30,true,false,0,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.mito_dup.txt"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.dup.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.pbc.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.flagstat.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam.bai"))
    }
     @Test fun `run filter step - sambamba nodupRemoval`() {


        cmdRunner.filter(BAM,DupMarker.sambamba,30,false,true,0,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.bam.bai"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.filt.flagstat.qc"))
    }
     @Test fun `run filter step-  sambamba dupRemoval`() {

        cmdRunner.filter(BAM,DupMarker.sambamba,30,false,false,0,1, testOutputDir.resolve("filteroutput"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.mito_dup.txt"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.dup.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.pbc.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.flagstat.qc"))
        Assertions.assertThat( testOutputDir.resolve("filteroutput.nodup.bam.bai"))
    }
}