package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.yimeng261.maidspell.compat.MaidSpellAllyResolver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidAllyHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 万法皆通「破法回响」（{@code spellbreaking_echo}）的护盾会清除范围内所有「所有者不是施法者本人」的弹射物、铁魔法召唤物与法术实体，
 * 判定只比对 {@code getOwner() != caster}，不认主人：女仆施放时，主人的箭、主人的召唤物、姐妹女仆及其召唤物的法术同样被一并清除。
 * <p>
 * 这里在其 {@code isSpellTarget} 判定为「应清除」之后追加豁免：凡与施法者属同一召唤链（顶层主人相同），
 * 或经万法皆通自带的 {@link MaidSpellAllyResolver#areFriendly} 判为友方的，一律放行。
 * 两套判据取并集：{@code MaidAllyHelper} 认本模组的召唤女仆与召唤链缓存，{@code MaidSpellAllyResolver} 认第三方模组的各类所有者接口。
 * <p>
 * 藤蔓（{@link RootEntity}）不在豁免之列：原逻辑把它们无条件清除，用意是解除施法者自身的束缚，与所有者无关。
 * <p>
 * 本补丁待上游修复后移除，见 {@code docs/tentative/tlm-spell-unified-experience.md} 的「长期 PR」一节。
 * 仅在万法皆通已加载时应用（{@code WsbMixinPlugin} 按包名 {@code touhou_little_maid_spell} 判定），并以 {@code require = 0} 兜底其内部改名。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.compat.irons_spellbooks.entity.spell.SpellbreakingEchoEntity", remap = false)
public abstract class SpellbreakingEchoEntityMixin {
    @ModifyReturnValue(method = "isSpellTarget(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At("RETURN"),
        require = 0)
    private static boolean winefoxsSpellbooks$spareFriendlyEntities(boolean original, Entity entity, Entity caster) {
        if (!original || entity instanceof RootEntity) {
            return original;
        }
        // 弹射物本身不在召唤链里，要先取到其射出者再比对
        Entity responsible = entity instanceof Projectile projectile && projectile.getOwner() != null ? projectile.getOwner() : entity;
        boolean friendly = MaidAllyHelper.isAllied(responsible, caster)
            || MaidSpellAllyResolver.areFriendly(entity, caster);
        return !friendly;
    }
}
