plugins {
	id("mod-platform")
	id("net.neoforged.moddev")
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
		required("framework") {
			slug("framework")
			forgeVersionRange = "[${prop("deps.framework")},)"
		}
	}
}

neoForge {
	version = property("deps.neoforge") as String
	accessTransformers.from(rootProject.file("src/main/resources/aw/${stonecutter.current.version}.cfg"))
	validateAccessTransformers = true

	if (hasProperty("deps.parchment")) parchment {
		val (mc, ver) = (property("deps.parchment") as String).split(':')
		mappingsVersion = ver
		minecraftVersion = mc
	}

	runs {
		register("client") {
			client()
			gameDirectory = file("run/")
			ideName = "NeoForge Client (${stonecutter.active?.version})"
			programArgument("--username=Dev")
		}
		register("server") {
			server()
			gameDirectory = file("run/")
			ideName = "NeoForge Server (${stonecutter.active?.version})"
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
	strictMaven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
	strictMaven("https://maven.bawnorton.com/releases/", "com.github.bawnorton.mixinsquared") { name = "MixinSquared" }
	strictMaven("https://maven.pkg.github.com/MrCrayfish/Maven/") {
		name = "MrCrayfish (GitHub)"
		credentials {
			username = project.findProperty("gpr.user") as? String
			password = project.findProperty("gpr.key") as? String
		}
	}
}

dependencies {
	implementation(libs.moulberry.mixinconstraints)
	jarJar(libs.moulberry.mixinconstraints)
	annotationProcessor(libs.mixinsquared.common)?.let { compileOnly(it) }
	jarJar(libs.mixinsquared.neoforge)?.let { implementation(it) }

	implementation("com.mrcrayfish:framework-neoforge:${prop("deps.minecraft")}-${prop("deps.framework")}")

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
