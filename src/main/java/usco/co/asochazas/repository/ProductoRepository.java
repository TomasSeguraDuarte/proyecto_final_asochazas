package usco.co.asochazas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

	List<Producto> findByVendedor_IdAndEstado_EstadoProductoNot(Long vendedorId, String estado);
	List<Producto> findByVendedor_IdAndEstado_EstadoProducto(Long vendedorId, String estado);
	List<Producto> findByEstado_EstadoProductoAndStockGreaterThan(String estado, int stockMinimo);
	List<Producto> findByEstado_EstadoProductoAndStockGreaterThanAndCategoria_Categoria(
	        String estado, int stockMinimo, String categoria);
	List<Producto> findByEstado_EstadoProductoAndStockGreaterThanAndNombreContainingIgnoreCase(
	        String estado, int stockMinimo, String nombre);
	@Modifying
	@Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.id = :productoId AND p.stock >= :cantidad")
	int descontarStock(@Param("productoId") Long productoId, @Param("cantidad") int cantidad);

	@Modifying
	@Query("UPDATE Producto p SET p.stock = p.stock + :cantidad WHERE p.id = :productoId")
	int devolverStock(@Param("productoId") Long productoId, @Param("cantidad") int cantidad);
}
