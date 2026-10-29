package trifectumsoftware.scalablestorageceu;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

public final class SSRecipes {

    private SSRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        GTRecipeBuilder.of(ScalableStorageCEu.id("storage_upgrade_tier_1"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .circuitMeta(1)
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.RedAlloy, 16)
                .inputItems(TagPrefix.gear, GTMaterials.WroughtIron, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(SSItems.STORAGE_UPGRADE_TIER_1.asStack())
                .duration(200)
                .EUt(30)
                .save(provider);

        GTRecipeBuilder.of(ScalableStorageCEu.id("storage_upgrade_tier_2"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .circuitMeta(2)
                .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Aluminium, 16)
                .inputItems(TagPrefix.plate, GTMaterials.RoseGold, 4)
                .inputItems(TagPrefix.gear, GTMaterials.Steel, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(288))
                .outputItems(SSItems.STORAGE_UPGRADE_TIER_2.asStack())
                .duration(400)
                .EUt(120)
                .save(provider);

        GTRecipeBuilder.of(ScalableStorageCEu.id("storage_upgrade_tier_3"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .circuitMeta(3)
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Electrum, 8)
                .inputItems(TagPrefix.gear, GTMaterials.StainlessSteel, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(576))
                .outputItems(SSItems.STORAGE_UPGRADE_TIER_3.asStack())
                .duration(600)
                .EUt(480)
                .save(provider);

        GTRecipeBuilder.of(ScalableStorageCEu.id("void_upgrade"), GTRecipeTypes.ASSEMBLER_RECIPES)
                .circuitMeta(4)
                .inputItems(TagPrefix.plate, GTMaterials.Iron, 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Copper, 8)
                .inputItems(Items.GLASS_BOTTLE, 1)
                .inputItems(TagPrefix.gear, GTMaterials.Bronze, 1)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(SSItems.VOID_UPGRADE.asStack())
                .duration(300)
                .EUt(120)
                .save(provider);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SSMachines.SCALABLE_STORAGE.getItem())
                .pattern("PSP")
                .pattern("SCS")
                .pattern("PSP")
                .define('P', Ingredient.of(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Steel)))
                .define('S', Ingredient.of(ChemicalHelper.get(TagPrefix.gear, GTMaterials.Steel)))
                .define('C', Items.CHEST)
                .unlockedBy("has_chest", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CHEST))
                .save(provider, ScalableStorageCEu.id("scalable_storage_controller"));
    }
}
