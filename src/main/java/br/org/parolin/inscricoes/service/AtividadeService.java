package br.org.parolin.inscricoes.service;

import br.org.parolin.inscricoes.dto.AtividadeForm;
import br.org.parolin.inscricoes.dto.AtividadeResumo;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.exception.RegraNegocioException;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.repository.AtividadeRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;

    public AtividadeService(AtividadeRepository atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    @Transactional
    public Atividade cadastrar(String nome, LocalDate data, Integer vagasTotal) {
        String nomeLimpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (nomeLimpo.isEmpty()) {
            throw new CampoInvalidoException("nome", "Informe o nome da atividade.");
        }
        if (nomeLimpo.indexOf('<') >= 0 || nomeLimpo.indexOf('>') >= 0) {
            throw new CampoInvalidoException("nome", "O nome não pode ter os símbolos < ou >.");
        }
        if (nomeLimpo.length() > 150) {
            throw new CampoInvalidoException("nome", "O nome pode ter no máximo 150 caracteres.");
        }
        if (data == null) {
            throw new CampoInvalidoException("dataAtividade", "Informe a data da atividade.");
        }
        if (vagasTotal == null || vagasTotal < 1) {
            throw new CampoInvalidoException("vagasTotal", "O número de vagas deve ser maior que zero.");
        }
        if (vagasTotal > AtividadeForm.VAGAS_MAXIMAS) {
            throw new CampoInvalidoException("vagasTotal", "O número de vagas pode ser no máximo 10000.");
        }
        return atividadeRepository.save(new Atividade(nomeLimpo, data, vagasTotal));
    }

    /** Todas as atividades, por data. */
    @Transactional(readOnly = true)
    public List<AtividadeResumo> listar() {
        return atividadeRepository.listarResumos();
    }

    /** Atividades de hoje em diante: as que aparecem na tela de inscrição. */
    @Transactional(readOnly = true)
    public List<AtividadeResumo> listarAPartirDe(LocalDate data) {
        return atividadeRepository.listarResumosAPartirDe(data);
    }

    @Transactional(readOnly = true)
    public AtividadeResumo resumo(Long id) {
        return atividadeRepository.buscarResumo(id)
                .orElseThrow(() -> new RegraNegocioException("Atividade não encontrada."));
    }
}
