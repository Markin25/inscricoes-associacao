// Único script da aplicação (sem bibliotecas de terceiros).
// Ao abrir a tela, leva o foco para o que importa, para o leitor de tela anunciar
// e quem usa teclado continuar dali. Ordem: aviso de erro/resultado (role=alert),
// depois o primeiro campo com erro, depois qualquer outro aviso marcado.
document.addEventListener('DOMContentLoaded', function () {
  var alvo = document.querySelector('[data-foco][role="alert"]')
    || document.querySelector('[aria-invalid="true"]')
    || document.querySelector('[data-foco]');
  if (alvo) {
    try { alvo.focus({ preventScroll: false }); } catch (e) { alvo.focus(); }
  }
});
