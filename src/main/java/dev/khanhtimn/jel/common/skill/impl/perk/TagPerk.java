package dev.khanhtimn.jel.common.skill.impl.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

/**
 * Grants a vanilla player tag when the skill reaches the unlock level.
 * <p>
 * Uses vanilla's {@code player.addTag()} / {@code player.removeTag()}, which
 * means datapacks can check tags via {@code @a[tag=jel.self_heal]}.
 * <p>
 * Also mirrors the tag to JEL's internal ability flag set so mod code can check
 * via {@code tracker.hasAbilityFlag()}.
 *
 * <h2>JSON</h2>
 * <pre>{@code
 * { "type": "jel:tag", "tag": "jel.self_heal", "unlock_level": 10 }
 * }</pre>
 */
public record TagPerk(
        int unlockLevel,
        String tag
        ) implements Perk {

    public static final MapCodec<TagPerk> CODEC
            = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("unlock_level")
                    .forGetter(TagPerk::unlockLevel),
            Codec.STRING.fieldOf("tag")
                    .forGetter(TagPerk::tag)
    ).apply(instance, TagPerk::new));

    @Override
    public PerkType<?> type() {
        return PerkType.TAG;
    }

    @Override
    public void apply(PerkContext ctx, int currentLevel) {
        ctx.player().addTag(tag);
        ctx.tracker().grantAbilityFlag(ResourceLocation.parse(tag.replace('.', ':')));
    }

    @Override
    public void revoke(PerkContext ctx) {
        ctx.player().removeTag(tag);
        ctx.tracker().removeAbilityFlag(ResourceLocation.parse(tag.replace('.', ':')));
    }
}
