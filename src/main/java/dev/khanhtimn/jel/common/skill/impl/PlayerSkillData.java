package dev.khanhtimn.jel.common.skill.impl;

import com.mrcrayfish.framework.api.sync.DataSerializer;
import com.mrcrayfish.framework.api.sync.SyncedObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mutable container for a player's skill progress and ability flags.
 * <p>
 * Extends {@link SyncedObject} so that Framework automatically syncs
 * dirty state to clients via {@link #markDirty()}.
 * <p>
 * <b>Ability flags are transient</b> — they are derived from skill levels
 * and definitions, recomputed on login/respawn/level-up, and never persisted
 * or synced. This ensures a single source of truth.
 * <p>
 * <b>Mutation methods are package-private</b> — external callers should use
 * {@link SkillLogic} which enforces invariants
 * (max level, effect recomputation, etc.).
 */
public final class PlayerSkillData extends SyncedObject {


	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSkillData> STREAM_CODEC =
			StreamCodec.of(PlayerSkillData::writeNetwork, PlayerSkillData::readNetwork);

	public static final DataSerializer<PlayerSkillData> SERIALIZER =
			new DataSerializer<>(STREAM_CODEC, PlayerSkillData::writeTag, PlayerSkillData::readTag);


	private final Object2ObjectOpenHashMap<ResourceLocation, SkillProgress> skills = new Object2ObjectOpenHashMap<>();

	private final ObjectOpenHashSet<ResourceLocation> abilityFlags = new ObjectOpenHashSet<>();

	private final HashMap<ResourceLocation, Double> originalBases = new HashMap<>();


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


	public boolean hasAbilityFlag(ResourceLocation flagId) {
		return abilityFlags.contains(flagId);
	}

	public Set<ResourceLocation> getAbilityFlags() {
		return Collections.unmodifiableSet(abilityFlags);
	}



	void setProgress(ResourceKey<SkillDefinition> skillKey, SkillProgress progress) {
		setProgress(skillKey.location(), progress);
	}

	/** Removes entry if progress is zero. Marks dirty for Framework sync. */
	void setProgress(ResourceLocation skillId, SkillProgress progress) {
		if (progress == null || (progress.level() == 0 && progress.xp() == 0)) {
			if (skills.remove(skillId) == null) return; // was already absent
		} else {
			SkillProgress existing = skills.put(skillId, progress);
			if (progress.equals(existing)) return; // no change
		}
		this.markDirty();
	}

	void clearSkills() {
		if (!skills.isEmpty()) {
			skills.clear();
			this.markDirty();
		}
	}


	public void grantAbilityFlag(ResourceLocation flagId) {
		abilityFlags.add(flagId);
	}

	public void removeAbilityFlag(ResourceLocation flagId) {
		abilityFlags.remove(flagId);
	}

	public void clearAbilityFlags() {
		abilityFlags.clear();
	}


	void saveOriginalBase(ResourceLocation attr, double originalValue) {
		originalBases.putIfAbsent(attr, originalValue);
	}


	Map<ResourceLocation, Double> getOriginalBases() {
		return Collections.unmodifiableMap(originalBases);
	}


	void clearOriginalBases() {
		originalBases.clear();
	}


	private CompoundTag writeTag(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();

		CompoundTag skillsTag = new CompoundTag();
		this.skills.forEach((id, progress) -> {
			CompoundTag progressTag = new CompoundTag();
			progressTag.putInt("level", progress.level());
			progressTag.putInt("xp", progress.xp());
			skillsTag.put(id.toString(), progressTag);
		});
		tag.put("skills", skillsTag);



		return tag;
	}

	private static PlayerSkillData readTag(Tag tag, HolderLookup.Provider provider) {
		CompoundTag data = (CompoundTag) tag;
		PlayerSkillData skillData = new PlayerSkillData();

		if (data.contains("skills", Tag.TAG_COMPOUND)) {
			CompoundTag skillsTag = data.getCompound("skills");
			for (String key : skillsTag.getAllKeys()) {
				ResourceLocation id = ResourceLocation.tryParse(key);
				if (id != null) {
					CompoundTag progressTag = skillsTag.getCompound(key);
					int level = progressTag.getInt("level");
					int xp = progressTag.getInt("xp");
					skillData.skills.put(id, new SkillProgress(level, xp));
				}
			}
		}

		return skillData;
	}


	private static void writeNetwork(RegistryFriendlyByteBuf buf, PlayerSkillData skillData) {
		buf.writeVarInt(skillData.skills.size());
		skillData.skills.forEach((id, progress) -> {
			buf.writeResourceLocation(id);
			buf.writeVarInt(progress.level());
			buf.writeVarInt(progress.xp());
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

		return skillData;
	}
}
