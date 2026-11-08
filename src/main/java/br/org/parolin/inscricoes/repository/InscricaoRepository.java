package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByMorador_IdAndAtividade_Id(Long moradorId, Long atividadeId);

    long countByAtividade_Id(Long atividadeId);
}
