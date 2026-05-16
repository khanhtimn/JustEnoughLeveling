package dev.khanhtimn.jel.api.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SkillProgress(int level, int xp) {
	public static final SkillProgress ZERO = new SkillProgress(0, 0);

	public static final Codec<SkillProgress> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("level").forGetter(SkillProgress::level),
			Codec.INT.fieldOf("xp").forGetter(SkillProgress::xp)
	).apply(inst, SkillProgress::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SkillProgress> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT.cast(), SkillProgress::level,
					ByteBufCodecs.VAR_INT.cast(), SkillProgress::xp,
					SkillProgress::new
			);

	public SkillProgress {
		level = Math.max(0, level);
		xp = Math.max(0, xp);
	}

	public SkillProgress withLevel(int newLevel) {
		return new SkillProgress(newLevel, xp);
	}

	public SkillProgress withXp(int newXp) {
		return new SkillProgress(level, newXp);
	}
}
