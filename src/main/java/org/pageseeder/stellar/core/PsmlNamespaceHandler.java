package org.pageseeder.stellar.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.extend.StylesheetFactory;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.simple.NoNamespaceHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * PsmlNamespaceHandler is a class that handles a custom, proprietary namespace
 * for PSML (Presumably some structured markup language).
 * It extends the functionalities of NoNamespaceHandler and implements the
 * NamespaceHandler interface.
 *
 * <p>This class manages associated stylesheets, extracts metadata, and provides
 * specialized behavior for processing elements in a given PSML namespace.
 *
 * @author Christophe Lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public class PsmlNamespaceHandler extends NoNamespaceHandler implements NamespaceHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(PsmlNamespaceHandler.class);

  List<StylesheetInfo> stylesheets = new ArrayList<>();

  PsmlNamespaceHandler() {
    this.stylesheets.add(getDefaultStylesheet(null));
  }

  @Nonnull
  @Override
  public String getNamespace() {
    return "";
  }

  @Nullable
  @Override
  public StylesheetInfo getDefaultStylesheet(StylesheetFactory factory) {
    URL url = PsmlNamespaceHandler.class.getClassLoader().getResource("psml.css");
    StylesheetInfo info = new StylesheetInfo();
    info.setOrigin(StylesheetInfo.AUTHOR);
    info.setMedia("all");
    info.setType("text/css");
    info.setTitle("PSML");
    info.setUri(url.toString());
    return info;
  }

  public void addAuthorStylesheet(String uri) {
    StylesheetInfo info = new StylesheetInfo();
    info.setOrigin(StylesheetInfo.AUTHOR);
    info.setUri(uri);
    info.setMedia("all");
    info.setType("text/css");
    this.stylesheets.add(info);
  }

  @Nullable
  @Override
  public String getDocumentTitle(Document doc) {
    String title = "";
    Element documentElement = doc.getDocumentElement();
    Element documentinfo = Utils.findFirstChild(documentElement, "documentinfo");
    if (documentinfo != null) {
      Element uriElem = Utils.findFirstChild(documentinfo, "uri");
      if (uriElem != null) {
        title = uriElem.getAttribute("title");
      }
    }
    LOGGER.info("PSMLNamespaceHandler.getDocumentTitle() => {}", title);
    return title;
  }

  @Nonnull
  @Override
  public List<StylesheetInfo> getStylesheets(Document doc) {
    return this.stylesheets;
  }

  @Nullable
  @Override
  public String getClass(Element e) {
    return e.getAttribute("class");
  }

  @Nullable
  @Override
  public String getID(Element e) {
    return e.getAttribute("id");
  }

  @Nullable
  @Override
  public String getElementStyling(Element e) {
    StringBuilder style = new StringBuilder();
    switch (e.getNodeName()) {
      case "cell":
      case "hcell": {
        String s;
        s = getAttribute(e, "colspan");
        if (s != null) {
          style.append("-fs-table-cell-colspan: ");
          style.append(s);
          style.append(";");
        }
        s = getAttribute(e, "rowspan");
        if (s != null) {
          style.append("-fs-table-cell-rowspan: ");
          style.append(s);
          style.append(";");
        }
        break;
      }
      case "image": {
        String s;
        s = getAttribute(e, "width");
        if (s != null) {
          style.append("width: ");
          style.append(convertToLength(s));
          style.append(";");
        }
        s = getAttribute(e, "height");
        if (s != null) {
          style.append("height: ");
          style.append(convertToLength(s));
          style.append(";");
        }
        break;
      }
      case "col": {
        String s;
        s = getAttribute(e, "span");
        if (s != null) {
          style.append("-fs-table-cell-colspan: ");
          style.append(s);
          style.append(";");
        }
        s = getAttribute(e, "width");
        if (s != null) {
          style.append("width: ");
          style.append(convertToLength(s));
          style.append(";");
        }
        break;
      }
    }
    return style.toString();
  }

  @Nullable
  @Override
  public String getNonCssStyling(Element e) {
    return null;
  }

  @Nullable
  @Override
  public String getLinkUri(Element e) {
    return e.getNodeName().equalsIgnoreCase("link") && e.hasAttribute("href") ? e.getAttribute("href") : null;
  }

  @Nullable
  @Override
  public String getAnchorName(Element e) {
    if (e.getNodeName().equalsIgnoreCase("anchor") && e.hasAttribute("name")) {
      return e.getAttribute("name");
    }
    return null;
  }

  @Override
  public boolean isImageElement(Element e) {
    return e.getNodeName().equalsIgnoreCase("image");
  }

  @Override
  public boolean isFormElement(Element e) {
    return false;
  }

  @Nullable
  @Override
  public String getImageSourceURI(Element e) {
    return e.getAttribute("src");
  }

  @Nullable
  private String getAttribute(Element e, String attrName) {
    String result = e.getAttribute(attrName).trim();
    return result.isEmpty() ? null : result;
  }

  protected String convertToLength(String value) {
    if (isInteger(value)) {
      return value + "px";
    } else {
      return value;
    }
  }

  protected boolean isInteger(String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (! (c >= '0' && c <= '9')) {
        return false;
      }
    }
    return true;
  }

}
