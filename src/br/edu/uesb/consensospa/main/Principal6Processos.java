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
public final class Principal6Processos {

    public int quant_processos;
    private final Processo processo;

    public Principal6Processos(int id, int quant_processos) {
        this.quant_processos = quant_processos;
        this.processo = new Processo(id, quant_processos, newProcessos(), newParticoesSincronas(), newQoS());
        this.processo.novoEleicao();
    }

    private boolean sortearCorreto() {

        List<Integer> processos_particao = encontrarParticao();
        int p = processos_particao.get(new Random().nextInt(processos_particao.size()));
        return processo.getId() == p;

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
        DirectedGraph<Integer, DefaultEdge> particao_sincrona_0 = new SimpleDirectedGraph<>(DefaultEdge.class);
        particao_sincrona_0.addVertex(0);
        particao_sincrona_0.addVertex(1);
        particao_sincrona_0.addVertex(2);
        particao_sincrona_0.addEdge(0, 1);
        particao_sincrona_0.addEdge(0, 2);
        particao_sincrona_0.addEdge(1, 2);
        particoes_sincronas.add(particao_sincrona_0);
        DirectedGraph<Integer, DefaultEdge> particao_sincrona_1 = new SimpleDirectedGraph<>(DefaultEdge.class);
        particao_sincrona_1.addVertex(3);
        particao_sincrona_1.addVertex(4);
        particao_sincrona_1.addVertex(5);
        particao_sincrona_1.addEdge(3, 4);
        particao_sincrona_1.addEdge(3, 5);
        particao_sincrona_1.addEdge(4, 5);
        particoes_sincronas.add(particao_sincrona_1);
        return particoes_sincronas;
    }

    public TipoQos[][] newQoS() {
        //Processos
        TipoQos[][] qos = new TipoQos[quant_processos][quant_processos];
        for (int i = 0; i < quant_processos; i++) {
            for (int j = 0; j < quant_processos; j++) {
                if (i >= 0 && i < 3 && j >= 0 && j < 3) {
                    qos[i][j] = TipoQos.TIMELY;
                } else if (i >= 3 && i < 6 && j >= 3 && j < 6) {
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
        Principal6Processos processos[] = new Principal6Processos[6];
        Integer processo_correto = null;
        for (int i = 0; i < processos.length; i++) {
            processos[i] = new Principal6Processos(i, processos.length);
            if (i == 0 || i == 3) {
                List<Integer> processos_particao = processos[i].encontrarParticao();
                processo_correto = processos_particao.get(new Random().nextInt(processos_particao.size()));
            }
            processos[i].getProcesso().setCorreto(processo_correto);
//            System.out.println(processo_correto);
        }

        for (Principal6Processos processo : processos) {
            try {
                processo.iniciarConsenso();
            } catch (InterruptedException | ExecutionException | IOException ex) {
                Logger.getLogger(Principal6Processos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
