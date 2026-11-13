package br.org.parolin.inscricoes.repository;

import br.org.parolin.inscricoes.model.Morador;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MoradorRepository extends JpaRepository<Morador, Long> {

    Optional<Morador> findByTelefone(String telefone);

    /** Busca por parte do nome (sem diferenciar maiúsculas) ou por parte do telefone. */
    @Query("""
            select m from Morador m
            where lower(m.nome) like lower(concat('%', :nome, '%'))
               or (:digitos <> '' and m.telefone like concat('%', :digitos, '%'))
            """)
    Page<Morador> buscar(@Param("nome") String nome, @Param("digitos") String digitos, Pageable pageable);
}
