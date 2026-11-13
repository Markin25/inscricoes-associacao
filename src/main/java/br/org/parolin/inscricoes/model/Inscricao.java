package br.org.parolin.inscricoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes")
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "morador_id", nullable = false)
    private Morador morador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atividade_id", nullable = false)
    private Atividade atividade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Inscricao() {
    }

    public Inscricao(Morador morador, Atividade atividade) {
        this.morador = morador;
        this.atividade = atividade;
    }

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Morador getMorador() { return morador; }
    public Atividade getAtividade() { return atividade; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
