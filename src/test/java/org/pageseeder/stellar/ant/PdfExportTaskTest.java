package org.pageseeder.stellar.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

final class PdfExportTaskTest {

  private static final Path SOURCE_DIR = Paths.get("src/test/resources");
  private static final Path BUILD_DIR = Paths.get("build/test/src");

  @BeforeAll
  static void setup() throws IOException {
    // Create the build directory if it doesn't exist
    if (!Files.exists(BUILD_DIR)) {
      Files.createDirectories(BUILD_DIR);
    }

    // Copy content recursively
    try (Stream<Path> stream = Files.walk(SOURCE_DIR)) {
      stream.forEach(source -> {
        Path destination = BUILD_DIR.resolve(SOURCE_DIR.relativize(source));
        try {
          Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          // Ignore if file already exists or handle as needed
          if (!Files.exists(destination)) {
            throw new RuntimeException("Failed to copy file", e);
          }
        }
      });
    }
  }

  @AfterAll
  static void tearDown() throws IOException {
//    // Clean up the build directory
//    if (Files.exists(BUILD_DIR)) {
//      try (Stream<Path> pathStream = Files.walk(BUILD_DIR)) {
//        pathStream.sorted(Comparator.reverseOrder())
//            .map(Path::toFile)
//            .forEach(File::delete);
//      }
//    }
  }

  @Test
  void testExecute_Whales_Default() {
    PdfExportTask task = new PdfExportTask();
    Project project = new Project();
    task.setProject(project);

    // Configure
    task.setSrc("build/test/src/psml/whales.psml");
    task.setDest("build/test/out/java_whales_default.pdf");

    // Execute
    task.execute();
    Assertions.assertTrue(new File("build/test/out/java_whales_default.pdf").exists());
  }

  @Test
  void testAntBuildFile_default() {
    File buildFile = new File("build/test/src/task-test.xml");
    Project project = new Project();
    project.setUserProperty("ant.file", buildFile.getAbsolutePath());
    project.init();

    ProjectHelper helper = ProjectHelper.getProjectHelper();
    project.addReference("ant.projectHelper", helper);
    helper.parse(project, buildFile);

    project.executeTarget("test-whale-default");
    Assertions.assertTrue(new File("build/test/out/ant_whales_default.pdf").exists());
  }

  @Test
  void testAntBuildFile_style() {
    File buildFile = new File("build/test/src/task-test.xml");
    Project project = new Project();
    project.setUserProperty("ant.file", buildFile.getAbsolutePath());
    project.init();

    ProjectHelper helper = ProjectHelper.getProjectHelper();
    project.addReference("ant.projectHelper", helper);
    helper.parse(project, buildFile);

    project.executeTarget("test-whale-style");
    Assertions.assertTrue(new File("build/test/out/ant_whales_default.pdf").exists());
  }

  @Test
  void testMissingDest() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(new Project());
    task.setSrc("build/test/src/psml/whales.psml");
    Assertions.assertThrows(BuildException.class, task::execute);
  }

  @Test
  void testMissingSrc() {
    PdfExportTask task = new PdfExportTask();
    task.setProject(new Project());
    task.setDest("build/test/whales_default.pdf");
    Assertions.assertThrows(BuildException.class, task::execute);
  }

}
