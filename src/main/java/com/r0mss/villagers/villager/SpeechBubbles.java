package com.r0mss.villagers.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Muestra un texto flotante (usando una entidad text_display vanilla) encima
 * de un aldeano durante unos segundos, simulando que "habla".
 * <p>
 * No usamos ningun paquete de red propio ni mixins: la entidad text_display
 * es completamente vanilla, y sus propiedades (billboard, color de fondo,
 * texto, etc) se configuran mediante NBT publico via {@link net.minecraft.world.entity.Entity#load(CompoundTag)},
 * exactamente igual que lo haria el comando /summon.
 */
public final class SpeechBubbles {

    private static final String TAG_EXPIRE_TICK = "villagers_bubble_expire";

    private static final int NORMAL_LIFESPAN_TICKS = 70;   // ~3.5s
    private static final int SHOUT_LIFESPAN_TICKS = 110;   // ~5.5s

    private SpeechBubbles() {
    }

    public static void say(LivingEntity speaker, String message) {
        say(speaker, message, false);
    }

    public static void shout(LivingEntity speaker, String message) {
        say(speaker, message, true);
    }

    private static void say(LivingEntity speaker, String message, boolean shout) {
        Level level = speaker.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
        display.moveTo(speaker.getX(), speaker.getEyeY() + 0.55, speaker.getZ(), 0.0F, 0.0F);

        // Propiedades base de la entidad (setters publicos normales)
        display.setNoGravity(true);
        display.setSilent(true);
        display.setInvulnerable(true);

        // Propiedades propias de la entidad "display" (no tienen setter publico,
        // se aplican mediante NBT publico, igual que /summon)
        CompoundTag tag = new CompoundTag();
        tag.putString("text", toJsonText(message, shout));
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

        int lifespan = shout ? SHOUT_LIFESPAN_TICKS : NORMAL_LIFESPAN_TICKS;
        display.getPersistentData().putLong(TAG_EXPIRE_TICK, serverLevel.getGameTime() + lifespan);

        if (shout) {
            serverLevel.playSound(null, speaker.blockPosition(), SoundEvents.VILLAGER_YES,
                    SoundSource.NEUTRAL, 1.0f, 0.8f);
        } else {
            serverLevel.playSound(null, speaker.blockPosition(), SoundEvents.VILLAGER_AMBIENT,
                    SoundSource.NEUTRAL, 0.5f, 1.1f);
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

    private static String toJsonText(String message, boolean shout) {
        String escaped = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        String color = shout ? "gold" : "white";
        StringBuilder json = new StringBuilder();
        json.append("{\"text\":\"").append(escaped).append("\",\"color\":\"").append(color).append("\"");
        if (shout) {
            json.append(",\"bold\":true");
        }
        json.append("}");
        return json.toString();
    }
}
