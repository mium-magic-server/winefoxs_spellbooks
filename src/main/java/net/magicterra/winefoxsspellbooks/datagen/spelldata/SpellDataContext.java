package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.magicterra.winefoxsspellbooks.magic.data.SpellCastingRangeData;
import net.magicterra.winefoxsspellbooks.magic.data.SpellCausedEffectData;

// Builder 共享是必要的：每个 contributor 调用 .add(...) 都是对同一个可变对象的累积写入。
public record SpellDataContext(
    SpellCastingRangeData.Builder range,
    SpellCausedEffectData.Builder effect
) {}
