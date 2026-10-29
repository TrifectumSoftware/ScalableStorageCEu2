package trifectumsoftware.scalablestorageceu.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import trifectumsoftware.scalablestorageceu.item.StorageUpgradeItem;
import trifectumsoftware.scalablestorageceu.item.VoidUpgradeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
public class ScalableStorageMachine extends MultiblockControllerMachine
                                    implements IFancyUIMachine, IDisplayUIMachine, IDropSaveMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ScalableStorageMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);

    private static final long BASE_CAPACITY = 4_000_000L;
    private static final long[] TIER_CAPACITY = { BASE_CAPACITY, 32_000_000L, 64_000_000L, 256_000_000L };
    private static final int DAMAGE_INTERVAL = 200;

    @Getter
    @DescSynced
    private ItemStack stored = ItemStack.EMPTY;
    @Getter
    @DescSynced
    private long storedAmount;
    @Getter
    @DescSynced
    private int upgradeTier;
    @Getter
    @DescSynced
    private boolean voidUpgrade;
    @Getter
    @DescSynced
    private boolean upgradesValid = true;

    private int damageTimer;
    private List<NotifiableItemStackHandler> inputBuses = List.of();
    private List<NotifiableItemStackHandler> outputBuses = List.of();
    @Nullable
    private TickableSubscription tickSubscription;

    public ScalableStorageMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubscription = subscribeServerTick(this::tick);
    }

    @Override
    public void onUnload() {
        unsubscribe(tickSubscription);
        tickSubscription = null;
        super.onUnload();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        refreshBuses();
        refreshUpgrades();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        resetStructureState();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        resetStructureState();
    }

    private void resetStructureState() {
        inputBuses = List.of();
        outputBuses = List.of();
        upgradeTier = 0;
        voidUpgrade = false;
        upgradesValid = true;
        damageTimer = 0;
    }

    private void tick() {
        if (!isFormed() || getMultiblockState().hasError()) {
            return;
        }

        refreshUpgrades();

        if (upgradeTier > 0 && upgradesValid && ++damageTimer >= DAMAGE_INTERVAL) {
            damageTimer = 0;
            damageUpgrades();
            refreshUpgrades();
        }

        if (!upgradesValid) {
            return;
        }

        pullFromInputs();
        pushToOutputs();
    }

    private void refreshBuses() {
        List<NotifiableItemStackHandler> inputs = new ArrayList<>();
        List<NotifiableItemStackHandler> outputs = new ArrayList<>();

        for (IMultiPart part : getParts()) {
            if (part instanceof ItemBusPartMachine bus) {
                var inventory = bus.getInventory();
                if (inventory.getHandlerIO().support(IO.IN)) {
                    inputs.add(inventory);
                }
                if (inventory.getHandlerIO().support(IO.OUT)) {
                    outputs.add(inventory);
                }
            }
        }

        inputBuses = inputs;
        outputBuses = outputs;
    }

    private void refreshUpgrades() {
        int highestTier = 0;
        boolean hasStorageUpgrade = false;
        boolean hasUsableUpgrade = false;
        boolean hasVoidUpgrade = false;

        for (NotifiableItemStackHandler inventory : inputBuses) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem upgrade) {
                    hasStorageUpgrade = true;
                    if (upgrade.isPermanent() || stack.getDamageValue() < stack.getMaxDamage()) {
                        hasUsableUpgrade = true;
                        highestTier = Math.max(highestTier, upgrade.getTier());
                    }
                } else if (stack.getItem() instanceof VoidUpgradeItem) {
                    hasVoidUpgrade = true;
                }
            }
        }

        boolean valid = !hasStorageUpgrade || hasUsableUpgrade;
        boolean changed = highestTier != upgradeTier || hasVoidUpgrade != voidUpgrade || valid != upgradesValid;
        upgradeTier = highestTier;
        voidUpgrade = hasVoidUpgrade;
        upgradesValid = valid;

        long capacity = getCapacity();
        if (storedAmount > capacity) {
            storedAmount = capacity;
            if (storedAmount <= 0) {
                stored = ItemStack.EMPTY;
            }
            changed = true;
        }

        if (changed) {
            markDirty();
        }
    }

    private void damageUpgrades() {
        for (NotifiableItemStackHandler inventory : inputBuses) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem upgrade && !upgrade.isPermanent() &&
                        stack.isDamageableItem()) {
                    int damage = stack.getDamageValue() + 1;
                    if (damage >= stack.getMaxDamage()) {
                        inventory.setStackInSlot(slot, ItemStack.EMPTY);
                    } else {
                        ItemStack damaged = stack.copy();
                        damaged.setDamageValue(damage);
                        inventory.setStackInSlot(slot, damaged);
                    }
                }
            }
        }
    }

    private void pullFromInputs() {
        for (NotifiableItemStackHandler inventory : inputBuses) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty() || isUpgrade(stack)) {
                    continue;
                }

                if (voidUpgrade && storedAmount >= getCapacity() && !hasOutputRoom()) {
                    inventory.setStackInSlot(slot, ItemStack.EMPTY);
                    continue;
                }

                ItemStack remainder = insertStored(stack, false);
                if (remainder.getCount() != stack.getCount()) {
                    inventory.setStackInSlot(slot, remainder);
                }
            }
        }
    }

    private void pushToOutputs() {
        if (stored.isEmpty() || storedAmount <= 0) {
            return;
        }

        int amount = (int) Math.min(storedAmount, stored.getMaxStackSize());
        for (NotifiableItemStackHandler inventory : outputBuses) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack remainder = inventory.storage.insertItem(slot, stored.copyWithCount(amount), false);
                int inserted = amount - remainder.getCount();
                if (inserted > 0) {
                    storedAmount -= inserted;
                    if (storedAmount <= 0) {
                        stored = ItemStack.EMPTY;
                        storedAmount = 0;
                    }
                    markDirty();
                    return;
                }
            }
        }
    }

    private ItemStack insertStored(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !upgradesValid || isUpgrade(stack)) {
            return stack;
        }
        if (!stored.isEmpty() && !GTUtil.isSameItemSameTags(stored, stack)) {
            return stack;
        }

        int toStore = (int) Math.min(stack.getCount(), getCapacity() - storedAmount);
        if (toStore <= 0) {
            return stack;
        }

        if (!simulate) {
            if (stored.isEmpty()) {
                stored = stack.copyWithCount(1);
            }
            storedAmount += toStore;
            markDirty();
        }

        return stack.copyWithCount(stack.getCount() - toStore);
    }

    private boolean hasOutputRoom() {
        if (stored.isEmpty()) {
            return true;
        }
        for (NotifiableItemStackHandler inventory : outputBuses) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (inventory.storage.insertItem(slot, stored.copyWithCount(1), true).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isUpgrade(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem || stack.getItem() instanceof VoidUpgradeItem;
    }

    public long getCapacity() {
        int tier = Math.max(0, Math.min(TIER_CAPACITY.length - 1, upgradeTier));
        return TIER_CAPACITY[tier];
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .addCustom(list -> {
                    if (!upgradesValid) {
                        list.add(Component
                                .translatable("scalablestorageceu.machine.scalable_storage.upgrade_depleted")
                                .withStyle(ChatFormatting.RED));
                    } else if (upgradeTier > 0) {
                        list.add(Component.translatable("scalablestorageceu.machine.scalable_storage.tier",
                                upgradeTier).withStyle(ChatFormatting.GREEN));
                    }

                    if (voidUpgrade) {
                        list.add(Component.translatable("scalablestorageceu.machine.scalable_storage.voiding")
                                .withStyle(ChatFormatting.DARK_RED));
                    }

                    list.add(Component.empty());

                    if (stored.isEmpty() || storedAmount <= 0) {
                        list.add(Component.translatable("scalablestorageceu.machine.scalable_storage.empty")
                                .withStyle(ChatFormatting.GRAY));
                    } else {
                        list.add(Component.translatable("scalablestorageceu.machine.scalable_storage.stored",
                                stored.getHoverName()).withStyle(ChatFormatting.GRAY));
                        list.add(Component.translatable("scalablestorageceu.machine.scalable_storage.amount",
                                FormattingUtil.formatNumbers(storedAmount),
                                FormattingUtil.formatNumbers(getCapacity())).withStyle(ChatFormatting.WHITE));
                    }
                });

        IDisplayUIMachine.super.addDisplayText(textList);
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(getScreenTexture())
                .addWidget(new ComponentPanelWidget(4, 5, this::addDisplayText)
                        .textSupplier(getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(200)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 208, this, player).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(Objects::nonNull).map(IFancyUIProvider.class::cast).toList();
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public boolean saveBreak() {
        return !stored.isEmpty();
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.put("stored", stored.serializeNBT());
        tag.putLong("storedAmount", storedAmount);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.contains("stored")) {
            stored = ItemStack.of(tag.getCompound("stored"));
        }
        storedAmount = tag.getLong("storedAmount");
    }
}
