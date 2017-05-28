/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.NetworkService;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.util.ArrayList;
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
public class NovoMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
//            while (true) {
            Future<Pacote> future = executorService.submit(new NetworkService(8000));
            int[] mensagem = {0, 1, 10};
            executorService.execute(new Enviar(0, "localhost", 8000, new Pacote(0, 1, TipoPacote.CONFIRMACAO_PREPARAR_PEDIDO, mensagem)));
            Pacote pacote = future.get();
            int[] mensagem_recebida = (int[]) pacote.getMensagem();
            System.out.println("Pacote Recebido: " + pacote.getTipo() + "(" + mensagem_recebida[0] +"," + mensagem_recebida[1] + "," + mensagem_recebida[2] + ")");
            executorService.shutdown();
//            }
        } catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(NovoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
