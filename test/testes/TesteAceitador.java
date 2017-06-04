/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.Aprendiz;
import br.edu.uesb.consensospa.Eleicao;
import br.edu.uesb.consensospa.Processo;
import br.edu.uesb.consensospa.enumerado.TipoValor;
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
public class TesteAceitador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
//        List<Integer> quorum = new ArrayList<>();
        List<Integer> defeituosos = new ArrayList<>();
        defeituosos.add(5);
        try {
            Processo processo = new Processo(0, 6);
            processo.addProcessos();
            processo.addParticoesSincronas();
            Eleicao eleicao = new Eleicao(0, processo.getProcessos(), defeituosos, processo.getParticoes_sincronas().get(0));
            Future<TipoValor> future = executorService.submit(new Aprendiz(0, 0, processo.getProcessos(), executorService, eleicao));
            System.out.println(future.get());
            executorService.shutdown();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(TesteAceitador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
