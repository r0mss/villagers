package com.r0mss.villagers.network;

import com.r0mss.villagers.Config;
import com.r0mss.villagers.VillagersMod;
import com.r0mss.villagers.block.InfoBoardBlockEntity;
import com.r0mss.villagers.client.InfoBoardScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro de los paquetes del tablon de informacion, y la logica para:
 * - Calcular la info del asentamiento y mandarla al cliente para que abra
 *   la pantalla (servidor -> cliente).
 * - Guardar el nuevo nombre que el jugador escribio (cliente -> servidor).
 */
public final class InfoBoardNetworking {

    // Margen de seguridad para validar que el jugador siga cerca del tablon al guardar
    private static final double RENAME_MAX_DISTANCE_SQ = 16.0 * 16.0;

    private InfoBoardNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(OpenInfoBoardPacket.TYPE, OpenInfoBoardPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> openScreenOnClient(payload)));

        registrar.playToServer(RenameInfoBoardPacket.TYPE, RenameInfoBoardPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> handleRename(payload, (ServerPlayer) context.player())));
    }

    /**
     * Escanea los aldeanos cercanos, arma las lineas de informacion, y le
     * manda al jugador el paquete para que abra la pantalla.
     */
    public static void openFor(ServerPlayer player, InfoBoardBlockEntity board, BlockPos pos) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double radius = Config.INFO_BOARD_RADIUS.get();
        List<Villager> nearby = serverLevel.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(radius));

        int total = nearby.size();
        Map<String, Integer> professionCounts = new LinkedHashMap<>();
        for (Villager villager : nearby) {
            String prettyName = prettyProfessionName(villager.getVillagerData().getProfession().name());
            professionCounts.merge(prettyName, 1, Integer::sum);
        }

        String settlementType;
        if (total < 5) {
            settlementType = "Settlement";
        } else if (total <= 15) {
            settlementType = "Village";
        } else {
            settlementType = "City";
        }

        List<String> lines = new ArrayList<>();
        lines.add("Type: " + settlementType);
        lines.add("Population: " + total + " villagers");
        lines.add("");
        if (professionCounts.isEmpty()) {
            lines.add("Workers: none");
        } else {
            lines.add("Workers:");
            professionCounts.forEach((name, count) -> lines.add("- " + name + " x" + count));
        }

        PacketDistributor.sendToPlayer(player, new OpenInfoBoardPacket(pos, board.getSettlementName(), lines));

        VillagersMod.LOGGER.info(
                "[villagers] [debug] Tablon abierto para {} en {} | nombreActual='{}' lineas={} totalAldeanos={}",
                player.getGameProfile().getName(), pos, board.getSettlementName(), lines, total
        );
    }

    private static void handleRename(RenameInfoBoardPacket packet, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            VillagersMod.LOGGER.info("[villagers] [debug] Rename ignorado: el jugador no esta en un ServerLevel");
            return;
        }

        double distSq = player.distanceToSqr(Vec3.atCenterOf(packet.pos()));
        if (distSq > RENAME_MAX_DISTANCE_SQ) {
            VillagersMod.LOGGER.info(
                    "[villagers] [debug] Rename ignorado por distancia: jugador={} pos={} distSq={} limiteSq={}",
                    player.getGameProfile().getName(), packet.pos(), distSq, RENAME_MAX_DISTANCE_SQ
            );
            return;
        }

        var blockEntity = serverLevel.getBlockEntity(packet.pos());
        if (blockEntity instanceof InfoBoardBlockEntity board) {
            String name = packet.newName();
            if (name.length() > 48) {
                name = name.substring(0, 48);
            }
            board.setSettlementName(name);
            VillagersMod.LOGGER.info(
                    "[villagers] [debug] Rename aplicado en {}: nuevoNombre='{}' confirmado='{}'",
                    packet.pos(), name, board.getSettlementName()
            );
        } else {
            VillagersMod.LOGGER.info(
                    "[villagers] [debug] Rename ignorado: no hay InfoBoardBlockEntity en {} (encontrado: {})",
                    packet.pos(), blockEntity
            );
        }
    }

    private static void openScreenOnClient(OpenInfoBoardPacket packet) {
        VillagersMod.LOGGER.info(
                "[villagers] [debug] Cliente recibio OpenInfoBoardPacket: pos={} nombre='{}' lineas={}",
                packet.pos(), packet.currentName(), packet.infoLines()
        );
        Minecraft.getInstance().setScreen(new InfoBoardScreen(packet.pos(), packet.currentName(), packet.infoLines()));
    }

    private static String prettyProfessionName(String professionId) {
        return switch (professionId) {
            case "none" -> "Unemployed";
            case "nitwit" -> "Nitwit";
            case "land_guardian" -> "Land Guardian";
            case "town_crier" -> "Town Crier";
            case "campanero" -> "Bell Keeper";
            default -> capitalize(professionId);
        };
    }

    private static String capitalize(String id) {
        String spaced = id.replace('_', ' ');
        if (spaced.isEmpty()) {
            return spaced;
        }
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
