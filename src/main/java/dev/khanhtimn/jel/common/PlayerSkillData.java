package dev.khanhtimn.jel.common;

import com.mrcrayfish.framework.api.sync.DataSerializer;
import com.mrcrayfish.framework.api.sync.SyncedObject;
import dev.khanhtimn.jel.api.skill.SkillDefinition;
import dev.khanhtimn.jel.api.skill.SkillProgress;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

/**
 * Mutable container for a player's skill progress and traits.
 * <p>
 * Extends {@link SyncedObject} so that Framework automatically syncs
 * dirty state to clients via {@link #markDirty()}.
 * <p>
 * <b>Traits are transient</b> — they are derived from skill levels
 * and definitions, recomputed on login/respawn/level-up, and never persisted.
 * They are synced to clients for UI and client-side logic.
 * <p>
 * <b>Mutation methods are package-private</b> — external callers should use
 * {@link dev.khanhtimn.jel.api.JelSkills JelSkills} which enforces invariants
 * (max level, effect recomputation, etc.).
 */
public final class PlayerSkillData extends SyncedObject {


	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSkillData> STREAM_CODEC =
			StreamCodec.of(PlayerSkillData::writeNetwork, PlayerSkillData::readNetwork);

	public static final DataSerializer<PlayerSkillData> SERIALIZER =
			new DataSerializer<>(STREAM_CODEC, PlayerSkillData::writeTag, PlayerSkillData::readTag);


	private final Object2ObjectOpenHashMap<ResourceLocation, SkillProgress> skills = new Object2ObjectOpenHashMap<>();

	private final Object2FloatOpenHashMap<ResourceLocation> traitValues = new Object2FloatOpenHashMap<>();

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

	/**
	 * Removes entry if progress is zero. Marks dirty for Framework sync.
	 */
	public void setProgress(ResourceLocation skillId, SkillProgress progress) {
		if (progress == null || (progress.level() == 0 && progress.xp() == 0)) {
			if (skills.remove(skillId) == null) return; // was already absent
		} else {
			SkillProgress existing = skills.put(skillId, progress);
			if (progress.equals(existing)) return; // no change
		}
		this.markDirty();
	}

	public void clearSkills() {
		if (!skills.isEmpty()) {
			skills.clear();
			this.markDirty();
		}
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

		buf.writeVarInt(skillData.traitValues.size());
		for (var entry : skillData.traitValues.object2FloatEntrySet()) {
			buf.writeResourceLocation(entry.getKey());
			buf.writeFloat(entry.getFloatValue());
		}
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

		return skillData;
	}
}
