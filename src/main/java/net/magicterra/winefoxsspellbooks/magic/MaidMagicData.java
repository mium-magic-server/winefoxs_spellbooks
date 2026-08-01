package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.MaidRecasts;
import net.magicterra.winefoxsspellbooks.mixin.MagicDataAccessor;
import net.minecraft.world.entity.LivingEntity;

/**
 * 女仆版 {@link MagicData}：把 {@code playerRecasts} 换成 {@link MaidRecasts}。
 * <p>
 * 父类那个字段构造时就地换掉，存读档等一切直接访问它的父类逻辑照旧生效，本类不必分叉 recast 的序列化。
 * {@link #getPlayerRecasts()} 仍要重写：父类在 {@code isMob} 时返回的是一张临时空表，压根不读该字段。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:22
 */
public class MaidMagicData extends MagicData {
    private final MaidRecasts maidRecasts;

    public MaidMagicData(LivingEntity livingEntity) {
        super(true);
        this.maidRecasts = new MaidRecasts(livingEntity);
        ((MagicDataAccessor) this).winefoxsSpellbooks$setPlayerRecasts(maidRecasts);
    }

    @Override
    public PlayerRecasts getPlayerRecasts() {
        return maidRecasts;
    }
}
