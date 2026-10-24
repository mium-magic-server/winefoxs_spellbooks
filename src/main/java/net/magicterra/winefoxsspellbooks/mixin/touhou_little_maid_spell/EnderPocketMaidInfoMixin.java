package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.yimeng261.maidspell.item.bauble.enderPocket.EnderPocketService;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.ByteBuf;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.EnderPocketManaHolder;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 给末影腰包女仆信息加上法力字段，并让其编解码器在原有字段之后多读写两个 float
 * <p>
 * 包住 {@code <clinit>} 里构造 {@code STREAM_CODEC} 的那次 {@code StreamCodec.of}，
 * 随后据它构造的 {@code LIST_STREAM_CODEC} 以及所有携带女仆列表的包都会带上法力。
 * 两端装的是同一份本模组与万法皆通，mixin 要么两端都生效、要么都不生效，包格式始终一致。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.item.bauble.enderPocket.EnderPocketService$EnderPocketMaidInfo", remap = false)
public abstract class EnderPocketMaidInfoMixin implements EnderPocketManaHolder {
    @Unique
    private float winefoxsSpellbooks$mana;
    @Unique
    private float winefoxsSpellbooks$maxMana = NO_MANA;

    @Override
    public float winefoxsSpellbooks$getMana() {
        return winefoxsSpellbooks$mana;
    }

    @Override
    public float winefoxsSpellbooks$getMaxMana() {
        return winefoxsSpellbooks$maxMana;
    }

    @Override
    public void winefoxsSpellbooks$setMana(float mana, float maxMana) {
        this.winefoxsSpellbooks$mana = mana;
        this.winefoxsSpellbooks$maxMana = maxMana;
    }

    @ModifyExpressionValue(
        method = "<clinit>",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/codec/StreamCodec;of(Lnet/minecraft/network/codec/StreamEncoder;Lnet/minecraft/network/codec/StreamDecoder;)Lnet/minecraft/network/codec/StreamCodec;"),
        require = 0)
    private static StreamCodec<ByteBuf, EnderPocketService.EnderPocketMaidInfo> winefoxsSpellbooks$appendMana(
        StreamCodec<ByteBuf, EnderPocketService.EnderPocketMaidInfo> original) {
        return StreamCodec.of((buf, info) -> {
            original.encode(buf, info);
            EnderPocketManaHolder holder = (EnderPocketManaHolder) (Object) info;
            buf.writeFloat(holder.winefoxsSpellbooks$getMana());
            buf.writeFloat(holder.winefoxsSpellbooks$getMaxMana());
        }, buf -> {
            EnderPocketService.EnderPocketMaidInfo info = original.decode(buf);
            ((EnderPocketManaHolder) (Object) info).winefoxsSpellbooks$setMana(buf.readFloat(), buf.readFloat());
            return info;
        });
    }
}
