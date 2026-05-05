package dev.khanhtimn.jel.common;

import com.mrcrayfish.framework.api.sync.DataSerializer;
import com.mrcrayfish.framework.api.sync.SyncedObject;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Tracks per-trait cooldowns using the world's game time as time source.
 * <p>
 * Server stores absolute game-time expiry values. Network sends remaining ticks
 * to avoid time-source mismatch with the client. The client stores receipt time
 * and remaining ticks, counting down locally.
 * <p>
 * Expired entries are removed inline during reads, and filtered out during
 * serialization. No periodic tick needed.
 */
public final class TraitCooldownTracker extends SyncedObject {

	public static final StreamCodec<RegistryFriendlyByteBuf, TraitCooldownTracker> STREAM_CODEC
			= StreamCodec.of(TraitCooldownTracker::writeNetwork, TraitCooldownTracker::readNetwork);

	public static final DataSerializer<TraitCooldownTracker> SERIALIZER
			= new DataSerializer<>(STREAM_CODEC, TraitCooldownTracker::writeTag, TraitCooldownTracker::readTag);

	private final Object2LongOpenHashMap<ResourceLocation> cooldowns = new Object2LongOpenHashMap<>();

	private boolean clientSide;
	private long receiveClientTick;

	public TraitCooldownTracker() {
		cooldowns.defaultReturnValue(Long.MIN_VALUE);
	}

	/**
	 * @param gameTime current value of {@code Level.getGameTime()}
	 */
	public boolean isOnCooldown(ResourceLocation traitId, long gameTime) {
		long expiry = cooldowns.getLong(traitId);
		if (expiry == Long.MIN_VALUE) {
			return false;
		}
		if (resolveRemaining(expiry, gameTime) <= 0) {
			cooldowns.removeLong(traitId);
			return false;
		}
		return true;
	}

	/**
	 * Server-only. Sets a cooldown using absolute game-time expiry.
	 */
	public void setCooldown(ResourceLocation traitId, long gameTime, int durationTicks) {
		cooldowns.put(traitId, gameTime + durationTicks);
		this.markDirty();
	}

	public int getRemainingTicks(ResourceLocation traitId, long gameTime) {
		long expiry = cooldowns.getLong(traitId);
		if (expiry == Long.MIN_VALUE) {
			return 0;
		}
		int remaining = resolveRemaining(expiry, gameTime);
		if (remaining <= 0) {
			cooldowns.removeLong(traitId);
			return 0;
		}
		return remaining;
	}

	public void clearAll() {
		if (!cooldowns.isEmpty()) {
			cooldowns.clear();
			this.markDirty();
		}
	}

	private int resolveRemaining(long storedValue, long currentTime) {
		if (clientSide) {
			long elapsed = currentTime - receiveClientTick;
			return (int) Math.max(0, storedValue - elapsed);
		}
		return (int) Math.max(0, storedValue - currentTime);
	}

	private CompoundTag writeTag(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		for (var entry : cooldowns.object2LongEntrySet()) {
			tag.putLong(entry.getKey().toString(), entry.getLongValue());
		}
		return tag;
	}

	private static TraitCooldownTracker readTag(Tag tag, HolderLookup.Provider provider) {
		TraitCooldownTracker tracker = new TraitCooldownTracker();
		CompoundTag data = (CompoundTag) tag;
		for (String key : data.getAllKeys()) {
			ResourceLocation id = ResourceLocation.tryParse(key);
			if (id != null) {
				tracker.cooldowns.put(id, data.getLong(key));
			}
		}
		return tracker;
	}

	private static void writeNetwork(RegistryFriendlyByteBuf buf, TraitCooldownTracker tracker) {
		buf.writeVarInt(tracker.cooldowns.size());
		for (var entry : tracker.cooldowns.object2LongEntrySet()) {
			buf.writeResourceLocation(entry.getKey());
			if (tracker.clientSide) {
				// Re-encoding a client-side tracker (shouldnt happen, but safe)
				buf.writeVarInt((int) Math.max(0, entry.getLongValue()));
			} else {
				// Server: write absolute expiry
				buf.writeLong(entry.getLongValue());
			}
		}
		buf.writeBoolean(tracker.clientSide);
	}

	private static TraitCooldownTracker readNetwork(RegistryFriendlyByteBuf buf) {
		TraitCooldownTracker tracker = new TraitCooldownTracker();
		int count = buf.readVarInt();
		for (int i = 0; i < count; i++) {
			ResourceLocation id = buf.readResourceLocation();
			long value = buf.readLong();
			tracker.cooldowns.put(id, value);
		}
		boolean wasClient = buf.readBoolean();
		// If the source was server-side (absolute expiry), we need to convert
		// to remaining ticks on the client. This will be done on first access
		// via initClientSide().
		tracker.clientSide = false;
		return tracker;
	}

	/**
	 * Called on the client after receiving a sync to set up client-side time
	 * resolution. Converts absolute server expiry values to remaining ticks
	 * relative to the provided client game time.
	 */
	public void initClientSide(long clientGameTime) {
		if (clientSide) {
			return;
		}
		this.clientSide = true;
		this.receiveClientTick = clientGameTime;
		// Convert absolute server expiries to remaining ticks
		var entries = new Object2LongOpenHashMap<>(cooldowns);
		cooldowns.clear();
		for (var entry : entries.object2LongEntrySet()) {
			long remaining = entry.getLongValue() - clientGameTime;
			if (remaining > 0) {
				cooldowns.put(entry.getKey(), remaining);
			}
		}
	}
}
