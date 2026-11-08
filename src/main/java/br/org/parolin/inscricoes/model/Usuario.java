package br.org.parolin.inscricoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Pessoa da equipe que entra no sistema. Moradores não são usuários. */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_usuario", nullable = false, unique = true, length = 50)
    private String nomeUsuario;

    /** Sempre hash BCrypt, nunca a senha. */
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "tentativas_falhas", nullable = false)
    private int tentativasFalhas;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    protected Usuario() {
    }

    public Usuario(String nomeUsuario, String senhaHash) {
        this.nomeUsuario = nomeUsuario;
        this.senhaHash = senhaHash;
    }

    public Long getId() { return id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public String getSenhaHash() { return senhaHash; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public int getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(int tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }
    public LocalDateTime getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(LocalDateTime bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }
}
