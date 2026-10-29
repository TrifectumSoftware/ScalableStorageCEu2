package trifectumsoftware.scalablestorageceu;

import com.tterrag.registrate.util.entry.ItemEntry;
import trifectumsoftware.scalablestorageceu.item.StorageUpgradeItem;
import trifectumsoftware.scalablestorageceu.item.VoidUpgradeItem;

public final class SSItems {

    public static final ItemEntry<StorageUpgradeItem> STORAGE_UPGRADE_TIER_1;
    public static final ItemEntry<StorageUpgradeItem> STORAGE_UPGRADE_TIER_2;
    public static final ItemEntry<StorageUpgradeItem> STORAGE_UPGRADE_TIER_3;
    public static final ItemEntry<VoidUpgradeItem> VOID_UPGRADE;

    static {
        ScalableStorageCEu.REGISTRATE.creativeModeTab(() -> SSCreativeTabs.SCALABLE_STORAGE);

        STORAGE_UPGRADE_TIER_1 = registerStorageUpgrade("storage_upgrade_tier_1", "Basic Storage Upgrade", 1, 10_000);
        STORAGE_UPGRADE_TIER_2 = registerStorageUpgrade("storage_upgrade_tier_2", "Advanced Storage Upgrade", 2,
                100_000);
        STORAGE_UPGRADE_TIER_3 = registerStorageUpgrade("storage_upgrade_tier_3", "Elite Storage Upgrade", 3, 0);

        VOID_UPGRADE = ScalableStorageCEu.REGISTRATE.item("void_upgrade", VoidUpgradeItem::new)
                .lang("Void Upgrade")
                .properties(properties -> properties.stacksTo(1))
                .register();
    }

    private SSItems() {}

    public static void init() {}

    private static ItemEntry<StorageUpgradeItem> registerStorageUpgrade(String name, String lang, int tier,
                                                                        int durability) {
        return ScalableStorageCEu.REGISTRATE.item(name, properties -> new StorageUpgradeItem(properties, tier))
                .lang(lang)
                .properties(properties -> {
                    properties.stacksTo(1);
                    if (durability > 0) {
                        properties.durability(durability);
                    }
                    return properties;
                })
                .register();
    }
}
