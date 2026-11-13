package usco.co.asochazas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol,Long>{
}
