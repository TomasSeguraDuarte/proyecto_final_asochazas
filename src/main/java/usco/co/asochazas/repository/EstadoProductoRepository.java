package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.EstadoProducto;

@Repository
public interface EstadoProductoRepository extends JpaRepository<EstadoProducto,Long>{
	EstadoProducto findByEstadoProducto(String estado);
}

