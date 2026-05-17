package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.magicterra.winefoxsspellbooks.registry.WsbSpells;

/**
 * 本 mod 自带法术的标签贡献。
 * 注意：本 mod 的法术已经在自己的 registry 里，不需要 {@code addOptional} 防御，可以直接 {@code add}。
 */
public final class WinefoxsSpellbooksTags {

    private WinefoxsSpellbooksTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.summon().add(WsbSpells.SUMMON_MAID_SPELL.get());
        ctx.positiveEffect().add(WsbSpells.MANA_TRANSFER_SPELL.get());
    }
}
