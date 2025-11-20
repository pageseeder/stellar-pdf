package org.pageseeder.stellar.core;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Element;
import org.xhtmlrenderer.render.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
 * @version 0.7.0
 */
public final class PsmlBookmark {

  private final String name;
  private final String idref;
  private Box box;

  private @Nullable List<PsmlBookmark> children;

  PsmlBookmark(String name, String idref) {
    this.name = Objects.requireNonNull(name);
    this.idref = Objects.requireNonNull(idref);
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

  /**
   * Adds a child bookmark to this bookmark.
   *
   * @param child the child bookmark to add; must not be null
   */
  public void addChild(PsmlBookmark child) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    this.children.add(child);
  }

  /**
   * Retrieves the list of child bookmarks contained within this bookmark.
   *
   * <p>If there are no children, an empty list is returned.
   *
   * @return a list of child {@code PsmlBookmark} objects; never null
   */
  public List<PsmlBookmark> getChildren() {
    return children == null ? List.of() : List.copyOf(children);
  }

  /**
   * Creates a new {@code PsmlBookmark} instance from an XML {@link Element}.
   *
   * <p>This method extracts attributes from the given {@code part} element such as
   * "prefix", "title", and "idref", and constructs a bookmark. The "prefix" and
   * "title" are combined to create the bookmark's name.
   *
   * <p>If the "prefix" is empty, the name consists of only the "title".
   *
   * @param part the {@link Element} representing a bookmark part; must not be null
   *             and is expected to have "prefix", "title", and "idref" attributes
   * @return a {@link PsmlBookmark} instance initialized with the name and ID reference
   */
  public static PsmlBookmark fromPart(Element part) {
    String prefix = part.getAttribute("prefix");
    String title = Utils.normalizeSpace(part.getAttribute("title"));
    String idref = part.getAttribute("idref");
    String name = prefix.isEmpty() ? title : prefix + " " + title;
    return new PsmlBookmark(name, idref);
  }

  /**
   * Creates a new {@code PsmlBookmark} instance from an XML {@link Element} representing a heading.
   *
   * <p>This method extracts the text content, "id", and "prefix" attributes from the provided {@link Element}.
   *
   * <p>The "prefix" and the normalized text content are combined to create the name of the bookmark.
   * If the "prefix" attribute is empty, the name will consist solely of the text content.
   *
   * @param heading the {@link Element} representing a heading; must not be null and is expected to have
   *                "id" and optionally "prefix" attributes
   * @return a {@link PsmlBookmark} instance initialized with the computed name and ID
   */
  public static PsmlBookmark fromHeading(Element heading) {
    String title = Utils.normalizeSpace(heading.getTextContent());
    String id = heading.getAttribute("id");
    String prefix = heading.getAttribute("prefix");
    String name = prefix.isEmpty() ? title : prefix + " " + title;
    return new PsmlBookmark(name, id);
  }

  @Override
  public String toString() {
    return "{"+ name + " @" + idref + ( children != null ? (" " + children) : "")+"}";
  }
}

