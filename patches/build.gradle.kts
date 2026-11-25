group = "local.aliexpress.combined"
tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach { options.release.set(11) }

patches {
    about {
        name = "AliExpress Combined Patches"
        description = "AliExpress 8.180.1 patches combined from hxreborn, johninnld and mben25, plus a Skip bundle deals patch"
        source = "https://gitlab.com/johninnld/nl-morphe-patches"
        author = "hxreborn, johninnl, mben25"
        contact = "https://gitlab.com/johninnld/nl-morphe-patches/-/issues"
        website = "https://gitlab.com/johninnld/nl-morphe-patches"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(libs.gson)
}

tasks.register<JavaExec>("generatePatchesList") {
    dependsOn(tasks.named("build"))
    classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
    mainClass.set("util.PatchListGeneratorKt")
}
tasks.named("javadoc") { enabled = false }

tasks.register<JavaExec>("applyToApk") {
    dependsOn(tasks.named("jar"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("harness.HarnessKt")
    maxHeapSize = "6g"
    args = listOf(
        providers.gradleProperty("apk").get(),
        providers.gradleProperty("mpp").orNull ?: tasks.named<Jar>("jar").get().archiveFile.get().asFile.absolutePath,
    ) + (providers.gradleProperty("only").orNull?.split(",") ?: emptyList())
}
