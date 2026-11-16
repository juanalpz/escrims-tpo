package com.escrims.infrastructure.persistence.memory;

import com.escrims.domain.entities.DatosScrim;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.repository.ScrimRepository;
import com.escrims.domain.state.BuscandoState;
import com.escrims.infrastructure.scheduler.ScrimSchedulerRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de scrims. Conserva los agregados
 * de dominio completos (estado, postulaciones y confirmaciones) y es la misma
 * fuente que recorre el scheduler para las transiciones automáticas.
 */
@Repository
public class InMemoryScrimRepository implements ScrimRepository, ScrimSchedulerRepository {

    private final Map<UUID, Scrim> scrims = new ConcurrentHashMap<>();

    @Override
    public void guardar(Scrim scrim) {
        scrims.put(scrim.getId(), scrim);
    }

    @Override
    public Optional<Scrim> buscarPorId(UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(scrims.get(id));
    }

    @Override
    public List<Scrim> buscarDisponibles(DatosScrim filtro) {
        return scrims.values().stream()
                .filter(s -> s.getEstado() instanceof BuscandoState)
                .filter(s -> filtro == null || coincide(s, filtro))
                .sorted(Comparator.comparing(Scrim::getFechaHora, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public void eliminar(UUID id) {
        scrims.remove(id);
    }

    @Override
    public List<Scrim> obtenerScrimsActivos() {
        return List.copyOf(scrims.values());
    }

    private boolean coincide(Scrim scrim, DatosScrim filtro) {
        if (filtro.getJuego() != null && scrim.getJuego() != filtro.getJuego()) {
            return false;
        }
        if (filtro.getFormato() != null && scrim.getFormato() != filtro.getFormato()) {
            return false;
        }
        if (filtro.getRegion() != null && scrim.getRegion() != filtro.getRegion()) {
            return false;
        }
        if (filtro.getLatenciaMaxima() > 0 && scrim.getLatenciaMaxima() > filtro.getLatenciaMaxima()) {
            return false;
        }
        if (filtro.getInicio() != null && (scrim.getFechaHora() == null
                || !scrim.getFechaHora().toLocalDate().equals(filtro.getInicio().toLocalDate()))) {
            return false;
        }
        return rangosSeSuperponen(scrim, filtro.getRangoMin(), filtro.getRangoMax());
    }

    /** Un scrim coincide si su intervalo de rangos admitidos toca la ventana pedida. */
    private boolean rangosSeSuperponen(Scrim scrim, Rango filtroMin, Rango filtroMax) {
        int ultimo = Rango.values().length - 1;
        int scrimMin = scrim.getRangoMin() != null ? scrim.getRangoMin().ordinal() : 0;
        int scrimMax = scrim.getRangoMax() != null ? scrim.getRangoMax().ordinal() : ultimo;
        int pedidoMin = filtroMin != null ? filtroMin.ordinal() : 0;
        int pedidoMax = filtroMax != null ? filtroMax.ordinal() : ultimo;
        return scrimMin <= pedidoMax && pedidoMin <= scrimMax;
    }
}
