/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.detectorfalhas.Processo;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class TesteDetectorFalhas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
                Processo processo[] = new Processo[6];
                for (int i = 0; i < processo.length; i++) {
                    try {
                        processo[i] = new Processo(i, processo.length);
                    } catch (IOException ex) {
                        Logger.getLogger(TesteDetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
        
                for (Processo processo1 : processo) {
                    try {
                        processo1.iniciarDetectorFalhas();
                    } catch (IOException ex) {
                        Logger.getLogger(TesteDetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
//        try {
//            Thread.sleep(15000);
//            processo[0].addDefeituosos();
//            System.out.println("PARA");
//        } catch (InterruptedException ex) {
//            Logger.getLogger(TesteDetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
//        Processo processo;
//        try {
//            processo = new Processo(1);
//            processo.iniciarDetectorFalhas();
//        } catch (IOException ex) {
//            Logger.getLogger(TesteDetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
