/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPrepararPedido;
import br.edu.uesb.consensospa.mensagens.PedidoAceito;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Matheus
 */
public class Proponente extends ConsensoAbstrato implements Runnable {

    private final List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;

    public Proponente(int id, int rodada, int maior_rodada, TipoValor valor, List<Integer> quorum, ExecutorService executorService, Eleicao eleicao, List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas) {
        super(id, rodada, maior_rodada, valor, quorum, executorService, eleicao);
        this.particoes_sincronas = particoes_sincronas;
    }

    @Override
    public void run() {
        try {
            boolean aceitou = false;
            NetworkService rede = new NetworkService(8100 + getId());
            while (!aceitou) {
                setRodada(maior(getRodada(), getUltima_rodada()) + getEleicao().getProcessos().size());
                //Fase 1 da rodada r: Preparar pedido
                setQuorum(gerarQuorum());
                Pacote pacote = new Pacote(getId(), TipoPacote.PREPARAR_PEDIDO, new PrepararPedido(getRodada()));
                broadcast(8000, pacote);

                int quant_confirmacoes_recebidas = 0;
                while (true) {
                    Future<Pacote> future = getExecutorService().submit(rede);
                    pacote = future.get();
                    if (pacote.getTipo() == TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO) {
                        ConfirmacaoPrepararPedido mensagem_recebida = (ConfirmacaoPrepararPedido) pacote.getMensagem();

                        System.out.println("Processo[" + getId() + "]: Pacote Recebido: "
                                + pacote + " do o processo "
                                + pacote.getId_origem() + " Quorum: " + getQuorum()
                                + " recebidos = " + quant_confirmacoes_recebidas);

                        removeAllQuorum(getEleicao().getDefeituosos()); //verificar se funciona!
                        if (getQuorum().contains(pacote.getId_origem())) {
                            quant_confirmacoes_recebidas++;
                            if (quant_confirmacoes_recebidas == getQuorum().size() - 1) {
                                setValor(mensagem_recebida.getValor());
                                setQuorum(gerarQuorum());
                                PedidoAceito mensagem = new PedidoAceito(getRodada(), getValor(), getQuorum());
                                pacote = new Pacote(getId(), TipoPacote.PEDIDO_ACEITO, mensagem);
                                broadcast(8200, pacote);
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
        } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
            Logger.getLogger(Proponente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean contemVertice(int vertice) {
        return particoes_sincronas.stream().anyMatch((particao_sincrona) -> (particao_sincrona.containsVertex(vertice)));
    }

    private List<Integer> gerarQuorum() {
        List<Integer> q = new ArrayList<>();
        getProcessos().stream().filter((processo) -> (!getEleicao().getDefeituosos().contains(processo) && contemVertice(processo))).forEach((processo) -> {
            q.add(processo);
        });
        return q;
    }

    private int checarQuorum(List<TipoValor> respostas) {
        int quant_sw = 0, quant_st = 0, quant_n = 0;
        for (TipoValor resposta : respostas) {
            switch (resposta) {
                case STAR_WARS:
                    quant_sw++;
                    break;
                case STAR_TREK:
                    quant_st++;
                    break;
                case NENHUMA:
                    quant_n++;
                    break;
            }
        }
        return maior(quant_sw, maior(quant_st, quant_n));
    }

}
