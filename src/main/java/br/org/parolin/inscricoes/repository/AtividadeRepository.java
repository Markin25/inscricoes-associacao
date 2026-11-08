package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.dto.AtividadeResumo;
import br.org.parolin.inscricoes.model.Atividade;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    /** SELECT ... FOR UPDATE: segura a linha da atividade até o fim da transação da inscrição. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Atividade a where a.id = :id")
    Optional<Atividade> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select new br.org.parolin.inscricoes.dto.AtividadeResumo(
                   a.id, a.nome, a.dataAtividade, a.vagasTotal, count(i.id))
            from Atividade a left join Inscricao i on i.atividade = a
            group by a.id, a.nome, a.dataAtividade, a.vagasTotal
            order by a.dataAtividade, a.nome
            """)
    List<AtividadeResumo> listarResumos();

    @Query("""
            select new br.org.parolin.inscricoes.dto.AtividadeResumo(
                   a.id, a.nome, a.dataAtividade, a.vagasTotal, count(i.id))
            from Atividade a left join Inscricao i on i.atividade = a
            where a.dataAtividade >= :aPartirDe
            group by a.id, a.nome, a.dataAtividade, a.vagasTotal
            order by a.dataAtividade, a.nome
            """)
    List<AtividadeResumo> listarResumosAPartirDe(@Param("aPartirDe") LocalDate aPartirDe);

    @Query("""
            select new br.org.parolin.inscricoes.dto.AtividadeResumo(
                   a.id, a.nome, a.dataAtividade, a.vagasTotal, count(i.id))
            from Atividade a left join Inscricao i on i.atividade = a
            where a.id = :id
            group by a.id, a.nome, a.dataAtividade, a.vagasTotal
            """)
    Optional<AtividadeResumo> buscarResumo(@Param("id") Long id);
}
