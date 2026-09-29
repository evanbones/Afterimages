plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val mcVersion = prop("deps.minecraft")
val loader = "fabric"

fun prop(name: String) = project.property(name) as String

version = prop("mod.version")
group = prop("mod.group")
base.archivesName = "${prop("mod.id")}-$loader-$minecraft"

sourceSets.main { resources.srcDir(rootProject.file("src/main/overlays/$loader")) }

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://maven.parchmentmc.org/") { name = "ParchmentMC" } }
        filter { includeGroup("org.parchmentmc.data") }
    }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraft:${prop("deps.parchment")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // Cloth Config
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${prop("deps.cloth_config")}")
    modImplementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

    // EMF
    modCompileOnly("maven.modrinth:entity-model-features:${prop("deps.emf")}-fabric-${prop("deps.emf_mc")}")
    modCompileOnly("maven.modrinth:entitytexturefeatures:${prop("deps.etf")}-fabric-${prop("deps.emf_mc")}")

    // Combat Roll
    modCompileOnly("maven.modrinth:combat-roll:${prop("deps.combat_roll")}-fabric")
    modCompileOnly("maven.modrinth:playeranimator:${prop("deps.player_animator")}-fabric")

    // AzureLib Armor (the GeoArmorRenderer API only exists on 1.20)
    if (stonecutter.eval(minecraft, "<1.21")) {
        modCompileOnly("maven.modrinth:azurelib-armor:${prop("deps.azurelib_armor")}")
    }

    compileOnly("org.jetbrains:annotations:26.0.2-1")
    compileOnly("com.google.code.findbugs:jsr305:3.0.1")
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/${prop("mod.id")}.accesswidener")

    runs.configureEach {
        ideConfigGenerated(true)
    }
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
            "minecraft_version" to prop("mod.mc_dep_fabric"),
            "fabric_version" to prop("deps.fabric_api"),
            "fabric_loader_version" to prop("deps.fabric_loader"),
            "mod_name" to prop("mod.name"),
            "mod_author" to prop("mod.author"),
            "mod_id" to prop("mod.id"),
            "license" to prop("mod.license"),
            "description" to prop("mod.description"),
            "credits" to prop("mod.credits"),
            "java_version" to javaVer,
        )
        inputs.properties(props)

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
            expand(props)
        }

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
        from(loomx.modJar.map { it.archiveFile })
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
    file = loomx.modJar.flatMap { it.archiveFile }
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    type = STABLE
    version = "${project.version}-$minecraft-$loader"
    displayName = "${prop("mod.name")} Fabric $minecraft - ${project.version}"
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
