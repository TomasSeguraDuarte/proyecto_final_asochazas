package usco.co.asochazas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.co.asochazas.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	boolean existsByCorreo(String correo);

	Optional<Usuario> findByCorreo(String correo);	
}
