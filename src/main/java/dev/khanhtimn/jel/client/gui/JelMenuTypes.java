package dev.khanhtimn.jel.client.gui;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import dev.khanhtimn.jel.Constants;
import net.minecraft.resources.ResourceLocation;

public final class JelMenuTypes {

	public static final ResourceLocation SKILLS_UI =
			ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skills");

	public static void init() {
		PlayerUIMenuType.register(SKILLS_UI, JelSkillUIHolder::new);
	}

	private JelMenuTypes() {
	}
}
