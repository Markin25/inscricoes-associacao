package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.model.Morador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoradorRepository extends JpaRepository<Morador, Long> {
}
