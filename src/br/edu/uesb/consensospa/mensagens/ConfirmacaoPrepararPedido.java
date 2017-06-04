/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.mensagens;

import br.edu.uesb.consensospa.enumerado.TipoValor;
import java.io.Serializable;

/**
 *
 * @author Matheus
 */
public class ConfirmacaoPrepararPedido implements Serializable {

    private final int rodada_origem;
    private final int rodada_destino;
    private final TipoValor valor;

    public ConfirmacaoPrepararPedido(int rodada_origem, int rodada_destino, TipoValor valor) {
        this.rodada_origem = rodada_origem;
        this.rodada_destino = rodada_destino;
        this.valor = valor;
    }

    public int getRodada_origem() {
        return rodada_origem;
    }

    public int getRodada_destino() {
        return rodada_destino;
    }

    public TipoValor getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return "(" + rodada_origem + ", " + rodada_destino + ", " + valor;
    }

}
