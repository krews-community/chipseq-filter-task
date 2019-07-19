package step
import mu.KotlinLogging
import util.*
import java.nio.file.*
import util.CmdRunner
import java.lang.Exception
import kotlin.math.max

private val log = KotlinLogging.logger {}
data class dup_sambamba(val dupmark_bam:String, val dup_qc:String )

fun CmdRunner.filter(bamFile:Path,dupeMarker:DupMarker,mapqThresh:Int,pairedEnd:Boolean,nodupRemoval:Boolean,multiMapping:Int,parallelism:Int,mito_chr_name:String,output: Path) {
    log.info { "Make output Diretory" }
    Files.createDirectories(output.parent)

    var filt_bam:String
    var tmpFiles = mutableListOf<String>() //Delete temp files at the end
    log.info { "Removing unmapped/low-quality reads..." }
    if(pairedEnd==true)
    {
       filt_bam = rm_unmappped_lowq_reads_pe(bamFile,multiMapping,mapqThresh,parallelism,output)
    } else {
       filt_bam = rm_unmappped_lowq_reads_se(bamFile,multiMapping,mapqThresh,parallelism,output)

    }
    var nodup_bam:String
    var dupS = dup_sambamba("","")

    if(nodupRemoval==true)
    {
        nodup_bam =  filt_bam
    } else {
        log.info { "Marking dupes with ${dupeMarker}" }
        if(dupeMarker===DupMarker.picard)
        {

            dupS= mark_dup_picard(filt_bam,output)
        } else if(dupeMarker===DupMarker.sambamba) {
            dupS= mark_dup_sambamba(filt_bam,parallelism,output)
        } else {
            throw Exception("Invalid dup marker ${dupeMarker}")
        }

        tmpFiles.add(filt_bam)
        log.info { "Removing Dupes" }
        if(pairedEnd==true)
        {
            nodup_bam = rm_dup_pe(dupS.dupmark_bam,parallelism,output)
        } else {
            nodup_bam = rm_dup_se(dupS.dupmark_bam,parallelism,output)
        }
        samtools_index(dupS.dupmark_bam)
        tmpFiles.add(dupS.dupmark_bam+".bai")
    }
    tmpFiles.add(dupS.dupmark_bam)
    val sbi = sambamba_index(nodup_bam,parallelism)
    val sbf  = sambamba_flagstat(nodup_bam,parallelism,output)
    var pbc_qc:String
    if(nodupRemoval==false)
    {
        if(pairedEnd==true)
        {
            pbc_qc = pbc_qc_pe(dupS.dupmark_bam,Math.max(1,parallelism-2),mito_chr_name,output)

        }else {
            pbc_qc = pbc_qc_se(dupS.dupmark_bam,mito_chr_name,output)
        }
    }
    if(nodupRemoval==false) {

        // ('Making mito dup log...')
        val mito_dup_log = make_mito_dup_log(dupS.dupmark_bam, output)
    }
    rm_f(tmpFiles)

}
fun CmdRunner.samtools_index(bam:String):String
{
    val bai = "$bam.bai"
    this.run("samtools index $bam")
    return bai
}
fun CmdRunner.pbc_qc_pe(bam:String, nth:Int,mito_chr_name:String,output: Path):String{
    val pbc_qc = "${output}.pbc.qc"
    val bamPath = output.parent.resolve(bam)

    val nmsrt_bam = sambamba_name_sort(bamPath, nth, output)
    var cmd = "bedtools bamtobed -bedpe -i ${nmsrt_bam} | "
    cmd += "awk \'BEGIN{{OFS='\\t'}}{{print $1,$2,$4,$6,$9,$10}}\' | "
    cmd += "grep -v  \'${mito_chr_name}\' | sort | uniq -c | "
    cmd += "awk \'BEGIN{{mt=0;m0=0;m1=0;m2=0}} ($1==1){{m1=m1+1}} "
    cmd += "($1==2){{m2=m2+1}} {{m0=m0+1}} {{mt=mt+$1}} END{{m1_m2=-1.0; "
    cmd += "if(m2>0) m1_m2=m1/m2; "
    cmd += "printf \"%d\\t%d\\t%d\\t%d\\t%f\\t%f\\t%f\\n\""
    cmd += ",mt,m0,m1,m2,m0/mt,m1/m0,m1_m2}}\' > ${pbc_qc}"

    this.run(cmd)
    rm_f(listOf(nmsrt_bam))
    return pbc_qc
}
fun CmdRunner.pbc_qc_se(bam:String,mito_chr_name:String,output: Path):String{
    val pbc_qc = "${output}.pbc.qc"//.format(prefix)

    var cmd = "bedtools bamtobed -i ${bam} | "
    cmd += "awk \'BEGIN{{OFS='\\t'}}{{print $1,$2,$3,$6}}\' | "
    cmd += "grep -v \'${mito_chr_name}\' | sort | uniq -c | "
    cmd += "awk \'BEGIN{{mt=0;m0=0;m1=0;m2=0}} ($1==1){{m1=m1+1}} "
    cmd += "($1==2){{m2=m2+1}} {{m0=m0+1}} {{mt=mt+$1}} END{{m1_m2=-1.0; "
    cmd += "if(m2>0) m1_m2=m1/m2; "
    cmd += "printf \"%d\\t%d\\t%d\\t%d\\t%f\\t%f\\t%f\\n\","
    cmd += "mt,m0,m1,m2,m0/mt,m1/m0,m1_m2}}\' > ${pbc_qc}"
    this.run(cmd)
    return pbc_qc

}
fun CmdRunner.sambamba_name_sort(bam:Path,nth:Int,output:Path):String{
    val opt = output.parent
    val prefix= opt.resolve(strip_ext_bam(bam.fileName.toString()))
    val nmsrt_bam = "${prefix}.nmsrt.bam"
    val cmd = "sambamba sort -n ${bam} -o ${nmsrt_bam} -t ${nth}"
    this.run(cmd)
    return nmsrt_bam
}

fun CmdRunner.rm_unmappped_lowq_reads_se(bam:Path, multimapping:Int, mapq_thresh:Int, nth:Int, output:Path):String {

    val filt_bam = "${output}.filt.bam"
    if(multimapping!=0)
    {
        val qname_sort_bam = sambamba_name_sort(bam, nth, output)
        var cmd2 = "samtools view -h ${qname_sort_bam} | "
        cmd2 += "$(which assign_multimappers.py) -k ${multimapping} | "
        cmd2 += "samtools view -F 1804 -Su /dev/stdin | "

        cmd2 += "sambamba sort /dev/stdin -o ${filt_bam} -t ${nth}"

        this.run(cmd2)
        rm_f(listOf(qname_sort_bam))
    }else {
        var cmd = "samtools view -F 1804 -q ${mapq_thresh} -u ${bam} | "
        cmd += "samtools sort /dev/stdin -o ${filt_bam} -T ${output} -@ ${nth}"

        this.run(cmd)
    }
    return filt_bam

}
fun CmdRunner.rm_unmappped_lowq_reads_pe(bam:Path, multimapping:Int, mapq_thresh:Int, nth:Int, output:Path):String {
   val filt_bam = "${output}.filt.bam"
   val tmp_filt_bam = "${output}.tmp_filt.bam"
   val fixmate_bam =  "${output}.fixmate.bam"
    if(multimapping!=0)
    {
        var cmd1 = "samtools view -F 524 -f 2 -u ${bam} | "
        cmd1 += "sambamba sort -n /dev/stdin -o ${tmp_filt_bam} -t ${nth} "
        this.run(cmd1)

        var cmd2 = "samtools view -h ${tmp_filt_bam} -@ ${nth} | "
        cmd2 += "$(which assign_multimappers.py) -k ${multimapping} --paired-end | "
        cmd2 += "samtools fixmate -r /dev/stdin ${fixmate_bam}"
        this.run(cmd2)
    } else {

        var cmd1 = "samtools view -F 1804 -f 2 -q ${mapq_thresh} -u ${bam} | "
        cmd1 += "sambamba sort -n /dev/stdin -o ${tmp_filt_bam} -t ${nth}"

        this.run(cmd1)

        val cmd2 = "samtools fixmate -r ${tmp_filt_bam} ${fixmate_bam}"

        this.run(cmd2)

    }
    rm_f(listOf(tmp_filt_bam))
    var cmd = "samtools view -F 1804 -f 2 -u ${fixmate_bam} | "
    cmd += "sambamba sort /dev/stdin -o ${filt_bam} -t ${nth}"

    this.run(cmd)
    rm_f(listOf(fixmate_bam))
    return filt_bam
}
fun CmdRunner.rm_f(tmpFiles: List<String>)
{
    val cmd ="rm -f ${tmpFiles.joinToString(" ")}"
    this.run(cmd)
}
fun CmdRunner.mark_dup_sambamba(bam:String,nth:Int,output: Path):dup_sambamba{
  //  # strip extension appended in the previous step
   // val prefix = strip_ext(bam,'filt')
    val dupmark_bam = "${output}.dupmark.bam"
    val dup_qc = "${output}.dup.qc"

   var cmd = "sambamba markdup -t ${nth} --hash-table-size=17592186044416 "
    cmd += "--overflow-list-size=20000000 "
    cmd += "--io-buffer-size=256 ${bam} ${dupmark_bam} 2> ${dup_qc}"
    this.run(cmd)
    return dup_sambamba(dupmark_bam,dup_qc)
}
fun CmdRunner.mark_dup_picard(bam:String,output: Path):dup_sambamba{
    //  # strip extension appended in the previous step
    //  prefix = strip_ext(prefix,'filt')
    val dupmark_bam = "${output}.dupmark.bam"
    val dup_qc = "${output}.dup.qc"


    var cmd = "java -Xmx4G -XX:ParallelGCThreads=1 -jar "
    cmd += locate_picard()
    cmd += " MarkDuplicates "
    cmd += "INPUT=${bam} OUTPUT=${dupmark_bam} "
    cmd += "METRICS_FILE=${dup_qc} VALIDATION_STRINGENCY=LENIENT "
    cmd += "USE_JDK_DEFLATER=TRUE USE_JDK_INFLATER=TRUE "
    cmd += "ASSUME_SORTED=true REMOVE_DUPLICATES=false"

    this.run(cmd)
    return dup_sambamba(dupmark_bam,dup_qc)
}
fun CmdRunner.locate_picard():String?{

    val cmd="which picard.jar"
    val ret=this.runCommand(cmd)
    return ret!!.trimEnd()
}
fun CmdRunner.rm_dup_se(dupmark_bam:String, nth:Int, output:Path):String{
    val nodup_bam = "${output}.nodup.bam"
    val cmd = "samtools view -@ ${nth} -F 1804 -b ${dupmark_bam} > ${nodup_bam}"
    this.run(cmd)
    return nodup_bam
}
fun CmdRunner.rm_dup_pe(dupmark_bam:String, nth:Int, output:Path):String{
    val nodup_bam = "${output}.nodup.bam"//.format(prefix)
    val cmd = "samtools view -@ ${nth} -F 1804 -f 2 -b ${dupmark_bam} > ${nodup_bam}"
    this.run(cmd)
    return nodup_bam
}

fun CmdRunner.sambamba_index(bam:String,nth:Int):String {
    val bai ="$bam.bai"
    this.run("sambamba index ${bam} -t ${nth}")
    return bai
}
fun CmdRunner.sambamba_flagstat(bam:String,nth:Int,output:Path):String
{
    val bamPath= output.parent.resolve(strip_ext_bam(bam))
    val flagstat_qc ="$bamPath.flagstat.qc"
    this.run("sambamba flagstat ${bam} -t ${nth} > ${flagstat_qc}")
    return flagstat_qc
}
fun  CmdRunner.make_mito_dup_log(dupmark_bam:String, output: Path):String {

    val mito_dup_log = "${output}.mito_dup.txt"

    // Get the mitochondrial reads that are marked duplicates
    val cmd1 = "printf \"mito_dups\\t$(samtools view -f 1024 -c ${dupmark_bam} chrM)\\n\" > ${mito_dup_log}"

    this.run(cmd1)


    val cmd2 = "printf \"total_dups\\t$(samtools view -f 1024 -c ${dupmark_bam})\\n\" >> ${mito_dup_log}"
    this.run(cmd2)

    return mito_dup_log
}