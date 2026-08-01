package net.magicterra.winefoxsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import java.util.Collection;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.LittleMaidSpellbooksCompat;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidLoadoutManager;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.BroomMode;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedMaidBroom;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * wsb 指令
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 01:03
 */
public class WsbCommand {
    /**
     * 默认召唤持续时间（秒）
     */
    private static final int DEFAULT_SUMMON_DURATION = 600;

    /**
     * 永久召唤的目标过期 tick。Iron's {@code SummonManager} 全程用 int tick 运算且无钳制，两处会溢出：
     * <ul>
     *   <li>召唤时 {@code expirationServerTick = getTickCount() + duration}——直接传
     *       {@code Integer.MAX_VALUE} 当时长立即溢出为负，despawn 判定 {@code expirationServerTick < tick}
     *       瞬间成立，召唤物秒消；</li>
     *   <li>主人登出时保存 {@code remaining = expirationTick - 当前 tick}，重新登入按
     *       {@code 当前 tick + remaining} 还原——若过期 tick 定在 {@code MAX_VALUE}，同一服务器会话内
     *       重登必然再次溢出，「永久」召唤在重登后约 1 秒被解除。</li>
     * </ul>
     * 取 {@code MAX_VALUE / 2}（约 1.7 年连续运行）：只要服务器单次运行不超此数，
     * 「召唤时长 = 目标 tick - 当前 tick」与「重登还原 = 当前 tick + remaining」均不会越过 int 上限。
     */
    private static final int PERMANENT_EXPIRATION_TICK = Integer.MAX_VALUE / 2;

    /**
     * 法术 ID 自动补全提供器
     */
    private static final SuggestionProvider<CommandSourceStack> SPELL_SUGGESTIONS =
        (context, builder) -> SharedSuggestionProvider.suggest(LittleMaidSpellbooksCompat.getAllSpellIds(), builder);

    /**
     * Loadout ID 自动补全提供器
     */
    private static final SuggestionProvider<CommandSourceStack> LOADOUT_SUGGESTIONS =
        (context, builder) -> SharedSuggestionProvider.suggestResource(
            MaidLoadoutManager.getInstance().getAllLoadouts().stream()
                .map(MaidLoadout::id)
                .toList(),
            builder
        );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wsb")
            .requires(source -> source.hasPermission(2))
            // ============ cast 子命令 ============
            .then(Commands.literal("cast")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("spell", ResourceLocationArgument.id())
                        .suggests(SPELL_SUGGESTIONS)
                        // 无等级参数，默认等级为 1
                        .executes(context -> executeSpell(context, 1))
                        // 带等级参数
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                            .executes(context -> executeSpell(context, IntegerArgumentType.getInteger(context, "level")))
                        )
                    )
                ))
            // ============ summon 子命令 ============
            .then(Commands.literal("summon")
                .requires(source -> source.hasPermission(2))
                // 无参数：玩家执行时召唤给自己
                .executes(WsbCommand::executeSummonForSelf)
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> executeSummon(context,
                        EntityArgument.getPlayer(context, "target"),
                        1, 1, null, DEFAULT_SUMMON_DURATION, null))
                    .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                        .executes(context -> executeSummon(context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "count"),
                            1, null, DEFAULT_SUMMON_DURATION, null))
                        .then(Commands.argument("summonLevel", IntegerArgumentType.integer(1, 10))
                            .executes(context -> executeSummon(context,
                                EntityArgument.getPlayer(context, "target"),
                                IntegerArgumentType.getInteger(context, "count"),
                                IntegerArgumentType.getInteger(context, "summonLevel"),
                                null, DEFAULT_SUMMON_DURATION, null))
                            .then(Commands.argument("loadout", ResourceLocationArgument.id())
                                .suggests(LOADOUT_SUGGESTIONS)
                                .executes(context -> executeSummon(context,
                                    EntityArgument.getPlayer(context, "target"),
                                    IntegerArgumentType.getInteger(context, "count"),
                                    IntegerArgumentType.getInteger(context, "summonLevel"),
                                    ResourceLocationArgument.getId(context, "loadout"),
                                    DEFAULT_SUMMON_DURATION, null))
                                .then(Commands.argument("duration", IntegerArgumentType.integer(-1))
                                    .executes(context -> executeSummon(context,
                                        EntityArgument.getPlayer(context, "target"),
                                        IntegerArgumentType.getInteger(context, "count"),
                                        IntegerArgumentType.getInteger(context, "summonLevel"),
                                        ResourceLocationArgument.getId(context, "loadout"),
                                        IntegerArgumentType.getInteger(context, "duration"), null))
                                    .then(Commands.argument("airForce", BoolArgumentType.bool())
                                        .executes(context -> executeSummon(context,
                                            EntityArgument.getPlayer(context, "target"),
                                            IntegerArgumentType.getInteger(context, "count"),
                                            IntegerArgumentType.getInteger(context, "summonLevel"),
                                            ResourceLocationArgument.getId(context, "loadout"),
                                            IntegerArgumentType.getInteger(context, "duration"),
                                            BoolArgumentType.getBool(context, "airForce")))
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * 玩家执行时召唤给自己
     */
    private static int executeSummonForSelf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        // 检查是否由玩家执行
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("commands.winefoxs_spellbooks.wsb.summon.player_only"));
            return 0;
        }

        Player player = source.getPlayerOrException();
        return executeSummon(context, player, 1, 1, null, DEFAULT_SUMMON_DURATION, null);
    }

    /**
     * 执行召唤女仆命令
     *
     * @param context       命令上下文
     * @param owner         女仆的 Owner 玩家
     * @param count         召唤数量
     * @param summonLevel   召唤等级
     * @param loadoutId     装备配置 ID（null 表示随机选择）
     * @param duration      持续时间（秒），-1 表示永久，0 视为非法
     * @param forceAirForce 强制飞行模式（null 表示按配置决定）
     * @return 成功召唤的数量
     */
    private static int executeSummon(
        CommandContext<CommandSourceStack> context,
        Player owner,
        int count,
        int summonLevel,
        @Nullable ResourceLocation loadoutId,
        int duration,
        @Nullable Boolean forceAirForce
    ) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        MaidLoadout loadout = null;
        if (loadoutId != null) {
            loadout = MaidLoadoutManager.getInstance().getLoadout(loadoutId).orElse(null);
            if (loadout == null) {
                source.sendFailure(Component.translatable("commands.winefoxs_spellbooks.wsb.summon.loadout_not_found", loadoutId.toString()));
                return 0;
            }
        }

        // 0 秒不是「立刻解除」而是操作失误：expirationServerTick 会被设成当前 tick，下一 tick 的 despawn
        // 判定立刻成立，玩家只看到一句成功提示，女仆在看清之前就没了。brigadier 的 integer(-1) 挡不住它
        if (duration == 0) {
            source.sendFailure(Component.translatable("commands.winefoxs_spellbooks.wsb.summon.duration_zero"));
            return 0;
        }

        // -1=永久：持续 = 目标过期 tick - 当前 tick（溢出考量见常量注释）。
        // 秒数无上界，先按 long 换算再夹到永久上限，否则 duration*20 溢出成负数会让女仆一出生就被解除
        int permanentTicks = PERMANENT_EXPIRATION_TICK - level.getServer().getTickCount();
        long requestedTicks = duration == -1 ? permanentTicks : duration * 20L;
        int ticks = (int) Math.min(requestedTicks, permanentTicks);

        float defaultAirForceChance = (float) (Config.getAirForceBaseChance() + Config.getAirForceChancePerLevel() * summonLevel);
        defaultAirForceChance = Mth.clamp(defaultAirForceChance, 0.0f, 1.0f);

        SummonedEntitiesCastData castData = new SummonedEntitiesCastData();
        int successCount = 0;
        int airForceIndex = 0;
        float radius = 1.5f + 0.185f * count;

        for (int i = 0; i < count; i++) {
            try {
                SummonedEntityMaid maid = new SummonedEntityMaid(SummonedEntityMaid.TYPE, level);
                maid.setSummonLevel(summonLevel);

                MaidLoadout selectedLoadout = loadout != null
                                              ? loadout
                                              : MaidLoadoutManager.getInstance().selectLoadout(level.random);
                if (selectedLoadout != null) {
                    maid.setPreSelectedLoadout(selectedLoadout);
                }

                boolean isAirForce = determineAirForce(selectedLoadout, forceAirForce, defaultAirForceChance, level);
                if (isAirForce) {
                    maid.setAirForce(true);
                    maid.setSummonIndex(airForceIndex++);
                }

                // 围绕玩家圆形分布
                float yrot = (float) (2 * Math.PI) / count * i + owner.getYRot() * ((float) Math.PI / 180);
                Vec3 spawn = Utils.moveToRelativeGroundLevel(
                    level,
                    owner.getEyePosition().add(new Vec3(
                        radius * Mth.cos(yrot),
                        0.0,
                        radius * Mth.sin(yrot)
                    )),
                    10
                );

                maid.setPos(spawn.x, spawn.y, spawn.z);
                maid.setYRot(owner.getYRot());

                maid.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(maid.blockPosition()),
                    MobSpawnType.COMMAND,
                    null
                );
                maid.setOldPosAndRot();

                level.addFreshEntity(maid);

                if (maid.isAirForce()) {
                    SummonedMaidBroom broom = SummonedMaidBroom.createForMaid(level, maid, owner);
                    level.addFreshEntity(broom);
                    maid.startRiding(broom, true);
                    SummonManager.initSummon(owner, broom, ticks, castData);
                }

                SummonManager.initSummon(owner, maid, ticks, castData);

                successCount++;
                WinefoxsSpellbooks.LOGGER.debug("Summoned maid: uuid={}, owner={}, level={}, airForce={}",
                    maid.getUUID(), owner.getName().getString(), summonLevel, isAirForce);

            } catch (Exception e) {
                WinefoxsSpellbooks.LOGGER.error("Failed to summon maid: {}", e.getMessage(), e);
            }
        }

        if (successCount > 0) {
            final int success = successCount;
            final Component loadoutText = loadoutId != null
                                          ? Component.literal(loadoutId.toString())
                                          : Component.translatable("commands.winefoxs_spellbooks.wsb.summon.loadout_random");
            final Component durationText = duration == -1
                                           ? Component.translatable("commands.winefoxs_spellbooks.wsb.summon.duration_permanent")
                                           : Component.translatable("commands.winefoxs_spellbooks.wsb.summon.duration_seconds", duration);
            source.sendSuccess(() -> Component.translatable(
                "commands.winefoxs_spellbooks.wsb.summon.success",
                success,
                summonLevel,
                loadoutText,
                durationText
            ), true);
        }

        if (successCount < count) {
            source.sendFailure(Component.translatable(
                "commands.winefoxs_spellbooks.wsb.summon.failed",
                count - successCount
            ));
        }

        return successCount;
    }

    /**
     * 根据配置和参数决定女仆是否飞行
     *
     * @param loadout       装备配置（可能为 null）
     * @param forceAirForce 强制飞行模式（null 表示按配置决定）
     * @param defaultChance 默认飞行概率
     * @param level         世界实例
     * @return 是否飞行
     */
    private static boolean determineAirForce(
        @Nullable MaidLoadout loadout,
        @Nullable Boolean forceAirForce,
        float defaultChance,
        ServerLevel level
    ) {
        // 如果命令明确指定了飞行模式
        if (forceAirForce != null) {
            return forceAirForce;
        }

        // 否则按配置决定
        if (loadout == null) {
            return level.random.nextFloat() < defaultChance;
        }

        BroomMode broomMode = loadout.broomMode();
        return switch (broomMode) {
            case ALWAYS -> true;
            case NEVER -> false;
            case DEFAULT -> level.random.nextFloat() < defaultChance;
        };
    }

    /**
     * 执行施法指令
     */
    private static int executeSpell(CommandContext<CommandSourceStack> context, int level) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        ResourceLocation spellId = ResourceLocationArgument.getId(context, "spell");

        // 获取法术
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null || spell == SpellRegistry.none()) {
            context.getSource().sendFailure(Component.translatable("commands.winefoxs_spellbooks.wsb.cast.spell_not_found", spellId.toString()));
            return 0;
        }

        int successCount = 0;
        int failCount = 0;

        for (Entity entity : targets) {
            if (!(entity instanceof IMagicEntity magicEntity)) {
                failCount++;
                continue;
            }

            MagicMaidAdapter adapter = entity instanceof MaidMagicEntity maid
                                       ? maid.winefoxsSpellbooks$getMagicMaidAdapter() : null;
            if (adapter != null) {
                adapter.setBypassCastCheck(true);
            }
            try {
                magicEntity.initiateCastSpell(spell, level);
                successCount++;
                WinefoxsSpellbooks.LOGGER.debug("Entity {} cast spell {} (level {})", entity.getUUID(), spellId, level);
            } catch (Exception e) {
                WinefoxsSpellbooks.LOGGER.error("Entity {} failed to cast spell {}: {}", entity.getUUID(), spellId, e.getMessage());
                failCount++;
            } finally {
                // 复位必须走 finally：施法抛异常时标志会永久停在 true，而适配器用它整体短路
                // 蓝量/冷却/前置检查，一次失败的指令就把这只女仆变成永久无消耗的施法者
                if (adapter != null) {
                    adapter.setBypassCastCheck(false);
                }
            }
        }

        // 发送结果消息
        if (successCount > 0) {
            final int success = successCount;
            context.getSource().sendSuccess(() -> Component.translatable(
                "commands.winefoxs_spellbooks.wsb.cast.success",
                success,
                spellId.toString(),
                level
            ), true);
        }

        if (failCount > 0) {
            context.getSource().sendFailure(Component.translatable(
                "commands.winefoxs_spellbooks.wsb.cast.failed",
                failCount
            ));
        }

        return successCount;
    }
}
