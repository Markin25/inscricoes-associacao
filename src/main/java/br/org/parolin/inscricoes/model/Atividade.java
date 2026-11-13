package br.org.parolin.inscricoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "atividades")
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "data_atividade", nullable = false)
    private LocalDate dataAtividade;

    /** Vagas restantes não são gravadas: vagasTotal - número de inscrições. */
    @Column(name = "vagas_total", nullable = false)
    private Integer vagasTotal;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Atividade() {
    }

    public Atividade(String nome, LocalDate dataAtividade, Integer vagasTotal) {
        this.nome = nome;
        this.dataAtividade = dataAtividade;
        this.vagasTotal = vagasTotal;
    }

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataAtividade() { return dataAtividade; }
    public Integer getVagasTotal() { return vagasTotal; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
