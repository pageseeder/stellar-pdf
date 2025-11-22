plugins {
    id("java-library")
    id("maven-publish")
    id("jacoco")
    alias(libs.plugins.jreleaser)
}

val title: String by project
val gitName: String by project
val website: String by project

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
    compileOnly(libs.annotations)
    compileOnly(libs.ant.core)

    api(libs.slf4j.api)

    implementation(libs.flyingsaucer.core)
    implementation(libs.flyingsaucer.pdf)

    runtimeOnly(libs.slf4j.simple)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testImplementation(libs.annotations)
    testImplementation(libs.ant.core)

    testRuntimeOnly(libs.junit.jupiter.engine)

}

// Set Gradle version
tasks.wrapper {
    gradleVersion = "8.14"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    options {
        encoding = "UTF-8"
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(title)
                description.set(project.description)
                url.set(website)
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
