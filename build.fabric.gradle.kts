plugins {
	id("mod-platform")
	id("fabric-loom")
	id("dev.mixinmcp.decompile") version "0.9.0"
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
		required("ldlib2") {
			slug("ldlib2")
			versionRange = ">=${prop("deps.ldlib2")}"
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
	runs.configureEach {
		vmArgs(
			"-Dmixin.debug.export=true",
			"-XX:+AllowEnhancedClassRedefinition"
		)
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
	strictMaven("https://maven.parchmentmc.org/") {
		name = "ParchmentMC"
	}
	strictMaven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
	strictMaven("https://maven.ryanliptak.com/") { name = "AppleSkin" }
	strictMaven("https://maven.shedaniel.me/") { name = "Cloth Config" }
	strictMaven("https://maven.bawnorton.com/releases/", "com.github.bawnorton.mixinsquared") { name = "MixinSquared" }

}

dependencies {
	minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
	mappings(
		loom.layered {
			officialMojangMappings()
			if (hasProperty("deps.parchment")) parchment(
				"org.parchmentmc.data:parchment-${prop("deps.minecraft")}:${
					prop(
						"deps.parchment"
					)
				}@zip"
			)
		})
	modImplementation(libs.fabric.loader)
	implementation(libs.moulberry.mixinconstraints)
	include(libs.moulberry.mixinconstraints)
	annotationProcessor(libs.mixinsquared.fabric)
	implementation(libs.mixinsquared.fabric)
	include(libs.mixinsquared.fabric)

	modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${prop("deps.fabric-language-kotlin")}")
	modLocalRuntime("maven.modrinth:architectury-api:${prop("deps.architectury-api")}+fabric")
	modLocalRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}")

	modImplementation("maven.modrinth:ldlib-fabric:${prop("deps.ldlib2")}")

	// Yoga layout engine
	implementation(libs.yoga)
	include(libs.yoga)

	// Taffy
	implementation(libs.taffy)
	include(libs.taffy)

	modImplementation("squeek.appleskin:appleskin-fabric:mc1.21-${prop("deps.appleskin")}")
	modApi("me.shedaniel.cloth:cloth-config-fabric:${prop("deps.clothconfig")}")
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
