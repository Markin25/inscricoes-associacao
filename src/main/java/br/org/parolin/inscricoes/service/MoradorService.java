package br.org.parolin.inscricoes.service;

import br.org.parolin.inscricoes.dto.Mensagens;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.exception.RegraNegocioException;
import br.org.parolin.inscricoes.exception.TelefoneJaCadastradoException;
import br.org.parolin.inscricoes.model.Morador;
import br.org.parolin.inscricoes.repository.MoradorRepository;
import br.org.parolin.inscricoes.util.TelefoneUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoradorService {

    public static final int TAMANHO_PAGINA = 20;
    /** Telefone só entra na busca a partir de 4 dígitos, para não trazer meio cadastro. */
    private static final int DIGITOS_MINIMOS_BUSCA = 4;

    private final MoradorRepository moradorRepository;

    public MoradorService(MoradorRepository moradorRepository) {
        this.moradorRepository = moradorRepository;
    }

    /**
     * Cadastra um morador com telefone único.
     * Sem @Transactional de propósito: se outra pessoa gravar o mesmo telefone ao mesmo
     * tempo, o UNIQUE do banco recusa e ainda conseguimos buscar o cadastro vencedor.
     */
    public Morador cadastrar(String nome, String telefone) {
        String nomeLimpo = validarNome(nome);
        String telefoneNormalizado = validarTelefone(telefone);
        moradorRepository.findByTelefone(telefoneNormalizado).ifPresent(existente -> {
            throw new TelefoneJaCadastradoException(existente);
        });
        try {
            return moradorRepository.saveAndFlush(new Morador(nomeLimpo, telefoneNormalizado));
        } catch (DataIntegrityViolationException e) {
            throw moradorRepository.findByTelefone(telefoneNormalizado)
                    .<RuntimeException>map(TelefoneJaCadastradoException::new)
                    .orElse(e);
        }
    }

    @Transactional(readOnly = true)
    public Morador buscarPorId(Long id) {
        return moradorRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Morador não encontrado."));
    }

    /** Lista paginada em ordem alfabética; com texto, filtra por nome ou telefone. */
    @Transactional(readOnly = true)
    public Page<Morador> listar(String busca, int pagina) {
        PageRequest pageRequest = PageRequest.of(Math.max(pagina, 0), TAMANHO_PAGINA,
                Sort.by(Sort.Order.asc("nome").ignoreCase(), Sort.Order.asc("id")));
        if (busca == null || busca.isBlank()) {
            return moradorRepository.findAll(pageRequest);
        }
        String texto = busca.trim();
        String digitos = TelefoneUtil.normalizar(texto);
        if (digitos.length() < DIGITOS_MINIMOS_BUSCA) {
            digitos = "";
        }
        return moradorRepository.buscar(texto, digitos, pageRequest);
    }

    static String validarNome(String nome) {
        String limpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (limpo.isEmpty()) {
            throw new CampoInvalidoException("nome", Mensagens.NOME_OBRIGATORIO);
        }
        if (limpo.indexOf('<') >= 0 || limpo.indexOf('>') >= 0) {
            throw new CampoInvalidoException("nome", Mensagens.NOME_SEM_SIMBOLOS);
        }
        if (limpo.length() > 120) {
            throw new CampoInvalidoException("nome", Mensagens.NOME_MUITO_LONGO);
        }
        return limpo;
    }

    static String validarTelefone(String telefone) {
        String normalizado = TelefoneUtil.normalizar(telefone);
        if (!TelefoneUtil.valido(normalizado)) {
            throw new CampoInvalidoException("telefone", Mensagens.TELEFONE_INVALIDO);
        }
        return normalizado;
    }
}
