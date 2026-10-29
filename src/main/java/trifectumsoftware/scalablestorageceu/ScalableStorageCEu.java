package trifectumsoftware.scalablestorageceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ScalableStorageCEu.MOD_ID)
public class ScalableStorageCEu {

    public static final String MOD_ID = "scalablestorageceu";
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(MOD_ID);

    public ScalableStorageCEu() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SSCreativeTabs.init();
        SSItems.init();
        modBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        REGISTRATE.registerRegistrate();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        SSMachines.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
