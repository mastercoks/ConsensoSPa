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
public class Pacote implements Serializable {

    private int id_origem;
    private int id_destino;
    private TipoPacote tipo;
    private Object mensagem;

    public Pacote(int id_origem, int id_destino, TipoPacote tipo) {
        this.id_origem = id_origem;
        this.id_destino = id_destino;
        this.tipo = tipo;
    }

    public Pacote(int id_origem, TipoPacote tipo, Object mensagem) {
        this.id_origem = id_origem;
        this.tipo = tipo;
        this.mensagem = mensagem;
    }

    public Pacote(int id_origem, int id_destino, TipoPacote tipo, Object mensagem) {
        this.id_origem = id_origem;
        this.id_destino = id_destino;
        this.tipo = tipo;
        this.mensagem = mensagem;
    }

    public int getId_origem() {
        return id_origem;
    }

    public void setId_origem(int id_origem) {
        this.id_origem = id_origem;
    }

    public int getId_destino() {
        return id_destino;
    }

    public void setId_destino(int id_destino) {
        this.id_destino = id_destino;
    }

    public TipoPacote getTipo() {
        return tipo;
    }

    public Object getMensagem() {
        return mensagem;
    }

    public void setTipo(TipoPacote tipo) {
        this.tipo = tipo;
    }

    public void setMensagem(int mensagem) {
        this.mensagem = mensagem;
    }

    @Override
    public String toString() {
        return tipo.toString() + mensagem.toString();
    }

}
