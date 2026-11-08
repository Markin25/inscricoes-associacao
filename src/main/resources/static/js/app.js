// Único script da aplicação (sem bibliotecas de terceiros).
// Ao abrir a tela, leva o foco para o aviso principal, para o leitor de tela anunciar
// o resultado e quem usa teclado continuar dali.
document.addEventListener('DOMContentLoaded', function () {
  var alvo = document.querySelector('[data-foco]');
  if (alvo) {
    try { alvo.focus({ preventScroll: false }); } catch (e) { alvo.focus(); }
  }
});
