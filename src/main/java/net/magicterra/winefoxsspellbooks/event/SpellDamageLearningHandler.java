package net.magicterra.winefoxsspellbooks.event;

import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellMemory;
import net.magicterra.winefoxsspellbooks.magic.StatusDamageTracker;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 把女仆法术打出的伤害归到对应的施法上，供 {@link MaidSpellMemory} 学习法术的实战伤害。
 * <p>
 * 本类只负责找出伤害属于哪只女仆（攻击者本人，或召唤物的召唤者），具体归到哪次施法由 {@link MaidSpellMemory#onDamage} 判断：先匹配施法期间生成的载体，再匹配当前 onCast， 没有独立载体且候选唯一的 {@link SpellDamageSource} 才按法术兜底。 载体（召唤物、弹射物）由 {@link #beginCastScope} 圈定的范围内 {@link #onEntityJoin} 登记。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-06
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class SpellDamageLearningHandler {
    /**
     * 正在执行 {@code onCast} 的那次被观测施法。期间生成的实体都记作这次施法的伤害载体。 按生成时机认载体，而不是按实体年龄推算：{@code tickCount} 不落盘，区块重载后会归零； 而且同一时段女仆射出的箭、别的法术的弹射物也会被年龄判断误认进来。
     * <p>
     * 同一线程内的嵌套施法会恢复上一层；集成服务器的客户端 onCast 不会清空服务端的范围。
     */
    private static final ThreadLocal<MaidSpellMemory.CastRecord> ACTIVE_CAST = new ThreadLocal<>();

    /**
     * 被观测的施法即将执行 {@code onCast} 时调用，必须与 {@link #endCastScope} 成对（放在 finally 里）。
     */
    @Nullable
    public static MaidSpellMemory.CastRecord beginCastScope(@Nullable MaidSpellMemory.CastRecord record) {
        MaidSpellMemory.CastRecord previous = ACTIVE_CAST.get();
        ACTIVE_CAST.set(record);
        return previous;
    }

    public static void endCastScope(@Nullable MaidSpellMemory.CastRecord previous) {
        if (previous == null) {
            ACTIVE_CAST.remove();
        } else {
            ACTIVE_CAST.set(previous);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        MaidSpellMemory.CastRecord activeCast = ACTIVE_CAST.get();
        if (activeCast != null && !event.getLevel().isClientSide && !event.loadedFromDisk()) {
            activeCast.addCarrier(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        // 全局伤害事件，绝大多数伤害的攻击者既不是女仆也不是召唤物，前两步就该出去
        float amount = event.getNewDamage();
        if (amount <= 0) return;
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        long gameTime = victim.level().getGameTime();
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) {
            // 燃烧、冰冻、中毒、凋零不带攻击者，按状态的施加者归属
            StatusDamageTracker.onUnattributedDamage(victim, event.getSource(), amount, gameTime);
            return;
        }

        // 召唤女仆本身也是会施法的女仆：先算作她自己的施法，没对上再当作召唤物归给她的召唤者
        if (attacker instanceof MaidMagicEntity && attacker instanceof LivingEntity caster
            && attribute(caster, event, gameTime)) {
            return;
        }
        if (attacker instanceof IMagicSummon summon
            && summon.getSummoner() instanceof LivingEntity summoner && summoner instanceof MaidMagicEntity) {
            attribute(summoner, event, gameTime);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        StatusDamageTracker.tick();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        StatusDamageTracker.clear();
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEntity().level().isClientSide) {
            StatusDamageTracker.onEffectAdded(event.getEntity(), event.getEffectInstance().getEffect(), event.getEffectSource());
        }
    }

    private static boolean attribute(LivingEntity owner, LivingDamageEvent.Post event, long gameTime) {
        MaidSpellMemory memory = owner.getExistingDataOrNull(WsbAttachments.MAID_SPELL_MEMORY);
        return memory != null
            && memory.onDamage(event.getSource(), event.getEntity(), event.getNewDamage(), owner, gameTime,
            ACTIVE_CAST.get());
    }
}
