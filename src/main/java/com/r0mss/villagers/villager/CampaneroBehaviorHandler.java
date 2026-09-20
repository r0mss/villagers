package com.r0mss.villagers.villager;

import com.r0mss.villagers.registry.ModProfessions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * El Campanero:
 * <p>
 * 1) Muestra un reloj flotante permanente sobre su campana, con la hora
 *    actual del mundo (formato real, ya que el vende relojes y maneja esa
 *    informacion).
 * 2) Toca la campana y anuncia por chat cuando cambia la jornada (mañana,
 *    mediodia, noche), variando entre 3 mensajes distintos por horario.
 */
public final class CampaneroBehaviorHandler {

    private static final String TAG_PERIOD = "villagers_campanero_period";
    private static final String TAG_CLOCK_UUID = "villagers_campanero_clock_uuid";
    private static final String TAG_NEXT_CLOCK_UPDATE = "villagers_campanero_next_clock_update";

    private static final int CLOCK_UPDATE_INTERVAL_TICKS = 20; // 1 segundo

    // Limites del dia (en "daytime" ticks, 0-24000) para cada jornada.
    // 0 = 6:00 AM (amanecer). 1000 ticks = 1 hora real.
    private static final long MIDDAY_START = 6000L;     // 12:00 PM
    private static final long NIGHT_START = 12000L;     // 6:00 PM

    private static final List<String> MORNING_LINES = List.of(
            "¡Buenos dias! ¡Hora de ponerse a trabajar!",
            "¡Arriba! El dia ha comenzado, a trabajar se ha dicho.",
            "¡Suena la campana! El pueblo despierta para otro dia de labor."
    );
    private static final List<String> MIDDAY_LINES = List.of(
            "¡Es mediodia! Buen momento para un descanso.",
            "El sol esta en lo alto, ya es mediodia.",
            "¡Campanadas de mediodia! La jornada sigue su curso."
    );
    private static final List<String> NIGHT_LINES = List.of(
            "¡Cae la noche! Es hora de resguardarse y dormir.",
            "Cuidado, las criaturas salen de noche. Mejor ir a descansar.",
            "Suena la campana de la noche... a dormir se ha dicho."
    );

    private CampaneroBehaviorHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!(entity instanceof Villager villager)) {
            return;
        }
        if (villager.getVillagerData().getProfession() != ModProfessions.CAMPANERO.get()) {
            return;
        }
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos jobSitePos = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE)
                .map(GlobalPos::pos)
                .orElse(null);
        if (jobSitePos == null) {
            return;
        }

        CompoundTag data = villager.getPersistentData();
        handleDayPeriod(serverLevel, villager, jobSitePos, data);
        handleClockDisplay(serverLevel, jobSitePos, data);
    }

    private static void handleDayPeriod(ServerLevel level, Villager villager, BlockPos jobSitePos, CompoundTag data) {
        int period = currentPeriod(level.getDayTime() % 24000L);

        if (!data.contains(TAG_PERIOD)) {
            // Primera vez que lo vemos: solo guardamos el periodo actual, sin anunciar
            // (para no tocar la campana apenas se asigna el trabajo).
            data.putInt(TAG_PERIOD, period);
            return;
        }

        int previousPeriod = data.getInt(TAG_PERIOD);
        if (previousPeriod == period) {
            return;
        }

        data.putInt(TAG_PERIOD, period);
        ringBell(level, jobSitePos);
        SpeechBubbles.announce(villager, pickLine(period), "Campanero");
    }

    private static void ringBell(ServerLevel level, BlockPos jobSitePos) {
        BlockState state = level.getBlockState(jobSitePos);
        if (state.getBlock() instanceof BellBlock bell) {
            bell.attemptToRing(level, jobSitePos, null);
        }
    }

    private static int currentPeriod(long dayTime) {
        if (dayTime < MIDDAY_START) {
            return 0; // mañana
        } else if (dayTime < NIGHT_START) {
            return 1; // mediodia
        } else {
            return 2; // noche
        }
    }

    private static String pickLine(int period) {
        List<String> pool = switch (period) {
            case 0 -> MORNING_LINES;
            case 1 -> MIDDAY_LINES;
            default -> NIGHT_LINES;
        };
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    /**
     * Mantiene un text_display permanente sobre la campana mostrando la
     * hora actual del mundo en formato real (ej. "6:00 AM").
     */
    private static void handleClockDisplay(ServerLevel level, BlockPos jobSitePos, CompoundTag data) {
        long time = level.getGameTime();
        if (data.contains(TAG_NEXT_CLOCK_UPDATE) && time < data.getLong(TAG_NEXT_CLOCK_UPDATE)) {
            return;
        }
        data.putLong(TAG_NEXT_CLOCK_UPDATE, time + CLOCK_UPDATE_INTERVAL_TICKS);

        Display.TextDisplay display = findOrCreateClockDisplay(level, jobSitePos, data);
        display.load(SpeechBubbles.buildDisplayTag(formatClock(level.getDayTime() % 24000L), "white", false));
    }

    private static Display.TextDisplay findOrCreateClockDisplay(ServerLevel level, BlockPos jobSitePos, CompoundTag data) {
        if (data.contains(TAG_CLOCK_UUID)) {
            UUID uuid = data.getUUID(TAG_CLOCK_UUID);
            Entity existing = level.getEntity(uuid);
            if (existing instanceof Display.TextDisplay display) {
                return display;
            }
        }

        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        display.moveTo(jobSitePos.getX() + 0.5, jobSitePos.getY() + 1.6, jobSitePos.getZ() + 0.5, 0.0F, 0.0F);
        display.setNoGravity(true);
        display.setSilent(true);
        display.setInvulnerable(true);
        level.addFreshEntity(display);
        data.putUUID(TAG_CLOCK_UUID, display.getUUID());
        return display;
    }

    private static String formatClock(long dayTime) {
        int totalMinutes = (int) (dayTime * 0.06); // 1000 ticks = 1 hora = 60 min
        int hour24 = ((totalMinutes / 60) + 6) % 24;
        int minute = totalMinutes % 60;

        String amPm = hour24 < 12 ? "AM" : "PM";
        int hour12 = hour24 % 12;
        if (hour12 == 0) {
            hour12 = 12;
        }

        return String.format("%d:%02d %s", hour12, minute, amPm);
    }
}
