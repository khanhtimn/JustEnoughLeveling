package dev.khanhtimn.jel.common.skill.skills;

import dev.khanhtimn.jel.common.skill.impl.SkillDefinition;
import dev.khanhtimn.jel.common.skill.impl.SkillPassive;
import dev.khanhtimn.jel.common.skill.impl.XpFormula;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

/**
 * Constitution skill: controls max health base value.
 * <p>
 * At level 0, the player starts with only 3 hearts (6.0 health).
 * Each level increases the base, reaching vanilla 20.0 at level 10.
 */
public final class Constitution {

	public static SkillDefinition create() {
		return SkillDefinition.builder()
				.name("Constitution")
				.description("Increases max health")
				.icon("minecraft:golden_apple")
				.color(0xCC3333)
				.maxLevel(10)
				.xpFormula(XpFormula.of(LevelBasedValue.perLevel(100, 50)))
				// Base health: explicit per-level values (level 0 → 6.0, level 10 → 20.0)
				.attributeBase("minecraft:generic.max_health",
						LevelBasedValue.lookup(
								List.of(6f, 8f, 10f, 12f, 14f, 16f, 17f, 18f, 19f, 20f),
								LevelBasedValue.constant(20f)
						))
				.passive(SkillPassive.abilityFlag(10, "jel:self_heal"))
				.build();
	}

	private Constitution() {
	}
}
