package at.ac.tuwien.ifs.corpussimplifier

import java.io.{File, StringReader}

import at.ac.tuwien.ifs.trecify.model.{Collection, Document}
import at.ac.tuwien.ifs.trecify.utility.Out
import org.terrier.indexing.tokenisation.Tokeniser
import org.terrier.terms.{PorterStemmer, Stopwords}

import scalaz.EphemeralStream

/**
 * Created by aldo on 24/06/15.
 */

class TerrierConfig(val pathStopwords: String = null)

class TerrierModel[A <: Document](config: TerrierConfig = null, collection: Collection[A], getPath: (A) => String, getDocID: (A) => String, getText: (A) => String) {

  def doJob(pathCollection: String, pathSimplifiedCollection: String): Unit = {
    val filePathCollection = new File(pathCollection)
    println(filePathCollection.getAbsolutePath)
    println(new File(pathSimplifiedCollection).getAbsolutePath)

    val docs = collection.getStreamDocuments()

    /*grouped(*/ docs/*, 1000)*/.map(d => {
      //for (d <- g/*.par*/)
      try {
        println(getDocID(d))
        export(
          pathSimplifiedCollection,
          getPath(d).replace(filePathCollection.getAbsolutePath, ""),
          getDocID(d),
          preprocess(getText(d)))
      } catch {
        case e:Exception => e.printStackTrace()
      }
    }).toList
  }

  def preprocess(text: String): String = {
    val stopwords = new Stopwords(null, getPathStopwords)
    val tokeniser = Tokeniser.getTokeniser()
    val tokens = tokeniser.getTokens(new StringReader(text)).toList
    val stemmer = new PorterStemmer()
    val ppTokens = tokens.filter(t => !stopwords.isStopword(t)).map(stemmer.stem(_))
    println(ppTokens.take(5).mkString(" "))
    ppTokens.mkString(" ")
  }


  def getPathStopwords =
    if (config != null && config.pathStopwords != null)
      config.pathStopwords
    else {
      getClass.getResource("/terrier/stopword-list.txt").getPath
    }

  def export(basePath: String, pathFile: String, docId: String, text: String) = {
    val path = new File(new File(basePath, pathFile).getAbsolutePath, docId).getAbsolutePath
    Out.writeTextFile(path, text)
  }

  def grouped[A](s: EphemeralStream[A], n: Int): EphemeralStream[List[A]] = {
    if (s.isEmpty)
      EphemeralStream.emptyEphemeralStream
    else {
      val l = s.take(n).toList
      EphemeralStream.cons(l, grouped(s.dropWhile(l.contains(_)), n))
    }
  }
}
