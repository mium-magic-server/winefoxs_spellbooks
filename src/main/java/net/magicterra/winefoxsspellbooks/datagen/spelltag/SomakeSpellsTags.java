package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.somake.somakespells.registries.ModSpells;
import net.minecraft.resources.ResourceLocation;

public final class SomakeSpellsTags {

    private SomakeSpellsTags() {
    }

    /**
     * 按注册名构造 Somake Spells 的资源位置，用于字段在依赖缺席时为 null 的法术。
     */
    private static ResourceLocation somake(String path) {
        return ResourceLocation.fromNamespaceAndPath("somakespells", path);
    }

    public static void contribute(SpellTagContext ctx) {
        // RITUAL_FLAME 只用来点燃祭坛、转化烈焰人，战斗中没有用处。
        // audit:ignore RITUAL_FLAME

        // 以下三个法术的前置条件或生效路径要求玩家身份，女仆放了只会失败或白耗蓝。
        // CRIMSON_REFLECTION：checkPreCastConditions 要求 ServerPlayer 且需充能的红魂灯笼，用于开启决斗。
        // WINGED_RUIN：前置条件要求施法者持有红魂（RED_SOUL）效果，该效果只来自玩家进度，女仆恒定失败。
        // SACRED_PHOENIX_BLESSING：目标合法性在双方均无队伍时只接受 Player 对 Player，女仆无队伍时找不到目标。
        // audit:ignore CRIMSON_REFLECTION, WINGED_RUIN, SACRED_PHOENIX_BLESSING

        // ========== Fire ==========
        ctx.attack().addOptional(ModSpells.ABYSSAL_BURN.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.APOCALYPTIC_BURST.get().getSpellResource());
        // DAMNED_DEMOMANS: 自身增益，增益期间由 handler 持续放出爆发伤害，交战时施放
        ctx.attack().addOptional(ModSpells.DAMNED_DEMOMANS.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.COMBUSTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ERUPTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRE_BLAST.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRE_ORBS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRESTORM_VORTEX.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.IGNIS_SHIELD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.INCINERATOR_SLASH.get().getSpellResource());

        // ========== Blood ==========
        ctx.defense().addOptional(ModSpells.BLOOD_CUT.get().getSpellResource());
        // BLOODMARK: onCast 对非 ServerPlayer 施法者直接返回，女仆放了只是白耗蓝。
        // audit:ignore BLOODMARK
        // ctx.attack().addOptional(ModSpells.BLOODMARK.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.BLOOD_RUSH.get().getSpellResource());
        // BLOODBOUND_BLADE: 把手持的剑掷出并定身目标，前置条件要求主手持剑，无剑时施放失败
        ctx.attack().addOptional(ModSpells.BLOODBOUND_BLADE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.BLOODY_LEGACY.get().getSpellResource());
        // CURSED_CONNECTION: 在两个敌人之间建链，伤害互相镜像
        ctx.attack().addOptional(ModSpells.CURSED_CONNECTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FRAGMENTED_REQUIEM.get().getSpellResource());
        // FUNERAL_BLOOM: 在准星落点（最远 32 格）生成半径 10 格以上的花雨风暴，逐波砸向范围内敌人
        ctx.attack().addOptional(ModSpells.FUNERAL_BLOOM.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ROSE_SECRET.get().getSpellResource());
        // WITHERED_ROSE_VORTEX: 在准星落点生成持续吸附并凋零敌人的漩涡
        ctx.attack().addOptional(ModSpells.WITHERED_ROSE_VORTEX.get().getSpellResource());

        // ========== Holy ==========
        // BLESSED_CONNECTION: 建链后两端共享治疗量
        ctx.supportEffect().addOptional(ModSpells.BLESSED_CONNECTION.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.BLESSING.get().getSpellResource());
        ctx.positiveEffect().addOptional(ModSpells.CUSTODIA_CAELI.get().getSpellResource());
        // GUARDIAN_CONNECTION: 把队友承受的伤害转移到自己身上
        ctx.positiveEffect().addOptional(ModSpells.GUARDIAN_CONNECTION.get().getSpellResource());
        // SOVEREIGN_ARMORY: 在身后生成传送门，向准星目标连续发射两轮兵器
        ctx.attack().addOptional(ModSpells.SOVEREIGN_ARMORY.get().getSpellResource());

        // ========== Lightning ==========
        ctx.attack().addOptional(ModSpells.LIGHTNING_BALL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_CUT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_DANCE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_FIELD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_LASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_SPARK.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_SPEAR.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_SWARM.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.STORM_AURA.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.THUNDER_CLOUD.get().getSpellResource());

        // ========== Aqua ==========
        ctx.attack().addOptional(ModSpells.HYDRO_SLASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SEA_SERPENT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SEA_SERPENT_JET.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.SUBMERGE.get().getSpellResource());
        // TIDAL_DASH: 锚点把施法者拽向目标并撞击，主用途是打伤害
        ctx.attack().addOptional(ModSpells.TIDAL_DASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.TIDAL_GRASP.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.TSUNAMI.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WATER_BALL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WATER_CONTROL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WATER_SPEAR.get().getSpellResource());

        // ========== Symmetry ==========
        ctx.attack().addOptional(ModSpells.AXE_CLEAVE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.DESERT_WRATH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.HALBERD_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.MIRROR_STRIKE.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.SYMMETRY_EMPOWERMENT.get().getSpellResource());

        // ========== Ender / Eldritch / Evocation / Geo ==========
        ctx.attack().addOptional(ModSpells.ABYSSAL_ORBS.get().getSpellResource());
        // CHAIN_CONNECTION: 锁链把两个目标拴在一起并互相拉扯
        ctx.attack().addOptional(ModSpells.CHAIN_CONNECTION.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.EARTHBOUND.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ELDRITCH_GAMBIT.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ENDER_CORRUPTION.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.EVOCATION_FORTITUDE.get().getSpellResource());
        ctx.movement().addOptional(ModSpells.RENDER_RUSH.get().getSpellResource());

        // ========== Nature / Ice ==========
        ctx.defense().addOptional(ModSpells.OVERGROWTH.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.PERMAFROST.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.PUMPKIN_BOMB.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SUMMON_DROWNED.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_DROWNED.get().getSpellResource());

        // ========== Sound / Spirit ==========
        // COMFORTING_LULLABY: 持续吟唱，周期性治疗范围内受伤的玩家（女仆自身不会被治疗），只作用于玩家
        ctx.supportEffect().addOptional(ModSpells.COMFORTING_LULLABY.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.JINGLE_BELL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.PHANTOM_BARRAGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.RAM_TCHUM.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.RESONANT_PULSE.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.REVERBERATION.get().getSpellResource());
        ctx.negativeEffect().addOptional(ModSpells.SLUMBER_MELODY.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SOUL_GRAB.get().getSpellResource());
        // 以下 Spirit 法术仅当 Legendary Monsters 已加载时才注册，字段在其缺席时为 null，
        // 故按注册名构造资源位置而不取 holder。审计脚本看不到 `.CONST.get()`，用 audit:by-id 标记，
        // 脚本会核对对应的注册名字符串字面量确实出现在文件里。
        // audit:by-id GRAVE_SIGIL, PROCESSION_OF_SOULS, SOUL_BASTION, SOUL_LATCH, SOUL_REPRISAL
        // audit:by-id SOULFALL_JUDGMENT, SPECTRAL_RONDO, SPIRAL_OF_RUIN, SPIRIT_EMPOWERMENT
        // GRAVE_SIGIL: 在准星落点依次升起墓碑符印，逐波伤害范围内敌人
        ctx.attack().addOptional(somake("grave_sigil"));
        // PROCESSION_OF_SOULS: 沿面朝方向依次触发一排灵魂打击
        ctx.attack().addOptional(somake("procession_of_souls"));
        // SOUL_BASTION: 以施法者为中心，先由外圈 12 格向内、再由内圈向外放出灵魂盾环
        ctx.attack().addOptional(somake("soul_bastion"));
        // SOUL_LATCH: 对目标建立灵魂锁链，持续减速并削弱，结束时爆发并为施法者回复 4 点生命
        ctx.attack().addOptional(somake("soul_latch"));
        // SOUL_REPRISAL: 扇形掷出六把灵魂匕首并召回
        ctx.attack().addOptional(somake("soul_reprisal"));
        // SOULFALL_JUDGMENT: 以施法者为中心 3 格内砸地，随后落下六道剑刃；前置要求站在地面
        ctx.attack().addOptional(somake("soulfall_judgment"));
        // SPECTRAL_RONDO: 向前旋转突进并两次横扫身周 3.5 格内的敌人；前置要求站在地面
        ctx.attack().addOptional(somake("spectral_rondo"));
        // SPIRAL_OF_RUIN: 射出弹体，沿途螺旋引爆多次
        ctx.attack().addOptional(somake("spiral_of_ruin"));
        // SPIRIT_EMPOWERMENT 在 Somake Spells 8952876 起仅当 Legendary Monsters 已加载时才注册，字段在其缺席时为 null，
        // 故按注册名构造资源位置而不取 holder。
        ctx.defense().addOptional(ResourceLocation.fromNamespaceAndPath("somakespells", "spirit_empowerment"));

        // 需要多次施放（recast）的法术
        // PHANTOM_BARRAGE 与 THUNDER_CLOUD 在 Somake Spells 8952876 中已无 getRecastCount / RecastInstance，不再属于此列。
        // GUARDIAN_CONNECTION 不属于此列：8952876 中其 getRecastCount 恒为 0，onCast 也不登记 RecastInstance，一次施放即建立守护连接。
        ctx.maidShouldRecast().addOptional(ModSpells.BLESSED_CONNECTION.get().getSpellResource());
        // ctx.maidShouldRecast().addOptional(ModSpells.BLOODMARK.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.CHAIN_CONNECTION.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.CURSED_CONNECTION.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.FIRE_ORBS.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.HYDRO_SLASH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.LIGHTNING_CUT.get().getSpellResource());
    }
}
