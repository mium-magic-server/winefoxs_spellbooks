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
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidLoadoutManager;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.BroomMode;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
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
import net.neoforged.neoforge.server.permission.PermissionAPI;

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
     * 永久召唤的持续时间（tick）- 约 68 年
     */
    private static final int PERMANENT_DURATION_TICKS = Integer.MAX_VALUE;

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
        PermissionAPI.initializePermissionAPI();
        dispatcher.register(Commands.literal("wsb")
            // ============ cast 子命令 ============
            .then(Commands.literal("cast")
                .requires(source -> source.hasPermission(2)) // 需要 OP 权限
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
                .requires(source -> source.hasPermission(2)) // 需要 OP 权限
                // 无参数：玩家执行时召唤给自己
                .executes(WsbCommand::executeSummonForSelf)
                // 指定目标玩家
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> executeSummon(context,
                        EntityArgument.getPlayer(context, "target"),
                        1, 1, null, DEFAULT_SUMMON_DURATION, null))
                    // 指定数量
                    .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                        .executes(context -> executeSummon(context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "count"),
                            1, null, DEFAULT_SUMMON_DURATION, null))
                        // 指定召唤等级
                        .then(Commands.argument("summonLevel", IntegerArgumentType.integer(1, 10))
                            .executes(context -> executeSummon(context,
                                EntityArgument.getPlayer(context, "target"),
                                IntegerArgumentType.getInteger(context, "count"),
                                IntegerArgumentType.getInteger(context, "summonLevel"),
                                null, DEFAULT_SUMMON_DURATION, null))
                            // 指定 loadout
                            .then(Commands.argument("loadout", ResourceLocationArgument.id())
                                .suggests(LOADOUT_SUGGESTIONS)
                                .executes(context -> executeSummon(context,
                                    EntityArgument.getPlayer(context, "target"),
                                    IntegerArgumentType.getInteger(context, "count"),
                                    IntegerArgumentType.getInteger(context, "summonLevel"),
                                    ResourceLocationArgument.getId(context, "loadout"),
                                    DEFAULT_SUMMON_DURATION, null))
                                // 指定持续时间
                                .then(Commands.argument("duration", IntegerArgumentType.integer(-1))
                                    .executes(context -> executeSummon(context,
                                        EntityArgument.getPlayer(context, "target"),
                                        IntegerArgumentType.getInteger(context, "count"),
                                        IntegerArgumentType.getInteger(context, "summonLevel"),
                                        ResourceLocationArgument.getId(context, "loadout"),
                                        IntegerArgumentType.getInteger(context, "duration"), null))
                                    // 指定飞行模式
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
     * @param duration      持续时间（秒），-1 表示永久
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

        // 1. 获取装备配置
        MaidLoadout loadout = null;
        if (loadoutId != null) {
            loadout = MaidLoadoutManager.getInstance().getLoadout(loadoutId).orElse(null);
            if (loadout == null) {
                source.sendFailure(Component.translatable("commands.winefoxs_spellbooks.wsb.summon.loadout_not_found", loadoutId.toString()));
                return 0;
            }
        }

        // 2. 计算持续时间（tick）
        int ticks = duration == -1
            ? PERMANENT_DURATION_TICKS
            : duration * 20;

        // 3. 计算默认飞行概率
        float defaultAirForceChance = (float) (Config.getAirForceBaseChance() + Config.getAirForceChancePerLevel() * summonLevel);
        defaultAirForceChance = Mth.clamp(defaultAirForceChance, 0.0f, 1.0f);

        SummonedEntitiesCastData castData = new SummonedEntitiesCastData();
        int successCount = 0;
        int airForceIndex = 0;
        float radius = 1.5f + 0.185f * count;

        for (int i = 0; i < count; i++) {
            try {
                // 4. 创建女仆实体
                SummonedEntityMaid maid = new SummonedEntityMaid(SummonedEntityMaid.TYPE, level);
                maid.setSummonLevel(summonLevel);

                // 5. 设置装备配置
                MaidLoadout selectedLoadout = loadout != null
                    ? loadout
                    : MaidLoadoutManager.getInstance().selectLoadout(level.random);
                if (selectedLoadout != null) {
                    maid.setPreSelectedLoadout(selectedLoadout);
                }

                // 6. 决定飞行模式
                boolean isAirForce = determineAirForce(selectedLoadout, forceAirForce, defaultAirForceChance, level);
                if (isAirForce) {
                    maid.setAirForce(true);
                    maid.setSummonIndex(airForceIndex++);
                }

                // 7. 计算生成位置（围绕玩家圆形分布）
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

                // 8. 触发 finalizeSpawn（应用装备和法术）
                maid.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(maid.blockPosition()),
                    MobSpawnType.COMMAND,
                    null
                );
                maid.setOldPosAndRot();

                // 9. 添加到世界
                level.addFreshEntity(maid);

                // 10. 飞行处理：创建扫帚
                if (maid.isAirForce()) {
                    SummonedMaidBroom broom = SummonedMaidBroom.createForMaid(level, maid, owner);
                    level.addFreshEntity(broom);
                    maid.startRiding(broom, true);
                    SummonManager.initSummon(owner, broom, ticks, castData);
                }

                // 11. 建立 Owner 关系
                SummonManager.initSummon(owner, maid, ticks, castData);

                successCount++;
                WinefoxsSpellbooks.LOGGER.debug("Summoned maid: uuid={}, owner={}, level={}, airForce={}",
                    maid.getUUID(), owner.getName().getString(), summonLevel, isAirForce);

            } catch (Exception e) {
                WinefoxsSpellbooks.LOGGER.error("Failed to summon maid: {}", e.getMessage(), e);
            }
        }

        // 12. 返回结果
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

            try {
                if (entity instanceof MaidMagicEntity maid) {
                    maid.winefoxsSpellbooks$getMagicMaidAdapter().setBypassCastCheck(true);
                }
                magicEntity.initiateCastSpell(spell, level);
                if (entity instanceof MaidMagicEntity maid) {
                    maid.winefoxsSpellbooks$getMagicMaidAdapter().setBypassCastCheck(false);
                }
                successCount++;
                WinefoxsSpellbooks.LOGGER.debug("Entity {} cast spell {} (level {})", entity.getUUID(), spellId, level);
            } catch (Exception e) {
                WinefoxsSpellbooks.LOGGER.error("Entity {} failed to cast spell {}: {}", entity.getUUID(), spellId, e.getMessage());
                failCount++;
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
