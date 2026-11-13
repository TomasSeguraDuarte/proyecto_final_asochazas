package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Interaccion;

@Repository
public interface InteraccionRepository extends JpaRepository<Interaccion,Long>{

}
