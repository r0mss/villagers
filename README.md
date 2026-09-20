# Aldeanos Inmersivos (villagers)

Mod para **NeoForge 1.21.1** que hace a los aldeanos mas inmersivos.

## Funcionalidades actuales

- **Guardian de Tierras**: nuevo trabajo. Su puesto (`guard_post`) es una
  placa de pared (no un bloque completo), asi que se puede colocar junto a
  una puerta sin estorbar el paso. El aldeano se para quieto y su cuerpo
  queda fijo mirando hacia donde apunta la placa; la cabeza sigue libre para
  mirar alrededor.
- **Pregonero**: nuevo trabajo. El aldeano se queda estatico junto a su podio
  (`town_crier_podium`) y grita noticias aleatorias predeterminadas cada
  20-45 segundos: texto flotante sobre su cabeza + mensaje en el chat de los
  jugadores cercanos (48 bloques) + sonido.

## Como darle trabajo a un aldeano

1. Coloca el bloque `guard_post` en una pared (clic derecho en la cara de un
   bloque solido) o el `town_crier_podium` en el piso — estan en la pestaña
   de creativo "Aldeanos Inmersivos", o en la pestaña de bloques de construccion.
2. Espera a que un aldeano desempleado camine cerca y lo reclame, igual que
   pasaria con un atril o una mesa de cartografo vanilla.
3. Una vez empleado, el aldeano se quedara fijo en su puesto.

## Compilar el mod

Este repo compila automaticamente en cada push gracias a GitHub Actions
(ver `.github/workflows/build.yml`). El `.jar` resultante queda disponible
como *artifact* descargable en la pestaña "Actions" de GitHub, dentro de la
ejecucion correspondiente.

Para compilar localmente necesitas Java 21 y conexion a internet (Gradle
descarga NeoForge la primera vez):

```bash
./gradlew build
```

El jar queda en `build/libs/`.

## Estructura del proyecto

```
src/main/java/com/r0mss/villagers/
├── VillagersMod.java              # Clase principal
├── block/
│   └── GuardPostBlock.java         # Bloque de pared del Guardian (con FACING)
├── registry/
│   ├── ModBlocks.java               # Bloques de trabajo
│   ├── ModItems.java                # Items (block items)
│   ├── ModPoiTypes.java             # Puntos de interes (job sites)
│   └── ModProfessions.java          # Profesiones nuevas
└── villager/
    ├── VillagerLines.java           # Noticias predeterminadas del pregonero
    ├── SpeechBubbles.java           # Texto flotante + chat + sonido
    └── VillagerBehaviorHandler.java # Logica: estatico, orientacion fija, grito
```

Ademas, `src/main/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json`
es imprescindible: es el tag vanilla que le dice a Minecraft que estos dos
puestos son trabajos que un aldeano desempleado puede notar y reclamar.

## Roadmap / ideas pendientes

- Mover las frases a un archivo de datos (JSON) editable sin recompilar.
- Texturas y modelo propios para los aldeanos de cada profesion (skin custom).
- Mas trabajos.
- Sonidos personalizados en vez de reutilizar sonidos vanilla.
