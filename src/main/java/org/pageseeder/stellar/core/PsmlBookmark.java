package org.pageseeder.stellar.core;

import org.xhtmlrenderer.render.Box;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Represents a bookmark in a PSML content structure.
 *
 * <p>PsmlBookmark objects are used for hierarchical nesting of bookmarks,
 * each having a name, a reference ID, and optionally associated children.
 * Bookmarks may also include an associated {@link Box}.
 *
 * <p>This class is immutable for its name and ID reference attributes but allows
 * modification of child bookmarks and the associated {@link Box}.
 *
 * @author Christophe lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public final class PsmlBookmark {

  private final String name;
  private final String idref;
  private Box box;

  private List<PsmlBookmark> children;

  PsmlBookmark(String name, String idref) {
    this.name = name;
    this.idref = idref;
  }

  public Box getBox() {
    return box;
  }

  public void setBox(Box box) {
    this.box = box;
  }

  public String getIdref() {
    return idref;
  }

  public String getName() {
    return name;
  }

  public void addChild(PsmlBookmark child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
  }

  public List<PsmlBookmark> getChildren() {
    return children == null ? emptyList() : children;
  }

}

