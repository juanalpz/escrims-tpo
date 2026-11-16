package com.escrims.infrastructure.persistence.memory;

import com.escrims.domain.entities.RegistroIncidencia;
import com.escrims.domain.repository.RegistroIncidenciaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementación en memoria del registro de abandonos y no-shows.
 */
@Repository
public class InMemoryRegistroIncidenciaRepository implements RegistroIncidenciaRepository {

    private final List<RegistroIncidencia> incidencias = new CopyOnWriteArrayList<>();

    @Override
    public void guardar(RegistroIncidencia incidencia) {
        incidencias.add(incidencia);
    }

    @Override
    public List<RegistroIncidencia> buscarPorUsuario(UUID usuarioId) {
        return incidencias.stream()
                .filter(i -> i.getUsuario().getId().equals(usuarioId))
                .toList();
    }

    @Override
    public boolean existe(UUID scrimId, UUID usuarioId) {
        return incidencias.stream().anyMatch(i -> i.getScrim().getId().equals(scrimId)
                && i.getUsuario().getId().equals(usuarioId));
    }
}
