# 01 · Entidades del dominio

Paquete: `domain/entities`. Son los "sustantivos" del sistema.

## Las principales

| Clase | Qué es |
|---|---|
| `Scrim` | La partida. Guarda su configuración (juego, formato, región, rangos, fecha…) y su estado actual. |
| `Usuario` | Un jugador: MMR, latencia promedio, región, roles preferidos, rango por juego, sanciones, historial (scrims jugados/abandonos) y rating de fair play. |
| `Equipo` | Uno de los dos bandos (A o B) del scrim. Tiene un cupo máximo y un rol por jugador. |
| `GestorLobbyScrim` | Arma Equipo A, Equipo B y la lista de espera. Ver [05](05-lobby-y-suplentes.md). |
| `Postulacion` | "Quiero jugar este scrim con este rol". Tiene estado (`EstadoPostulacion`). |
| `Confirmacion` | "Confirmo que voy a estar". Una por jugador seleccionado. |
| `Sancion` | Strikes + fecha hasta la cual el usuario está en *cooldown*. |
| `ReporteConducta` / `EstadoReporte` | Reporte de mala conducta (PENDIENTE → EN_REVISION → RESUELTO/RECHAZADO). |
| `Estadistica` | Datos de la partida terminada. |

## Enums

| Enum | Valores |
|---|---|
| `Juego` | VALORANT, LOL, CS2 |
| `Formato` | 1v1, 3v3, 5v5 (cupos totales = jugadores por lado × 2) |
| `Rango` | HIERRO, BRONCE, PLATA, ORO, PLATINO, DIAMANTE, RADIANTE (cada uno con un intervalo de MMR) |
| `Region` | LAS, SA, NA, EU |
| `Modalidad` | RANKED_LIKE, CASUAL, PRACTICA_ESTRATOS |
| `RolJuego` | DUELISTA, CENTINELA, TOP, JUNGLA, ADC, AWP, IGL… |
| `LadoEquipo` | EQUIPO_A, EQUIPO_B |

## Cosas que conviene saber

- **Configuración inmutable, estado mutable.** Un `Scrim` no cambia su juego/formato/fecha una vez creado; solo cambian sus jugadores, postulaciones, confirmaciones y estado.
- **Igualdad por id.** `Scrim`, `Equipo`, `Sancion` y `Usuario` se comparan por su `UUID`, no por sus campos.
- **El orden de `Rango` importa.** Se compara con `ordinal()`: HIERRO < BRONCE < … < RADIANTE.
- **Un Scrim no se arma "a mano"**: se crea con el [Builder](02-builder.md), que valida todo.
