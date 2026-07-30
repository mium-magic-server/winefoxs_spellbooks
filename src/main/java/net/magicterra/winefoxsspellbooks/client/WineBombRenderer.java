package net.magicterra.winefoxsspellbooks.client;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.projectile.WineBombProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 狐酿炸弹渲染器（GeckoLib）：一串翻滚的紫葡萄炸弹。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public class WineBombRenderer extends GeoEntityRenderer<WineBombProjectile> {

    public WineBombRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "wine_bomb")));
    }
}
