package org.pageseeder.stellar.core;

import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.DOMUtil;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Holds metadata information from a PSML document to use as PDF info values.
 *
 * @author Christophe lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public final class Info {

  private static final Logger LOGGER = LoggerFactory.getLogger(Info.class);

  private final Map<PdfName, PdfString> pdfInfoValues = new HashMap<>();

  /**
   * Loads metadata information from the given XML Document and populates it into a Metadata object.
   * This method extracts details such as document information, metadata properties, and other
   * specific attributes defined within the provided Document structure.
   *
   * @param doc the XML Document from which metadata is to be loaded; must not be null and should
   *            represent a well-formed XML document containing applicable metadata elements
   * @return a Metadata object populated with the extracted metadata from the Document
   */
  public static Info load(Document doc) {
    Info meta = new Info();
    meta.parseDocument(doc);
    meta.parseDocumentInfo(doc);
    meta.parseMetadata(doc);
    return meta;
  }

  /**
   * Add PDF info values to the target PDF document.
   *
   * @param renderer the PDF renderer to add the info values to
   */
  public void writeValues(ITextRenderer renderer) {
    for (Map.Entry<PdfName, PdfString> values : this.pdfInfoValues.entrySet()) {
      renderer.getOutputDevice().getWriter().getInfo().put(values.getKey(), values.getValue());
    }
  }

  private void parseDocument(Document doc) {
    Element documentElement = (Element)doc.getDocumentElement().getElementsByTagName("document").item(0);
    if (documentElement != null) {
      String date = documentElement.getAttribute("date");
      String version = documentElement.getAttribute("version");
      if (!date.isEmpty()) {
        this.pdfInfoValues.put(PdfName.DATE, Utils.toPdfDate(date));
      }
      if (!version.isEmpty() && !"current".equals(version)) {
        PdfString pdfDate = new PdfString(version);
        this.pdfInfoValues.put(PdfName.VERSION, pdfDate);
      }
    }
  }

  private void parseDocumentInfo(Document doc ) {
    Element documentInfo = (Element)doc.getDocumentElement().getElementsByTagName("documentinfo").item(0);
    if (documentInfo != null) {
      Element titleElement = (Element)doc.getDocumentElement().getElementsByTagName("displaytitle").item(0);
      if (titleElement != null) {
        LOGGER.debug(titleElement.getTextContent());
        String titleContent = Utils.normalizeSpace(titleElement.getTextContent());
        PdfString pdfString = new PdfString(titleContent);
        this.pdfInfoValues.put(PdfName.TITLE, pdfString);
      }
      Element descriptionElement = (Element)doc.getDocumentElement().getElementsByTagName("description").item(0);
      if (descriptionElement != null) {
        LOGGER.debug(descriptionElement.getTextContent());
        String titleContent = Utils.normalizeSpace(descriptionElement.getTextContent());
        PdfString pdfString = new PdfString(titleContent);
        this.pdfInfoValues.put(PdfName.SUBJECT, pdfString);
      }
    }
  }

  private void parseMetadata(Document doc) {
    Element metadata = (Element)doc.getDocumentElement().getElementsByTagName("metadata").item(0);
    if (metadata != null) {
      NodeList properties = metadata.getElementsByTagName("property");
      for (int i = 0; i < properties.getLength(); i++) {
        Element property = (Element)properties.item(i);
        String name = property.getAttribute("name");
        switch (name) {
          case "subject": {
            PdfString subject = getProperty(property);
            this.pdfInfoValues.put(PdfName.SUBJECT, subject);
            LOGGER.debug("Subject: {}", subject);
            break;
          }
          case "authors":
          case "author": {
            PdfString author = getProperty(property);
            this.pdfInfoValues.put(PdfName.AUTHOR, author);
            LOGGER.debug("Author: {}", author);
            break;
          }
          case "keywords": {
            PdfString keywords = getProperty(property);
            this.pdfInfoValues.put(PdfName.KEYWORDS, keywords);
            LOGGER.debug("Keywords: {}", keywords);
            break;
          }
          default:
            // Ignore other metadata
        }
      }
    }
  }

  private PdfString getProperty(Element property) {
    if ("true".equals(property.getAttribute("multiple"))) {
      return new PdfString(DOMUtil.getChildren(property, "value").stream()
          .map(Node::getTextContent)
          .map(Utils::normalizeSpace)
          .collect(Collectors.joining(", ")));
    }
    return new PdfString(Utils.normalizeSpace(property.getAttribute("value")));
  }

}
