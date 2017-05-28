/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import br.edu.uesb.consensospa.enumerado.TipoPacote;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class Consenso implements Runnable {

    private final int id;
    private int rodada;
    private int maiorRodada;
    private List<Integer> quorum;
    private Eleicao eleicao;

    public Consenso(int id, Eleicao eleicao) {
        this.id = id;
        this.eleicao = eleicao;
        this.rodada = 0;
        this.maiorRodada = 0;
        this.quorum = new ArrayList<>();
    }

    @Override
    public void run() {
        while (eleicao.getLider() == id) {
            new Thread(new Proponente(id, rodada, maiorRodada, eleicao, quorum)).start();
        }
    }

}
