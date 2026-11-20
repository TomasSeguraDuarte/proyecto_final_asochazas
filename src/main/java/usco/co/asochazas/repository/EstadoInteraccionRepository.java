package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.EstadoInteraccion;
import usco.co.asochazas.model.EstadoProducto;

@Repository
public interface EstadoInteraccionRepository extends JpaRepository<EstadoInteraccion,Long>{
	EstadoInteraccion findByEstadoInteraccion(String estado);
}
