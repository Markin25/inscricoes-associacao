package br.org.parolin.inscricoes.dto;

import br.org.parolin.inscricoes.util.TelefoneUtil;
import java.time.LocalDateTime;

public record InscritoResumo(String nome, String telefone, LocalDateTime inscritoEm) {

    public String telefoneFormatado() {
        return TelefoneUtil.formatar(telefone);
    }
}
