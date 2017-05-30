/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import br.edu.uesb.consensospa.Processo;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class TesteConsenso {

    public static void main(String[] args) {
        Processo processo;
        try {
            processo = new Processo(0, 6);
            processo.iniciarConsenso();
            System.out.println(processo.getConsenso().getValor());
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(TesteConsenso.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
