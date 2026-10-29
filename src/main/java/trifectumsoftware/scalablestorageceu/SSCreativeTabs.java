package trifectumsoftware.scalablestorageceu;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.RegistryEntry;

public final class SSCreativeTabs {

    public static final RegistryEntry<CreativeModeTab> SCALABLE_STORAGE = ScalableStorageCEu.REGISTRATE
            .defaultCreativeTab("scalable_storage", builder -> builder
                    .title(Component.translatable("itemGroup.scalablestorageceu"))
                    .icon(() -> SSItems.STORAGE_UPGRADE_TIER_3.asStack())
                    .displayItems(SSCreativeTabs::addItems))
            .register();

    private SSCreativeTabs() {}

    public static void init() {}

    private static void addItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        var tab = SCALABLE_STORAGE;
        for (var entry : ScalableStorageCEu.REGISTRATE.<Item>getAll(Registries.ITEM)) {
            if (ScalableStorageCEu.REGISTRATE.isInCreativeTab(entry, tab) &&
                    !(entry.get() instanceof BlockItem)) {
                output.accept(entry.get());
            }
        }
        for (var entry : ScalableStorageCEu.REGISTRATE.<Block>getAll(Registries.BLOCK)) {
            if (ScalableStorageCEu.REGISTRATE.isInCreativeTab(entry, tab)) {
                Item item = entry.get().asItem();
                if (item != Items.AIR) {
                    output.accept(item);
                }
            }
        }
    }
}
