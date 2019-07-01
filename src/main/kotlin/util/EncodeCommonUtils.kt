package util


fun strip_ext_bam(bam:String):String{
    val regex = """.(bam|Bam)""".toRegex()
    return regex.replace(bam, "")
}
fun strip_ext(f:String,ext:String):String{
    val regex = """.(${ext}|${ext})""".toRegex();
    return  regex.replace(f, "")
}