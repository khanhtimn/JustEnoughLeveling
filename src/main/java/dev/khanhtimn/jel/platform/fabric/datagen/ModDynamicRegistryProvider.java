package dev.khanhtimn.jel.platform.fabric.datagen;

//? fabric {

/*import dev.khanhtimn.jel.Constants;
import dev.khanhtimn.jel.core.ModRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModDynamicRegistryProvider extends FabricDynamicRegistryProvider {

	public ModDynamicRegistryProvider(FabricDataOutput output,
	                                  CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		entries.addAll(registries.lookupOrThrow(ModRegistries.SKILL_REGISTRY_KEY));
	}

	@NotNull
	@Override
	public String getName() {
		return Constants.MOD_ID + ":dynamic_registries";
	}
}
*///?}
