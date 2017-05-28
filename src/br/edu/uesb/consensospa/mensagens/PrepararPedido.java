/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.mensagens;

/**
 *
 * @author Matheus
 */
public class PrepararPedido {

    private final int rodada;

    public PrepararPedido(int rodada) {
        this.rodada = rodada;
    }

    public int getRodada() {
        return rodada;
    }

}
