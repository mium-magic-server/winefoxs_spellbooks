package net.magicterra.winefoxsspellbooks.api.event;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.Map;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.api.spell.SpellFlag;
import net.neoforged.bus.api.Event;

/**
 * 注册法术类型事件
 * <p>
 * 在服务器启动时触发，允许其他模组修改女仆的法术类型集合。 事件直接暴露各类型的 Set，调用方可自由添加或移除法术。
 * <p>
 * 此事件在标签加载之后、配置文件额外法术加载之前触发。
 * <p>
 * <b>每次标签重载都会重新触发，且触发前集合会被清空重填。</b> 因此监听方必须在<b>每一次</b>
 * 事件里重新施加自己的改动，不能只在首次启动时加一遍——否则重载数据包后就丢了。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterSpellType(RegisterSpellTypeEvent event) {
 *     // 按行为类别取集合（推荐）
 *     event.spellsOf(MaidSpellAction.ATTACK).add(MySpells.FIREBALL.get());
 *     event.spellsOf(MaidSpellAction.DEFENSE).addAll(List.of(MySpells.SHIELD.get(), MySpells.BARRIER.get()));
 *
 *     // 移除不适合女仆使用的法术
 *     event.spellsOf(MaidSpellAction.ATTACK).remove(SomeSpells.DANGEROUS_SPELL.get());
 *
 *     // 打标记
 *     event.spellsOf(SpellFlag.SUMMON).add(MySpells.SUMMON_WOLF.get());
 *
 *     // 也可以通过 ResourceLocation 查找后操作
 *     SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse("irons_spellbooks:blood_slash"))
 *         .ifPresent(spell -> event.spellsOf(MaidSpellAction.NEGATIVE).remove(spell));
 * }
 * }</pre>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-19 00:42
 */
public class RegisterSpellTypeEvent extends Event {

    private final Map<MaidSpellAction, Set<AbstractSpell>> byAction;

    private final Map<SpellFlag, Set<AbstractSpell>> byFlag;

    /**
     * 构造事件
     * <p>
     * 传入的 Set 将直接被修改，由 {@code MaidSpellCatalog} 提供
     *
     * @param byAction 行为类别 → 法术集合
     * @param byFlag   标记 → 法术集合
     */
    public RegisterSpellTypeEvent(Map<MaidSpellAction, Set<AbstractSpell>> byAction,
                                  Map<SpellFlag, Set<AbstractSpell>> byFlag) {
        this.byAction = byAction;
        this.byFlag = byFlag;
    }

    /**
     * 取某个行为类别的法术集合，可直接增删。
     *
     * @param action 行为类别
     * @return 该类别的法术集合（活引用）
     */
    public Set<AbstractSpell> spellsOf(MaidSpellAction action) {
        return byAction.get(action);
    }

    /**
     * 取某个标记的法术集合，可直接增删。
     *
     * @param flag 标记
     * @return 该标记的法术集合（活引用）
     */
    public Set<AbstractSpell> spellsOf(SpellFlag flag) {
        return byFlag.get(flag);
    }

    /**
     * 攻击法术集合，可直接修改。
     */
    public Set<AbstractSpell> getAttackSpells() {
        return spellsOf(MaidSpellAction.ATTACK);
    }

    /**
     * 防御法术集合，可直接修改。
     */
    public Set<AbstractSpell> getDefenseSpells() {
        return spellsOf(MaidSpellAction.DEFENSE);
    }

    /**
     * 移动法术集合，可直接修改。
     */
    public Set<AbstractSpell> getMovementSpells() {
        return spellsOf(MaidSpellAction.MOVEMENT);
    }

    /**
     * 支援法术集合，可直接修改。
     */
    public Set<AbstractSpell> getSupportSpells() {
        return spellsOf(MaidSpellAction.SUPPORT);
    }

    /**
     * 正面效果法术集合，可直接修改。
     */
    public Set<AbstractSpell> getPositiveEffectSpells() {
        return spellsOf(MaidSpellAction.POSITIVE);
    }

    /**
     * 支援效果法术集合，可直接修改。
     */
    public Set<AbstractSpell> getSupportEffectSpells() {
        return spellsOf(MaidSpellAction.SUPPORT_OTHER);
    }

    /**
     * 负面效果法术集合，可直接修改。
     */
    public Set<AbstractSpell> getNegativeEffectSpells() {
        return spellsOf(MaidSpellAction.NEGATIVE);
    }

    /**
     * 召唤法术集合，可直接修改。
     */
    public Set<AbstractSpell> getSummonSpells() {
        return spellsOf(SpellFlag.SUMMON);
    }

    /**
     * 需要二重咏唱的法术集合，可直接修改。
     */
    public Set<AbstractSpell> getRecastSpells() {
        return spellsOf(SpellFlag.SHOULD_RECAST);
    }
}
