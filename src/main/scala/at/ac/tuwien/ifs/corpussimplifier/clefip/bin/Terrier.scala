package at.ac.tuwien.ifs.corpussimplifier.clefip.bin

import java.io.File

import at.ac.tuwien.ifs.corpussimplifier.{TerrierConfig, TerrierModel}
import at.ac.tuwien.ifs.trecify.clefip.model.{CLEFIPCollection, CLEFIPDocument}


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

    val collection = new CLEFIPCollection(pathCollection.getAbsolutePath)
    val model = new TerrierModel[CLEFIPDocument](config,
      collection,
      (d: CLEFIPDocument) => d.file.getAbsolutePath.replace(pathCollection.getAbsolutePath, ""),
      (d: CLEFIPDocument) => d.getDocNo,
      (d: CLEFIPDocument) => d.getDescription + "\n" + d.getClaims)

    model.doJob(pathCollection.getAbsolutePath, pathSimplifiedCollection.getAbsolutePath)
  }
}
