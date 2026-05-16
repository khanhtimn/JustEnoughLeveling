package dev.khanhtimn.jel.client.gui;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import dev.khanhtimn.jel.client.gui.sync.SkillSyncSetup;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class JelSkillUIHolder implements PlayerUIMenuType.PlayerUIHolder {

	private final SkillSyncSetup syncSetup;

	public JelSkillUIHolder(Player player) {
		this.syncSetup = new SkillSyncSetup(player);
	}

	@NotNull
	@Override
	public ModularUI createUI(@NotNull Player player) {
		return ModularUI.of(SkillUIBuilder.buildUI(syncSetup), player);
	}
}
