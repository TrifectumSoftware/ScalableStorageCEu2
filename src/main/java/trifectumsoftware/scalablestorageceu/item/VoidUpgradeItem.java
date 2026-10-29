package trifectumsoftware.scalablestorageceu.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidUpgradeItem extends Item {

    public VoidUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.scalablestorageceu.void_upgrade")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.scalablestorageceu.upgrade.usage")
                .withStyle(ChatFormatting.GRAY));
    }
}
