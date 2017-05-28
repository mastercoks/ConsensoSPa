/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class Aceitador {

    private final int id;
    private final ExecutorService executorService;
    private int rodada;
    private int maior_rodada;
    private int valor;
    private List<Integer> quorum;
    private final List<Integer> processos;

    public Aceitador(int id, int rodada, int maior_rodada, int valor, List<Integer> quorum, List<Integer> processos) {
        this.id = id;
        this.rodada = rodada;
        this.maior_rodada = maior_rodada;
        this.valor = valor;
        this.quorum = quorum;
        this.processos = processos;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public void iniciar() {
        executorService.execute(new T2());
        executorService.execute(new T3());
    }

    private class T2 implements Runnable {

        @Override
        public void run() {

            try {
                while (true) {
                    Future<Pacote> future = executorService.submit(new NetworkService(8000 + id));
                    Pacote pacote = future.get();
                    PrepararPedido mensagem_recebida = (PrepararPedido) pacote.getMensagem();
                    if (mensagem_recebida.getRodada() > maior(rodada, maior_rodada)) {
                        ConfirmacaoPrepararPedido mensagem = new ConfirmacaoPrepararPedido(mensagem_recebida.getRodada(), rodada, valor);
                        pacote = new Pacote(id, pacote.getId_origem(), TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO, mensagem);
                        executorService.execute(new Enviar(id, "localhost", 8100 + id, pacote));
                        maior_rodada = mensagem_recebida.getRodada();
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
                    Future<Pacote> future = executorService.submit(new NetworkService(8200 + id));
                    Pacote pacote = future.get();
                    PedidoAceito mensagem_recebida = (PedidoAceito) pacote.getMensagem();
                    if (mensagem_recebida.getRodada() >= maior(rodada, maior_rodada)) {
                        maior_rodada = mensagem_recebida.getRodada();
                        rodada = mensagem_recebida.getRodada();
                        valor = mensagem_recebida.getValor();
                        quorum = mensagem_recebida.getQuorum();
                        ConfirmacaoPedidoAceito mensagem = new ConfirmacaoPedidoAceito(rodada, valor);
                        pacote = new Pacote(id, pacote.getId_origem(), TipoPacote.CONFIRMACAO_PEDIDO_ACEITO, mensagem);
                        broadcast(8300, pacote);
                    }
                }
            } catch (IOException | InterruptedException | ExecutionException | ClassNotFoundException ex) {
                Logger.getLogger(Aceitador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void broadcast(int porta, Pacote pacote) throws IOException, UnknownHostException, ClassNotFoundException {
        for (int processo : processos) {
            if (processo != id) {
                executorService.execute(new Enviar(id, "localhost", porta + processo, pacote));
            }
        }
    }

    private int maior(int rodada, int maiorRodada) {
        if (rodada > maiorRodada) {
            return rodada;
        } else {
            return maiorRodada;
        }
    }

}
