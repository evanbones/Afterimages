plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val mcVersion = prop("deps.minecraft")
val loader = "neoforge"

fun prop(name: String) = project.property(name) as String

version = prop("mod.version")
group = prop("mod.group")
base.archivesName = "${prop("mod.id")}-$loader-$minecraft"

sourceSets.main { resources.srcDir(rootProject.file("src/main/overlays/$loader")) }

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

neoForge {
    version = prop("deps.neoforge")
    accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    parchment {
        minecraftVersion = minecraft
        mappingsVersion = prop("deps.parchment")
    }

    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", prop("mod.id"))
            ideName = "NeoForge ${name.replaceFirstChar(Char::uppercase)} ($minecraft)"
        }
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2-1")
    compileOnly("com.google.code.findbugs:jsr305:3.0.1")

    // Cloth Config
    implementation("me.shedaniel.cloth:cloth-config-neoforge:${prop("deps.cloth_config")}")

    // EMF
    compileOnly("maven.modrinth:entity-model-features:${prop("deps.emf")}-neoforge-${prop("deps.emf_mc")}")
    compileOnly("maven.modrinth:entitytexturefeatures:${prop("deps.etf")}-neoforge-${prop("deps.emf_mc")}")

    // Combat Roll
    compileOnly("maven.modrinth:combat-roll:${prop("deps.combat_roll")}-neoforge")
    compileOnly("maven.modrinth:playeranimator:${prop("deps.player_animator")}-forge")
}

val javaVer = prop("deps.java_version").toInt()
java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVer)
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = javaVer
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "group" to project.group,
            "minecraft_version" to minecraft,
            "minecraft_version_range" to prop("mod.mc_dep_forgelike"),
            "neoforge_version" to prop("deps.neoforge"),
            "neoforge_loader_version_range" to prop("deps.neoforge_loader_version_range"),
            "mod_name" to prop("mod.name"),
            "mod_author" to prop("mod.author"),
            "mod_id" to prop("mod.id"),
            "license" to prop("mod.license"),
            "description" to prop("mod.description"),
            "credits" to prop("mod.credits"),
            "java_version" to javaVer,
        )
        inputs.properties(props)

        filesMatching(listOf("pack.mcmeta", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
            expand(props)
        }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${prop("mod.name")}" }
        }

        manifest.attributes(
            "Specification-Title" to prop("mod.name"),
            "Specification-Vendor" to prop("mod.author"),
            "Specification-Version" to archiveVersion,
            "Implementation-Title" to loader,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor" to prop("mod.author"),
            "Built-On-Minecraft" to mcVersion,
        )
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${project.version}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        System.getenv("local_maven_url")?.let { maven(it) }
    }
}

publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} NeoForge $minecraft - ${project.version}"
    modLoaders.add(loader)

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = prop("publish.curseforge")
        minecraftVersions.add(mcVersion)
        client = true
        server = false

        optional("cloth-config")
        optional("combat-roll")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = prop("publish.modrinth")
        minecraftVersions.add(mcVersion)

        optional("cloth-config")
        optional("combat-roll")
    }
}
