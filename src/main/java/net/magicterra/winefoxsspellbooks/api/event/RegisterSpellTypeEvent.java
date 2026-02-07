package net.magicterra.winefoxsspellbooks.api.event;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.Set;
import net.neoforged.bus.api.Event;

/**
 * 注册法术类型事件
 * <p>
 * 在服务器启动时触发，允许其他模组修改女仆的法术类型集合。
 * 事件直接暴露各类型的 Set，调用方可自由添加或移除法术。
 * <p>
 * 此事件在标签加载之后、配置文件额外法术加载之前触发。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterSpellType(RegisterSpellTypeEvent event) {
 *     // 添加法术
 *     event.getAttackSpells().add(MySpells.FIREBALL.get());
 *     event.getDefenseSpells().addAll(List.of(MySpells.SHIELD.get(), MySpells.BARRIER.get()));
 *
 *     // 移除不适合女仆使用的法术
 *     event.getAttackSpells().remove(SomeSpells.DANGEROUS_SPELL.get());
 *
 *     // 也可以通过 ResourceLocation 查找后操作
 *     SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse("irons_spellbooks:blood_slash"))
 *         .ifPresent(spell -> event.getNegativeEffectSpells().remove(spell));
 * }
 * }</pre>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-19 00:42
 */
public class RegisterSpellTypeEvent extends Event {

    private final Set<AbstractSpell> attackSpells;
    private final Set<AbstractSpell> defenseSpells;
    private final Set<AbstractSpell> movementSpells;
    private final Set<AbstractSpell> supportSpells;
    private final Set<AbstractSpell> positiveEffectSpells;
    private final Set<AbstractSpell> supportEffectSpells;
    private final Set<AbstractSpell> negativeEffectSpells;
    private final Set<AbstractSpell> summonSpells;
    private final Set<AbstractSpell> recastSpells;

    /**
     * 构造事件
     * <p>
     * 传入的 Set 将直接被修改，由 MaidSpellRegistry 提供
     *
     * @param attackSpells         攻击法术集合
     * @param defenseSpells        防御法术集合
     * @param movementSpells       移动法术集合
     * @param supportSpells        支援法术集合（自我治疗）
     * @param positiveEffectSpells 正面效果法术集合
     * @param supportEffectSpells  支援效果法术集合
     * @param negativeEffectSpells 负面效果法术集合
     * @param summonSpells         召唤法术集合
     * @param recastSpells         需要二重咏唱的法术集合
     */
    public RegisterSpellTypeEvent(
            Set<AbstractSpell> attackSpells,
            Set<AbstractSpell> defenseSpells,
            Set<AbstractSpell> movementSpells,
            Set<AbstractSpell> supportSpells,
            Set<AbstractSpell> positiveEffectSpells,
            Set<AbstractSpell> supportEffectSpells,
            Set<AbstractSpell> negativeEffectSpells,
            Set<AbstractSpell> summonSpells,
            Set<AbstractSpell> recastSpells) {
        this.attackSpells = attackSpells;
        this.defenseSpells = defenseSpells;
        this.movementSpells = movementSpells;
        this.supportSpells = supportSpells;
        this.positiveEffectSpells = positiveEffectSpells;
        this.supportEffectSpells = supportEffectSpells;
        this.negativeEffectSpells = negativeEffectSpells;
        this.summonSpells = summonSpells;
        this.recastSpells = recastSpells;
    }

    /**
     * 获取攻击法术集合
     * <p>
     * 攻击法术在法术攻击模式下对敌人施放
     *
     * @return 攻击法术集合，可直接修改
     */
    public Set<AbstractSpell> getAttackSpells() {
        return attackSpells;
    }

    /**
     * 获取防御法术集合
     * <p>
     * 自我强化法术，所有模式下对自身施放
     *
     * @return 防御法术集合，可直接修改
     */
    public Set<AbstractSpell> getDefenseSpells() {
        return defenseSpells;
    }

    /**
     * 获取移动法术集合
     * <p>
     * 移动法术在法术攻击模式下用于移动到目标位置
     *
     * @return 移动法术集合，可直接修改
     */
    public Set<AbstractSpell> getMovementSpells() {
        return movementSpells;
    }

    /**
     * 获取支援法术集合
     * <p>
     * 治疗法术，所有模式下对自身施放
     *
     * @return 支援法术集合，可直接修改
     */
    public Set<AbstractSpell> getSupportSpells() {
        return supportSpells;
    }

    /**
     * 获取正面效果法术集合
     * <p>
     * 正面效果法术在法术支援模式下对队友施放
     *
     * @return 正面效果法术集合，可直接修改
     */
    public Set<AbstractSpell> getPositiveEffectSpells() {
        return positiveEffectSpells;
    }

    /**
     * 获取支援效果法术集合
     * <p>
     * 治疗效果法术在法术支援模式下对队友施放
     *
     * @return 支援效果法术集合，可直接修改
     */
    public Set<AbstractSpell> getSupportEffectSpells() {
        return supportEffectSpells;
    }

    /**
     * 获取负面效果法术集合
     * <p>
     * 负面效果法术在所有模式下对敌人施放
     *
     * @return 负面效果法术集合，可直接修改
     */
    public Set<AbstractSpell> getNegativeEffectSpells() {
        return negativeEffectSpells;
    }

    /**
     * 获取召唤法术集合
     * <p>
     * 召唤法术需要标记以避免重新咏唱
     *
     * @return 召唤法术集合，可直接修改
     */
    public Set<AbstractSpell> getSummonSpells() {
        return summonSpells;
    }

    /**
     * 获取需要二重咏唱的法术集合
     * <p>
     * 例如炽焰追踪弹幕等需要二重咏唱的法术
     *
     * @return 需要二重咏唱的法术集合，可直接修改
     */
    public Set<AbstractSpell> getRecastSpells() {
        return recastSpells;
    }
}
