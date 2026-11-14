package usco.co.asochazas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.SolicitudVendedor;


@Repository
public interface SolicitudVendedorRepository extends JpaRepository<SolicitudVendedor,Long>{

    Optional<SolicitudVendedor> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndEstado_EstadoSolicitud(Long usuarioId, String estadoSolicitud);

    List<SolicitudVendedor> findByEstado_EstadoSolicitud(String estadoSolicitud);
}
