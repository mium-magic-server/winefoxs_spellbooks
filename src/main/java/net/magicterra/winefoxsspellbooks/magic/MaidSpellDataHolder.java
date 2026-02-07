package net.magicterra.winefoxsspellbooks.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.ConcatenatedListView;

/**
 * 可用法术和等级包装
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-09-11 00:41
 */
public class MaidSpellDataHolder {
    /**
     * 法术列表 Codec
     */
    private static final Codec<List<SpellData>> SPELL_LIST_CODEC = SpellData.CODEC.listOf();

    /**
     * 法术列表 StreamCodec
     */
    private static final StreamCodec<ByteBuf, List<SpellData>> SPELL_LIST_STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, (SpellData spellData) -> spellData.getSpell().getSpellResource(),
        ByteBufCodecs.INT, SpellData::getLevel,
        ByteBufCodecs.BOOL, SpellData::isLocked,
        SpellData::new
    ).apply(ByteBufCodecs.list());

    /**
     * MaidSpellDataHolder 的 Codec
     */
    public static final Codec<MaidSpellDataHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SPELL_LIST_CODEC.optionalFieldOf("attack_spells", List.of()).forGetter(h -> h.attackSpells),
        SPELL_LIST_CODEC.optionalFieldOf("defense_spells", List.of()).forGetter(h -> h.defenseSpells),
        SPELL_LIST_CODEC.optionalFieldOf("movement_spells", List.of()).forGetter(h -> h.movementSpells),
        SPELL_LIST_CODEC.optionalFieldOf("support_spells", List.of()).forGetter(h -> h.supportSpells),
        SPELL_LIST_CODEC.optionalFieldOf("positive_effect_spells", List.of()).forGetter(h -> h.positiveEffectSpells),
        SPELL_LIST_CODEC.optionalFieldOf("negative_effect_spells", List.of()).forGetter(h -> h.negativeEffectSpells),
        SPELL_LIST_CODEC.optionalFieldOf("support_effect_spells", List.of()).forGetter(h -> h.supportEffectSpells)
    ).apply(instance, MaidSpellDataHolder::new));

    public static final StreamCodec<ByteBuf, MaidSpellDataHolder> STREAM_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public MaidSpellDataHolder decode(@Nonnull ByteBuf b) {
            return new MaidSpellDataHolder(
                SPELL_LIST_STREAM_CODEC.decode(b),
                SPELL_LIST_STREAM_CODEC.decode(b),
                SPELL_LIST_STREAM_CODEC.decode(b),
                SPELL_LIST_STREAM_CODEC.decode(b),
                SPELL_LIST_STREAM_CODEC.decode(b),
                SPELL_LIST_STREAM_CODEC.decode(b),
                SPELL_LIST_STREAM_CODEC.decode(b)
            );
        }

        @Override
        public void encode(@Nonnull ByteBuf b, @Nonnull MaidSpellDataHolder h) {
            SPELL_LIST_STREAM_CODEC.encode(b, h.getAttackSpells());
            SPELL_LIST_STREAM_CODEC.encode(b, h.getDefenseSpells());
            SPELL_LIST_STREAM_CODEC.encode(b, h.getMovementSpells());
            SPELL_LIST_STREAM_CODEC.encode(b, h.getSupportSpells());
            SPELL_LIST_STREAM_CODEC.encode(b, h.getPositiveEffectSpells());
            SPELL_LIST_STREAM_CODEC.encode(b, h.getNegativeEffectSpells());
            SPELL_LIST_STREAM_CODEC.encode(b, h.getSupportEffectSpells());
        }
    };

    private final ArrayList<SpellData> attackSpells;
    private final ArrayList<SpellData> defenseSpells;
    private final ArrayList<SpellData> movementSpells;
    private final ArrayList<SpellData> supportSpells;
    private final ArrayList<SpellData> positiveEffectSpells;
    private final ArrayList<SpellData> negativeEffectSpells;
    private final ArrayList<SpellData> supportEffectSpells;

    /**
     * 默认构造函数
     */
    public MaidSpellDataHolder() {
        this.attackSpells = new ArrayList<>();
        this.defenseSpells = new ArrayList<>();
        this.movementSpells = new ArrayList<>();
        this.supportSpells = new ArrayList<>();
        this.positiveEffectSpells = new ArrayList<>();
        this.negativeEffectSpells = new ArrayList<>();
        this.supportEffectSpells = new ArrayList<>();
    }

    /**
     * Codec 使用的构造函数
     */
    public MaidSpellDataHolder(
        List<SpellData> attackSpells,
        List<SpellData> defenseSpells,
        List<SpellData> movementSpells,
        List<SpellData> supportSpells,
        List<SpellData> positiveEffectSpells,
        List<SpellData> negativeEffectSpells,
        List<SpellData> supportEffectSpells) {
        this.attackSpells = new ArrayList<>(attackSpells);
        this.defenseSpells = new ArrayList<>(defenseSpells);
        this.movementSpells = new ArrayList<>(movementSpells);
        this.supportSpells = new ArrayList<>(supportSpells);
        this.positiveEffectSpells = new ArrayList<>(positiveEffectSpells);
        this.negativeEffectSpells = new ArrayList<>(negativeEffectSpells);
        this.supportEffectSpells = new ArrayList<>(supportEffectSpells);
    }

    // ==================== Getter 方法 ====================

    public ArrayList<SpellData> getAttackSpells() {
        return attackSpells;
    }

    public ArrayList<SpellData> getDefenseSpells() {
        return defenseSpells;
    }

    public ArrayList<SpellData> getMovementSpells() {
        return movementSpells;
    }

    public ArrayList<SpellData> getSupportSpells() {
        return supportSpells;
    }

    public ArrayList<SpellData> getPositiveEffectSpells() {
        return positiveEffectSpells;
    }

    public ArrayList<SpellData> getNegativeEffectSpells() {
        return negativeEffectSpells;
    }

    public ArrayList<SpellData> getSupportEffectSpells() {
        return supportEffectSpells;
    }

    // ==================== 判断方法 ====================

    public boolean hasAnySpells() {
        return attackSpells.size() +
            defenseSpells.size() +
            movementSpells.size() +
            supportSpells.size() +
            positiveEffectSpells.size() +
            negativeEffectSpells.size() +
            supportEffectSpells.size() > 0;
    }

    public boolean hasAnyCastingTaskSpells() {
        return attackSpells.size() +
            defenseSpells.size() +
            movementSpells.size() +
            supportSpells.size() +
            negativeEffectSpells.size() > 0;
    }

    public boolean hasAnyMagicSupportTaskSpells() {
        return defenseSpells.size() +
            supportSpells.size() +
            positiveEffectSpells.size() +
            negativeEffectSpells.size() +
            supportEffectSpells.size() > 0;
    }

    // ==================== 更新方法 ====================

    public void updateAttackSpells(Collection<SpellData> spells) {
        attackSpells.clear();
        attackSpells.addAll(spells);
    }

    public void updateDefenseSpells(Collection<SpellData> spells) {
        defenseSpells.clear();
        defenseSpells.addAll(spells);
    }

    public void updateMovementSpells(Collection<SpellData> spells) {
        movementSpells.clear();
        movementSpells.addAll(spells);
    }

    public void updateSupportSpells(Collection<SpellData> spells) {
        supportSpells.clear();
        supportSpells.addAll(spells);
    }

    public void updatePositiveEffectSpells(Collection<SpellData> spells) {
        positiveEffectSpells.clear();
        positiveEffectSpells.addAll(spells);
    }

    public void updateNegativeEffectSpells(Collection<SpellData> spells) {
        negativeEffectSpells.clear();
        negativeEffectSpells.addAll(spells);
    }

    public void updateSupportEffectSpells(Collection<SpellData> spells) {
        supportEffectSpells.clear();
        supportEffectSpells.addAll(spells);
    }

    public Collection<SpellData> getAllSpells() {
        return ConcatenatedListView.of(attackSpells, defenseSpells, movementSpells, supportSpells, positiveEffectSpells, negativeEffectSpells, supportEffectSpells);
    }

    public void clear() {
        attackSpells.clear();
        defenseSpells.clear();
        movementSpells.clear();
        supportSpells.clear();
        positiveEffectSpells.clear();
        negativeEffectSpells.clear();
        supportEffectSpells.clear();
    }
}
