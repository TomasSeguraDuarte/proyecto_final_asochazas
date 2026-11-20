package usco.co.asochazas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Interaccion;

@Repository
public interface InteraccionRepository extends JpaRepository<Interaccion,Long>{

	boolean existsByUsuario_IdAndProducto_IdAndEstado_EstadoInteraccion(Long compradorId, Long productoId,
			String estadointeraccion);

	List<Interaccion> findByProducto_Vendedor_Id(Long vendedorId);

	List<Interaccion> findByUsuario_Id(Long compradorId);

}
