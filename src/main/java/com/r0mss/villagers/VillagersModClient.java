package com.r0mss.villagers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Registra la pantalla de configuracion nativa de NeoForge para que
 * aparezca el boton "Config" en el menu de mods, sin necesitar mods
 * externos como Cloth Config.
 */
@Mod(value = VillagersMod.MODID, dist = Dist.CLIENT)
public class VillagersModClient {

    public VillagersModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
