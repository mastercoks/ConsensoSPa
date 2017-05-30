/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.mensagens;

import br.edu.uesb.consensospa.enumerado.TipoValor;

/**
 *
 * @author Matheus
 */
public class ConfirmacaoPedidoAceito {

    private final int rodada;
    private final TipoValor valor;

    public ConfirmacaoPedidoAceito(int rodada, TipoValor valor) {
        this.rodada = rodada;
        this.valor = valor;
    }

    public int getRodada() {
        return rodada;
    }

    public TipoValor getValor() {
        return valor;
    }

}
