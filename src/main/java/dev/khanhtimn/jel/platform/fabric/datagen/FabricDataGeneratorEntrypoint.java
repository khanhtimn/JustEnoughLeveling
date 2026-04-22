package dev.khanhtimn.jel.platform.fabric.datagen;

//? fabric {

/*import dev.khanhtimn.jel.core.ModRegistries;
import dev.khanhtimn.jel.common.skill.ModSkills;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
//? != 1.19.2 {
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
//?}
import net.minecraft.core.RegistrySetBuilder;

public class FabricDataGeneratorEntrypoint implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		final FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(ModDynamicRegistryProvider::new);
		pack.addProvider((FabricDataOutput output) -> new ModRecipeProvider(output, generator.getRegistries()));
	}

	@Override
	public void buildRegistry(RegistrySetBuilder builder) {
		builder.add(ModRegistries.SKILL_REGISTRY_KEY, ModSkills::bootstrap);
	}

}
*///?}
