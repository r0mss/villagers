package com.r0mss.villagers.villager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listas de frases predeterminadas.
 * <p>
 * Por ahora son mensajes fijos en español elegidos al azar. Mas adelante
 * esto se puede mover a un archivo de datos (JSON) para poder agregar
 * frases sin recompilar el mod.
 */
public final class VillagerLines {

    private VillagerLines() {
    }

    // Frases ambientales genericas, cualquier aldeano las puede decir de vez en cuando
    private static final List<String> AMBIENT_CHATTER = List.of(
            "Que calor hace hoy...",
            "Necesito mas esmeraldas.",
            "¿Alguien ha visto a mi gato?",
            "El pan de hoy quedo delicioso.",
            "Deberian arreglar ese camino.",
            "Anoche escuche a un zombie cerca del pueblo.",
            "Hoy es un buen dia para comerciar.",
            "Extraño los viejos tiempos.",
            "¡Que buena cosecha este año!",
            "Alguien dejo la puerta abierta otra vez."
    );

    // Frases especificas para el Guardian de Tierras
    private static final List<String> GUARDIAN_CHATTER = List.of(
            "Nadie pasara sin que yo lo vea.",
            "Todo tranquilo por aqui.",
            "Mantengo la guardia, como siempre.",
            "Esta zona esta bajo mi vigilancia."
    );

    // Noticias predeterminadas que grita el Pregonero
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

    public static String randomChatter(boolean isGuardian) {
        List<String> pool = isGuardian ? GUARDIAN_CHATTER : AMBIENT_CHATTER;
        return pick(pool);
    }

    public static String randomNews() {
        return pick(TOWN_CRIER_NEWS);
    }

    private static String pick(List<String> pool) {
        int index = ThreadLocalRandom.current().nextInt(pool.size());
        return pool.get(index);
    }
}
