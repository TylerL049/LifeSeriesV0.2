package net.mat0u5.lifeseries.dependencies;

import net.fabricmc.loader.api.FabricLoader;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.mat0u5.lifeseries.utils.player.PlayerUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DependencyManager {

    public static boolean blockbenchImportLibraryLoaded() {
        return isModLoaded("bil");
    }

    public static boolean polymerLoaded() {
        return isModLoaded("polymer-bundled");
    }

    public static boolean voicechatLoaded() {
        return isModLoaded("voicechat");
    }

    public static boolean wildLifeModsLoaded() {
        return blockbenchImportLibraryLoaded() && polymerLoaded();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean checkWildLifeDependencies() {
        if (!polymerLoaded()) {
            PlayerUtils.broadcastMessage(
                    Text.literal("§cYou must install the ").append(
                            Text.literal("Polymer mod")
                                    .styled(style -> style
                                            .withColor(Formatting.BLUE)
                                            .withClickEvent(TextUtils.openURLClickEvent("https://modrinth.com/mod/polymer"))
                                            .withUnderline(true)
                                    )
                    ).append(Text.of(" §cto play Wild Life."))
            );
        }
        if (!blockbenchImportLibraryLoaded()) {
            PlayerUtils.broadcastMessage(
                    Text.literal("§cYou must install the ").append(
                            Text.literal("Blockbench Import Library mod")
                                    .styled(style -> style
                                            .withColor(Formatting.BLUE)
                                            .withClickEvent(TextUtils.openURLClickEvent("https://modrinth.com/mod/blockbench-import-library"))
                                            .withUnderline(true)
                                    )
                    ).append(Text.of(" §cto play Wild Life."))
            );
        }
        return wildLifeModsLoaded();
    }
}
