package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPedidoAceito;
import br.edu.uesb.consensospa.mensagens.Decisao;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Aprendiz extends ConsensoAbstrato implements Callable<TipoValor> {

    public Aprendiz(int id, int rodada, List<Integer> quorum, List<Integer> processos, ExecutorService executorService, Eleicao eleicao) {
        super(id, rodada, quorum, processos, executorService, eleicao);
    }

    @Override
    public TipoValor call() throws IOException, UnknownHostException, ClassNotFoundException, InterruptedException, ExecutionException {
        while (true) {
            Future<Pacote> future = getExecutorService().submit(new NetworkService(8000 + getId()));
            Pacote pacote = future.get();
            if (pacote.getTipo() == TipoPacote.CONFIRMACAO_PEDIDO_ACEITO) {
                ConfirmacaoPedidoAceito mensagem_recebida = (ConfirmacaoPedidoAceito) pacote.getMensagem();
                getQuorum().removeAll(getEleicao().getDefeituosos()); //verificar se funciona!
                if (getRodada() == mensagem_recebida.getRodada() && getQuorum().contains(pacote.getId_origem())) {
                    TipoValor valor = mensagem_recebida.getValor();
                    Decisao mensagem = new Decisao(valor);
                    pacote = new Pacote(getId(), TipoPacote.DECISAO, mensagem);
                    broadcast(8300, pacote);
                    return valor;
                }
            } else if (pacote.getTipo() == TipoPacote.DECISAO) {
                TipoValor valor = ((Decisao) pacote.getMensagem()).getValor();
                Decisao mensagem = new Decisao(valor);
                pacote = new Pacote(getId(), TipoPacote.DECISAO, mensagem);
                broadcast(8300, pacote);
                return valor;
            }
        }
    }
}
