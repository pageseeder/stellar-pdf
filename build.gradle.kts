plugins {
    id("java-library")
    id("maven-publish")
    id("jacoco")
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonar)
}

val title: String by project
val gitName: String by project

group = "org.pageseeder.stellar"
version = file("version.txt").readText().trim()
description = findProperty("description") as String?

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(libs.ant.core)
    api(libs.jspecify)
    api(libs.slf4j.api)

    implementation(libs.flyingsaucer.core)
    implementation(libs.flyingsaucer.pdf)

    runtimeOnly(libs.slf4j.simple)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testImplementation(libs.jspecify)
    testImplementation(libs.ant.core)

    testRuntimeOnly(libs.junit.jupiter.engine)

}

sonar {
    properties {
        property("sonar.projectKey", "pageseeder_stellar-pdf")
        property("sonar.organization", "pageseeder")
        // Tell SonarCloud where the JaCoCo XML report is
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath
        )
    }
}

// Set Gradle version
tasks.wrapper {
    gradleVersion = "8.14.5"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.test {
    useJUnitPlatform()
    // make sure report generation happens after tests when requested
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)   // Sonar reads this
        html.required.set(true)  // nice to have for CI artifacts/debugging
        csv.required.set(false)
    }
}

tasks.withType<Javadoc> {
    options {
        encoding = "UTF-8"
    }
}

tasks.shadowJar {
    archiveClassifier.set("standalone")
    dependencies {
        exclude(dependency("org.apache.ant:.*"))
        exclude(dependency("org.jspecify:.*"))
        exclude(dependency("org.slf4j:.*"))
    }
    relocate("org.xhtmlrenderer", "org.pageseeder.stellar.internal.xhtmlrenderer")
    relocate("com.lowagie", "org.pageseeder.stellar.internal.lowagie")
    relocate("javax.annotation", "org.pageseeder.stellar.internal.javax.annotation")
    mergeServiceFiles()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(title)
                description.set(project.description)
                url.set("https://github.com/pageseeder/${gitName}")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                organization {
                    name.set("Allette Systems")
                    url.set("https://www.allette.com.au")
                }
                scm {
                    url.set("git@github.com:pageseeder/${gitName}.git")
                    connection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
                    developerConnection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
                }
                developers {
                    developer {
                        name.set("Christophe Lauret")
                        email.set("clauret@weborganic.com")
                    }
                    developer {
                        name.set("Philip Rutherford")
                        email.set("philipr@weborganic.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    configFile.set(file("jreleaser.toml"))
}

tasks.register<Copy>("copyToLib") {
    group = "publishing"
    description = "Copy latest version plus dependencies to build/output/lib folder"
    dependsOn(tasks.jar)
    into(layout.buildDirectory.dir("output/lib"))
    from(configurations.runtimeClasspath)
    from(layout.buildDirectory.file("libs/pso-stellarpdf-${version}.jar"))
    doFirst {
        delete(layout.buildDirectory.dir("output/lib"))
    }
}
