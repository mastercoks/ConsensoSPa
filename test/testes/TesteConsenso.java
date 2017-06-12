/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.main.Principal6Processos;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class TesteConsenso {

    public static void main(String[] args) throws IOException {
        Principal6Processos processos[] = new Principal6Processos[6];
        Integer processo_correto = null;
        for (int i = 0; i < processos.length; i++) {
            processos[i] = new Principal6Processos(i, processos.length);
            if (i == 0 || i == 3) {
                List<Integer> processos_particao = processos[i].encontrarParticao();
                processo_correto = processos_particao.get(new Random().nextInt(processos_particao.size()));
            }
            processos[i].getProcesso().setCorreto(processo_correto);
        }

        for (Principal6Processos processo : processos) {
            try {
                processo.iniciarConsenso();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(TesteConsenso.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
