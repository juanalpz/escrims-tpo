# 04 · Strategy – matchmaking

**Paquete:** `domain/strategy`

## Idea
Hay varias formas de decidir "quién entra a este scrim". Cada una es una clase intercambiable detrás de la misma interfaz.

```java
public interface MatchmakingStrategy {
    Set<Usuario> seleccionarCandidatos(Scrim scrim, Set<Usuario> candidatos);
    boolean esElegible(Scrim scrim, Usuario candidato);
}
```

- `esElegible`: ¿este candidato cumple el criterio?
- `seleccionarCandidatos`: elige hasta `cuposDisponibles()` candidatos, del mejor al peor. Nunca incluye a quienes ya están en el scrim.

## Las tres estrategias

### `ByMMRStrategy(tolerancia)` – por nivel
- El MMR del candidato debe caer dentro del rango del scrim (si tiene).
- Si ya hay jugadores: el candidato no puede alejarse más de `tolerancia` del **MMR promedio** del lobby; se eligen los más cercanos.
- Si el lobby está vacío: se busca el **grupo más parejo** (el más grande cuya diferencia mejor–peor ≤ tolerancia) con una ventana deslizante sobre los MMR ordenados.

### `ByLatencyStrategy(latenciaMaxima)` – por ping
- El ping promedio debe ser ≤ umbral. El umbral es el **más estricto** entre el de la estrategia y el del scrim.
- Se ordena por menor ping; a igual ping, gana quien es de la región del scrim.

### `ByHistoryStrategy(pesoCompatibilidad)` – por conducta y roles
Puntaje de 0 a 1:

```
puntaje = peso × sinergia + (1 − peso) × fairPlay
```

- **Sinergia:** 1 si juega algún rol buscado (o el scrim no exige roles), 0.5 si no declaró roles, 0 si ninguno le sirve.
- **Fair play:** promedio de reputación y constancia (1 − tasa de abandono), menos 0.15 por cada strike.
- **Descartados:** sanción en cooldown, abandono > 50%, o puntaje < 0.4.
- La selección primero cubre cada rol buscado con el mejor candidato que lo juega y luego completa por puntaje.

## Cómo usarlas

```java
MatchmakingStrategy estrategia = new ByMMRStrategy(200);
Set<Usuario> elegidos = estrategia.seleccionarCandidatos(scrim, candidatos);
```
Cambiar de criterio es cambiar la instancia; el resto del código no se entera.
