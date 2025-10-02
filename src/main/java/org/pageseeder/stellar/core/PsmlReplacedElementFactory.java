package org.pageseeder.stellar.core;

import com.lowagie.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;
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

      // Local image
      String src = element.getAttribute("src");
      LOGGER.debug("Replace {}: {}", nodeName, src);
      if (src.matches("^(?:[a-z0-9A-Z_-]{1,255})?(?:/[a-z0-9A-Z_-]{1,255}){1,16}\\.(?:png|jpg|gif)$")) {
        File f = new File(this.root, src);
        try {
          byte[] bytes = Files.readAllBytes(f.toPath());
          Image image = Image.getInstance(bytes);
          ITextFSImage fsImage = new ITextFSImage(image);

          // Image dimensions (in pixels)
          float imgWidthPx = image.getWidth();
          float imgHeightPx = image.getHeight();

          // Check available space on the page
          Box masterBox = layoutContext.getLayer().getMaster();
          float adjust = computeAdjustLayout(layoutContext, element);
          float maxWidth = masterBox.getContentWidth() - adjust;
          float maxHeight = (imgHeightPx * maxWidth) / imgWidthPx;

          if (cssWidth != -1 || cssHeight != -1) {
            // Scale image if necessary
            if (cssWidth > maxWidth || cssHeight > maxHeight) {
              float scale = maxWidth / cssWidth;
              int targetWidth = Math.round(cssWidth * scale);
              int targetHeight = Math.round(cssHeight * scale);
              LOGGER.debug("Scale {}x{} to {}x{}", cssWidth, cssHeight, targetWidth, targetHeight);
              fsImage.scale(targetWidth, targetHeight);
            } else {
              fsImage.scale(cssWidth, cssHeight);
            }
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

  /**
   * Computes the adjustment value for the layout by accumulating the combined margins
   * and paddings (left and right) of the specified element's ancestors.
   *
   * <p>The computation iterates through the element's parent hierarchy, retrieving and
   * calculating the relevant CSS properties (margin-left, padding-left, margin-right,
   * and padding-right) for each ancestor element, and summing them to determine the
   * total adjustment value.
   *
   * @param layoutContext the current layout context, used to retrieve rendering and CSS information
   * @param element the target element for which the layout adjustment is computed
   *
   * @return the computed adjustment value as a float, representing the cumulative effect of
   *         margins and paddings across ancestor elements (in dots)
   */
  private float computeAdjustLayout(LayoutContext layoutContext, Element element) {
    RenderingContext context = layoutContext.getSharedContext().newRenderingContextInstance();
    float adjust = 0;

    Element parent = (Element)element.getParentNode();
    while (parent != null) {
      CascadedStyle style = layoutContext.getCss().getCascadedStyle(parent, false);
      if (style != null) {
        CalculatedStyle cs = new EmptyStyle().deriveStyle(style);
        float marginLeft = cs.getFloatPropertyProportionalTo(CSSName.MARGIN_LEFT, 0, context);
        float paddingLeft = cs.getFloatPropertyProportionalTo(CSSName.PADDING_LEFT, 0, context);
        float marginRight = cs.getFloatPropertyProportionalTo(CSSName.MARGIN_RIGHT, 0, context);
        float paddingRight = cs.getFloatPropertyProportionalTo(CSSName.PADDING_RIGHT, 0, context);
        adjust += (marginLeft+marginRight+paddingLeft+paddingRight);
        LOGGER.debug("Parent: {} -> {}", parent.getNodeName(), adjust);
      }

      if (parent.getParentNode() instanceof Element) {
        parent = (Element) parent.getParentNode();
      } else {
        parent = null;
      }
    }
    return adjust;
  }

}