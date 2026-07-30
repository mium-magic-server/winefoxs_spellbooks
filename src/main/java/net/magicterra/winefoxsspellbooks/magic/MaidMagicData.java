package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import java.lang.reflect.Field;
import net.magicterra.winefoxsspellbooks.magic.recast.MaidRecasts;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * 女仆版 {@link MagicData}：把 {@code playerRecasts} 换成 {@link MaidRecasts}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:22
 */
public class MaidMagicData extends MagicData {
    private static final Field playerRecastsField = FieldUtils.getDeclaredField(MagicData.class, "playerRecasts", true);

    protected LivingEntity livingEntity;

    public MaidMagicData(LivingEntity livingEntity) {
        super(true);
        this.livingEntity = livingEntity;
        try {
            FieldUtils.writeField(playerRecastsField, this, new MaidRecasts(livingEntity));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PlayerRecasts getPlayerRecasts() {
        try {
            return (PlayerRecasts) FieldUtils.readField(playerRecastsField, this);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
