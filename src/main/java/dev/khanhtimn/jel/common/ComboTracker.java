package dev.khanhtimn.jel.common;

import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Transient per-player tracker for consecutive melee hits on the same target.
 */
public final class ComboTracker {

    private static final int COMBO_WINDOW_TICKS = 80;
    private static final int MAX_TRACKED = 8;

    private final Object2ObjectOpenHashMap<UUID, ComboState> combos = new Object2ObjectOpenHashMap<>();

    public record ComboState(int hitCount, long lastHitTick) {

    }

    public int recordHit(UUID targetId, long gameTime) {
        ComboState state = combos.get(targetId);
        if (state == null || gameTime - state.lastHitTick() > COMBO_WINDOW_TICKS) {
            if (combos.size() >= MAX_TRACKED) {
                evictOldest(gameTime);
            }
            combos.put(targetId, new ComboState(1, gameTime));
            return 1;
        }
        ComboState next = new ComboState(state.hitCount() + 1, gameTime);
        combos.put(targetId, next);
        return next.hitCount();
    }

    public int getComboCount(UUID targetId, long gameTime) {
        ComboState state = combos.get(targetId);
        if (state == null || gameTime - state.lastHitTick() > COMBO_WINDOW_TICKS) {
            return 0;
        }
        return state.hitCount();
    }

    public void onTargetDeath(UUID targetId) {
        combos.remove(targetId);
    }

    public void onPlayerDeath() {
        combos.clear();
    }

    private void evictOldest(long gameTime) {
        UUID oldest = null;
        long oldestTick = Long.MAX_VALUE;
        for (var entry : combos.entrySet()) {
            if (entry.getValue().lastHitTick() < oldestTick) {
                oldestTick = entry.getValue().lastHitTick();
                oldest = entry.getKey();
            }
        }
        if (oldest != null) {
            combos.remove(oldest);
        }
    }
}
