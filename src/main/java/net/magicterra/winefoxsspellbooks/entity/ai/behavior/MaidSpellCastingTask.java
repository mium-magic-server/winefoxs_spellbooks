package net.magicterra.winefoxsspellbooks.entity.ai.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * 女仆施法行为
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-08 20:27
 */
public class MaidSpellCastingTask extends Behavior<Mob> {
    protected final IMagicEntity magicEntity;

    protected SpellData currentSpell = SpellData.EMPTY;
    protected int seeTime = 0;

    public MaidSpellCastingTask(IMagicEntity magicEntity) {
        super(ImmutableMap.of(
            MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.PATH, MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), MemoryStatus.REGISTERED
        ), 1200);
        this.magicEntity = magicEntity;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, Mob mob) {
        if (!(mob instanceof IMagicEntity)) {
            return false;
        }
        SpellData spellData = mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get()).orElse(null);
        if (spellData == null || spellData == SpellData.EMPTY) {
            return false;
        }
        currentSpell = spellData;

        if (magicEntity.isCasting()) {
            // 正在施法
            return false;
        }
        if (magicEntity.isDrinkingPotion()) {
            // 正在喝药
            return false;
        }

        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
            .or(() -> mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()))
            .orElse(null);
        if (target == null) {
            return true;
        }

        boolean canSee;
        if (mob instanceof EntityMaid maid) {
            canSee = maid.canSee(target);
        } else {
            canSee = BehaviorUtils.canSee(mob, target);
        }
        boolean condition = (canSee || seeTime > -50) && target.isAlive();
        if (!condition) {
            return false;
        }

        double distanceSquared = target == null ? 0 : mob.distanceToSqr(target);
        float spellcastingRange = MaidSpellRegistry.getSpellRange(currentSpell.getSpell());
        float spellcastingRangeSqr = spellcastingRange * spellcastingRange;

        if (currentSpell == SpellData.EMPTY) {
            // 无可用法术
            return false;
        }

        // 攻击范围
        return distanceSquared <= spellcastingRangeSqr;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, Mob mob, long gameTimeIn) {
        MagicData magicData = magicEntity.getMagicData();
        if (!magicData.isCasting()) {
            return false;
        }
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
            .or(() -> mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()))
            .orElse(null);
        if (target == null) {
            return true;
        }
        if (target.isDeadOrDying()) {
            return false;
        }
        boolean canSee;
        if (mob instanceof EntityMaid maid) {
            canSee = maid.canSee(target);
        } else {
            canSee = BehaviorUtils.canSee(mob, target);
        }
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }
        if (!canSee && seeTime <= -50) {
            return false;
        }
        SpellData spellData = magicData.getCastingSpell();
        if (spellData.getSpell().shouldAIStopCasting(spellData.getLevel(), mob, target)) {
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel worldIn, Mob mob, long gameTimeIn) {
        mob.setAggressive(true);
        doSpellAction(mob);
    }

    @Override
    protected void stop(ServerLevel worldIn, Mob mob, long gameTimeIn) {
        seeTime = 0;
        mob.setAggressive(false);
        if (magicEntity.isCasting()) {
            magicEntity.cancelCast();
        }
        mob.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get());
//        PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
//        for (RecastInstance activeRecast : playerRecasts.getActiveRecasts()) {
//            spellCastingMob.initiateCastSpell(SpellRegistry.getSpell(activeRecast.getSpellId()), activeRecast.getSpellLevel());
//        }
    }

    protected void doSpellAction(Mob mob) {
        // 获取咒语类型
        var spellData = currentSpell;
        var spell = spellData.getSpell();
        int spellLevel = spellData.getLevel();
        spellLevel = Math.max(spellLevel, 1);

        PlayerRecasts playerRecasts = magicEntity.getMagicData().getPlayerRecasts();
        boolean hasRecastForSpell = playerRecasts.hasRecastForSpell(spell);

        //Make sure cast is valid. if not, try again shortly
        if (currentSpell != SpellData.EMPTY && !hasRecastForSpell) {
            LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                .or(() -> mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()))
                .orElse(null);
            if (target != null) {
                mob.setYRot(Mth.rotateIfNecessary(mob.getYRot(), mob.yHeadRot, 0.0F));
                mob.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            }
            magicEntity.initiateCastSpell(spell, spellLevel);
        }
    }
}
