package trifectumsoftware.scalablestorageceu.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageUpgradeItem extends Item {

    private final int tier;

    public StorageUpgradeItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    public boolean isPermanent() {
        return tier >= 3;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.scalablestorageceu.storage_upgrade." + tier)
                .withStyle(ChatFormatting.GRAY));

        if (!isPermanent() && stack.isDamageableItem()) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            String percent = String.format(java.util.Locale.ROOT, "%.1f", remaining * 100.0 / stack.getMaxDamage());
            tooltip.add(Component.translatable("tooltip.scalablestorageceu.storage_upgrade.durability",
                    percent, formatDuration(remaining * 10)).withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable("tooltip.scalablestorageceu.upgrade.usage")
                .withStyle(ChatFormatting.GRAY));
    }

    private static String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }
}
