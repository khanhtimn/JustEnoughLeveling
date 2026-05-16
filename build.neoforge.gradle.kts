plugins {
	id("mod-platform")
	id("net.neoforged.moddev")
	id("dev.mixinmcp.decompile") version "0.9.0"
}

platform {
	loader = "neoforge"
	dependencies {
		required("minecraft") {
			forgeVersionRange = "[${prop("deps.minecraft")}]"
		}
		required("neoforge") {
			forgeVersionRange = "[1,)"
		}
		required("ldlib2") {
			slug("ldlib2")
			forgeVersionRange = "[${prop("deps.ldlib2")},)"
		}
	}
}

neoForge {
	version = property("deps.neoforge") as String
	accessTransformers.from(rootProject.file("src/main/resources/aw/${stonecutter.current.version}.cfg"))
	validateAccessTransformers = true

	if (hasProperty("deps.parchment")) parchment {
		minecraftVersion = prop("deps.minecraft")
		mappingsVersion = prop("deps.parchment")
	}

	runs {
		register("client") {
			client()
			gameDirectory = file("run/")
			ideName = "NeoForge Client (${stonecutter.active?.version})"
			jvmArgument("-Dmixin.debug.export=true")
			programArgument("--username=Dev")
		}
		register("server") {
			server()
			gameDirectory = file("run/")
			ideName = "NeoForge Server (${stonecutter.active?.version})"
			jvmArgument("-Dmixin.debug.export=true")
		}
	}

	mods {
		register(property("mod.id") as String) {
			sourceSet(sourceSets["main"])
		}
	}
	sourceSets["main"].resources.srcDir("${rootDir}/versions/datagen/${stonecutter.current.version.split("-")[0]}/src/main/generated")
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
	strictMaven("https://maven.firstdark.dev/snapshots") { name = "LDLib2" }

}

dependencies {
	implementation(libs.moulberry.mixinconstraints)
	jarJar(libs.moulberry.mixinconstraints)
	annotationProcessor(libs.mixinsquared.common)?.let { compileOnly(it) }
	jarJar(libs.mixinsquared.neoforge)?.let { implementation(it) }

	implementation("com.lowdragmc.ldlib2:ldlib2-neoforge-${prop("deps.minecraft")}:${prop("deps.ldlib2")}:all")

	implementation("squeek.appleskin:appleskin-neoforge:mc1.21-${prop("deps.appleskin")}")
	api("me.shedaniel.cloth:cloth-config-neoforge:${prop("deps.clothconfig")}")

	runtimeOnly("maven.modrinth:sodium:mc${prop("deps.minecraft")}-${prop("deps.sodium")}-neoforge")
	runtimeOnly("maven.modrinth:sodium-extra:mc${prop("deps.minecraft")}-${prop("deps.sodium-extra")}+neoforge")
	runtimeOnly("dev.emi:emi-neoforge:${prop("deps.emi")}+${prop("deps.minecraft")}")
	runtimeOnly("maven.modrinth:jade:${prop("deps.jade")}+neoforge")
}

tasks.named("createMinecraftArtifacts") {
	dependsOn(tasks.named("stonecutterGenerate"))
}

stonecutter {
	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
		replace("location()", "identifier()")
	}
}
