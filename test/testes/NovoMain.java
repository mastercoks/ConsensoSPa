/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.mensagens.ConfirmacaoPedidoAceito;
import br.edu.uesb.consensospa.mensagens.Decisao;
import br.edu.uesb.consensospa.mensagens.PedidoAceito;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.util.ArrayList;
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
public class NovoMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
//        Future<Pacote> future = executorService.submit(new NetworkService(8000));
//        Pacote pacote = new Pacote(0, TipoPacote.PREPARAR_PEDIDO, new PrepararPedido(2));
//        pacote = future.get();
//        PrepararPedido mensagem_recebida = (PrepararPedido) pacote.getMensagem();
//        System.out.println("Pacote Recebido: " + pacote.getTipo() + "(" + mensagem_recebida.getRodada() + ")");

        try {
//            for (int i = 1; i < 6; i++) {
//                ConfirmacaoPedidoAceito mensagem = new ConfirmacaoPedidoAceito(0, TipoValor.STAR_WARS);
//                Pacote pacote = new Pacote(i, TipoPacote.CONFIRMACAO_PEDIDO_ACEITO, mensagem);
//                executorService.execute(new Enviar(i, "localhost", 8300, pacote));
//                System.out.println("Processo[" + pacote.getId_origem() + "]: Pacote Enviado: " + pacote + " para o processo " + pacote.getId_destino());
//            }
            Decisao mensagem = new Decisao(TipoValor.STAR_WARS);
            Pacote pacote = new Pacote(3, TipoPacote.DECISAO, mensagem);
            executorService.execute(new Enviar(3, "localhost", 8300, pacote));
            System.out.println("Processo[" + pacote.getId_origem() + "]: Pacote Enviado: " + pacote + " para o processo " + pacote.getId_destino());
            executorService.shutdown();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(NovoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
