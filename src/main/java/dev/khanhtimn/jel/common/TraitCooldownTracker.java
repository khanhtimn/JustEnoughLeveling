package dev.khanhtimn.jel.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Tracks per-trait cooldowns using the world's game time as time source.
 * <p>
 * Server stores absolute game-time expiry values. Network sends absolute expiry;
 * client converts to remaining ticks on receipt via {@link #initClientSide(long)}.
 * Expired entries are removed inline during reads. No periodic tick needed.
 */
public final class TraitCooldownTracker {

	public static final Codec<TraitCooldownTracker> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG)
					.fieldOf("cooldowns").forGetter(t -> Map.copyOf(t.cooldowns))
	).apply(inst, TraitCooldownTracker::fromCodec));

	public static final StreamCodec<RegistryFriendlyByteBuf, TraitCooldownTracker> STREAM_CODEC
			= StreamCodec.of(TraitCooldownTracker::writeNetwork, TraitCooldownTracker::readNetwork);

	private static TraitCooldownTracker fromCodec(Map<ResourceLocation, Long> cooldowns) {
		TraitCooldownTracker tracker = new TraitCooldownTracker();
		tracker.cooldowns.putAll(cooldowns);
		return tracker;
	}

	private final Object2LongOpenHashMap<ResourceLocation> cooldowns = new Object2LongOpenHashMap<>();

	private boolean clientSide;
	private long receiveClientTick;

	public TraitCooldownTracker() {
		cooldowns.defaultReturnValue(Long.MIN_VALUE);
	}

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

	public void setCooldown(ResourceLocation traitId, long gameTime, int durationTicks) {
		cooldowns.put(traitId, gameTime + durationTicks);
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
		cooldowns.clear();
	}

	private int resolveRemaining(long storedValue, long currentTime) {
		if (clientSide) {
			long elapsed = currentTime - receiveClientTick;
			return (int) Math.max(0, storedValue - elapsed);
		}
		return (int) Math.max(0, storedValue - currentTime);
	}

	private static void writeNetwork(RegistryFriendlyByteBuf buf, TraitCooldownTracker tracker) {
		buf.writeVarInt(tracker.cooldowns.size());
		for (var entry : tracker.cooldowns.object2LongEntrySet()) {
			buf.writeResourceLocation(entry.getKey());
			buf.writeLong(entry.getLongValue());
		}
	}

	private static TraitCooldownTracker readNetwork(RegistryFriendlyByteBuf buf) {
		TraitCooldownTracker tracker = new TraitCooldownTracker();
		int count = buf.readVarInt();
		for (int i = 0; i < count; i++) {
			ResourceLocation id = buf.readResourceLocation();
			long value = buf.readLong();
			tracker.cooldowns.put(id, value);
		}
		return tracker;
	}

	public void initClientSide(long clientGameTime) {
		if (clientSide) {
			return;
		}
		this.clientSide = true;
		this.receiveClientTick = clientGameTime;
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
