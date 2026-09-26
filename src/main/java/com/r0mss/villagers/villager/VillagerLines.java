package com.r0mss.villagers.villager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Claves de traduccion de las noticias predeterminadas que grita el Pregonero.
 * El texto real vive en los archivos de idioma (en_us.json es el principal,
 * es_es.json la traduccion), asi que esto solo elige una clave al azar.
 */
public final class VillagerLines {

    private static final List<String> TOWN_CRIER_NEWS_KEYS = List.of(
            "message.villagers.crier.news.market",
            "message.villagers.crier.news.blacksmith",
            "message.villagers.crier.news.creepers",
            "message.villagers.crier.news.harvest",
            "message.villagers.crier.news.pirates",
            "message.villagers.crier.news.well",
            "message.villagers.crier.news.curfew",
            "message.villagers.crier.news.trade_route",
            "message.villagers.crier.news.strangers",
            "message.villagers.crier.news.peaceful"
    );

    private VillagerLines() {
    }

    public static String randomNewsKey() {
        int index = ThreadLocalRandom.current().nextInt(TOWN_CRIER_NEWS_KEYS.size());
        return TOWN_CRIER_NEWS_KEYS.get(index);
    }
}
