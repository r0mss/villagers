package com.r0mss.villagers.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Muestra un anuncio del Pregonero: un texto flotante encima de su cabeza
 * (entidad text_display vanilla) y el mismo mensaje en el chat de los
 * jugadores cercanos.
 * <p>
 * No usamos ningun paquete de red propio ni mixins: la entidad text_display
 * es completamente vanilla, y sus propiedades (billboard, color de fondo,
 * texto, etc) se configuran mediante NBT publico via {@link net.minecraft.world.entity.Entity#load(CompoundTag)},
 * exactamente igual que lo haria el comando /summon.
 */
public final class SpeechBubbles {

    private static final String TAG_EXPIRE_TICK = "villagers_bubble_expire";
    private static final int LIFESPAN_TICKS = 110; // ~5.5s
    private static final double CHAT_RANGE = 48.0;

    private SpeechBubbles() {
    }

    public static void announce(LivingEntity speaker, String message) {
        Level level = speaker.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnFloatingText(serverLevel, speaker, message);
        broadcastToNearbyChat(serverLevel, speaker, message);

        serverLevel.playSound(null, speaker.blockPosition(), SoundEvents.VILLAGER_YES,
                SoundSource.NEUTRAL, 1.0f, 0.8f);
    }

    private static void spawnFloatingText(ServerLevel serverLevel, LivingEntity speaker, String message) {
        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
        display.moveTo(speaker.getX(), speaker.getEyeY() + 0.55, speaker.getZ(), 0.0F, 0.0F);

        // Propiedades base de la entidad (setters publicos normales)
        display.setNoGravity(true);
        display.setSilent(true);
        display.setInvulnerable(true);

        // Propiedades propias de la entidad "display" (no tienen setter publico,
        // se aplican mediante NBT publico, igual que /summon)
        CompoundTag tag = new CompoundTag();
        tag.putString("text", toJsonText(message));
        tag.putString("billboard", "center");
        tag.putFloat("view_range", 16.0f);
        tag.putFloat("shadow_radius", 0.0f);
        tag.putFloat("shadow_strength", 0.0f);
        tag.putShort("line_width", (short) 200);
        tag.putInt("background", 0);            // fondo totalmente transparente
        tag.putByte("text_opacity", (byte) -1);  // texto totalmente opaco
        tag.putBoolean("see_through", false);
        tag.putBoolean("default_background", false);
        tag.putBoolean("shadow", true);
        display.load(tag);

        serverLevel.addFreshEntity(display);
        display.getPersistentData().putLong(TAG_EXPIRE_TICK, serverLevel.getGameTime() + LIFESPAN_TICKS);
    }

    private static void broadcastToNearbyChat(ServerLevel serverLevel, LivingEntity speaker, String message) {
        // Formato identico al de un mensaje de jugador normal: "<Nombre> mensaje",
        // sin color ni negrita especial, para que no se vea como un mensaje de sistema.
        Component chatMessage = Component.literal("<Pregonero> " + message);

        double rangeSq = CHAT_RANGE * CHAT_RANGE;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(speaker) <= rangeSq) {
                player.sendSystemMessage(chatMessage);
            }
        }
    }

    /**
     * Debe llamarse en el tick de cualquier entidad text_display creada por
     * este mod, para eliminarla cuando expire su tiempo de vida.
     */
    public static void tickDisplay(Display.TextDisplay display) {
        CompoundTag data = display.getPersistentData();
        if (!data.contains(TAG_EXPIRE_TICK)) {
            return;
        }
        long expireAt = data.getLong(TAG_EXPIRE_TICK);
        if (display.level().getGameTime() >= expireAt) {
            display.discard();
        }
    }

    private static String toJsonText(String message) {
        String escaped = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "{\"text\":\"" + escaped + "\",\"color\":\"gold\",\"bold\":true}";
    }
}
