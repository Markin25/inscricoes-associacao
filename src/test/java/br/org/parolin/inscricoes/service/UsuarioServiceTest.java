package br.org.parolin.inscricoes.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.parolin.inscricoes.model.Usuario;
import br.org.parolin.inscricoes.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;

    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, new BCryptPasswordEncoder(4));
    }

    @Test
    void naoCriaUsuarioInicialSeJaExisteAlgum() {
        when(usuarioRepository.count()).thenReturn(1L);

        service.criarUsuarioInicialSeNecessario("coordenacao", "senha-bem-longa");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoCriaUsuarioInicialSemVariaveisDeAmbiente() {
        when(usuarioRepository.count()).thenReturn(0L);

        service.criarUsuarioInicialSeNecessario("", "");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criaUsuarioInicialComTabelaVazia() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.findByNomeUsuario("coordenacao")).thenReturn(Optional.empty());

        service.criarUsuarioInicialSeNecessario("coordenacao", "senha-bem-longa");

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void recusaSenhaComMenosDe10Caracteres() {
        assertThatThrownBy(() -> service.criarUsuario("coordenacao", "123456789"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).save(any());
    }
}
