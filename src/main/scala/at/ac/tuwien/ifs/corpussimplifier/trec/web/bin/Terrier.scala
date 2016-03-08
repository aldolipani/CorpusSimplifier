package at.ac.tuwien.ifs.corpussimplifier.trec.web.bin

import java.io.File

import at.ac.tuwien.ifs.corpussimplifier.{TerrierConfig, TerrierModel}
import at.ac.tuwien.ifs.trecify.trec.model.{TRECCollection, TRECDocument}


/**
 * Created by aldo on 09/06/15.
 */
object Terrier extends App {

  override def main(args: Array[String]): Unit = {
    val pathCollection = new File(args(0))
    val pathSimplifiedCollection = new File(args(1))

    val config =
      if (args.length > 2)
        new TerrierConfig(args(2))
      else
        null

    println(pathCollection)
    println(pathSimplifiedCollection)

    val collection = new TRECCollection(pathCollection.getAbsolutePath)
    val model = new TerrierModel[TRECDocument](config,
      collection,
      (d: TRECDocument) => d.path.replace(pathCollection.getAbsolutePath, "").replace(".gz", ""),
      (d: TRECDocument) => d.docno,
      (d: TRECDocument) => d.h3 + "\n" + d.text)

    model.doJob(pathCollection.getAbsolutePath, pathSimplifiedCollection.getAbsolutePath)
  }
}
