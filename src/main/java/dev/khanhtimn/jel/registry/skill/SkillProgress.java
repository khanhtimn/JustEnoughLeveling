package dev.khanhtimn.jel.registry.skill;

/**
 * Immutable value object representing a player's progress in a single skill.
 * Serialization is handled by {@link dev.khanhtimn.jel.component.SkillsComponent}.
 */
public record SkillProgress(int level, int xp) {
	public static final SkillProgress ZERO = new SkillProgress(0, 0);

	public SkillProgress withLevel(int newLevel) {
		return new SkillProgress(newLevel, xp);
	}

	public SkillProgress withXp(int newXp) {
		return new SkillProgress(level, newXp);
	}
}
