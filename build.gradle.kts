plugins {
    java
}

allprojects {
    group = "com.novamc"
    version = providers.gradleProperty("novaVersion").get()

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:${rootProject.property("junitVersion")}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}

tasks.register("printVersion") {
    doLast { println("NovaMC ${project.version}") }
}
