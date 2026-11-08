package br.org.parolin.inscricoes.service;

import br.org.parolin.inscricoes.dto.ResultadoInscricao;
import br.org.parolin.inscricoes.exception.InscricaoDuplicadaException;
import br.org.parolin.inscricoes.exception.NaoEncontradoException;
import br.org.parolin.inscricoes.exception.VagasEsgotadasException;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.model.Inscricao;
import br.org.parolin.inscricoes.model.Morador;
import br.org.parolin.inscricoes.repository.AtividadeRepository;
import br.org.parolin.inscricoes.repository.InscricaoRepository;
import br.org.parolin.inscricoes.repository.MoradorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final MoradorRepository moradorRepository;

    public InscricaoService(InscricaoRepository inscricaoRepository,
                            AtividadeRepository atividadeRepository,
                            MoradorRepository moradorRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.moradorRepository = moradorRepository;
    }

    /**
     * Inscreve o morador se houver vaga, tudo numa transação só.
     * A linha da atividade fica travada (SELECT ... FOR UPDATE) até o commit: quem chega
     * ao mesmo tempo espera e, ao continuar, já conta a inscrição de quem chegou antes.
     * Assim duas pessoas nunca ficam com a mesma última vaga.
     */
    @Transactional
    public ResultadoInscricao inscrever(Long moradorId, Long atividadeId) {
        Atividade atividade = atividadeRepository.findByIdForUpdate(atividadeId)
                .orElseThrow(() -> new NaoEncontradoException("Atividade não encontrada."));
        Morador morador = moradorRepository.findById(moradorId)
                .orElseThrow(() -> new NaoEncontradoException("Morador não encontrado."));

        // Repetida é verificada antes das vagas: quem já está inscrito ouve "já inscrito", não "esgotada".
        if (inscricaoRepository.existsByMorador_IdAndAtividade_Id(moradorId, atividadeId)) {
            throw new InscricaoDuplicadaException();
        }
        long inscritos = inscricaoRepository.countByAtividade_Id(atividadeId);
        if (inscritos >= atividade.getVagasTotal()) {
            throw new VagasEsgotadasException();
        }
        try {
            inscricaoRepository.saveAndFlush(new Inscricao(morador, atividade));
        } catch (DataIntegrityViolationException e) {
            // Segunda linha de defesa: UNIQUE (morador_id, atividade_id) do banco.
            throw new InscricaoDuplicadaException();
        }
        long restantes = atividade.getVagasTotal() - (inscritos + 1);
        return new ResultadoInscricao(restantes, morador.getNome(), atividade.getNome());
    }
}
