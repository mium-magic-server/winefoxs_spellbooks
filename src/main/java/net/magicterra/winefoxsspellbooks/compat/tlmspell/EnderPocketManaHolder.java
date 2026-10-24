package net.magicterra.winefoxsspellbooks.compat.tlmspell;

/**
 * 由 mixin 挂到万法皆通 {@code EnderPocketMaidInfo} 上的法力字段
 * <p>
 * 服务端生成女仆列表时填入，随原有的 HUD / 列表包一并编解码，不另发网络包。
 * 上限为负表示该女仆没有法力可显示（未执行施法任务）。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public interface EnderPocketManaHolder {
    float NO_MANA = -1.0F;

    float winefoxsSpellbooks$getMana();

    float winefoxsSpellbooks$getMaxMana();

    void winefoxsSpellbooks$setMana(float mana, float maxMana);

    default boolean winefoxsSpellbooks$hasMana() {
        return winefoxsSpellbooks$getMaxMana() >= 0.0F;
    }
}
