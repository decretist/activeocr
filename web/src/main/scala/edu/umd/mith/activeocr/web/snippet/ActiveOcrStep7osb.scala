/*
 * #%L
 * Active OCR Web Application
 * %%
 * Copyright (C) 2011 - 2013 University of Maryland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.umd.mith.activeocr.web {
package snippet {

import edu.umd.mith.activeocr.util.model.{Bbox,OcroReader,TermLine}
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import net.liftweb.http.{S,SHtml,SessionVar,StatefulSnippet}
import net.liftweb.util.Helpers._
import org.imgscalr.Scalr.crop
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

object nodesVar7osb extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])
object pagesVar7osb extends SessionVar[Int](0)

class ActiveOcrStep7osb extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi07multipage.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val pages = OcroReader.parsePage(reader)

  val lineNumber = (S.param("line") map { _.toInt } openOr(0))
  val pageNumber = (S.param("page") map { _.toInt } openOr(0))
  val lastPageNumber = pages.length -1
  val firstPage = "/activeocr7osb?page=0"
  val prevPage = "/activeocr7osb?page=" + (if (pageNumber > 0) pageNumber - 1 else 0).toString 
  val nextPage = "/activeocr7osb?page=" + (if (pageNumber < lastPageNumber) pageNumber + 1 else lastPageNumber).toString
  val lastPage = "/activeocr7osb?page=" + lastPageNumber.toString
  val oldPageNumber = pagesVar7osb.is
  if (nodesVar7osb.is.isEmpty || pageNumber != oldPageNumber) {
    nodesVar7osb(pages(pageNumber).bbList)
  }
  pagesVar7osb(pageNumber)
  val nodes = nodesVar7osb.is
  val lastLineNumber = nodes.length - 1
  val thisPage = "activeocr7osb?page=" + pageNumber.toString
  val firstLine = thisPage + "&line=0"
  val prevLine = thisPage + "&line=" + (if (lineNumber > 0) lineNumber - 1 else 0).toString
  val nextLine = thisPage + "&line=" + (if (lineNumber < lastLineNumber) lineNumber + 1 else lastLineNumber).toString
  val lastLine = thisPage + "&line=" + lastLineNumber.toString
  val imageFileUrl = pages(pageNumber).getUri
  val img = ImageIO.read(new URL(imageFileUrl))
  var ocrText = ""
  nodes(lineNumber) match {
    case l@TermLine(s, x, y, w, h) =>
      if ((w > 0) && (h > 0)) {
        ocrText = l.s
        var tmpImg = crop(img, x, y, w, h)
        ImageIO.write(tmpImg, "png", new File("./src/main/webapp/images/tmp.png"))
      }
    case _ => () // do nothing
  }

  def dispatch = {
    case "renderTop" => renderTop
    case "renderBottom" => renderBottom
  }

  def updateAt(i: Int, correction: String) = {
    val nodes = nodesVar7osb.is
    val updatedNode = nodes(i) match {
      case l: TermLine => l.copy(s = correction)
    }
    nodesVar7osb(nodes.updated(i, updatedNode))
  }

  def renderTop(in: NodeSeq): NodeSeq = {
    bind ("prefix", in,
      "firstPage" -> <a href={firstPage}>&lt;&lt; First Page</a>,
      "prevPage" -> <a href={prevPage}>&lt; Previous Page</a>,
      "nextPage" -> <a href={nextPage}>Next Page &gt;</a>,
      "lastPage" -> <a href={lastPage}>Last Page &gt;&gt;</a>,
      "firstLine" -> <a href={firstLine}>&lt;&lt; First Line</a>,
      "prevLine" -> <a href={prevLine}>&lt; Previous Line</a>,
      "nextLine" -> <a href={nextLine}>Next Line &gt;</a>,
      "lastLine" -> <a href={lastLine}>Last Line &gt;&gt;</a>,
      "ocrText" -> ocrText,
      "correction" -> SHtml.text(ocrText, { s: String => ocrText = s }, "size" -> "80"),
      "perform" -> SHtml.submit("Update", () => perform(ocrText)),
      "outputNodes" -> SHtml.submit("Output", () => outputNodes())
    )
  }

  def renderBottom(in: NodeSeq): NodeSeq = {
    <div>
      <svg version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        width="100%" height="100%"
        viewBox="0 0 680 1149">
        <image xlink:href={ pages(pageNumber).getUri() }
          width="680" height="1149"/>
        { nodes(this.lineNumber).toSVG }
      </svg>
    </div>
  }

  def perform(correction: String): Unit = {
    updateAt(this.lineNumber, correction)
  }

  def outputNodes(): Unit = {
    val nodes = nodesVar7osb.is
    var index = 1
    var outputFile = "/dev/null"
    var outputPrinter = new java.io.PrintWriter(outputFile)
    for (node <- nodes) {
      node match {
        case l@TermLine(s, _, _, _, _) => {
          outputFile = "./temp/0001/0100" + f"$index%02x" + ".gt.txt"
          index = index + 1
          outputPrinter = new java.io.PrintWriter(outputFile)
          outputPrinter.println(s)
          outputPrinter.close()
        }
        case _ => assert(false, "Unexpected Bbox type.")
      }
    }
  }
}

}
}