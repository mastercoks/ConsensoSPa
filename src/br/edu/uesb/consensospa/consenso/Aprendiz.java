package br.edu.uesb.consensospa.consenso;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPedidoAceito;
import br.edu.uesb.consensospa.mensagens.Decisao;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Aprendiz implements Callable<TipoValor> {

    private final Consenso consenso;

    public Aprendiz(Consenso consenso) {
        this.consenso = consenso;
    }

    @Override
    public TipoValor call() throws IOException, UnknownHostException, ClassNotFoundException, InterruptedException, ExecutionException {
        int quant_confirmacoes_recebidas = 0;
        NetworkService rede = new NetworkService(8300 + consenso.getProcesso().getId());
        while (true) {
            Future<Pacote> future = consenso.getProcesso().getExecutorService().submit(rede);
            Pacote pacote = future.get();
            if (pacote.getTipo() == TipoPacote.CONFIRMACAO_PEDIDO_ACEITO) {
                ConfirmacaoPedidoAceito mensagem_recebida = (ConfirmacaoPedidoAceito) pacote.getMensagem();

                System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Pacote Recebido: "
                        + pacote + " do o processo "
                        + pacote.getId_origem() + " Quorum: " + consenso.getQuorum()
                        + " recebidos = " + quant_confirmacoes_recebidas);

                consenso.removeAllQuorum(consenso.getProcesso().getEleicao().getDefeituosos()); //verificar se funciona!
                if (consenso.getRodada() == mensagem_recebida.getRodada() && consenso.getQuorum().contains(pacote.getId_origem())) {
                    quant_confirmacoes_recebidas++;
                    if (quant_confirmacoes_recebidas == consenso.getQuorum().size() - 1) {
                        TipoValor valor = mensagem_recebida.getValor();
                        Decisao mensagem = new Decisao(valor);
                        pacote = new Pacote(consenso.getProcesso().getId(), TipoPacote.DECISAO, mensagem);
                        consenso.broadcast(8300, pacote);
                        rede.fecharServidor();
                        return valor;
                    }
                }
            } else if (pacote.getTipo() == TipoPacote.DECISAO) {
                System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Pacote Recebido: " + pacote + " do o processo " + pacote.getId_origem());
                TipoValor valor = ((Decisao) pacote.getMensagem()).getValor();
                Decisao mensagem = new Decisao(valor);
                pacote = new Pacote(consenso.getProcesso().getId(), TipoPacote.DECISAO, mensagem);
                consenso.broadcast(8300, pacote);
                rede.fecharServidor();
                return valor;
            }
        }
    }

}
