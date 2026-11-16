package com.escrims.infrastructure.persistence;

import com.escrims.domain.entities.*;
import com.escrims.infrastructure.persistence.entity.*;
import com.escrims.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ScrimJpaRepositoryTest {

    @Autowired private UsuarioJpaRepository usuarioRepo;
    @Autowired private ScrimJpaRepository scrimRepo;
    @Autowired private PostulacionJpaRepository postulacionRepo;
    @Autowired private ConfirmacionJpaRepository confirmacionRepo;
    @Autowired private EstadisticaJpaRepository estadisticaRepo;

    @Test
    @DisplayName("Persiste y consulta Usuario, Scrim, Postulación, Confirmación y Estadística con Spring Data JPA")
    void deberiaPersistirYRecuperarCicloCompletoEnBaseDeDatos() {
        UsuarioJpaEntity org = usuarioRepo.save(new UsuarioJpaEntity(
                UUID.randomUUID(), "organizador_pro", "org@escrims.gg", "hash123",
                true, Juego.VALORANT, Rango.DIAMANTE, Region.LAS
        ));

        UsuarioJpaEntity jugador = usuarioRepo.save(new UsuarioJpaEntity(
                UUID.randomUUID(), "duelist_las", "player@escrims.gg", "hash456",
                true, Juego.VALORANT, Rango.PLATINO, Region.LAS
        ));

        ScrimJpaEntity scrim = scrimRepo.save(new ScrimJpaEntity(
                UUID.randomUUID(), org, Juego.VALORANT, Formato.CINCO_VS_CINCO,
                Region.LAS, Rango.ORO, Rango.DIAMANTE, 80,
                LocalDateTime.now().plusHours(3), 60, Modalidad.RANKED_LIKE,
                10, "Buscando jugadores"
        ));

        PostulacionJpaEntity post = new PostulacionJpaEntity(scrim, jugador, RolJuego.DUELIST);
        post.setEstado(EstadoPostulacion.ACEPTADA);
        postulacionRepo.save(post);

        ConfirmacionJpaEntity conf = new ConfirmacionJpaEntity(scrim, jugador);
        conf.confirmar();
        confirmacionRepo.save(conf);

        EstadisticaJpaEntity stat = estadisticaRepo.save(
                new EstadisticaJpaEntity(scrim, jugador, true, "24/8/6", "Gran entrada en sitio A")
        );

        assertEquals(1, scrimRepo.findByJuegoAndRegionAndLatenciaMaxLessThanEqual(Juego.VALORANT, Region.LAS, 90).size());
        assertEquals(1, postulacionRepo.countByScrimIdAndEstado(scrim.getId(), EstadoPostulacion.ACEPTADA));
        assertTrue(confirmacionRepo.findByScrimId(scrim.getId()).get(0).isConfirmado());
        assertTrue(estadisticaRepo.findByScrimId(scrim.getId()).get(0).isMvp());
        assertNotNull(stat.getId());
    }
}
