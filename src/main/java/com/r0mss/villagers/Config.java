package com.r0mss.villagers;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuracion del mod. Por ahora es minima; se puede ampliar mas adelante
 * para permitir ajustar, por ejemplo, la frecuencia con la que hablan los
 * aldeanos o el volumen de los gritos del Pregonero.
 * <p>
 * Nota: esta clase NO lleva @EventBusSubscriber porque no tiene ningun
 * metodo @SubscribeEvent. NeoForge exige que toda clase marcada con esa
 * anotacion tenga al menos un listener, o falla al cargar el mod.
 */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue VILLAGERS_CAN_SPEAK = BUILDER
            .comment("Si es false, ningun aldeano hablara de forma ambiental (los trabajos especiales igual funcionan).")
            .define("villagersCanSpeak", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
