plugins {
    application
}

dependencies {
    implementation(project(":common"))
    implementation(project(":updater"))
}

application {
    mainClass.set("com.novamc.launcher.LauncherMain")
}

tasks.jar {
    dependsOn(":common:jar", ":updater:jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    manifest {
        attributes(
            "Main-Class" to "com.novamc.launcher.LauncherMain",
            "Implementation-Title" to "NovaMC Launcher",
            "Implementation-Version" to project.version
        )
    }
}
