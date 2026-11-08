package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.model.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
}
