package dev.khanhtimn.jel.core;

import dev.khanhtimn.jel.event.SkillEvents;

public class ModEvents {
	public static void init() {
		SkillEvents.register();
	}
}
