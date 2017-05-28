/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.mensagens;

import java.util.List;

/**
 *
 * @author Matheus
 */
public class PedidoAceito {

    private final int rodada;
    private final int valor;
    private final List<Integer> quorum;

    public PedidoAceito(int rodada, int valor, List<Integer> quorum) {
        this.rodada = rodada;
        this.valor = valor;
        this.quorum = quorum;
    }

    public int getRodada() {
        return rodada;
    }

    public int getValor() {
        return valor;
    }

    public List<Integer> getQuorum() {
        return quorum;
    }

}
