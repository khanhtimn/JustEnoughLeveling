package dev.khanhtimn.jel.core;

import dev.khanhtimn.jel.event.CombatEvents;
import dev.khanhtimn.jel.event.FarmingEvents;
import dev.khanhtimn.jel.event.FoodEvents;
import dev.khanhtimn.jel.event.MiningEvents;
import dev.khanhtimn.jel.event.SkillEvents;

public class ModEvents {
	public static void init() {
		SkillEvents.register();
		FoodEvents.register();
		CombatEvents.register();
		MiningEvents.register();
		FarmingEvents.register();
	}
}
