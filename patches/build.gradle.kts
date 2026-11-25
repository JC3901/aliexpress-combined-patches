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

// Test-applies a bundle to an APK. Pass -Pmpp=<bundle>; it does not rebuild the bundle, so running
// it never replaces the buildAndroid output (with classes.dex) by a plain jar.
tasks.register<JavaExec>("applyToApk") {
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("harness.HarnessKt")
    maxHeapSize = "6g"
    args = listOf(
        providers.gradleProperty("apk").get(),
        providers.gradleProperty("mpp").orNull ?: tasks.named<Jar>("jar").get().archiveFile.get().asFile.absolutePath,
    ) + (providers.gradleProperty("only").orNull?.split(",") ?: emptyList())
}

// buildAndroid must run last: a later plain `build` rewrites the .mpp without classes.dex,
// and Morphe Manager then loads 0 patches.
tasks.matching { it.name == "buildAndroid" }.configureEach {
    mustRunAfter("build", "generatePatchesList")
}
