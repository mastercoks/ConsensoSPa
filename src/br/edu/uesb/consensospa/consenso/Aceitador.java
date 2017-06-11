/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.consenso;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPedidoAceito;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPrepararPedido;
import br.edu.uesb.consensospa.mensagens.PedidoAceito;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Matheus
 */
public class Aceitador {

    private final Consenso consenso;

    public Aceitador(Consenso consenso) {
        this.consenso = consenso;
    }

    public void iniciar() {
        consenso.getProcesso().getExecutorService().execute(new T2());
        consenso.getProcesso().getExecutorService().execute(new T3());
    }

    private class T2 implements Runnable {

        @Override
        public void run() {
            try {
                NetworkService rede = new NetworkService(8000 + consenso.getProcesso().getId());
                while (true) {
                    if (!consenso.getProcesso().isCrash()) {
                        consenso.setValor(consenso.escolherValor());
                        System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Valor proposto: " + consenso.getValor());
                        Future<Pacote> future = consenso.getProcesso().getExecutorService().submit(rede);
                        Pacote pacote = future.get();
                        PrepararPedido mensagem_recebida = (PrepararPedido) pacote.getMensagem();
                        System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Pacote Recebido: " + pacote + " do o processo " + pacote.getId_origem());
                        if (pacote.getTipo() == TipoPacote.PREPARAR_PEDIDO && mensagem_recebida.getRodada() > consenso.maior(consenso.getRodada(), consenso.getUltima_rodada())) {
                            ConfirmacaoPrepararPedido mensagem = new ConfirmacaoPrepararPedido(mensagem_recebida.getRodada(), consenso.getRodada(), consenso.getValor());
                            pacote = new Pacote(consenso.getProcesso().getId(), pacote.getId_origem(), TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO, mensagem);
                            System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Pacote Enviando: " + pacote + " para o processo " + pacote.getId_destino());
                            consenso.getProcesso().getExecutorService().execute(new Enviar(consenso.getProcesso().getId(), "localhost", 8100 + pacote.getId_destino(), pacote));
                            consenso.setUltima_rodada(mensagem_recebida.getRodada());
                        }
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
//                System.err.println("Processo[" + consenso.getId() + "]: Aceitador T2 finalizado.");
            }
        }
    }

    private class T3 implements Runnable {

        @Override
        public void run() {
            try {
                NetworkService rede = new NetworkService(8200 + consenso.getProcesso().getId());
                while (true) {
                    if (!consenso.getProcesso().isCrash()) {
                        Future<Pacote> future = consenso.getProcesso().getExecutorService().submit(rede);
                        Pacote pacote = future.get();
                        PedidoAceito mensagem_recebida = (PedidoAceito) pacote.getMensagem();
                        System.out.println("Processo[" + consenso.getProcesso().getId() + "]: Pacote Recebido: " + pacote + " do o processo " + pacote.getId_origem());
                        if (pacote.getTipo() == TipoPacote.PEDIDO_ACEITO && mensagem_recebida.getRodada() >= consenso.maior(consenso.getRodada(), consenso.getUltima_rodada())) {
                            consenso.setUltima_rodada(mensagem_recebida.getRodada());
                            consenso.setRodada(mensagem_recebida.getRodada());
                            consenso.setValor(mensagem_recebida.getValor());
                            consenso.setQuorum(mensagem_recebida.getQuorum());
                            ConfirmacaoPedidoAceito mensagem = new ConfirmacaoPedidoAceito(consenso.getRodada(), consenso.getValor());
                            pacote = new Pacote(consenso.getProcesso().getId(), TipoPacote.CONFIRMACAO_PEDIDO_ACEITO, mensagem);
                            consenso.broadcast(8300, pacote);
                        }
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
//                System.err.println("Processo[" + consenso.getId() + "]: Aceitador T3 finalizado.");
            }
        }
    }

}
