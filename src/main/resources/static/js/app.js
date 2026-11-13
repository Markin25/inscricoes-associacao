// Único script da aplicação (sem bibliotecas de terceiros). Tudo funciona sem ele;
// ele só melhora: foco no aviso certo e o guia de etapas da inscrição.
document.addEventListener('DOMContentLoaded', function () {
  // Ao abrir a tela, leva o foco para o que importa, para o leitor de tela anunciar
  // e quem usa teclado continuar dali. Ordem: aviso de erro/resultado (role=alert),
  // depois o primeiro campo com erro, depois qualquer outro aviso marcado.
  var alvo = document.querySelector('[data-foco][role="alert"]')
    || document.querySelector('[aria-invalid="true"]')
    || document.querySelector('[data-foco]');
  if (alvo) {
    try { alvo.focus({ preventScroll: false }); } catch (e) { alvo.focus(); }
  }

  // Inscrição: ao marcar uma atividade, atualiza o resumo e o guia de etapas
  // (2. Atividade concluída, 3. Confirmar passa a ser a etapa atual).
  var resumo = document.getElementById('resumo-atividade');
  var etapaAtividade = document.querySelector('.etapas [data-etapa="atividade"]');
  var etapaConfirmar = document.querySelector('.etapas [data-etapa="confirmar"]');
  document.querySelectorAll('input[name="atividadeId"]').forEach(function (opcao) {
    opcao.addEventListener('change', function () {
      if (resumo) {
        resumo.textContent = opcao.getAttribute('data-nome');
      }
      if (resumo && etapaAtividade && etapaConfirmar) {
        marcarEtapa(etapaAtividade, 'feita', '✓', ' (concluída)');
        marcarEtapa(etapaConfirmar, 'atual', '3', ' (etapa atual)');
      }
    });
  });

  // Botão "enviando": ao enviar, mostra o texto de data-enviando e ignora um segundo clique
  // (evita a dúvida "cliquei duas vezes?"). O servidor também recusa inscrição repetida.
  document.querySelectorAll('form').forEach(function (form) {
    var botao = form.querySelector('button[type="submit"][data-enviando]');
    if (!botao) { return; }
    form.addEventListener('submit', function (evento) {
      if (form.getAttribute('data-enviado') === 'sim') { evento.preventDefault(); return; }
      form.setAttribute('data-enviado', 'sim');
      botao.setAttribute('data-texto-original', botao.textContent);
      botao.textContent = botao.getAttribute('data-enviando');
      botao.setAttribute('aria-busy', 'true');
      botao.classList.add('enviando');
    });
  });
  // Voltar pelo navegador reabre a página da memória: devolve os botões ao normal.
  window.addEventListener('pageshow', function (evento) {
    if (!evento.persisted) { return; }
    document.querySelectorAll('form[data-enviado]').forEach(function (form) {
      form.removeAttribute('data-enviado');
      var botao = form.querySelector('button.enviando');
      if (botao) {
        botao.textContent = botao.getAttribute('data-texto-original');
        botao.removeAttribute('aria-busy');
        botao.classList.remove('enviando');
      }
    });
  });

  function marcarEtapa(li, estado, numero, textoLeitor) {
    li.className = estado;
    if (estado === 'atual') { li.setAttribute('aria-current', 'step'); } else { li.removeAttribute('aria-current'); }
    li.querySelector('.num').textContent = numero;
    var oculto = li.querySelector('.visually-hidden');
    if (oculto) { oculto.textContent = textoLeitor; }
  }
});
