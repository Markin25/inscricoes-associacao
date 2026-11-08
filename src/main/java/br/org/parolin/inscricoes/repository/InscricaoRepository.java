package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.dto.InscritoResumo;
import br.org.parolin.inscricoes.model.Inscricao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByMorador_IdAndAtividade_Id(Long moradorId, Long atividadeId);

    long countByAtividade_Id(Long atividadeId);

    /** Inscritos na ordem em que se inscreveram. */
    @Query("""
            select new br.org.parolin.inscricoes.dto.InscritoResumo(m.nome, m.telefone, i.criadoEm)
            from Inscricao i join i.morador m
            where i.atividade.id = :atividadeId
            order by i.criadoEm, i.id
            """)
    List<InscritoResumo> listarInscritos(@Param("atividadeId") Long atividadeId);
}
