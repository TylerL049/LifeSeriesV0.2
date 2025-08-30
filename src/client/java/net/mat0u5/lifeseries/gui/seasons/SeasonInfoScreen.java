package net.mat0u5.lifeseries.gui.seasons;

import net.mat0u5.lifeseries.gui.DefaultScreen;
import net.mat0u5.lifeseries.render.RenderUtils;
import net.mat0u5.lifeseries.seasons.season.Seasons;
import net.mat0u5.lifeseries.seasons.season.doublelife.DoubleLife;
import net.mat0u5.lifeseries.seasons.season.lastlife.LastLife;
import net.mat0u5.lifeseries.seasons.season.limitedlife.LimitedLife;
import net.mat0u5.lifeseries.seasons.season.secretlife.SecretLife;
import net.mat0u5.lifeseries.seasons.season.thirdlife.ThirdLife;
import net.mat0u5.lifeseries.seasons.season.wildlife.WildLife;
import net.mat0u5.lifeseries.utils.other.TextUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SeasonInfoScreen extends DefaultScreen {

    public static Seasons season;

    public SeasonInfoScreen(Seasons season) {
        super(Text.literal("Season Info Screen"), 1.3f, 1.3f);
        this.season = season;
    }

    public Identifier getSeasonLogo() {
        return Identifier.of("lifeseries","textures/gui/"+season.getId()+".png");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        // Background + images
        //? if <= 1.21 {
        Identifier logo = getSeasonLogo();
        if (logo != null) {
            RenderUtils.drawTextureScaled(context, logo, startX+10, endY - 64 - 5, 0, 0, 256, 256, 0.25f, 0.25f);
            RenderUtils.drawTextureScaled(context, logo, endX - 64 - 10, endY - 64 - 5, 0, 0, 256, 256, 0.25f, 0.25f);
        }
        //?} else {
        /*Identifier logo = getSeasonLogo();
        if (logo != null) {
            RenderUtils.drawTextureScaled(context, logo, startX+10, endY - 64 - 10, 0, 0, 256, 256, 256, 256, 0.25f, 0.25f);
            RenderUtils.drawTextureScaled(context, logo, endX - 64 - 10, endY - 64 - 10, 0, 0, 256, 256, 256, 256, 0.25f, 0.25f);
        }
        *///?}

        //? if <= 1.21.5 {
        context.getMatrices().push();
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        //?} else {
        /*context.getMatrices().pushMatrix();
        context.getMatrices().scale(1.5f, 1.5f);
        *///?}
        String seasonName = season.getName();
        RenderUtils.drawTextCenterScaled(context, this.textRenderer, Text.of("§0"+ seasonName), centerX, startY + 10, 1.5f, 1.5f);
        //? if <= 1.21.5 {
        context.getMatrices().pop();
        //?} else {
        /*context.getMatrices().popMatrix();
        *///?}

        int currentY = startY + 40;
        MutableText adminCommandsText = Text.literal("§8Available §nadmin§8 commands: ");
        MutableText adminCommandsTextActual = getSeasonAdminCommands();
        if (adminCommandsTextActual != null) {
            MutableText combined = adminCommandsText.copy().append(adminCommandsTextActual);
            if (textRenderer.getWidth(combined) < (endX - startX)) {
                RenderUtils.drawTextLeft(context, this.textRenderer, combined, startX + 20, currentY);
                currentY += textRenderer.fontHeight + 5;
            }
            else {
                RenderUtils.drawTextLeft(context, this.textRenderer, adminCommandsText, startX + 20, currentY);
                currentY += textRenderer.fontHeight + 5;
                RenderUtils.drawTextLeft(context, this.textRenderer, TextUtils.format("  {}", adminCommandsTextActual), startX + 20, currentY);
                currentY += textRenderer.fontHeight + 8;
            }
        }

        MutableText commandsText = Text.literal("§8Available §nnon-admin§8 commands: ");
        MutableText commandsTextActual = getSeasonCommands();
        if (commandsTextActual != null) {
            MutableText combined = commandsText.copy().append(commandsTextActual);
            if (textRenderer.getWidth(combined) < (endX - startX)) {
                RenderUtils.drawTextLeft(context, this.textRenderer, combined, startX + 20, currentY);
                currentY += textRenderer.fontHeight + 10;
            }
            else {
                RenderUtils.drawTextLeft(context, this.textRenderer, commandsText, startX + 20, currentY);
                currentY += textRenderer.fontHeight + 5;
                RenderUtils.drawTextLeft(context, this.textRenderer, TextUtils.format("  {}", commandsTextActual), startX + 20, currentY);
                currentY += textRenderer.fontHeight + 10;
            }
        }

        //? if <= 1.21.5 {
        context.getMatrices().push();
        context.getMatrices().scale(1.15f, 1.15f, 1.0f);
        //?} else {
        /*context.getMatrices().pushMatrix();
        context.getMatrices().scale(1.15f, 1.15f);
        *///?}
        Text howToStart = Text.of("§0§nHow to start a session");
        RenderUtils.drawTextLeftScaled(context, this.textRenderer, howToStart, startX + 20, currentY+3, 1.15f, 1.15f);
        currentY += textRenderer.fontHeight + 13;
        //? if <= 1.21.5 {
        context.getMatrices().pop();
        //?} else {
        /*context.getMatrices().popMatrix();
        *///?}

        Text sessionTimer = Text.of("§8Run §3'/session timer set <time>'§8 to set the desired session time.");
        RenderUtils.drawTextLeft(context, this.textRenderer, sessionTimer, startX + 20, currentY);
        currentY += textRenderer.fontHeight + 5;

        Text sessionStart = Text.of("§8After that, run §3'/session start'§8 to start the session.");
        RenderUtils.drawTextLeft(context, this.textRenderer, sessionStart, startX + 20, currentY);
        currentY += textRenderer.fontHeight + 15;

        Text configText = Text.of("§0§nRun §8§n'/lifeseries config'§0§n to open the Life Series configuration!");
        RenderUtils.drawTextLeft(context, this.textRenderer, configText, startX + 20, currentY);
        currentY += textRenderer.fontHeight + 5;
    }

    private static @Nullable MutableText getSeasonCommands() {
        MutableText commandsTextActual = null;
        if (season == Seasons.THIRD_LIFE || season == Seasons.SIMPLE_LIFE || season == Seasons.REAL_LIFE) commandsTextActual = Text.literal(ThirdLife.COMMANDS_TEXT);
        if (season == Seasons.LAST_LIFE) commandsTextActual = Text.literal(LastLife.COMMANDS_TEXT);
        if (season == Seasons.DOUBLE_LIFE) commandsTextActual = Text.literal(DoubleLife.COMMANDS_TEXT);
        if (season == Seasons.LIMITED_LIFE) commandsTextActual = Text.literal(LimitedLife.COMMANDS_TEXT);
        if (season == Seasons.SECRET_LIFE) commandsTextActual = Text.literal(SecretLife.COMMANDS_TEXT);
        if (season == Seasons.WILD_LIFE) commandsTextActual = Text.literal(WildLife.COMMANDS_TEXT);
        return commandsTextActual;
    }

    @Nullable
    private static MutableText getSeasonAdminCommands() {
        MutableText adminCommandsTextActual = null;
        if (season == Seasons.THIRD_LIFE || season == Seasons.SIMPLE_LIFE || season == Seasons.REAL_LIFE) adminCommandsTextActual = Text.literal(ThirdLife.COMMANDS_ADMIN_TEXT);
        if (season == Seasons.LAST_LIFE) adminCommandsTextActual = Text.literal(LastLife.COMMANDS_ADMIN_TEXT);
        if (season == Seasons.DOUBLE_LIFE) adminCommandsTextActual = Text.literal(DoubleLife.COMMANDS_ADMIN_TEXT);
        if (season == Seasons.LIMITED_LIFE) adminCommandsTextActual = Text.literal(LimitedLife.COMMANDS_ADMIN_TEXT);
        if (season == Seasons.SECRET_LIFE) adminCommandsTextActual = Text.literal(SecretLife.COMMANDS_ADMIN_TEXT);
        if (season == Seasons.WILD_LIFE) adminCommandsTextActual = Text.literal(WildLife.COMMANDS_ADMIN_TEXT);
        return adminCommandsTextActual;
    }
}