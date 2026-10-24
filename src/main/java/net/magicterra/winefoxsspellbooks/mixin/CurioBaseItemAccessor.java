package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入 Iron's 饰品基类，开放其原生栏位字段。
 * <p>
 * Iron's 只在栏位名等于该字段时才给属性加成，女仆放宽到任意栏位时需要知道原生栏位是哪个。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@Mixin(CurioBaseItem.class)
public interface CurioBaseItemAccessor {
    @Accessor("attributeSlot")
    String winefoxsSpellbooks$getAttributeSlot();
}
