dependencies {
    implementation(project(":common"))
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "NovaMC Client Core",
            "Implementation-Version" to project.version
        )
    }
}
