# 02 · Builder – crear un Scrim

**Clase:** `domain/builder/ScrimBuilder`

## Problema
Un `Scrim` tiene muchos parámetros y reglas de negocio. Un constructor gigante sería ilegible y fácil de usar mal.

## Solución
Un builder con interfaz fluida. Se encadenan los datos y `build()` **valida todo junto** antes de devolver el scrim.

```java
Scrim scrim = new ScrimBuilder()
    .organizador(organizador)
    .juego(Juego.VALORANT)
    .formato(Formato.CINCO_VS_CINCO)
    .region(Region.LAS)
    .rango(Rango.ORO, Rango.DIAMANTE)     // opcional
    .latenciaMax(80)                      // opcional
    .fechaHora(LocalDateTime.now().plusDays(1))
    .agregarReglaRol(RolJuego.DUELISTA, 2) // opcional
    .build();
```

## Valores por defecto
- Modalidad: `CASUAL`
- Duración: 60 minutos
- Cupos: se calculan del `Formato` (no hace falta pasarlos)

## Qué valida `build()`

| Validación | Error si… |
|---|---|
| Obligatorios | falta organizador, juego, formato, región o fecha |
| RN-01 / RN-06 | organizador sin email verificado o con sanción activa |
| RN-03 | solo se define un extremo del rango, o min > max |
| RN-04 | latencia fuera de 1–500 ms |
| Fecha | no es futura |
| Duración | fuera de 15–360 min |
| Cupos | si se pasan a mano, no coinciden con el formato |
| RN-05 | la suma de roles obligatorios supera los cupos |

Al final el scrim se entrega en estado `BuscandoState` (RN-08). Ver [03-state.md](03-state.md).
