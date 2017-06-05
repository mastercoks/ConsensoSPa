/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.main.Principal;
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

    public static void main(String[] args) {
        Principal processos[] = new Principal[6];
        Integer processo_correto = null;
        for (int i = 0; i < processos.length; i++) {
            processos[i] = new Principal(i, processos.length);
            if (i == 0 || i == 3) {
                List<Integer> processos_particao = processos[i].encontrarParticao();
                processo_correto = processos_particao.get(new Random().nextInt(processos_particao.size()));
            }
            processos[i].getProcesso().setCorreto(processo_correto);
        }

        for (Principal processo : processos) {
            try {
                processo.iniciarConsenso();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(TesteConsenso.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
