package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * MaidMagicData
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:22
 */
public class MaidMagicData extends MagicData {
    private static final Field playerRecastsField = FieldUtils.getDeclaredField(MagicData.class, "playerRecasts", true);

    protected EntityMaid maid;

    public MaidMagicData(EntityMaid maid) {
        super(true);
        this.maid = maid;
        try {
            FieldUtils.writeField(playerRecastsField, this, new MaidRecasts(maid));
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
