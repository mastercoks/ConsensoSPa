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
public class Decisao {

    private final int valor;

    public Decisao(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

}
