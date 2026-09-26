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
 */
public class InfoBoardScreen extends Screen {

    private static final int BOX_WIDTH = 220;

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
        int top = this.height / 2 - 70;

        this.nameBox = new EditBox(this.font, centerX - (BOX_WIDTH / 2), top + 20, BOX_WIDTH, 20,
                Component.literal("Settlement name"));
        this.nameBox.setMaxLength(48);
        this.nameBox.setValue(this.initialName);
        this.addRenderableWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
                    PacketDistributor.sendToServer(new RenameInfoBoardPacket(this.boardPos, this.nameBox.getValue()));
                    this.onClose();
                })
                .bounds(centerX - 100, top + 50 + (this.infoLines.size() * 10) + 10, 95, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(centerX + 5, top + 50 + (this.infoLines.size() * 10) + 10, 95, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int top = this.height / 2 - 70;

        guiGraphics.drawCenteredString(this.font, "Settlement name:", centerX, top, 0xFFFFFF);

        int lineY = top + 45;
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
