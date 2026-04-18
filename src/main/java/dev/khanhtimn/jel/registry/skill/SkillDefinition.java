package dev.khanhtimn.jel.registry.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SkillDefinition(
		SkillDisplay display,
		int maxLevel,
		XpFormula xp,
		List<SkillPassive> passives
) {
	public static final Codec<SkillDefinition> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(
					SkillDisplay.CODEC.fieldOf("display")
							.forGetter(SkillDefinition::display),
					Codec.INT.optionalFieldOf("max_level", 10)
							.forGetter(SkillDefinition::maxLevel),
					XpFormula.CODEC.optionalFieldOf(
									"xp",
									XpFormula.Vanilla.INSTANCE
							)
							.forGetter(SkillDefinition::xp),
					SkillPassive.CODEC.listOf()
							.optionalFieldOf("passives", List.of())
							.forGetter(SkillDefinition::passives)
			).apply(instance, SkillDefinition::new));

	public static final Codec<SkillDefinition> NETWORK_CODEC = CODEC;

	public SkillDefinition {
		if (maxLevel < 1) maxLevel = 1;
		passives = List.copyOf(passives);
	}

	public int clampLevel(int level) {
		return net.minecraft.util.Mth.clamp(level, 0, maxLevel);
	}

	public boolean isMaxLevel(int level) {
		return level >= maxLevel;
	}

	/** Skill XP needed to reach targetLevel from targetLevel-1. */
	public int xpCostForLevel(int targetLevel) {
		if (targetLevel <= 0 || targetLevel > maxLevel) return 0;
		return xp.costForLevel(targetLevel);
	}

	/** Skill XP needed to go from currentLevel to currentLevel+1. */
	public int xpCostForNextLevel(int currentLevel) {
		if (isMaxLevel(currentLevel)) return 0;
		return xp.costForLevel(currentLevel + 1);
	}

	/** Total skill XP to reach targetLevel from 0. */
	public int totalXpCostToReachLevel(int targetLevel) {
		targetLevel = clampLevel(targetLevel);
		return xp.totalCostToLevel(targetLevel);
	}
}
