package net.magicterra.winefoxsspellbooks.event;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.MaidAllyHelper;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * 传奇法师（Legendary Mage）死亡爆炸的友军保护。
 * <p>
 * 传奇法师有两处在实体死亡时调用无来源的 {@code explode(null, ...)}：火焰标记死亡爆炸与「狂焰」效果死亡爆炸。
 * 爆炸没有来源实体，范围内所有人一并受伤，包括击杀者的主人、女仆与宠物。
 * {@code FireMarkDeathExplosionMixin} 与 {@code PyroFlameDeathExplosionMixin} 在调用前后登记这次爆炸的「责任方」，
 * 本类在同一线程、同一次 {@code explode} 调用内触发的 {@link ExplosionEvent.Detonate} 中，
 * 把与任一责任方同一召唤链（顶层主人相同）的实体从受影响列表移除。
 * <p>
 * 责任方可有多个：致死伤害的来源、死者的 {@code getKillCredit()}、给死者挂上标记的施法者。
 * 只认击杀者时，死者靠火焰持续伤害死去、最后一次受击又已超过 5 秒，击杀者就会缺失，爆炸照常误伤；
 * 标记施法者由传奇法师写入死者的持久化数据，不随击杀记录过期，可补上这一缺口。
 * <p>
 * 以 {@link ThreadLocal} 传递：事件在 {@code explode} 内同步派发，无需跨 tick 保存；登记方负责在 {@code finally} 中清除。
 * 传奇法师未加载时从不登记，监听器直接返回，没有额外开销。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class LegendaryMageExplosionGuard {
    private static final ThreadLocal<Registration> CURRENT = new ThreadLocal<>();

    private record Registration(String label, List<Entity> parties) {
    }

    /**
     * 登记当前线程正在进行的死亡爆炸的责任方。
     *
     * @param label   日志中的爆炸名称
     * @param parties 责任方，可含空值与重复项（环境伤害等无来源的死亡），全部为空时不做任何豁免
     */
    public static void enter(String label, @Nullable Entity... parties) {
        List<Entity> list = Arrays.stream(parties).filter(Objects::nonNull).distinct().toList();
        CURRENT.set(new Registration(label, list));
    }

    /**
     * 清除登记。必须在 {@code finally} 中调用。
     */
    public static void exit() {
        CURRENT.remove();
    }

    /**
     * 过滤受影响实体。优先级取最低，晚于其他模组的修改，仅做移除。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDetonate(ExplosionEvent.Detonate event) {
        Registration registration = CURRENT.get();
        if (registration == null || registration.parties().isEmpty()) {
            return;
        }
        List<Entity> parties = registration.parties();
        int before = event.getAffectedEntities().size();
        event.getAffectedEntities().removeIf(entity -> parties.stream().anyMatch(party -> MaidAllyHelper.isAllied(entity, party)));
        int spared = before - event.getAffectedEntities().size();
        if (WinefoxsSpellbooks.DEBUG && spared > 0) {
            String names = parties.stream().map(party -> party.getName().getString()).collect(Collectors.joining("、"));
            WinefoxsSpellbooks.LOGGER.info("传奇法师{}：已为 {} 一方豁免 {} 个友方实体", registration.label(), names, spared);
        }
    }
}
