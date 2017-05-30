/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPedidoAceito;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPrepararPedido;
import br.edu.uesb.consensospa.mensagens.PedidoAceito;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class Aceitador extends ConsensoAbstrato {

    public Aceitador(int id, int rodada, int maior_rodada, TipoValor valor, List<Integer> quorum, List<Integer> processos, ExecutorService executorService) {
        super(id, rodada, maior_rodada, valor, quorum, processos, executorService);
    }

    public void iniciar() {
        getExecutorService().execute(new T2());
        getExecutorService().execute(new T3());
    }

    private class T2 implements Runnable {

        @Override
        public void run() {

            try {
                while (true) {
                    Future<Pacote> future = getExecutorService().submit(new NetworkService(8000 + getId()));
                    Pacote pacote = future.get();
                    PrepararPedido mensagem_recebida = (PrepararPedido) pacote.getMensagem();
                    if (pacote.getTipo() == TipoPacote.PREPARAR_PEDIDO && mensagem_recebida.getRodada() > maior(getRodada(), getMaior_rodada())) {
                        ConfirmacaoPrepararPedido mensagem = new ConfirmacaoPrepararPedido(mensagem_recebida.getRodada(), getRodada(), getValor());
                        pacote = new Pacote(getId(), pacote.getId_origem(), TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO, mensagem);
                        getExecutorService().execute(new Enviar(getId(), "localhost", 8100 + getId(), pacote));
                        setMaior_rodada(mensagem_recebida.getRodada());
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
                Logger.getLogger(Aceitador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class T3 implements Runnable {

        @Override
        public void run() {

            try {
                while (true) {
                    Future<Pacote> future = getExecutorService().submit(new NetworkService(8200 + getId()));
                    Pacote pacote = future.get();
                    PedidoAceito mensagem_recebida = (PedidoAceito) pacote.getMensagem();
                    if (pacote.getTipo() == TipoPacote.PEDIDO_ACEITO && mensagem_recebida.getRodada() >= maior(getRodada(), getMaior_rodada())) {
                        setMaior_rodada(mensagem_recebida.getRodada());
                        setRodada(mensagem_recebida.getRodada());
                        setValor(mensagem_recebida.getValor());
                        setQuorum(mensagem_recebida.getQuorum());
                        ConfirmacaoPedidoAceito mensagem = new ConfirmacaoPedidoAceito(getRodada(), getValor());
                        pacote = new Pacote(getId(), TipoPacote.CONFIRMACAO_PEDIDO_ACEITO, mensagem);
                        broadcast(8300, pacote);
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
                Logger.getLogger(Aceitador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
