package com.r0mss.villagers.villager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Noticias predeterminadas que grita el Pregonero.
 * <p>
 * Por ahora son mensajes fijos en español elegidos al azar. Mas adelante
 * esto se puede mover a un archivo de datos (JSON) para poder agregar
 * frases sin recompilar el mod.
 */
public final class VillagerLines {

    private static final List<String> TOWN_CRIER_NEWS = List.of(
            "¡Escuchen, escuchen! ¡El mercado abre al amanecer!",
            "¡Se busca herrero! ¡Buen pago garantizado!",
            "¡Cuidado con los creepers cerca del bosque!",
            "¡Gran cosecha de trigo este año, celebremos!",
            "¡Se reporta actividad de piratas en la costa!",
            "¡El pozo del pueblo fue reparado esta semana!",
            "¡Aviso! ¡No se permite comerciar despues del anochecer!",
            "¡Nueva ruta comercial abierta hacia el norte!",
            "¡Extraños fueron vistos merodeando anoche!",
            "¡Todo tranquilo en el pueblo, buenas noticias!"
    );

    private VillagerLines() {
    }

    public static String randomNews() {
        int index = ThreadLocalRandom.current().nextInt(TOWN_CRIER_NEWS.size());
        return TOWN_CRIER_NEWS.get(index);
    }
}
