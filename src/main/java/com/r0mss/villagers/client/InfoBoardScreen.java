package com.r0mss.villagers.client;

import com.r0mss.villagers.network.RenameInfoBoardPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Pantalla que se abre al hacer clic derecho en el tablon de informacion.
 * Muestra un campo editable para el nombre del asentamiento y, debajo, la
 * informacion calculada por el servidor (tipo de lugar, poblacion, trabajos).
 * <p>
 * El diseño usa posiciones FIJAS para el campo de nombre y los botones
 * (no dependen de cuanta informacion haya), para que siempre queden en un
 * lugar predecible y clickeable.
 */
public class InfoBoardScreen extends Screen {

    private static final int BOX_WIDTH = 240;

    private final BlockPos boardPos;
    private final String initialName;
    private final List<String> infoLines;

    private EditBox nameBox;

    public InfoBoardScreen(BlockPos boardPos, String initialName, List<String> infoLines) {
        super(Component.literal("Settlement Info Board"));
        this.boardPos = boardPos;
        this.initialName = initialName;
        this.infoLines = infoLines;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = 40;

        this.nameBox = new EditBox(this.font, centerX - (BOX_WIDTH / 2), top + 35, BOX_WIDTH, 20,
                Component.literal("Settlement name"));
        this.nameBox.setMaxLength(48);
        this.nameBox.setValue(this.initialName);
        this.addRenderableWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);

        int buttonY = top + 65;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
                .bounds(centerX - 100, buttonY, 95, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(centerX + 5, buttonY, 95, 20)
                .build());
    }

    private void save() {
        PacketDistributor.sendToServer(new RenameInfoBoardPacket(this.boardPos, this.nameBox.getValue()));
        this.minecraft.player.displayClientMessage(
                Component.literal("[Villagers] Save request sent for name: '" + this.nameBox.getValue() + "'"),
                false
        );
        this.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter en el campo de texto tambien guarda, ademas del boton
        if ((keyCode == 257 || keyCode == 335) && this.nameBox.isFocused()) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int top = 40;

        guiGraphics.drawCenteredString(this.font, this.title, centerX, top, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Settlement name:", centerX, top + 22, 0xAAAAAA);

        int infoTop = top + 100;
        guiGraphics.drawCenteredString(this.font, "--- Settlement info ---", centerX, infoTop, 0xFFD700);

        int lineY = infoTop + 15;
        for (String line : this.infoLines) {
            guiGraphics.drawString(this.font, line, centerX - (BOX_WIDTH / 2), lineY, 0xCCCCCC, false);
            lineY += 10;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
