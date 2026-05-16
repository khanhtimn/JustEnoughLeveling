package dev.khanhtimn.jel.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public final class PlayerSkillData {

	public static final Codec<PlayerSkillData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.unboundedMap(ResourceLocation.CODEC, SkillProgress.CODEC)
					.fieldOf("skills").forGetter(d -> Map.copyOf(d.skills)),
			ResourceLocation.CODEC.listOf()
					.xmap(HashSet::new, ArrayList::new)
					.fieldOf("perks").forGetter(d -> new HashSet<>(d.unlockedPerks)),
			Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC)
					.fieldOf("branches").forGetter(d -> Map.copyOf(d.branchChoices))
	).apply(inst, PlayerSkillData::fromCodec));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSkillData> STREAM_CODEC =
			StreamCodec.of(PlayerSkillData::writeNetwork, PlayerSkillData::readNetwork);

	private static PlayerSkillData fromCodec(
			Map<ResourceLocation, SkillProgress> skills,
			HashSet<ResourceLocation> perks,
			Map<ResourceLocation, ResourceLocation> branches
	) {
		PlayerSkillData data = new PlayerSkillData();
		data.skills.putAll(skills);
		data.unlockedPerks.addAll(perks);
		data.branchChoices.putAll(branches);
		return data;
	}

	private final Object2ObjectOpenHashMap<ResourceLocation, SkillProgress> skills = new Object2ObjectOpenHashMap<>();

	private final Object2FloatOpenHashMap<ResourceLocation> traitValues = new Object2FloatOpenHashMap<>();

	private final HashMap<ResourceLocation, Double> originalBases = new HashMap<>();

	private final ObjectOpenHashSet<ResourceLocation> unlockedPerks = new ObjectOpenHashSet<>();

	private final HashMap<ResourceLocation, IntSet> requirementsMet = new HashMap<>();

	private final Object2ObjectOpenHashMap<ResourceLocation, ResourceLocation> branchChoices = new Object2ObjectOpenHashMap<>();


	public int getLevel(ResourceKey<SkillDefinition> skillKey) {
		return getLevel(skillKey.location());
	}

	public int getLevel(ResourceLocation skillId) {
		return skills.getOrDefault(skillId, SkillProgress.ZERO).level();
	}

	public int getXp(ResourceKey<SkillDefinition> skillKey) {
		return getXp(skillKey.location());
	}

	public int getXp(ResourceLocation skillId) {
		return skills.getOrDefault(skillId, SkillProgress.ZERO).xp();
	}

	public SkillProgress getProgress(ResourceKey<SkillDefinition> skillKey) {
		return getProgress(skillKey.location());
	}

	public SkillProgress getProgress(ResourceLocation skillId) {
		return skills.getOrDefault(skillId, SkillProgress.ZERO);
	}

	public boolean hasSkill(ResourceKey<SkillDefinition> skillKey) {
		return skills.containsKey(skillKey.location());
	}

	public boolean hasSkill(ResourceLocation skillId) {
		return skills.containsKey(skillId);
	}

	public Map<ResourceLocation, SkillProgress> getAllSkills() {
		return Collections.unmodifiableMap(skills);
	}


	public boolean hasTrait(ResourceLocation traitId) {
		return traitValues.containsKey(traitId);
	}

	public float getTraitValue(ResourceLocation traitId) {
		return traitValues.getOrDefault(traitId, 0f);
	}

	public Object2FloatOpenHashMap<ResourceLocation> getTraitEntries() {
		return traitValues;
	}


	public void setProgress(ResourceKey<SkillDefinition> skillKey, SkillProgress progress) {
		setProgress(skillKey.location(), progress);
	}

	public void setProgress(ResourceLocation skillId, SkillProgress progress) {
		if (progress == null || (progress.level() == 0 && progress.xp() == 0)) {
			skills.remove(skillId);
		} else {
			skills.put(skillId, progress);
		}
	}

	public void clearSkills() {
		skills.clear();
	}


	public void setTraitValue(ResourceLocation traitId, float value) {
		traitValues.put(traitId, value);
	}

	public void removeTraitValue(ResourceLocation traitId) {
		traitValues.removeFloat(traitId);
	}

	public void clearTraits() {
		traitValues.clear();
	}


	public void saveOriginalBase(ResourceLocation attr, double originalValue) {
		originalBases.putIfAbsent(attr, originalValue);
	}


	public Map<ResourceLocation, Double> getOriginalBases() {
		return Collections.unmodifiableMap(originalBases);
	}


	public void clearOriginalBases() {
		originalBases.clear();
	}


	public boolean isPerkUnlocked(ResourceLocation perkId) {
		return unlockedPerks.contains(perkId);
	}

	public void unlockPerk(ResourceLocation perkId) {
		unlockedPerks.add(perkId);
	}


	public boolean isRequirementMet(ResourceLocation skillId, int targetLevel) {
		IntSet met = requirementsMet.get(skillId);
		return met != null && met.contains(targetLevel);
	}

	public void setRequirementMet(ResourceLocation skillId, int targetLevel, boolean met) {
		if (met) {
			requirementsMet.computeIfAbsent(skillId, k -> new IntOpenHashSet()).add(targetLevel);
		} else {
			IntSet set = requirementsMet.get(skillId);
			if (set != null) {
				set.remove(targetLevel);
				if (set.isEmpty()) {
					requirementsMet.remove(skillId);
				}
			}
		}
	}

	public void clearRequirementsMet() {
		requirementsMet.clear();
	}


	public Optional<ResourceLocation> getBranch(ResourceLocation skillId) {
		return Optional.ofNullable(branchChoices.get(skillId));
	}

	public Optional<ResourceLocation> getBranch(ResourceKey<SkillDefinition> skillKey) {
		return getBranch(skillKey.location());
	}

	public void setBranch(ResourceLocation skillId, ResourceLocation branchId) {
		branchChoices.put(skillId, branchId);
	}

	public void clearBranch(ResourceLocation skillId) {
		branchChoices.remove(skillId);
	}

	public boolean isBranch(ResourceKey<SkillDefinition> skillKey, ResourceLocation branchId) {
		return branchId.equals(branchChoices.get(skillKey.location()));
	}

	public boolean isBranch(ResourceLocation skillId, ResourceLocation branchId) {
		return branchId.equals(branchChoices.get(skillId));
	}

	public void clearAllBranches() {
		branchChoices.clear();
	}


	private static void writeNetwork(RegistryFriendlyByteBuf buf, PlayerSkillData skillData) {
		buf.writeVarInt(skillData.skills.size());
		skillData.skills.forEach((id, progress) -> {
			buf.writeResourceLocation(id);
			buf.writeVarInt(progress.level());
			buf.writeVarInt(progress.xp());
		});

		buf.writeVarInt(skillData.traitValues.size());
		for (var entry : skillData.traitValues.object2FloatEntrySet()) {
			buf.writeResourceLocation(entry.getKey());
			buf.writeFloat(entry.getFloatValue());
		}

		buf.writeVarInt(skillData.unlockedPerks.size());
		for (ResourceLocation perkId : skillData.unlockedPerks) {
			buf.writeResourceLocation(perkId);
		}

		int reqEntries = skillData.requirementsMet.values().stream().mapToInt(IntSet::size).sum();
		buf.writeVarInt(reqEntries);
		for (var entry : skillData.requirementsMet.entrySet()) {
			for (int level : entry.getValue()) {
				buf.writeResourceLocation(entry.getKey());
				buf.writeVarInt(level);
			}
		}

		buf.writeVarInt(skillData.branchChoices.size());
		skillData.branchChoices.forEach((skillId, branchId) -> {
			buf.writeResourceLocation(skillId);
			buf.writeResourceLocation(branchId);
		});
	}

	private static PlayerSkillData readNetwork(RegistryFriendlyByteBuf buf) {
		PlayerSkillData skillData = new PlayerSkillData();

		int skillCount = buf.readVarInt();
		for (int i = 0; i < skillCount; i++) {
			ResourceLocation id = buf.readResourceLocation();
			int level = buf.readVarInt();
			int xp = buf.readVarInt();
			skillData.skills.put(id, new SkillProgress(level, xp));
		}

		int traitCount = buf.readVarInt();
		for (int i = 0; i < traitCount; i++) {
			ResourceLocation id = buf.readResourceLocation();
			float value = buf.readFloat();
			skillData.traitValues.put(id, value);
		}

		int perkCount = buf.readVarInt();
		for (int i = 0; i < perkCount; i++) {
			skillData.unlockedPerks.add(buf.readResourceLocation());
		}

		int reqCount = buf.readVarInt();
		for (int i = 0; i < reqCount; i++) {
			ResourceLocation skillId = buf.readResourceLocation();
			int level = buf.readVarInt();
			skillData.requirementsMet.computeIfAbsent(skillId, k -> new IntOpenHashSet()).add(level);
		}

		int branchCount = buf.readVarInt();
		for (int i = 0; i < branchCount; i++) {
			ResourceLocation skillId = buf.readResourceLocation();
			ResourceLocation branchId = buf.readResourceLocation();
			skillData.branchChoices.put(skillId, branchId);
		}

		return skillData;
	}
}
