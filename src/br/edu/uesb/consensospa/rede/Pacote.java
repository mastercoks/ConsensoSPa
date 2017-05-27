/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.rede;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import java.io.Serializable;

/**
 *
 * @author Matheus
 */
public class Pacote implements Serializable{

    private TipoPacote tipo;
    private int mensagem;

    public Pacote(TipoPacote tipo) {
        this.tipo = tipo;
    }
    
    public Pacote(TipoPacote tipo, int mensagem) {
        this.tipo = tipo;
        this.mensagem = mensagem;
    }

    public TipoPacote getTipo() {
        return tipo;
    }

    public int getMensagem() {
        return mensagem;
    }

    public void setTipo(TipoPacote tipo) {
        this.tipo = tipo;
    }

    public void setMensagem(int mensagem) {
        this.mensagem = mensagem;
    }

}
