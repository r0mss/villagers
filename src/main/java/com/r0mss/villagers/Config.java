package com.r0mss.villagers;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuracion del mod. Los valores se pueden ajustar desde el menu de
 * mods (boton "Config") sin necesidad de recompilar.
 */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Guardian de Tierras: combate ---
    public static final ModConfigSpec.DoubleValue GUARDIAN_ATTACK_RANGE = BUILDER
            .comment("Distancia (en bloques) a la que el Guardian de Tierras ataca mobs hostiles.")
            .defineInRange("guardianAttackRange", 2.5, 1.0, 8.0);

    public static final ModConfigSpec.DoubleValue GUARDIAN_ATTACK_DAMAGE = BUILDER
            .comment("Daño que hace el Guardian de Tierras por golpe (2.0 = 1 corazon).")
            .defineInRange("guardianAttackDamage", 7.0, 1.0, 40.0);

    // --- Rangos de chat ---
    public static final ModConfigSpec.DoubleValue TOWN_CRIER_CHAT_RANGE = BUILDER
            .comment("Distancia (en bloques) a la que los jugadores escuchan al Pregonero en el chat.")
            .defineInRange("townCrierChatRange", 48.0, 8.0, 256.0);

    public static final ModConfigSpec.DoubleValue CAMPANERO_CHAT_RANGE = BUILDER
            .comment("Distancia (en bloques) a la que los jugadores escuchan los anuncios de jornada del Campanero en el chat.")
            .defineInRange("campaneroChatRange", 96.0, 8.0, 256.0);

    // --- Pregonero: frecuencia de gritos ---
    public static final ModConfigSpec.IntValue TOWN_CRIER_SHOUT_MIN_SECONDS = BUILDER
            .comment("Tiempo minimo (en segundos) entre noticias que grita el Pregonero.")
            .defineInRange("townCrierShoutMinSeconds", 20, 1, 600);

    public static final ModConfigSpec.IntValue TOWN_CRIER_SHOUT_MAX_SECONDS = BUILDER
            .comment("Tiempo maximo (en segundos) entre noticias que grita el Pregonero.")
            .defineInRange("townCrierShoutMaxSeconds", 45, 1, 600);

    // --- Tablon de informacion ---
    public static final ModConfigSpec.DoubleValue INFO_BOARD_RADIUS = BUILDER
            .comment("Radio (en bloques) que cuenta el tablon de informacion como parte del asentamiento.")
            .defineInRange("infoBoardRadius", 100.0, 16.0, 512.0);

    // --- Nombres de aldeanos ---
    public static final ModConfigSpec.BooleanValue VILLAGER_NAMES_ENABLED = BUILDER
            .comment("Si es true, cada aldeano recibe un nombre propio permanente. Apagado por defecto",
                    "para evitar conflictos con otros mods que agregan la misma funcionalidad.")
            .define("villagerNamesEnabled", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
