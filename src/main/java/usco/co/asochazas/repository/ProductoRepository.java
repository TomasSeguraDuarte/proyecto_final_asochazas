package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,Long>{

}
