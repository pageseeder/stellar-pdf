package org.pageseeder.stellar.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

final class PdfExportTaskTest {

  private static final Path TEST_DIR = Paths.get("build/test");

  @BeforeAll
  static void setup() throws IOException {
    Path resourcesDir = Paths.get("src/test/resources");
    Path testSrcDir = Paths.get("build/test/src");

    // Create the build directory if it doesn't exist
    if (!Files.exists(testSrcDir)) {
      Files.createDirectories(testSrcDir);
    }

    // Copy content recursively
    try (Stream<Path> stream = Files.walk(resourcesDir)) {
      stream.forEach(source -> {
        Path destination = testSrcDir.resolve(resourcesDir.relativize(source));
        try {
          if (source.toFile().isFile() || source.toFile().isDirectory() && !destination.toFile().exists())
            Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
          throw new UncheckedIOException("Failed to copy file", ex);
        }
      });
    }
  }

  @Test
  void testExecute_Whales_Default() {
    File outputFile = new File("build/test/out/java_whales_default.pdf");
    deleteFileIfExists(outputFile);
    PdfExportTask task = new PdfExportTask();
    task.setProject(newProject());

    // Configure
    task.setSrc("src/psml/whales.psml");
    task.setDest("out/java_whales_default.pdf");

    // Execute
    task.execute();
    Assertions.assertTrue(outputFile.exists());
  }

  @Test
  void testAntBuildFile_Whales_Default() {
    runAntTarget("test-whales-default", "build/test/out/ant_whales_default.pdf");
  }

  @Test
  void testAntBuildFile_Whales_Styled() {
    runAntTarget("test-whales-styled", "build/test/out/ant_whales_styled.pdf");
  }

  @Test
  void testAntBuildFile_Policy_Default() {
    runAntTarget("test-policy-default", "build/test/out/ant_policy_default.pdf");
  }

  @Test
  void testAntBuildFile_Policy_Style() {
    runAntTarget("test-policy-styled", "build/test/out/ant_policy_styled.pdf");
  }

  @Test
  void testMissingDest() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(newProject());
    task.setSrc("src/psml/whales.psml");
    Assertions.assertThrows(BuildException.class, task::execute);
  }

  @Test
  void testMissingSrc() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(newProject());
    task.setDest("out/whales.pdf");
    Assertions.assertThrows(BuildException.class, task::execute);
  }

  @Test
  void testSrcDoesNotExist() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(newProject());
    task.setSrc("does/not/_exist_.psml");
    Assertions.assertThrows(BuildException.class, task::execute);
  }

  @Test
  void testStylesheetDoesNotExist() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(newProject());
    task.setSrc("src/psml/whales.psml");
    task.setDest("out/whales.pdf");
    task.setFontsDir("does/not/_exist_.css");
    Assertions.assertThrows(BuildException.class, task::execute);
  }

  @Test
  void testFontsDirDoesNotExist() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(newProject());
    task.setSrc("src/psml/whales.psml");
    task.setDest("out/whales.pdf");
    task.setFontsDir("/does/not/_exist_/");
    Assertions.assertThrows(BuildException.class, task::execute);
  }





  private Project newProject() {
    Project project = new Project();
    project.setBaseDir(TEST_DIR.toFile());
    return project;
  }

  private void runAntTarget(String targetName, String expectedOutputFile) {
    File outputFile = new File(expectedOutputFile);
    deleteFileIfExists(outputFile);

    File buildFile = TEST_DIR.resolve("src/task-test.xml").toFile();
    Project project = newProject();
    project.setUserProperty("ant.file", buildFile.getAbsolutePath());
    project.init();

    ProjectHelper helper = ProjectHelper.getProjectHelper();
    project.addReference("ant.projectHelper", helper);
    helper.parse(project, buildFile);

    project.executeTarget(targetName);
    Assertions.assertTrue(outputFile.exists());
  }

  private void deleteFileIfExists(File file) {
    if (file.exists()) {
      try {
        Files.delete(file.toPath());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

}
