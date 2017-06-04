/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.consenso;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPrepararPedido;
import br.edu.uesb.consensospa.mensagens.PedidoAceito;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class Proponente implements Runnable {

//    private final List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;
    private final Consenso consenso;

    public Proponente(Consenso consenso) {
        this.consenso = consenso;
    }

    @Override
    public void run() {
        try {
            boolean aceitou = false;
            NetworkService rede = new NetworkService(8100 + consenso.getId());
            while (!aceitou) {
                consenso.setRodada(consenso.maior(consenso.getRodada(), consenso.getUltima_rodada()) + consenso.getEleicao().getProcessos().size());
                //Fase 1 da rodada r: Preparar pedido
                consenso.setQuorum(consenso.gerarQuorum());
                Pacote pacote = new Pacote(consenso.getId(), TipoPacote.PREPARAR_PEDIDO, new PrepararPedido(consenso.getRodada()));
                consenso.broadcast(8000, pacote);
                
                //Fase 2 da rodade r: Aceitar pedido
                List<TipoValor> respostas = new ArrayList<>();
                respostas.add(consenso.getValor());
                int quant_confirmacoes_recebidas = 0;
                while (true) {
                    Future<Pacote> future = consenso.getExecutorService().submit(rede);
                    pacote = future.get();
                    if (pacote.getTipo() == TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO) {
                        ConfirmacaoPrepararPedido mensagem_recebida = (ConfirmacaoPrepararPedido) pacote.getMensagem();

                        System.out.println("Processo[" + consenso.getId() + "]: Pacote Recebido: "
                                + pacote + " do o processo "
                                + pacote.getId_origem() + " Quorum: " + consenso.getQuorum()
                                + " recebidos = " + quant_confirmacoes_recebidas);

                        consenso.removeAllQuorum(consenso.getEleicao().getDefeituosos()); //verificar se funciona!
                        if (consenso.getQuorum().contains(pacote.getId_origem()) && consenso.getRodada() >= mensagem_recebida.getRodada_origem()) {
                            respostas.add(mensagem_recebida.getValor());
                            quant_confirmacoes_recebidas++;
                            if (quant_confirmacoes_recebidas == consenso.getQuorum().size() - 1) {
                                consenso.setValor(consenso.checarQuorum(respostas, 2));
                                consenso.setQuorum(consenso.gerarQuorum());
                                PedidoAceito mensagem = new PedidoAceito(consenso.getRodada(), consenso.getValor(), consenso.getQuorum());
                                pacote = new Pacote(consenso.getId(), TipoPacote.PEDIDO_ACEITO, mensagem);
                                consenso.broadcast(8200, pacote);
                                System.out.println("Processo[" + consenso.getId() + "]: quorum = " + respostas);
                                aceitou = true;
                                break;
                            }
                        }
                    }
                }

                //Fase 2 da rodada r: Aceitar pedido
//                int quorum = 1;
//                List<TipoValor> respostas = new ArrayList<>();
//                respostas.add(getValor());
//                while (quorum < ((getQuorum().size() / 2) + 1) || getQuorum().size() == respostas.size()) {
//                    Future<Pacote> future = getExecutorService().submit(new NetworkService(8100 + getId()));
//                    pacote = future.get();
//                    setQuorum(gerarQuorum());
//                    ConfirmacaoPrepararPedido mensagem_recebida = (ConfirmacaoPrepararPedido) pacote.getMensagem();
//                    respostas.add(mensagem_recebida.getValor());
//                    quorum = checarQuorum(respostas);
//                }
//                if (pacote.getTipo() == TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO) {
////                    setValor(); //
//                    setQuorum(gerarQuorum());
//                    pacote = new Pacote(getId(), TipoPacote.PEDIDO_ACEITO, new PedidoAceito(getRodada(), getValor(), getQuorum()));
//                    broadcast(8100, pacote);
//                    aceitou = true;
//                }
            }
            rede.fecharServidor();
        } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
            Logger.getLogger(Proponente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
