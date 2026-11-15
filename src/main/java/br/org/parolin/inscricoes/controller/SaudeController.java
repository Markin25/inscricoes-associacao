package br.org.parolin.inscricoes.controller;

import br.org.parolin.inscricoes.service.SaudeService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rotas públicas para o monitoramento (UptimeRobot). Respondem só "ok" ou "indisponivel".
 * /saude não toca no banco: pode ser chamada a cada 5 minutos sem impedir o banco gratuito
 * (Neon) de entrar em repouso. /saude/banco confere o banco e deve ser chamada com intervalo longo.
 */
@RestController
public class SaudeController {

    private final SaudeService saudeService;

    public SaudeController(SaudeService saudeService) {
        this.saudeService = saudeService;
    }

    @GetMapping("/saude")
    public ResponseEntity<String> aplicacao() {
        return resposta(HttpStatus.OK, "ok");
    }

    @GetMapping("/saude/banco")
    public ResponseEntity<String> banco() {
        return saudeService.bancoDisponivel()
                ? resposta(HttpStatus.OK, "ok")
                : resposta(HttpStatus.SERVICE_UNAVAILABLE, "indisponivel");
    }

    private static ResponseEntity<String> resposta(HttpStatus status, String texto) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_PLAIN)
                .cacheControl(CacheControl.noStore())
                .body(texto);
    }
}
