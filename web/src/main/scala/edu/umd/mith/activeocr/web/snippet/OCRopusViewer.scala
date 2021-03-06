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

import edu.umd.mith.activeocr.util.model.LocalHostOcroReader
import java.io.File
import net.liftweb.util.Helpers._
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

class OCRopusViewer {
  val source = Source.fromFile("../data/luxmundi07multipage.html")
  val reader = new XMLEventReader(source)
  val pages = LocalHostOcroReader.parsePage(reader)

  def transform(in: NodeSeq): NodeSeq = {
    <div>
      {for (page <- pages) yield <div>{page.toSVG}</div>}
    </div>
  }
}

}
}
