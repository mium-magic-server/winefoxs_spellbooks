package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.yimeng261.maidspell.item.bauble.enderPocket.EnderPocketService;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.EnderPocketManaHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在万法皆通末影腰包 HUD 每行女仆信息的血条下方补一根法力条，条上仿照血条居中标注「当前/上限」
 * <p>
 * 数据取自 {@link EnderPocketManaHolder}，随万法皆通原有的包下发；没有法力数据的女仆（未执行施法任务）不画。
 * 坐标照搬 1.9.1 的 {@code renderRow}：血条位于 (x+42, y+17)，宽 79、高 8；坐标文字位于 y+31。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.client.overlay.EnderPocketHudOverlay", remap = false)
public abstract class EnderPocketHudOverlayMixin {
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$BAR_WIDTH = 79;
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$BAR_HEIGHT = 5;
    /**
     * 配色同浮动法力条：外框、空槽、填充
     */
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$BORDER = 0xFF0B0F1E;
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$EMPTY = 0xFF1C2648;
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$FILL = 0xFF3E8CF0;
    /**
     * 条上文字缩小，免得压到下方坐标文字
     */
    @Unique
    private static final float WINEFOXS_SPELLBOOKS$TEXT_SCALE = 0.625F;
    /**
     * 条尾铁箍书卷图标的边长（原物品图标 16px 缩到与条、文字相称）
     */
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$ICON_SIZE = 9;

    @Inject(method = "renderRow", at = @At("TAIL"), require = 0)
    private static void winefoxsSpellbooks$renderMana(GuiGraphics graphics, Minecraft mc,
                                                      EnderPocketService.EnderPocketMaidInfo info, int x, int y,
                                                      CallbackInfo ci) {
        EnderPocketManaHolder holder = (EnderPocketManaHolder) (Object) info;
        if (!holder.winefoxsSpellbooks$hasMana()) {
            return;
        }
        float current = holder.winefoxsSpellbooks$getMana();
        float max = holder.winefoxsSpellbooks$getMaxMana();
        // 紧贴血条下沿（血条占 y+17 ~ y+25）
        int barX = x + 42;
        int barY = y + 26;
        float ratio = max <= 0.0F ? 0.0F : Mth.clamp(current / max, 0.0F, 1.0F);
        graphics.fill(barX, barY, barX + WINEFOXS_SPELLBOOKS$BAR_WIDTH, barY + WINEFOXS_SPELLBOOKS$BAR_HEIGHT,
            WINEFOXS_SPELLBOOKS$BORDER);
        graphics.fill(barX + 1, barY + 1, barX + WINEFOXS_SPELLBOOKS$BAR_WIDTH - 1,
            barY + WINEFOXS_SPELLBOOKS$BAR_HEIGHT - 1, WINEFOXS_SPELLBOOKS$EMPTY);
        int fillWidth = Math.round((WINEFOXS_SPELLBOOKS$BAR_WIDTH - 2) * ratio);
        if (fillWidth > 0) {
            graphics.fill(barX + 1, barY + 1, barX + 1 + fillWidth, barY + WINEFOXS_SPELLBOOKS$BAR_HEIGHT - 1,
                WINEFOXS_SPELLBOOKS$FILL);
        }

        // 与血条同样的「当前/上限」居中压在条上，白字带阴影；条比血条矮，字号缩小，字形（约 7px 高）在条内垂直居中
        String mana = Math.max(0, Mth.floor(current)) + "/" + Math.max(0, Mth.floor(max));
        graphics.pose().pushPose();
        graphics.pose().translate(barX + WINEFOXS_SPELLBOOKS$BAR_WIDTH / 2.0F,
            barY + (WINEFOXS_SPELLBOOKS$BAR_HEIGHT - 7 * WINEFOXS_SPELLBOOKS$TEXT_SCALE) / 2.0F, 0.0F);
        graphics.pose().scale(WINEFOXS_SPELLBOOKS$TEXT_SCALE, WINEFOXS_SPELLBOOKS$TEXT_SCALE, 1.0F);
        graphics.drawCenteredString(mc.font, mana, 0, 0, 0xFFFFFFFF);
        graphics.pose().popPose();

        // 条尾标一本铁箍书卷（铁魔法的标志性法术书），表明这是铁魔法的法力；直接画其物品图标，自绘剪影缩到这个尺寸认不出来
        float iconScale = WINEFOXS_SPELLBOOKS$ICON_SIZE / 16.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(barX + WINEFOXS_SPELLBOOKS$BAR_WIDTH + 2, barY - 2, 0.0F);
        graphics.pose().scale(iconScale, iconScale, 1.0F);
        graphics.renderItem(new ItemStack(ItemRegistry.IRON_SPELL_BOOK.get()), 0, 0);
        graphics.pose().popPose();
    }
}
