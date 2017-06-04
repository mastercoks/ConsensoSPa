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
import java.net.UnknownHostException;
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
                NetworkService rede = new NetworkService(8000 + getId());
                while (true) {
                    Future<Pacote> future = getExecutorService().submit(rede);
                    Pacote pacote = future.get();
                    PrepararPedido mensagem_recebida = (PrepararPedido) pacote.getMensagem();
                    System.out.println("Processo[" + getId() + "]: Pacote Recebido: " + pacote + " do o processo " + pacote.getId_origem());
                    if (pacote.getTipo() == TipoPacote.PREPARAR_PEDIDO && mensagem_recebida.getRodada() > maior(getRodada(), getUltima_rodada())) {
                        ConfirmacaoPrepararPedido mensagem = new ConfirmacaoPrepararPedido(mensagem_recebida.getRodada(), getRodada(), getValor());
                        pacote = new Pacote(getId(), pacote.getId_origem(), TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO, mensagem);
                        System.out.println("Processo[" + getId() + "]: Pacote Enviando: " + pacote + " para o processo " + pacote.getId_destino());
                        getExecutorService().execute(new Enviar(getId(), "localhost", 8100 + pacote.getId_destino(), pacote));
                        setUltima_rodada(mensagem_recebida.getRodada());
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
                NetworkService rede = new NetworkService(8200 + getId());
                while (true) {
                    Future<Pacote> future = getExecutorService().submit(rede);
                    Pacote pacote = future.get();
                    PedidoAceito mensagem_recebida = (PedidoAceito) pacote.getMensagem();
                    System.out.println("Processo[" + getId() + "]: Pacote Recebido: " + pacote + " do o processo " + pacote.getId_origem());
                    if (pacote.getTipo() == TipoPacote.PEDIDO_ACEITO && mensagem_recebida.getRodada() >= maior(getRodada(), getUltima_rodada())) {
                        setUltima_rodada(mensagem_recebida.getRodada());
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
