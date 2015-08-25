var janelaFilha = null;
var janelaFilhaURL = null;
var janelaFilhaTitulo = null;

function abrirJanelaFilha() {
	janelaFilha = window.open(janelaFilhaURL, janelaFilhaTitulo, '',
			true);
	janelaFilha.onbeforeunload = function() {
		// -- isso aqui da problema quando a janela eh recarregada
		// alert('rodar o fecha janela pelo usuario');
		// janelaFechadaPeloUsuario(); //-- remoteCommand
	}
}
function fecharJanelaFilha() {
	if (janelaFilha != null) {
		janelaFilha.close();
	}
	janelaFilha = null;
}
function recarregarJanelaFilha() {
	if (janelaFilha != null) {
		if (janelaFilha.closed) {
			abrirJanelaFilha();
		} else if (janelaFilha.name != '') {
			janelaFilha.location.reload();
		} else {
			abrirJanelaFilha();
		}
	}
}