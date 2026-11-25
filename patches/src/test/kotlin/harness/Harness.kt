package harness

import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.patch.loadPatchesFromJar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.io.File

/** Applies every patch in a bundle to an APK and reports which ones succeed. */
fun main(args: Array<String>) {
    val apk = File(args[0])
    val mpp = File(args[1])
    val only = args.drop(2).toSet()
    val tmp = File("/tmp/harness-work").apply { deleteRecursively(); mkdirs() }
    val patches = loadPatchesFromJar(setOf(mpp)).filter { it.name != null && (only.isEmpty() || it.name in only) }.toSet()
    println("Loaded ${patches.size} patches: ${patches.map { it.name }}")
    var failed = 0
    Patcher(PatcherConfig(apk, tmp)).use { patcher ->
        patcher += patches
        runBlocking {
            patcher().collect { r ->
                if (r.exception == null) println("OK   ${r.patch.name}")
                else { failed++; println("FAIL ${r.patch.name}: ${r.exception!!.message?.lines()?.take(6)?.joinToString(" | ")}") }
            }
        }
        val result = patcher.get()
        val out = File("/tmp/harness-out").apply { deleteRecursively(); mkdirs() }
        result.dexFiles.forEach { dex -> File(out, dex.name).outputStream().use { dex.stream.copyTo(it) } }
        println("Dex files changed: ${result.dexFiles.size} -> $out")
    }
    println(if (failed == 0) "ALL PATCHES APPLIED" else "$failed FAILED")
}
