package net.mat0u5.lifeseries.dependencies;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.mat0u5.lifeseries.Main;

public class PolymerDependency {
    public static void onInitialize() {
        PolymerResourcePackUtils.addModAssets(Main.MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
    }
}
