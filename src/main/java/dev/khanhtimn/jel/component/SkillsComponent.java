package dev.khanhtimn.jel.component;

import com.mrcrayfish.framework.api.sync.DataSerializer;
import com.mrcrayfish.framework.api.sync.SyncedObject;
import dev.khanhtimn.jel.registry.skill.SkillDefinition;
import dev.khanhtimn.jel.registry.skill.SkillProgress;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
 */
public final class SkillsComponent extends SyncedObject {

	// ---- Framework serialization ----

	public static final StreamCodec<RegistryFriendlyByteBuf, SkillsComponent> STREAM_CODEC =
			StreamCodec.of(SkillsComponent::writeNetwork, SkillsComponent::readNetwork);

	public static final DataSerializer<SkillsComponent> SERIALIZER =
			new DataSerializer<>(STREAM_CODEC, SkillsComponent::writeTag, SkillsComponent::readTag);

	// ---- State ----

	// Keyed by skill id (ResourceLocation of the SkillDefinition)
	private final Object2ObjectOpenHashMap<ResourceLocation, SkillProgress> skills = new Object2ObjectOpenHashMap<>();

	// Ability flags unlocked by this player
	private final ObjectOpenHashSet<ResourceLocation> abilityFlags = new ObjectOpenHashSet<>();

	// ---- Query helpers ----

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

	// ---- Mutation helpers (all call markDirty()) ----

	public void setProgress(
			ResourceKey<SkillDefinition> skillKey,
			SkillProgress progress
	) {
		setProgress(skillKey.location(), progress);
	}

	public void setProgress(
			ResourceLocation skillId,
			SkillProgress progress
	) {
		if (progress == null || (progress.level() == 0 && progress.xp() == 0)) {
			skills.remove(skillId);
		} else {
			skills.put(skillId, progress);
		}
		this.markDirty();
	}

	public void setLevel(
			ResourceKey<SkillDefinition> skillKey,
			int level
	) {
		setLevel(skillKey.location(), level);
	}

	public void setLevel(ResourceLocation skillId, int level) {
		SkillProgress current = skills.getOrDefault(skillId, SkillProgress.ZERO);
		setProgress(skillId, current.withLevel(level));
	}

	public void setXp(
			ResourceKey<SkillDefinition> skillKey,
			int xp
	) {
		setXp(skillKey.location(), xp);
	}

	public void setXp(ResourceLocation skillId, int xp) {
		SkillProgress current = skills.getOrDefault(skillId, SkillProgress.ZERO);
		setProgress(skillId, current.withXp(xp));
	}

	public void addXp(
			ResourceKey<SkillDefinition> skillKey,
			int xpDelta
	) {
		addXp(skillKey.location(), xpDelta);
	}

	public void addXp(ResourceLocation skillId, int xpDelta) {
		if (xpDelta == 0) return;
		SkillProgress current = skills.getOrDefault(skillId, SkillProgress.ZERO);
		int newXp = Math.max(0, current.xp() + xpDelta);
		setProgress(skillId, current.withXp(newXp));
		// Note: level-up logic based on SkillDefinition/xp formula
		// should be handled externally and then applied via setLevel.
	}

	// ---- Ability flags ----

	public boolean hasAbilityFlag(ResourceLocation flagId) {
		return abilityFlags.contains(flagId);
	}

	public void grantAbilityFlag(ResourceLocation flagId) {
		if (abilityFlags.add(flagId)) {
			this.markDirty();
		}
	}

	public void revokeAbilityFlag(ResourceLocation flagId) {
		if (abilityFlags.remove(flagId)) {
			this.markDirty();
		}
	}

	public Set<ResourceLocation> getAbilityFlags() {
		return Collections.unmodifiableSet(abilityFlags);
	}

	// ---- Raw accessors (useful for recompute from definitions) ----

	public Map<ResourceLocation, SkillProgress> getAllSkills() {
		return Collections.unmodifiableMap(skills);
	}

	public void clearSkills() {
		if (!skills.isEmpty()) {
			skills.clear();
			this.markDirty();
		}
	}

	public void clearAbilityFlags() {
		if (!abilityFlags.isEmpty()) {
			abilityFlags.clear();
			this.markDirty();
		}
	}

	// ---- NBT serialization (for Framework saveToFile) ----

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

		ListTag flagsTag = new ListTag();
		this.abilityFlags.forEach(id -> flagsTag.add(StringTag.valueOf(id.toString())));
		tag.put("ability_flags", flagsTag);

		return tag;
	}

	private static SkillsComponent readTag(Tag tag, HolderLookup.Provider provider) {
		CompoundTag data = (CompoundTag) tag;
		SkillsComponent component = new SkillsComponent();

		if (data.contains("skills", Tag.TAG_COMPOUND)) {
			CompoundTag skillsTag = data.getCompound("skills");
			for (String key : skillsTag.getAllKeys()) {
				ResourceLocation id = ResourceLocation.tryParse(key);
				if (id != null) {
					CompoundTag progressTag = skillsTag.getCompound(key);
					int level = progressTag.getInt("level");
					int xp = progressTag.getInt("xp");
					component.skills.put(id, new SkillProgress(level, xp));
				}
			}
		}

		if (data.contains("ability_flags", Tag.TAG_LIST)) {
			ListTag flagsTag = data.getList("ability_flags", Tag.TAG_STRING);
			flagsTag.forEach(t -> {
				ResourceLocation id = ResourceLocation.tryParse(t.getAsString());
				if (id != null) component.abilityFlags.add(id);
			});
		}

		return component;
	}

	// ---- Network serialization (for Framework sync packets) ----

	private static void writeNetwork(RegistryFriendlyByteBuf buf, SkillsComponent component) {
		buf.writeVarInt(component.skills.size());
		component.skills.forEach((id, progress) -> {
			buf.writeResourceLocation(id);
			buf.writeVarInt(progress.level());
			buf.writeVarInt(progress.xp());
		});

		buf.writeVarInt(component.abilityFlags.size());
		component.abilityFlags.forEach(buf::writeResourceLocation);
	}

	private static SkillsComponent readNetwork(RegistryFriendlyByteBuf buf) {
		SkillsComponent component = new SkillsComponent();

		int skillCount = buf.readVarInt();
		for (int i = 0; i < skillCount; i++) {
			ResourceLocation id = buf.readResourceLocation();
			int level = buf.readVarInt();
			int xp = buf.readVarInt();
			component.skills.put(id, new SkillProgress(level, xp));
		}

		int flagCount = buf.readVarInt();
		for (int i = 0; i < flagCount; i++) {
			component.abilityFlags.add(buf.readResourceLocation());
		}

		return component;
	}
}
