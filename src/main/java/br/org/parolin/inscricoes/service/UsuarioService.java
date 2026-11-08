package br.org.parolin.inscricoes.service;

import br.org.parolin.inscricoes.model.Usuario;
import br.org.parolin.inscricoes.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Usuários da equipe: login (UserDetailsService), criação com senha BCrypt e
 * bloqueio temporário depois de tentativas inválidas seguidas.
 */
@Service
public class UsuarioService implements UserDetailsService {

    public static final int SENHA_TAMANHO_MINIMO = 10;
    public static final int MAXIMO_TENTATIVAS = 5;
    public static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);
    private static final int NOME_TAMANHO_MAXIMO = 50;

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock relogio;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Clock relogio) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.relogio = relogio;
    }

    /**
     * Usuário bloqueado vira "conta travada" para o Spring Security, que recusa antes de
     * conferir a senha. A tela mostra a mesma mensagem de senha errada.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nomeUsuario) {
        Usuario usuario = usuarioRepository.findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário ou senha incorretos."));
        return User.withUsername(usuario.getNomeUsuario())
                .password(usuario.getSenhaHash())
                .disabled(!usuario.isAtivo())
                .accountLocked(estaBloqueado(usuario))
                .roles("EQUIPE")
                .build();
    }

    /** Senha errada: soma uma tentativa; na 5ª seguida, bloqueia por 15 minutos. */
    @Transactional
    public void registrarFalha(String nomeUsuario) {
        usuarioRepository.findByNomeUsuario(nomeUsuario).ifPresent(usuario -> {
            if (estaBloqueado(usuario)) {
                return;
            }
            int tentativas = usuario.getTentativasFalhas() + 1;
            if (tentativas >= MAXIMO_TENTATIVAS) {
                usuario.setBloqueadoAte(LocalDateTime.now(relogio).plus(TEMPO_BLOQUEIO));
                usuario.setTentativasFalhas(0);
                log.warn("Usuário '{}' bloqueado por {} minutos após {} tentativas inválidas.",
                        usuario.getNomeUsuario(), TEMPO_BLOQUEIO.toMinutes(), MAXIMO_TENTATIVAS);
            } else {
                usuario.setTentativasFalhas(tentativas);
            }
        });
    }

    /** Login correto zera o contador. */
    @Transactional
    public void registrarSucesso(String nomeUsuario) {
        usuarioRepository.findByNomeUsuario(nomeUsuario).ifPresent(usuario -> {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
        });
    }

    @Transactional
    public Usuario criarUsuario(String nomeUsuario, String senha) {
        String nome = nomeUsuario == null ? "" : nomeUsuario.trim();
        if (nome.isEmpty() || nome.length() > NOME_TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Nome de usuário deve ter de 1 a " + NOME_TAMANHO_MAXIMO + " caracteres.");
        }
        if (senha == null || senha.length() < SENHA_TAMANHO_MINIMO) {
            throw new IllegalArgumentException("A senha deve ter pelo menos " + SENHA_TAMANHO_MINIMO + " caracteres.");
        }
        if (usuarioRepository.findByNomeUsuario(nome).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já existe.");
        }
        return usuarioRepository.save(new Usuario(nome, passwordEncoder.encode(senha)));
    }

    /**
     * Cria o primeiro usuário a partir de APP_ADMIN_USER e APP_ADMIN_PASSWORD,
     * somente se a tabela usuarios estiver vazia. Senha curta impede a subida.
     */
    @Transactional
    public void criarUsuarioInicialSeNecessario(String nomeUsuario, String senha) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        if (nomeUsuario == null || nomeUsuario.isBlank() || senha == null || senha.isEmpty()) {
            log.warn("Nenhum usuário cadastrado. Defina APP_ADMIN_USER e APP_ADMIN_PASSWORD e reinicie a aplicação.");
            return;
        }
        criarUsuario(nomeUsuario, senha);
        log.info("Usuário inicial '{}' criado.", nomeUsuario.trim());
    }

    private boolean estaBloqueado(Usuario usuario) {
        return usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(LocalDateTime.now(relogio));
    }
}
