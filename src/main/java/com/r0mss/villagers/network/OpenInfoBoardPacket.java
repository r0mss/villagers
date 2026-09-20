package com.r0mss.villagers.network;

import com.r0mss.villagers.VillagersMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Enviado del servidor al cliente cuando un jugador hace clic derecho en un
 * tablon de informacion: trae la posicion, el nombre actual del asentamiento,
 * y las lineas de informacion ya calculadas (poblacion, tipo, trabajadores).
 */
public record OpenInfoBoardPacket(BlockPos pos, String currentName, List<String> infoLines) implements CustomPacketPayload {

    public static final Type<OpenInfoBoardPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagersMod.MODID, "open_info_board"));

    public static final StreamCodec<ByteBuf, OpenInfoBoardPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenInfoBoardPacket::pos,
            ByteBufCodecs.STRING_UTF8, OpenInfoBoardPacket::currentName,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenInfoBoardPacket::infoLines,
            OpenInfoBoardPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
