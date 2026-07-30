package net.magicterra.winefoxsspellbooks.client.model;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * 雪狐狸公共美术模型。
 * <p>
 * 统一提供 geo、animation 与 texture 三类资产，供不同狐影实体复用；具体播放哪些动画由实体控制器决定。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-25
 */
public class SnowFoxModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {

    private static final ResourceLocation ASSET = ResourceLocation.fromNamespaceAndPath(
        WinefoxsSpellbooks.MODID, "snow_fox");

    public SnowFoxModel() {
        super(ASSET);
    }
}
