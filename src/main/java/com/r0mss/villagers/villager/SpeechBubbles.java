package com.r0mss.villagers.villager;

import com.r0mss.villagers.VillagersMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Utilidades para mostrar texto flotante (entidad text_display vanilla)
 * y anuncios en el chat con formato de aldeano, reutilizadas por el
 * Pregonero y el Campanero.
 * <p>
 * Los mensajes se mandan como claves de traduccion (no texto ya resuelto),
 * tanto en el chat como en el texto flotante, para que cada jugador lo vea
 * en su propio idioma (igual que hace el juego con cualquier otro texto).
 * <p>
 * No usamos ningun paquete de red propio ni mixins: la entidad text_display
 * es completamente vanilla, y sus propiedades (billboard, color de fondo,
 * texto, etc) se configuran mediante NBT publico via {@link net.minecraft.world.entity.Entity#load(CompoundTag)},
 * exactamente igual que lo haria el comando /summon.
 */
public final class SpeechBubbles {

    private static final String TAG_EXPIRE_TICK = "villagers_bubble_expire";
    private static final int LIFESPAN_TICKS = 110; // ~5.5s
    private static final Object[] NO_ARGS = new Object[0];

    private SpeechBubbles() {
    }

    public static void announce(LivingEntity speaker, String translationKey, String speakerNameKey, double chatRange) {
        announce(speaker, translationKey, NO_ARGS, speakerNameKey, chatRange);
    }

    public static void announce(LivingEntity speaker, String translationKey, Object[] args, String speakerNameKey, double chatRange) {
        Level level = speaker.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnFloatingText(serverLevel, speaker, translationKey, args);
        broadcastToNearbyChat(serverLevel, speaker, translationKey, args, speakerNameKey, chatRange);

        serverLevel.playSound(null, speaker.blockPosition(), SoundEvents.VILLAGER_YES,
                SoundSource.NEUTRAL, 1.0f, 0.8f);
    }

    private static void spawnFloatingText(ServerLevel serverLevel, LivingEntity speaker, String translationKey, Object[] args) {
        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
        double x = speaker.getX();
        double y = speaker.getEyeY() + 0.55;
        double z = speaker.getZ();
        display.moveTo(x, y, z, 0.0F, 0.0F);

        display.setNoGravity(true);
        display.setSilent(true);
        display.setInvulnerable(true);
        display.load(buildDisplayTagTranslatable(translationKey, args, "gold", true));

        boolean added = serverLevel.addFreshEntity(display);
        display.getPersistentData().putLong(TAG_EXPIRE_TICK, serverLevel.getGameTime() + LIFESPAN_TICKS);

        VillagersMod.LOGGER.info(
                "[villagers] [debug] Burbuja de texto: hablante={} pos=({}, {}, {}) uuid={} addFreshEntity={} isAddedToLevel={}",
                speaker.getUUID(), x, y, z, display.getUUID(), added, display.isAddedToLevel()
        );
    }

    private static void broadcastToNearbyChat(ServerLevel serverLevel, LivingEntity speaker, String translationKey,
                                               Object[] args, String speakerNameKey, double chatRange) {
        // Formato identico al de un mensaje de jugador normal: "<Nombre> mensaje",
        // sin color ni negrita especial, para que no se vea como un mensaje de sistema.
        // Component.translatable se resuelve en el idioma de CADA cliente que lo recibe.
        MutableComponent chatMessage = Component.literal("<")
                .append(Component.translatable(speakerNameKey))
                .append("> ")
                .append(Component.translatable(translationKey, args));

        double rangeSq = chatRange * chatRange;
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

    /**
     * Construye el NBT publico (identico a lo que usaria /summon) para
     * configurar una entidad text_display con texto FIJO (no traducible),
     * usado por ejemplo para el reloj del Campanero (numeros, no requieren
     * traduccion).
     */
    public static CompoundTag buildDisplayTag(String message, String color, boolean bold) {
        CompoundTag tag = new CompoundTag();
        tag.putString("text", toJsonPlainText(message, color, bold));
        applyCommonDisplayProperties(tag);
        return tag;
    }

    /**
     * Igual que {@link #buildDisplayTag}, pero con un componente "translate"
     * en vez de texto plano, para que cada jugador vea el mensaje en su
     * propio idioma.
     */
    public static CompoundTag buildDisplayTagTranslatable(String translationKey, Object[] args, String color, boolean bold) {
        CompoundTag tag = new CompoundTag();
        tag.putString("text", toJsonTranslatable(translationKey, args, color, bold));
        applyCommonDisplayProperties(tag);
        return tag;
    }

    private static void applyCommonDisplayProperties(CompoundTag tag) {
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
    }

    private static String toJsonPlainText(String message, String color, boolean bold) {
        StringBuilder json = new StringBuilder();
        json.append("{\"text\":\"").append(escapeJson(message)).append("\",\"color\":\"").append(color).append("\"");
        if (bold) {
            json.append(",\"bold\":true");
        }
        json.append("}");
        return json.toString();
    }

    private static String toJsonTranslatable(String translationKey, Object[] args, String color, boolean bold) {
        StringBuilder json = new StringBuilder();
        json.append("{\"translate\":\"").append(escapeJson(translationKey)).append("\"");
        if (args.length > 0) {
            json.append(",\"with\":[");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append("\"").append(escapeJson(String.valueOf(args[i]))).append("\"");
            }
            json.append("]");
        }
        json.append(",\"color\":\"").append(color).append("\"");
        if (bold) {
            json.append(",\"bold\":true");
        }
        json.append("}");
        return json.toString();
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
