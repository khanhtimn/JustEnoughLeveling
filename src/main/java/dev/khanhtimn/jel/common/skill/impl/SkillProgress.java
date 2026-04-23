package dev.khanhtimn.jel.common.skill.impl;


public record SkillProgress(int level, int xp) {
	public static final SkillProgress ZERO = new SkillProgress(0, 0);

	public SkillProgress {
		level = Math.max(0, level);
		xp = Math.max(0, xp);
	}

	public SkillProgress withLevel(int newLevel) {
		return new SkillProgress(newLevel, xp);
	}

	public SkillProgress withXp(int newXp) {
		return new SkillProgress(level, newXp);
	}
}
