import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import step.*
import util.*
import java.nio.file.*
import util.CmdRunner


fun main(args: Array<String>) = Cli().main(args)

class Cli : CliktCommand() {

    private val bamFile: Path by option("-bam", help = "path for raw BAM file.")
        .path().required()
    private val dupeMarker: DupMarker by option("-dupMarker", help = "Dupe marker for filtering mapped reads in BAM.")
            .choice(DupMarker.values().associateBy { it.lowerHyphenName }).default(DupMarker.picard)
    private val outputPrefix: String by option("-outputPrefix", help = "output file name prefix; defaults to 'output'").default("output")
    private val mapqThresh: Int by option("-mapqThresh", help = "output file name prefix; defaults to 'output'").int().default(30)
    private val pairedEnd: Boolean by option("-pairedEnd", help = "Paired-end BAM.").flag()
    private val nodupRemoval: Boolean by option("-nodupRemoval", help = "no dupe reads removal when filtering BAM.").flag()
    private val multiMapping: Int by option("-multiMapping", help = "Multimapping reads.").int().default(0)
    private val parallelism: Int by option("-parallelism", help = "Number of threads to parallelize.").int().default(1)
    private val outDir by option("-outputDir", help = "path to output Directory")
        .path().required()
    private val mito_chr_name: String by option("-mito-chr-name", help = "Mito chromosome name.").default("chrM")


    override fun run() {
        val cmdRunner = DefaultCmdRunner()

        cmdRunner.runTask(bamFile,dupeMarker,outputPrefix,mapqThresh,pairedEnd,nodupRemoval,multiMapping,parallelism,mito_chr_name, outDir)
    }
}

/**
 * Runs pre-processing and bwa for raw input files
 *
 * @param bwaInputs bwa Input
 * @param outDir Output Path
 */
fun CmdRunner.runTask(bamFile:Path,dupeMarker:DupMarker,outputPrefix:String,mapqThresh:Int,pairedEnd:Boolean,nodupRemoval:Boolean,multiMapping:Int,parallelism:Int,mito_chr_name:String, outDir: Path) {

        filter(bamFile,dupeMarker,mapqThresh,pairedEnd,nodupRemoval,multiMapping,parallelism,mito_chr_name, outDir.resolve(outputPrefix))

}