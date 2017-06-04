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
public class Decisao implements Serializable {

    private final TipoValor valor;

    public Decisao(TipoValor valor) {
        this.valor = valor;
    }

    public TipoValor getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return "(" + valor + ")";
    }

}
