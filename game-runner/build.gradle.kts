/*
 * The version dependent code is written only once, for 1.8.9 (src/v1_8_9).
 *
 * Every other Minecraft version only contains what really differs between the versions: its own
 * Helper (all calls into Minecraft go through it) and its mixins. The remaining classes
 * (functions, VersionedConnect, ...) are generated from the 1.8.9 sources during the build, so
 * the versions can never drift apart.
 *
 * Rule for new code: never call Minecraft API that differs between the versions directly, add a
 * function to the Helper of every version instead.
 */
val sourceVersion = "v1_8_9"

/** Files every version writes itself, relative to its package. */
val versionSpecificFiles = listOf("Helper.java", "mixins/**")

/** The generated versions with the imports that are named differently than in 1.8.9. */
val generatedVersions = mapOf(
    "v1_12_2" to mapOf(
        "net.minecraft.util.BlockPos" to "net.minecraft.util.math.BlockPos"
    )
)

generatedVersions.forEach { (version, renamedImports) ->
    val generateSources = tasks.register<Sync>("generate${version.replaceFirstChar(Char::uppercase)}Sources") {
        description = "Generates the $version sources that are identical to $sourceVersion."
        inputs.property("renamedImports", renamedImports)

        from("src/$sourceVersion/java") {
            exclude(versionSpecificFiles.map { "**/$sourceVersion/$it" })
            filter { line: String ->
                renamedImports.entries.fold(line.replace(sourceVersion, version)) { text, (from, to) -> text.replace(from, to) }
            }
            eachFile { path = path.replace(sourceVersion, version) }
            includeEmptyDirs = false
        }
        into(layout.buildDirectory.dir("generated/sources/$sourceVersion/$version/java"))
    }

    pluginManager.withPlugin("java") {
        the<SourceSetContainer>().matching { it.name == version }.configureEach {
            java.srcDir(generateSources)
        }
    }
}
