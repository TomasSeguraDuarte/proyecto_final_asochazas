package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria,Long>{

}
