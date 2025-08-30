package net.mat0u5.lifeseries.features;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.network.packets.ImagePayload;
import net.minecraft.client.MinecraftClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SnailSkinsClient {
    private static final String CMD = "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\n    \"layer0\": \"minecraft:item/golden_horse_armor\"\n  },\n  \"overrides\": [\n__REPLACE__\n  ]\n}";
    private static final String ITEMS_ENTRY = "{\"model\":{\"model\":\"snailtextures:item/snail/__REPLACE__\",\"tints\":[{\"default\":16777215,\"type\":\"minecraft:dye\"}],\"type\":\"minecraft:model\"}}";
    private static final String BODY_1 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[9.0,13.0,12.0],\"to\":[10.0,16.0,13.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.0,12.5,3.5,12.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[3.0,12.0,3.5,13.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[2.5,12.0,3.0,13.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,2.5,12.5,4.0]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.5,12.0,4.0,12.5]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[2.0,12.0,2.5,13.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[10.0,13.0,13.0]}},{\"from\":[6.0,8.0,11.0],\"to\":[10.0,13.0,13.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,10.0,10.0,9.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[10.0,6.5,11.0,9.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[10.0,4.0,11.0,6.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[8.0,8.0,10.0,10.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,10.0,10.0,11.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[6.0,8.0,8.0,10.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[7.0,8.0,12.0]}},{\"from\":[6.0,8.0,9.0],\"to\":[10.0,10.0,11.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,5.0,11.0,4.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[9.0,11.5,10.0,12.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[8.0,11.5,9.0,12.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[8.0,10.5,10.0,11.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,5.0,11.0,6.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[6.0,10.5,8.0,11.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[7.0,8.0,10.0]}},{\"from\":[6.0,13.0,12.0],\"to\":[7.0,16.0,13.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.0,12.5,3.5,12.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[3.0,12.0,3.5,13.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[2.5,12.0,3.0,13.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,2.5,12.5,4.0]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.5,12.0,4.0,12.5]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[2.0,12.0,2.5,13.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[7.0,13.0,13.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_2 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[4.0,9.0,4.0],\"to\":[12.0,17.0,12.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.0,12.0,0.0,8.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.0,4.0,8.0,8.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,4.0,4.0,8.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[4.0,0.0,8.0,4.0]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,0.0,8.0,4.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,4.0,4.0]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[6.0,9.5,5.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_3 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[6.0,8.0,12.0],\"to\":[10.0,10.0,14.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,12.0,10.0,11.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,1.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,0.5,13.0,1.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[11.0,8.0,13.0,9.0]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[8.0,11.5,6.0,12.5]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[11.0,7.0,13.0,8.0]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[7.0,8.0,13.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_4 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[6.01,8.01,3.5],\"to\":[9.99,9.99,11.5],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[6.0,12.0,4.0,8.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[11.0,6.0,15.0,7.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[11.0,6.0,15.0,7.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,0.0,14.0,0.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[10.0,4.0,8.0,8.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,12.0,2.0,12.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[8.0,8.5,5.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_5 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[6.0,8.0,8.0],\"to\":[10.0,10.0,12.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[6.0,10.0,4.0,8.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,6.0,15.0,7.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,6.0,15.0,7.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.0,0.0,14.0,0.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[10.0,4.0,8.0,6.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,12.0,2.0,12.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[8.0,8.5,9.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_6 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[7.5,18.0,7.5],\"to\":[8.5,20.0,8.5],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.5,2.5,13.0,3.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.5,2.5,13.0,3.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.5,2.5,13.0,3.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.5,2.5,13.0,3.0]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.5,2.5,13.0,3.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[12.5,2.5,13.0,3.0]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[8.5,18.0,8.5]}},{\"from\":[6.0,17.0,6.0],\"to\":[10.0,18.0,10.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,11.0,12.0,9.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,10.5,16.0,11.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,9.5,16.0,10.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,10.0,16.0,10.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,9.0,12.0,11.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,9.0,16.0,9.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[7.0,17.0,7.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_7 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[5.0,20.01,5.0],\"to\":[11.0,20.01,11.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[16.0,6.0,13.0,3.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,3.0,0.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,3.0,0.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,3.0,0.0]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[16.0,3.0,13.0,6.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,3.0,0.0]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[5.0,20.01,5.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_8 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[0.0,21.0,4.0],\"to\":[16.0,21.6,20.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,11.0,12.0,9.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,10.5,16.0,11.0]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,9.5,16.0,10.0]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,10.0,16.0,10.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,9.0,12.0,11.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.0,9.0,16.0,9.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[1.0,21.0,5.0]}},{\"from\":[0.010000229,20.5,4.01],\"to\":[15.99,21.0,4.51],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[1.0,20.5,5.0]}},{\"from\":[0.010000229,20.5,19.49],\"to\":[15.99,21.0,19.99],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[1.0,20.5,20.5]}},{\"from\":[0.0,20.5,4.0],\"to\":[0.5,21.0,20.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[1.0,20.5,5.0]}},{\"from\":[15.5,20.5,4.0],\"to\":[16.0,21.0,20.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,14.75,0.25]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[14.5,0.0,15.5,0.25]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[16.5,20.5,5.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";
    private static final String BODY_9 = "{\"textures\":{\"0\":\"snailtextures:item/snail/texture__REPLACE__\",\"particle\":\"snailtextures:item/snail/texture__REPLACE__\"},\"elements\":[{\"from\":[6.0,17.0,15.0],\"to\":[7.0,21.0,15.1],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[6.0,17.0,15.0]}},{\"from\":[9.0,17.0,15.0],\"to\":[10.0,21.0,15.1],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[9.0,17.0,15.0]}},{\"from\":[9.0,17.0,9.0],\"to\":[10.0,21.0,9.1],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[9.0,17.0,9.0]}},{\"from\":[6.0,17.0,9.0],\"to\":[7.0,21.0,9.1],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[6.0,17.0,9.0]}},{\"from\":[5.0,17.0,13.0],\"to\":[5.1,21.0,14.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[5.0,17.0,13.0]}},{\"from\":[5.0,17.0,10.0],\"to\":[5.1,21.0,11.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[5.0,17.0,10.0]}},{\"from\":[11.0,17.0,10.0],\"to\":[11.1,21.0,11.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[11.0,17.0,10.0]}},{\"from\":[11.0,17.0,13.0],\"to\":[11.1,21.0,14.0],\"faces\":{\"up\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"west\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"east\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.0,2.5]},\"south\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]},\"down\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[0.0,0.0,0.5,0.0]},\"north\":{\"texture\":\"#0\",\"tintindex\":0,\"uv\":[13.0,0.5,13.5,2.5]}},\"rotation\":{\"axis\":\"y\",\"angle\":0.0,\"origin\":[11.0,17.0,13.0]}}],\"display\":{\"head\":{\"rotation\":[0.0,180.0,0.0]}}}";

    public static int skinReloadTicks = 0;
    public static void handleSnailSkin(ImagePayload payload) {
        int index = payload.index();
        int maxIndex = payload.maxIndex();

        String imageName = "texture"+index;
        byte[] imageBytes = payload.bytes();

        Main.LOGGER.info("Added dynamic image: " + imageName);
        MinecraftClient client = MinecraftClient.getInstance();
        skinReloadTicks = 30;
        client.execute(() -> addImage(imageName, imageBytes, index, maxIndex));
    }

    public static final String PACK_NAME = "[Life Series Mod] Snail Textures";
    private static Path resourcePackPath;
    private static boolean packInitialized = false;

    public static void initialize() {
        if (packInitialized) return;

        try {
            File resourcePacksFolder = new File(MinecraftClient.getInstance().runDirectory, "resourcepacks");
            resourcePackPath = resourcePacksFolder.toPath().resolve(PACK_NAME);

            Path assetsDir = resourcePackPath.resolve("assets").resolve("snailtextures");
            Path itemsDir = assetsDir.resolve("items");
            Path modelsDir = assetsDir.resolve("models").resolve("item").resolve("snail");
            Path texturesDir = assetsDir.resolve("textures").resolve("item").resolve("snail");
            Path cmdDir = resourcePackPath.resolve("assets").resolve("minecraft").resolve("models").resolve("item");

            Files.createDirectories(assetsDir);
            Files.createDirectories(itemsDir);
            Files.createDirectories(modelsDir);
            Files.createDirectories(texturesDir);
            Files.createDirectories(cmdDir);

            Files.writeString(cmdDir.resolve("golden_horse_armor.json"), CMD);

            Path packMcmetaPath = resourcePackPath.resolve("pack.mcmeta");
            String packMcmetaContent = "{\"pack\":{\"description\":\"Life Series Snails\",\"pack_format\":34}}";
            Files.writeString(packMcmetaPath, packMcmetaContent);

            packInitialized = true;

            Main.LOGGER.info("Initialized dynamic resource pack at: " + resourcePackPath);
        } catch (IOException e) {
            e.printStackTrace();
            Main.LOGGER.info("Failed to initialize dynamic resource pack: " + e.getMessage());
        }
    }

    public static CompletableFuture<Void> addImage(String imageName, byte[] imageData, int index, int maxIndex) {
        if (!packInitialized) {
            initialize();
        }

        return CompletableFuture.runAsync(() -> {
            try {
                // Convert byte array to image
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
                if (image == null) {
                    throw new IOException("Failed to decode image data");
                }

                File resourcePacksFolder = new File(MinecraftClient.getInstance().runDirectory, "resourcepacks");
                resourcePackPath = resourcePacksFolder.toPath().resolve(PACK_NAME);
                Path texturesDir = resourcePackPath.resolve("assets").resolve("snailtextures").resolve("textures").resolve("item").resolve("snail");
                String textureName = imageName + ".png";
                Path targetPath = texturesDir.resolve(textureName);

                // Create parent directories
                Files.createDirectories(targetPath.getParent());

                // Write the image
                ImageIO.write(image, "PNG", targetPath.toFile());

                Main.LOGGER.info("Added image to resource pack: " + targetPath);

                int modelDataStart = 10000 + index*10;

                //Add the items files
                Path assetsDir = resourcePackPath.resolve("assets").resolve("snailtextures");
                Path itemsDir = assetsDir.resolve("items");
                Files.createDirectories(itemsDir);
                Files.writeString(itemsDir.resolve("body1_"+(modelDataStart+1)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body1_"+(modelDataStart+1)));
                Files.writeString(itemsDir.resolve("body2_"+(modelDataStart+2)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body2_"+(modelDataStart+2)));
                Files.writeString(itemsDir.resolve("body3_"+(modelDataStart+3)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body3_"+(modelDataStart+3)));
                Files.writeString(itemsDir.resolve("body4_"+(modelDataStart+4)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body4_"+(modelDataStart+4)));
                Files.writeString(itemsDir.resolve("body5_"+(modelDataStart+5)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body5_"+(modelDataStart+5)));
                Files.writeString(itemsDir.resolve("body6_"+(modelDataStart+6)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body6_"+(modelDataStart+6)));
                Files.writeString(itemsDir.resolve("body7_"+(modelDataStart+7)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body7_"+(modelDataStart+7)));
                Files.writeString(itemsDir.resolve("body8_"+(modelDataStart+8)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body8_"+(modelDataStart+8)));
                Files.writeString(itemsDir.resolve("body9_"+(modelDataStart+9)+".json"), ITEMS_ENTRY.replaceAll("__REPLACE__", "body9_"+(modelDataStart+9)));

                //Add the model file
                Path modelsDir = assetsDir.resolve("models").resolve("item").resolve("snail");
                Files.createDirectories(modelsDir);
                Files.writeString(modelsDir.resolve("body1_"+(modelDataStart+1)+".json"), BODY_1.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body2_"+(modelDataStart+2)+".json"), BODY_2.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body3_"+(modelDataStart+3)+".json"), BODY_3.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body4_"+(modelDataStart+4)+".json"), BODY_4.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body5_"+(modelDataStart+5)+".json"), BODY_5.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body6_"+(modelDataStart+6)+".json"), BODY_6.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body7_"+(modelDataStart+7)+".json"), BODY_7.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body8_"+(modelDataStart+8)+".json"), BODY_8.replaceAll("__REPLACE__", String.valueOf(index)));
                Files.writeString(modelsDir.resolve("body9_"+(modelDataStart+9)+".json"), BODY_9.replaceAll("__REPLACE__", String.valueOf(index)));

                //Add the custom model data file
                Path cmdDir = resourcePackPath.resolve("assets").resolve("minecraft").resolve("models").resolve("item");
                Files.createDirectories(cmdDir);
                List<String> replaceCMD = new ArrayList<>();
                for (int i = 0; i <= maxIndex; i++) {
                    for (int y = 1; y < 10; y++) {
                        int newModelData = 10000 + i*10 + y;
                        replaceCMD.add("\t{\"model\": \"snailtextures:item/snail/body"+y+"_"+newModelData+"\",\"predicate\": {\"custom_model_data\": "+newModelData + "}}");
                    }
                }
                Files.writeString(cmdDir.resolve("golden_horse_armor.json"), CMD.replaceAll("__REPLACE__", String.join(",\n",replaceCMD)));



            } catch (IOException e) {
                e.printStackTrace();
                Main.LOGGER.info("Failed to add image to resource pack: " + e.getMessage());
            }
        });
    }
}
