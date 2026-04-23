plugins {
	id("mod-platform")
	id("fabric-loom")
}

platform {
	loader = "fabric"
	dependencies {
		required("minecraft") {
			versionRange = prop("deps.minecraft")
		}
		required("fabric-api") {
			slug("fabric-api")
			versionRange = ">=${prop("deps.fabric-api")}"
		}
		required("fabricloader") {
			versionRange = ">=${libs.fabric.loader.get().version}"
		}
		required("framework") {
			slug("framework")
			versionRange = ">=${prop("deps.framework")}"
		}
		optional("modmenu") {}
	}
}

loom {
	accessWidenerPath = rootProject.file("src/main/resources/aw/${stonecutter.current.version}.accesswidener")
	runs.named("client") {
		client()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "client"
		programArgs("--username=Dev")
		configName = "Fabric Client"
	}
	runs.named("server") {
		server()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "server"
		configName = "Fabric Server"
	}
}

fabricApi {
	configureDataGeneration {
		outputDirectory =
			file("${rootDir}/versions/datagen/${stonecutter.current.version.split("-")[0]}/src/main/generated")
		client = true
	}
}

repositories {
	mavenCentral()
	exclusiveContent {
		forRepository { maven("https://api.modrinth.com/maven/") { name = "Modrinth" } }
		filter { includeGroup("maven.modrinth") }
	}
	exclusiveContent {
		forRepository { maven("https://cursemaven.com/") { name = "CurseForge" } }
		filter { includeGroup("curse.maven") }
	}
	strictMaven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
	strictMaven("https://maven.bawnorton.com/releases/", "com.github.bawnorton.mixinsquared") { name = "MixinSquared" }
	strictMaven("https://maven.pkg.github.com/MrCrayfish/Maven/") {
		name = "MrCrayfish (GitHub)"
		credentials {
			username = project.findProperty("gpr.user") as String?
			password = project.findProperty("gpr.key") as String?
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
	mappings(
		loom.layered {
			officialMojangMappings()
			if (hasProperty("deps.parchment")) parchment("org.parchmentmc.data:parchment-${prop("deps.parchment")}@zip")
		})
	modImplementation(libs.fabric.loader)
	implementation(libs.moulberry.mixinconstraints)
	include(libs.moulberry.mixinconstraints)
	annotationProcessor(libs.mixinsquared.fabric)
	implementation(libs.mixinsquared.fabric)
	include(libs.mixinsquared.fabric)

	modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
	modLocalRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

	modImplementation("com.mrcrayfish:framework-fabric:${prop("deps.minecraft")}-${prop("deps.framework")}")
	modLocalRuntime("maven.modrinth:sodium:mc${prop("deps.minecraft")}-${prop("deps.sodium")}-fabric")
	modLocalRuntime("maven.modrinth:sodium-extra:mc${prop("deps.minecraft")}-${prop("deps.sodium-extra")}+fabric")
	modLocalRuntime("dev.emi:emi-fabric:${prop("deps.emi")}+${prop("deps.minecraft")}")
	modLocalRuntime("maven.modrinth:jade:${prop("deps.jade")}+fabric")
}

stonecutter {
	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
		replace("location()", "identifier()")
	}
}
