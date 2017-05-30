/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoValor;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Matheus
 */
public class Proponente extends ConsensoAbstrato implements Runnable {

    public Proponente(int id, int rodada, int maior_rodada, TipoValor valor, List<Integer> quorum, List<Integer> processos, ExecutorService executorService, Eleicao eleicao) {
        super(id, rodada, maior_rodada, valor, quorum, processos, executorService, eleicao);
    }

    @Override
    public void run() {
        setRodada(maior(getRodada(), getMaior_rodada()) + getEleicao().getProcessos().size());
        //Fase 1 da rodada r: Preparação
//            for (int processo : eleicao.getProcessos()) {
////                if (!eleicao.getDefeituosos().contains(processo) && eleicao.contemVertice(processo)) {
////                    quorum.add(processo);
////                }
////                if (processo != id) {
////                    Pacote pacote = new Pacote(id, processo, TipoPacote.PREPARAR_PEDIDO, new PrepararPedido(rodada));
////                    try {
////                        new Thread(new Enviar(id, "localhost", 8000 + processo, pacote)).start();
////                    } catch (IOException | ClassNotFoundException ex) {
////                        Logger.getLogger(Consenso.class.getName()).log(Level.SEVERE, null, ex);
////                    }
////                }
//            }
    }

}
