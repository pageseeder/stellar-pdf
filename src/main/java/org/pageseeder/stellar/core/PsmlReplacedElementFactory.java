package org.pageseeder.stellar.core;

import com.lowagie.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Defines the replacement of an element factory by an image
 *
 * @author Christophe Lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
class PsmlReplacedElementFactory implements ReplacedElementFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PsmlReplacedElementFactory.class);

  private final ReplacedElementFactory superFactory;
  private final File root;

  public PsmlReplacedElementFactory(ReplacedElementFactory superFactory, File root) {
    this.superFactory = superFactory;
    this.root = root;
  }

  @Override
  public ReplacedElement createReplacedElement(LayoutContext layoutContext,
                                               BlockBox blockBox,
                                               UserAgentCallback userAgentCallback,
                                               int cssWidth,
                                               int cssHeight) {

    Element element = blockBox.getElement();
    if (element == null) {
      return null;
    }
    String nodeName = element.getNodeName();

    if ("image".equals(nodeName)) {
      LOGGER.info("Replace {}", nodeName);

      // Local image
      String src = element.getAttribute("src");
      if (src.matches("^(?:[a-z0-9A-Z_-]{1,255})?(?:/[a-z0-9A-Z_-]{1,255}){1,16}\\.(?:png|jpg|gif)$")) {
        File f = new File(this.root, src);
        try {
          byte[] bytes = Files.readAllBytes(f.toPath());
          Image image = Image.getInstance(bytes);
          ITextFSImage fsImage = new ITextFSImage(image);

          if (cssWidth != -1 || cssHeight != -1) {
            fsImage.scale(cssWidth, cssHeight);
          }
          return new ITextImageElement(fsImage);

        } catch (IOException ex) {
          LOGGER.warn("Unable to replace local image in PDF {}", ex.getMessage(), ex);
        }
      } else {
        LOGGER.warn("Unable to replace local image in PDF: invalid path {}", src);
      }
    }
    return this.superFactory.createReplacedElement(layoutContext, blockBox, userAgentCallback, cssWidth, cssHeight);
  }

  @Override
  public void reset() {
    this.superFactory.reset();
  }

  @Override
  public void remove(Element e) {
    this.superFactory.remove(e);
  }

  @Override
  public void setFormSubmissionListener(FormSubmissionListener listener) {
    this.superFactory.setFormSubmissionListener(listener);
  }

}