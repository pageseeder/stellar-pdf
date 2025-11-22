package org.pageseeder.stellar.core;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BookmarksTest {

  /**
   * Test for the Bookmarks.load(Document doc) method.
   * Verifies that the method correctly extracts bookmarks from a simple PSML document with default max level.
   */
  @Test
  void testLoad_DefaultMaxLevel() throws Exception {
    String xml = "<psml><toc><toc-tree><toc-part id=\"1\" name=\"Part 1\"></toc-part></toc-tree></toc></psml>";
    Document doc = parseXml(xml);

    Bookmarks bookmarks = Bookmarks.load(doc);

    assertEquals(1, getBookmarksSize(bookmarks), "Bookmarks list size should match the number of parts");
  }

  /**
   * Test for the Bookmarks.load(Document doc, int maxLevel) method.
   * Verifies that the method correctly respects maxLevel when extracting bookmarks from headings.
   */
  @Test
  void testLoad_HeadingsWithMaxLevel() throws Exception {
    String xml = "<document>"
        + "<heading level=\"1\" id=\"h-1\">Document title</heading>"
        + "<heading level=\"2\" id=\"h-2\" prefix=\"A\">Part A</heading>"
        + "<heading level=\"3\" id=\"h-3\">Example 1</heading>"
        + "<heading level=\"3\" id=\"h-4\">Example 2</heading>"
        + "<heading level=\"4\" id=\"h-5\">Ignore</heading>"
        + "<heading level=\"2\" id=\"h-6\" prefix=\"B\">Part B</heading>"
        + "<heading level=\"3\" id=\"h-7\">Example 3</heading>"
        + "</document>";
    Document doc = parseXml(xml);

    Bookmarks bookmarks = Bookmarks.load(doc, 3);

    assertEquals(1, getBookmarksSize(bookmarks), "Bookmarks list size should not include headings with level > maxLevel");
  }

  /**
   * Test for the Bookmarks.load(Document doc, int maxLevel) method.
   * Verifies that the method correctly respects maxLevel when extracting bookmarks from headings.
   */
  @Test
  void testLoad_HeadingsWhales() throws Exception {
    String xml = Files.readString(Path.of("src/test/resources/psml/whales.psml"));

    Document doc = parseXml(xml);

    Bookmarks bookmarks = Bookmarks.load(doc, 4);

    assertEquals(1, getBookmarksSize(bookmarks), "Bookmarks list size should not include headings with level > maxLevel");
  }

  // Helper method to parse XML string into Document
  private Document parseXml(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));
  }

  // Helper method to get the size of bookmarks list from reflection
  private int getBookmarksSize(Bookmarks bookmarks) {
    try {
      java.lang.reflect.Field listField = Bookmarks.class.getDeclaredField("list");
      listField.setAccessible(true);
      List<?> list = (List<?>) listField.get(bookmarks);
      return list.size();
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Unable to access bookmarks list", e);
    }
  }
}