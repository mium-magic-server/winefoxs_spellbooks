package net.magicterra.winefoxsspellbooks.damage;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

/**
 * 模组自有伤害类型常量
 * <p>
 * 伤害类型本体走数据包 JSON：{@code data/winefoxs_spellbooks/damage_type/winefox_hex_magic.json}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public final class WsbDamageTypes {
    private WsbDamageTypes() {
    }

    /** 酒狐巫法学派伤害 */
    public static final ResourceKey<DamageType> WINEFOX_HEX_MAGIC = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "winefox_hex_magic"));
}
