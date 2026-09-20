package com.r0mss.villagers.network;

import com.r0mss.villagers.VillagersMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Enviado del cliente al servidor cuando el jugador guarda un nuevo nombre
 * en la pantalla del tablon de informacion.
 */
public record RenameInfoBoardPacket(BlockPos pos, String newName) implements CustomPacketPayload {

    public static final Type<RenameInfoBoardPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagersMod.MODID, "rename_info_board"));

    public static final StreamCodec<ByteBuf, RenameInfoBoardPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RenameInfoBoardPacket::pos,
            ByteBufCodecs.STRING_UTF8, RenameInfoBoardPacket::newName,
            RenameInfoBoardPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
