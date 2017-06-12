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

/**
 *
 * @author Matheus
 */
public class Proponente implements Runnable {

    private final Consenso consenso;

    public Proponente(Consenso consenso) {
        this.consenso = consenso;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
//            boolean aceitou = false;
            NetworkService rede = new NetworkService(8100 + consenso.getProcesso().getId());
            while (!consenso.getProcesso().isAceitou() && !consenso.getProcesso().isCrash()) {
                System.out.println("--------------- Rodada " + (consenso.getRodada() / consenso.getProcesso().getQuant_processos() + 1)
                        + " ---------------" + consenso.getProcesso().getDefeituosos());
                consenso.setRodada(consenso.maior(consenso.getRodada(), consenso.getUltima_rodada())
                        + consenso.getProcesso().getQuant_processos());
                //Fase 1 da rodada r: Preparar pedido
                consenso.setQuorum(consenso.gerarQuorum());
                Pacote pacote = new Pacote(consenso.getProcesso().getId(), TipoPacote.PREPARAR_PEDIDO, new PrepararPedido(consenso.getRodada()));
                consenso.broadcast(8000, pacote);

                //Fase 2 da rodade r: Aceitar pedido
                List<TipoValor> respostas = new ArrayList<>();
                consenso.setValor(consenso.escolherValor());
                System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Valor proposto: " + consenso.getValor());
                respostas.add(consenso.getValor());
                int quant_confirmacoes_recebidas = 0;
                while (!consenso.getProcesso().isCrash()) {
                    Future<Pacote> future = consenso.getProcesso().getExecutorService().submit(rede);
                    pacote = future.get();
                    Thread.sleep(500);
                    if (pacote.getTipo() == TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO) {
                        ConfirmacaoPrepararPedido mensagem_recebida = (ConfirmacaoPrepararPedido) pacote.getMensagem();

                        System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Pacote Recebido: "
                                + pacote + " do o processo "
                                + pacote.getId_origem() + " Quorum: " + consenso.getQuorum()
                                + " recebidos = " + quant_confirmacoes_recebidas);

                        consenso.removeAllQuorum(consenso.getProcesso().getDefeituosos()); //verificar se funciona!
                        if (consenso.getQuorum().contains(pacote.getId_origem()) && consenso.getRodada() >= mensagem_recebida.getRodada_origem()) {
                            respostas.add(mensagem_recebida.getValor());
                            quant_confirmacoes_recebidas++;
                            if (quant_confirmacoes_recebidas == consenso.getQuorum().size() - 1) {
                                TipoValor valor = consenso.checarQuorum(respostas, ((consenso.getQuorum().size() / 2) + 1));
                                System.out.println("Processo[" + consenso.getProcesso().getId() + "]: quorum = " + respostas);
                                if (valor != null) {
                                    consenso.setValor(valor);
                                    consenso.setQuorum(consenso.gerarQuorum());
                                    PedidoAceito mensagem = new PedidoAceito(consenso.getRodada(), consenso.getValor(), consenso.getQuorum());
                                    pacote = new Pacote(consenso.getProcesso().getId(), TipoPacote.PEDIDO_ACEITO, mensagem);
                                    consenso.broadcast(8200, pacote);

                                    consenso.getProcesso().setAceitou(true);
//                                    aceitou = true;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            rede.fecharServidor();
        } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
            System.err.println("Erro no Propenente: " + ex);
        }
    }

}
