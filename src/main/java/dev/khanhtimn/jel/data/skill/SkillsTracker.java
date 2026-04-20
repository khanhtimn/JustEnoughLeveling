package dev.khanhtimn.jel.data.skill;

import com.mrcrayfish.framework.api.sync.DataSerializer;
import com.mrcrayfish.framework.api.sync.SyncedObject;
import dev.khanhtimn.jel.common.skill.SkillDefinition;
import dev.khanhtimn.jel.common.skill.SkillProgress;
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
 * {@link dev.khanhtimn.jel.common.skill.SkillLogic} which enforces invariants
 * (max level, passive recomputation, etc.).
 */
public final class SkillsTracker extends SyncedObject {

	// ---- Framework serialization ----

	public static final StreamCodec<RegistryFriendlyByteBuf, SkillsTracker> STREAM_CODEC =
			StreamCodec.of(SkillsTracker::writeNetwork, SkillsTracker::readNetwork);

	public static final DataSerializer<SkillsTracker> SERIALIZER =
			new DataSerializer<>(STREAM_CODEC, SkillsTracker::writeTag, SkillsTracker::readTag);

	// ---- State ----

	// Persisted + synced: keyed by skill id (ResourceLocation of the SkillDefinition)
	private final Object2ObjectOpenHashMap<ResourceLocation, SkillProgress> skills = new Object2ObjectOpenHashMap<>();

	// Transient: derived from skill levels × definitions, NOT persisted or synced.
	private final ObjectOpenHashSet<ResourceLocation> abilityFlags = new ObjectOpenHashSet<>();

	// ---- Public query helpers (read-only) ----

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

	// ---- Ability flags (transient, read-only public API) ----

	public boolean hasAbilityFlag(ResourceLocation flagId) {
		return abilityFlags.contains(flagId);
	}

	public Set<ResourceLocation> getAbilityFlags() {
		return Collections.unmodifiableSet(abilityFlags);
	}

	// ---- Mutation (package-private — use SkillLogic for external access) ----

	/**
	 * Set progress for a skill. Removes entry if progress is zero.
	 * Marks dirty for Framework sync.
	 */
	public void setProgress(
			ResourceKey<SkillDefinition> skillKey,
			SkillProgress progress
	) {
		setProgress(skillKey.location(), progress);
	}

	/**
	 * Set progress for a skill. Removes entry if progress is zero.
	 * Marks dirty for Framework sync.
	 */
	public void setProgress(
			ResourceLocation skillId,
			SkillProgress progress
	) {
		if (progress == null || (progress.level() == 0 && progress.xp() == 0)) {
			if (skills.remove(skillId) == null) return; // was already absent
		} else {
			SkillProgress existing = skills.put(skillId, progress);
			if (progress.equals(existing)) return; // no change
		}
		this.markDirty();
	}

	/** Clear all skill progress. */
	public void clearSkills() {
		if (!skills.isEmpty()) {
			skills.clear();
			this.markDirty();
		}
	}

	// ---- Ability flag mutation (package-private — called by PassiveApplier) ----

	/** Grant an ability flag. No persistence, transient only. */
	public void grantAbilityFlag(ResourceLocation flagId) {
		abilityFlags.add(flagId);
	}

	/** Clear all ability flags (called before passive recomputation). */
	public void clearAbilityFlags() {
		abilityFlags.clear();
	}

	// ---- NBT serialization (skills only — ability flags are transient) ----

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

		// Ability flags are NOT serialized — they are derived state.

		return tag;
	}

	private static SkillsTracker readTag(Tag tag, HolderLookup.Provider provider) {
		CompoundTag data = (CompoundTag) tag;
		SkillsTracker tracker = new SkillsTracker();

		if (data.contains("skills", Tag.TAG_COMPOUND)) {
			CompoundTag skillsTag = data.getCompound("skills");
			for (String key : skillsTag.getAllKeys()) {
				ResourceLocation id = ResourceLocation.tryParse(key);
				if (id != null) {
					CompoundTag progressTag = skillsTag.getCompound(key);
					int level = progressTag.getInt("level");
					int xp = progressTag.getInt("xp");
					tracker.skills.put(id, new SkillProgress(level, xp));
				}
			}
		}

		return tracker;
	}

	// ---- Network serialization (skills only — ability flags are transient) ----

	private static void writeNetwork(RegistryFriendlyByteBuf buf, SkillsTracker tracker) {
		buf.writeVarInt(tracker.skills.size());
		tracker.skills.forEach((id, progress) -> {
			buf.writeResourceLocation(id);
			buf.writeVarInt(progress.level());
			buf.writeVarInt(progress.xp());
		});

		// Ability flags are NOT synced — they are derived state.
	}

	private static SkillsTracker readNetwork(RegistryFriendlyByteBuf buf) {
		SkillsTracker tracker = new SkillsTracker();

		int skillCount = buf.readVarInt();
		for (int i = 0; i < skillCount; i++) {
			ResourceLocation id = buf.readResourceLocation();
			int level = buf.readVarInt();
			int xp = buf.readVarInt();
			tracker.skills.put(id, new SkillProgress(level, xp));
		}

		return tracker;
	}
}
