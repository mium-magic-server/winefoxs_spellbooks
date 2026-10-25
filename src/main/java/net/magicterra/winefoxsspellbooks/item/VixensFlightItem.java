package net.magicterra.winefoxsspellbooks.item;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import java.util.List;
import net.magicterra.winefoxsspellbooks.registry.WsbDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * 品酒排：一排四支酒签，每支装一瓶普通药水（250 mB）
 * <p>
 * 装填从左到右，取用从右到左，喝空的酒签留在托板上。装填方式：
 * <ul>
 *   <li>物品栏中与药水瓶互相右键，收纳袋式：药水装进来留下空玻璃瓶，空玻璃瓶点上来倒出最右一支</li>
 *   <li>原版炼药锅只装水：右键取水，潜行右键把最右一支水倒回去</li>
 *   <li>铁魔法炼金锅：右键取 250 mB 药水，潜行右键倒回去</li>
 * </ul>
 * 饮用走原版喝药流程。不继承 {@link net.minecraft.world.item.PotionItem}：车万女仆在女仆喝完任何 PotionItem 后
 * 都会把整叠物品扣掉一个并塞回玻璃瓶，会把整排吞掉。女仆饮用见 {@link net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MaidVialPicker}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public class VixensFlightItem extends Item {
    /**
     * 饮用时长，与药水相同
     */
    private static final int DRINK_DURATION = 32;

    /**
     * 空酒签的着色：液体遮罩乘上它后像一支空玻璃瓶（取自原版药水瓶的玻璃高光）
     */
    private static final int EMPTY_VIAL_COLOR = 0xFFD4E5F7;

    /**
     * 每支酒签折合的流体量，与铁魔法炼金锅的一瓶同口径
     */
    private static final int VIAL_FLUID_AMOUNT = 250;

    private static final String[] VIAL_MARKS = {"①", "②", "③", "④"};

    public VixensFlightItem() {
        super(new Properties().stacksTo(1).component(WsbDataComponents.VIALS.get(), VialsContents.EMPTY));
    }

    public static VialsContents vials(ItemStack stack) {
        return stack.getOrDefault(WsbDataComponents.VIALS.get(), VialsContents.EMPTY);
    }

    private static void setVials(ItemStack stack, VialsContents vials) {
        stack.set(WsbDataComponents.VIALS.get(), vials);
    }

    /**
     * 物品着色：tintIndex 0 是托板和玻璃，1～4 依次是第 0～3 支的液体
     */
    public static int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex <= 0 || tintIndex > VialsContents.SIZE) {
            return -1;
        }
        VialsContents vials = vials(stack);
        int index = tintIndex - 1;
        return vials.isEmpty(index) ? EMPTY_VIAL_COLOR : FastColor.ARGB32.opaque(vials.get(index).getColor());
    }

    // ==================== 饮用 ====================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (vials(player.getItemInHand(hand)).lastFilled() < 0) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_DURATION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        VialsContents vials = vials(stack);
        Integer choice = stack.remove(WsbDataComponents.VIAL_CHOICE.get());
        // 女仆事先选好的那支仍在就喝它，否则按最右一支
        int index = choice != null && choice >= 0 && choice < VialsContents.SIZE && !vials.isEmpty(choice) ? choice : vials.lastFilled();
        if (index < 0) {
            return stack;
        }
        PotionContents contents = vials.get(index);
        Player player = entity instanceof Player p ? p : null;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, VialsContents.toPotion(contents));
        }
        // 同 PotionItem.finishUsingItem
        if (!level.isClientSide) {
            contents.forEachEffect(effect -> {
                if (effect.getEffect().value().isInstantenous()) {
                    effect.getEffect().value().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0);
                } else {
                    entity.addEffect(effect);
                }
            });
        }
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (player == null || !player.hasInfiniteMaterials()) {
            setVials(stack, vials.with(index, PotionContents.EMPTY));
        }
        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    // ==================== 炼药锅 / 炼金锅 ====================

    /**
     * 先于方块自身的交互执行：两种锅都在这里处理，炼金锅本身只认手持物品里的药水组件
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        boolean pour = player != null && player.isSecondaryUseActive();
        // 客户端只预判能否成功，改动以服务端为准
        boolean simulate = level.isClientSide;
        boolean done;
        if (level.getBlockEntity(pos) instanceof AlchemistCauldronTile cauldron) {
            done = pour ? pourIntoAlchemistCauldron(stack, cauldron, simulate) : fillFromAlchemistCauldron(stack, cauldron, simulate);
        } else {
            BlockState state = level.getBlockState(pos);
            done = pour ? pourIntoCauldron(stack, level, pos, state, simulate) : fillFromCauldron(stack, level, pos, state, simulate);
        }
        if (!done) {
            return InteractionResult.PASS;
        }
        if (!simulate) {
            level.playSound(null, pos, pour ? SoundEvents.BOTTLE_EMPTY : SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(player, pour ? GameEvent.FLUID_PLACE : GameEvent.FLUID_PICKUP, pos);
            if (player != null) {
                player.awardStat(Stats.USE_CAULDRON);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean fillFromCauldron(ItemStack stack, Level level, BlockPos pos, BlockState state, boolean simulate) {
        VialsContents vials = vials(stack);
        int index = vials.firstEmpty();
        if (index < 0 || !state.is(Blocks.WATER_CAULDRON)) {
            return false;
        }
        if (!simulate) {
            setVials(stack, vials.with(index, new PotionContents(Potions.WATER)));
            // 一支酒签按一格水位算，同玻璃瓶
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return true;
    }

    private static boolean pourIntoCauldron(ItemStack stack, Level level, BlockPos pos, BlockState state, boolean simulate) {
        VialsContents vials = vials(stack);
        int index = vials.lastFilled();
        if (index < 0 || !vials.get(index).is(Potions.WATER)) {
            return false;
        }
        BlockState filled;
        if (state.is(Blocks.CAULDRON)) {
            filled = Blocks.WATER_CAULDRON.defaultBlockState();
        } else if (state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL) {
            filled = state.cycle(LayeredCauldronBlock.LEVEL);
        } else {
            return false;
        }
        if (!simulate) {
            setVials(stack, vials.with(index, PotionContents.EMPTY));
            level.setBlockAndUpdate(pos, filled);
        }
        return true;
    }

    private static boolean fillFromAlchemistCauldron(ItemStack stack, AlchemistCauldronTile cauldron, boolean simulate) {
        VialsContents vials = vials(stack);
        int index = vials.firstEmpty();
        if (index < 0) {
            return false;
        }
        // 与玻璃瓶一样取最上层的流体
        FluidStack top = cauldron.fluidInventory.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (top.getAmount() < VIAL_FLUID_AMOUNT) {
            return false;
        }
        PotionContents contents;
        if (top.is(Fluids.WATER)) {
            contents = new PotionContents(Potions.WATER);
        } else {
            ItemStack potion = PotionFluid.from(top);
            if (!VialsContents.canHold(potion)) {
                // 墨水、鲜血等非药水流体，以及喷溅型、滞留型药水
                return false;
            }
            contents = potion.get(DataComponents.POTION_CONTENTS);
        }
        if (!simulate) {
            cauldron.fluidInventory.drain(top.copyWithAmount(VIAL_FLUID_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
            cauldron.setChanged();
            setVials(stack, vials.with(index, contents));
        }
        return true;
    }

    private static boolean pourIntoAlchemistCauldron(ItemStack stack, AlchemistCauldronTile cauldron, boolean simulate) {
        VialsContents vials = vials(stack);
        int index = vials.lastFilled();
        if (index < 0) {
            return false;
        }
        PotionContents contents = vials.get(index);
        FluidStack fluid = contents.is(Potions.WATER)
            ? new FluidStack(Fluids.WATER, VIAL_FLUID_AMOUNT)
            : PotionFluid.from(VialsContents.toPotion(contents)).copyWithAmount(VIAL_FLUID_AMOUNT);
        if (fluid.isEmpty() || cauldron.fluidInventory.fill(fluid, IFluidHandler.FluidAction.SIMULATE) != VIAL_FLUID_AMOUNT) {
            return false;
        }
        if (!simulate) {
            cauldron.fluidInventory.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            cauldron.setChanged();
            setVials(stack, vials.with(index, PotionContents.EMPTY));
        }
        return true;
    }

    // ==================== 物品栏收纳（收纳袋式，右键） ====================

    /**
     * 手持品酒排右键物品栏里的药水 / 玻璃瓶
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        // 先查格子能否取放：exchange 会立即改动品酒排，事后 safeTake 失败就会凭空多出一支
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        ItemStack other = slot.getItem();
        ItemStack result = exchange(stack, other, player);
        if (result == null) {
            return false;
        }
        slot.safeTake(1, 1, player);
        if (slot.getItem().isEmpty()) {
            slot.safeInsert(result);
        } else {
            player.getInventory().placeItemBackInInventory(result);
        }
        return true;
    }

    /**
     * 手持药水 / 玻璃瓶右键物品栏里的品酒排
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        ItemStack result = exchange(stack, other, player);
        if (result == null) {
            return false;
        }
        other.shrink(1);
        if (other.isEmpty()) {
            access.set(result);
        } else {
            player.getInventory().placeItemBackInInventory(result);
        }
        slot.setChanged();
        return true;
    }

    /**
     * 药水装进最左空槽、换回玻璃瓶；玻璃瓶倒出最右一支、换回药水
     *
     * @return 换回的物品；不能交换时为 {@code null}
     */
    private static ItemStack exchange(ItemStack flight, ItemStack other, Player player) {
        VialsContents vials = vials(flight);
        if (VialsContents.canHold(other)) {
            int index = vials.firstEmpty();
            if (index < 0) {
                return null;
            }
            setVials(flight, vials.with(index, other.get(DataComponents.POTION_CONTENTS)));
            playSound(player, SoundEvents.BOTTLE_EMPTY);
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (other.is(Items.GLASS_BOTTLE)) {
            int index = vials.lastFilled();
            if (index < 0) {
                return null;
            }
            setVials(flight, vials.with(index, PotionContents.EMPTY));
            playSound(player, SoundEvents.BOTTLE_FILL);
            return VialsContents.toPotion(vials.get(index));
        }
        return null;
    }

    private static void playSound(Player player, SoundEvent sound) {
        player.playSound(sound, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
    }

    // ==================== 提示 ====================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        VialsContents vials = vials(stack);
        for (int i = 0; i < VialsContents.SIZE; i++) {
            if (vials.isEmpty(i)) {
                tooltip.add(Component.literal(VIAL_MARKS[i] + " ")
                    .append(Component.translatable("tooltip.winefoxs_spellbooks.vixens_flight.empty"))
                    .withStyle(ChatFormatting.DARK_GRAY));
                continue;
            }
            PotionContents contents = vials.get(i);
            tooltip.add(Component.literal(VIAL_MARKS[i] + " ").withColor(contents.getColor())
                .append(VialsContents.toPotion(contents).getHoverName().copy().withStyle(ChatFormatting.WHITE)));
            for (MobEffectInstance effect : contents.getAllEffects()) {
                tooltip.add(Component.literal("   ").append(describeEffect(effect, context.tickRate())));
            }
        }
        int next = vials.lastFilled();
        if (next >= 0) {
            tooltip.add(Component.translatable("tooltip.winefoxs_spellbooks.vixens_flight.next", VIAL_MARKS[next])
                .withStyle(ChatFormatting.GRAY));
        }
    }

    private static MutableComponent describeEffect(MobEffectInstance effect, float tickRate) {
        MutableComponent text = Component.translatable(effect.getDescriptionId());
        if (effect.getAmplifier() > 0) {
            text = Component.translatable("potion.withAmplifier", text, Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        if (!effect.endsWithin(20)) {
            text = Component.translatable("potion.withDuration", text, MobEffectUtil.formatDuration(effect, 1.0f, tickRate));
        }
        return text.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting());
    }
}
