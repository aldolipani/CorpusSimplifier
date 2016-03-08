package at.ac.tuwien.ifs.corpussimplifier.clefehealth.bin

import java.io.File
import java.nio.charset.CodingErrorAction

import at.ac.tuwien.ifs.corpussimplifier.{TerrierConfig, TerrierModel}
import at.ac.tuwien.ifs.trecify.ehealth.model.{EHealthCollection, EHealthDocument}
import org.jsoup.Jsoup
import org.jsoup.nodes.Node

import scala.io.{Codec, Source}


/**
 * Created by aldo on 09/06/15.
 */
object Terrier extends App {

  override def main(args: Array[String]): Unit = {
    val pathCollection = new File(args(0))
    val pathSimplifiedCollection = new File(args(1))

    val config =
      if (args.length >= 2)
        new TerrierConfig(args(2))
      else
        null

    println(pathCollection)
    println(pathSimplifiedCollection)

    val collection = new EHealthCollection(pathCollection.getAbsolutePath)
    val model = new TerrierModel[EHealthDocument](config,
      collection,
      (d: EHealthDocument) => d.file.getAbsolutePath.replace(pathCollection.getAbsolutePath, ""),
      (d: EHealthDocument) => d.file.getName.trim,
      (d: EHealthDocument) => html2text(d.file))

    model.doJob(pathCollection.getAbsolutePath, pathSimplifiedCollection.getAbsolutePath)
  }

  def html2text(file: File): String = {
    def removeComments(node: Node) {
      for (i <- (0 until node.childNodes.size) if i < node.childNodes.size) {
        val child = node.childNode(i)
        if (child.nodeName().equals("#comment"))
          child.remove()
        else
          removeComments(child)
      }
    }

    val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    val xml = Source.fromFile(file)(codec)
    val xmlStr = xml.getLines().drop(5).mkString("\n")
    xml.close()

    val doc = Jsoup.parse(xmlStr)
    doc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
    doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);

    val elements = doc.select("script, style")
    for (i <- (0 until elements.size())) elements.get(i).remove()
    removeComments(doc)
    doc.text
  }
}
