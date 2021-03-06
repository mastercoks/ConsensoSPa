/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.main.Principal20Processos;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class TesteDetectorFalhas20Processos {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Principal20Processos processos[] = new Principal20Processos[20];
        Integer processo_correto = null;
        
        for (int i = 0; i < processos.length; i++) {
            processos[i] = new Principal20Processos(i, processos.length);
            if (i == 0 || i == 5 || i == 10 || i == 15) {
                List<Integer> processos_particao = processos[i].encontrarParticao();
                processo_correto = processos_particao.get(new Random().nextInt(processos_particao.size()));
            }
            processos[i].getProcesso().setCorreto(processo_correto);
        }

        for (Principal20Processos processo : processos) {
            try {
                processo.iniciarDetectorFalhas();
                System.out.println("Processo[" + processo.getProcesso().getId() + "] : " + processo.getProcesso().isCorreto());
            } catch (IOException ex) {
                Logger.getLogger(TesteDetectorFalhas20Processos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//        Principal processos[] = new Principal[6];
//        for (int i = 0; i < processos.length; i++) {
//            processos[i] = new Principal(i, processos.length);
//        }
//
//        for (Principal processo : processos) {
//            try {
//                processo.iniciarDetectorFalhas();
//            } catch (IOException ex) {
//                Logger.getLogger(TesteDetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

}
