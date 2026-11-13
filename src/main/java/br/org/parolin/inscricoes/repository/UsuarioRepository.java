package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNomeUsuario(String nomeUsuario);
}
