/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.mensagens.PrepararPedido;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public class Proponente implements Runnable {

        private final int id;
        private int rodada;
        private int maior_rodada;
        private final Eleicao eleicao;
        private List<Integer> quorum;

        public Proponente(int id, int rodada, int maior_rodada, Eleicao eleicao, List<Integer> quorum) {
            this.id = id;
            this.rodada = rodada;
            this.maior_rodada = maior_rodada;
            this.eleicao = eleicao;
            this.quorum = quorum;
        }

        @Override
        public void run() {
            rodada = maior(rodada, maior_rodada) + eleicao.getProcessos().size();
            //Fase 1 da rodada r: Preparação
            for (int processo : eleicao.getProcessos()) {
                if (!eleicao.getDefeituosos().contains(processo) && eleicao.contemVertice(processo)) {
                    quorum.add(processo);
                }
                if (processo != id) {
                    Pacote pacote = new Pacote(id, processo, TipoPacote.PREPARAR_PEDIDO, new PrepararPedido(rodada));
                    try {
                        new Thread(new Enviar(id, "localhost", 8000 + processo, pacote)).start();
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(Consenso.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        private int maior(int rodada, int maior_rodada) {
            if (rodada > maior_rodada) {
                return rodada;
            } else {
                return maior_rodada;
            }
        }
    }
