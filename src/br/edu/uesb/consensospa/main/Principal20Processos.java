/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.main;

import br.edu.uesb.consensospa.detectorfalhas.Processo;
import br.edu.uesb.consensospa.enumerado.TipoQos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * @author Matheus
 */
public final class Principal20Processos {

    public int quant_processos;
    private final Processo processo;

    public Principal20Processos(int id, int quant_processos) {
        this.quant_processos = quant_processos;
        this.processo = new Processo(id, quant_processos, newProcessos(), newParticoesSincronas(), newQoS());
        this.processo.novoEleicao();
    }

    public List<Integer> encontrarParticao() {
        List<Integer> processos_particao = new ArrayList<>();
        processo.getParticoes_sincronas().stream().filter((particao_sincrona) -> (particao_sincrona.containsVertex(processo.getId()))).forEach((particao_sincrona) -> {
            processo.getProcessos().stream().filter((processo_aux) -> (particao_sincrona.containsVertex(processo_aux))).forEach((processo_aux) -> {
                processos_particao.add(processo_aux);
            });
        });
        return processos_particao;
    }

    public void iniciarConsenso() throws InterruptedException, ExecutionException, IOException {
        processo.novoConsenso();
        processo.getConsenso().iniciar();
    }

    public void iniciarDetectorFalhas() throws IOException {
        processo.novoConsenso();
        processo.getConsenso().novoDetectorFalhas();
        processo.getConsenso().getDetectorFalhas().iniciar();
    }

    private List<Integer> newProcessos() {
        List<Integer> processos = new ArrayList<>();
        for (int i = 0; i < quant_processos; i++) {
            processos.add(i);
        }
        return processos;
    }

    public List<DirectedGraph<Integer, DefaultEdge>> newParticoesSincronas() {
        List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas = new ArrayList<>();
        //Partição Sincrona 1
        DirectedGraph<Integer, DefaultEdge> particao_sincrona_0 = new SimpleDirectedGraph<>(DefaultEdge.class);
        particao_sincrona_0.addVertex(0);
        particao_sincrona_0.addVertex(1);
        particao_sincrona_0.addVertex(2);
        particao_sincrona_0.addVertex(3);
        particao_sincrona_0.addVertex(4);
        particao_sincrona_0.addEdge(0, 1);
        particao_sincrona_0.addEdge(0, 2);
        particao_sincrona_0.addEdge(0, 3);
        particao_sincrona_0.addEdge(0, 4);
        particao_sincrona_0.addEdge(1, 2);
        particao_sincrona_0.addEdge(1, 3);
        particao_sincrona_0.addEdge(1, 4);
        particao_sincrona_0.addEdge(2, 3);
        particao_sincrona_0.addEdge(2, 4);
        particao_sincrona_0.addEdge(3, 4);
        particoes_sincronas.add(particao_sincrona_0);

        //Partição Sincrona 2
        DirectedGraph<Integer, DefaultEdge> particao_sincrona_1 = new SimpleDirectedGraph<>(DefaultEdge.class);
        particao_sincrona_1.addVertex(5);
        particao_sincrona_1.addVertex(6);
        particao_sincrona_1.addVertex(7);
        particao_sincrona_1.addVertex(8);
        particao_sincrona_1.addVertex(9);
        particao_sincrona_1.addEdge(5, 6);
        particao_sincrona_1.addEdge(5, 7);
        particao_sincrona_1.addEdge(5, 8);
        particao_sincrona_1.addEdge(5, 9);
        particao_sincrona_1.addEdge(6, 7);
        particao_sincrona_1.addEdge(6, 8);
        particao_sincrona_1.addEdge(6, 9);
        particao_sincrona_1.addEdge(7, 8);
        particao_sincrona_1.addEdge(7, 9);
        particao_sincrona_1.addEdge(8, 9);
        particoes_sincronas.add(particao_sincrona_1);

        //Partição Sincrona 3
        DirectedGraph<Integer, DefaultEdge> particao_sincrona_2 = new SimpleDirectedGraph<>(DefaultEdge.class);
        particao_sincrona_2.addVertex(10);
        particao_sincrona_2.addVertex(11);
        particao_sincrona_2.addVertex(12);
        particao_sincrona_2.addVertex(13);
        particao_sincrona_2.addVertex(14);
        particao_sincrona_2.addEdge(10, 11);
        particao_sincrona_2.addEdge(10, 12);
        particao_sincrona_2.addEdge(10, 13);
        particao_sincrona_2.addEdge(10, 14);
        particao_sincrona_2.addEdge(11, 12);
        particao_sincrona_2.addEdge(11, 13);
        particao_sincrona_2.addEdge(11, 14);
        particao_sincrona_2.addEdge(12, 13);
        particao_sincrona_2.addEdge(12, 14);
        particao_sincrona_2.addEdge(13, 14);
        particoes_sincronas.add(particao_sincrona_2);

        //Partição Sincrona 4
        DirectedGraph<Integer, DefaultEdge> particao_sincrona_3 = new SimpleDirectedGraph<>(DefaultEdge.class);
        particao_sincrona_3.addVertex(15);
        particao_sincrona_3.addVertex(16);
        particao_sincrona_3.addVertex(17);
        particao_sincrona_3.addVertex(18);
        particao_sincrona_3.addVertex(19);
        particao_sincrona_3.addEdge(15, 16);
        particao_sincrona_3.addEdge(15, 17);
        particao_sincrona_3.addEdge(15, 18);
        particao_sincrona_3.addEdge(15, 19);
        particao_sincrona_3.addEdge(16, 17);
        particao_sincrona_3.addEdge(16, 18);
        particao_sincrona_3.addEdge(16, 19);
        particao_sincrona_3.addEdge(17, 18);
        particao_sincrona_3.addEdge(17, 19);
        particao_sincrona_3.addEdge(18, 19);
        particoes_sincronas.add(particao_sincrona_3);
        return particoes_sincronas;
    }

    private TipoQos[][] newQoS() {
        //Processos
        TipoQos[][] qos = new TipoQos[quant_processos][quant_processos];
        for (int i = 0; i < quant_processos; i++) {
            for (int j = 0; j < quant_processos; j++) {
                if (i >= 0 && i < 5 && j >= 0 && j < 5) {
                    qos[i][j] = TipoQos.TIMELY;
                } else if (i >= 5 && i < 10 && j >= 5 && j < 10) {
                    qos[i][j] = TipoQos.TIMELY;
                } else if (i >= 10 && i < 15 && j >= 10 && j < 15) {
                    qos[i][j] = TipoQos.TIMELY;
                } else if (i >= 15 && i < 20 && j >= 15 && j < 20) {
                    qos[i][j] = TipoQos.TIMELY;
                } else {
                    qos[i][j] = TipoQos.UNTIMELY;
                }
            }
        }
        return qos;
    }

    public Processo getProcesso() {
        return processo;
    }

    public static void main(String[] args) {
        Principal20Processos processos[] = new Principal20Processos[20];
        Integer processo_correto = null;
        for (int i = 0; i < processos.length; i++) {
            processos[i] = new Principal20Processos(i, processos.length);
            if (i == 0 || i == 5 || i == 10 || i == 15) {
                List<Integer> processos_particao = processos[i].encontrarParticao();
                processo_correto = processos_particao.get(new Random().nextInt(processos_particao.size()));
            }
            processos[i].getProcesso().setCorreto(processo_correto);
        }

        for (Principal20Processos processo : processos) {
            try {
                processo.iniciarConsenso();
            } catch (InterruptedException | ExecutionException | IOException ex) {
                Logger.getLogger(Principal20Processos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
