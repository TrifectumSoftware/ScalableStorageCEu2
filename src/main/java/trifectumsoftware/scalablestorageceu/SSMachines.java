package trifectumsoftware.scalablestorageceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.network.chat.Component;

import trifectumsoftware.scalablestorageceu.machine.ScalableStorageMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;

public final class SSMachines {

    public static final MultiblockMachineDefinition SCALABLE_STORAGE = ScalableStorageCEu.REGISTRATE
            .multiblock("scalable_storage", ScalableStorageMachine::new)
            .langValue("Scalable Storage Silo")
            .tooltips(
                    Component.translatable("scalablestorageceu.machine.scalable_storage.tooltip.0"),
                    Component.translatable("scalablestorageceu.machine.scalable_storage.tooltip.1"))
            .rotationState(RotationState.ALL)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .partAppearance((controller, part, side) -> GTBlocks.CASING_STEEL_SOLID.get().defaultBlockState())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCC", "CCC", "CCC")
                    .aisle("CCC", "CCC", "CCC")
                    .aisle("CCC", "CSC", "CCC")
                    .where('S', controller(blocks(definition.get())))
                    .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .setMinGlobalLimited(20)
                            .or(abilities(PartAbility.IMPORT_ITEMS)
                                    .setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(4))
                            .or(abilities(PartAbility.EXPORT_ITEMS)
                                    .setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(4)))
                    .build())
            .model((context, provider, builder) -> {
                var casing = GTCEu.id("block/casings/solid/machine_casing_solid_steel");
                var model = provider.models().nested()
                        .parent(provider.models().getExistingFile(
                                ScalableStorageCEu.id("block/multiblock/scalable_storage")));
                model.texture("all", casing);
                builder.forAllStatesModels(state -> model);
                builder.addReplaceableTextures("all");
                builder.addTextureOverride("all", casing);
            })
            .register();

    private SSMachines() {}

    public static void init() {}
}
