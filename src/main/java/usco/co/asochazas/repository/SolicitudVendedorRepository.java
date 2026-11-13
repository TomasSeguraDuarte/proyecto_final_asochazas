package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.SolicitudVendedor;

@Repository
public interface SolicitudVendedorRepository extends JpaRepository<SolicitudVendedor,Long>{

}
