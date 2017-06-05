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
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * @author Matheus
 */
public final class Principal {

    public int quant_processos;
    private final Processo processo;

    public Principal(int id, int quant_processos) {
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

    public void iniciar() throws IOException, InterruptedException, ExecutionException {
        boolean correto = sortearCorreto();
        iniciarDetectorFalhas();
        iniciarConsenso();
//        detectorFalhas = new DetectorFalhas(id, 9000 + id, processos, particoes_sincronas, quant_processos, qos);
//        consenso = new Consenso(id, executorService, detectorFalhas.getEleicao());
//        consenso.iniciar();
//        detectorFalhas.iniciar();
    }

    public void iniciarConsenso() throws InterruptedException, ExecutionException {
        processo.novoConsenso();
        processo.getConsenso().iniciar();
    }

    public void iniciarDetectorFalhas() throws IOException {
        processo.novoDetectorFalhas();
        processo.getDetectorFalhas().iniciar();
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

    private TipoQos[][] newQoS() {
        //Processos
        TipoQos[][] qos = new TipoQos[quant_processos][quant_processos];
        qos[0][0] = TipoQos.TIMELY;
        qos[1][1] = TipoQos.TIMELY;
        qos[2][2] = TipoQos.TIMELY;
        qos[3][3] = TipoQos.TIMELY;
        qos[4][4] = TipoQos.TIMELY;
        qos[5][5] = TipoQos.TIMELY;

        //Canal(0,i)
        qos[0][1] = TipoQos.TIMELY;
        qos[0][2] = TipoQos.TIMELY;
        qos[0][3] = TipoQos.UNTIMELY;
        qos[0][4] = TipoQos.UNTIMELY;
        qos[0][5] = TipoQos.UNTIMELY;
        //Canal(1,i)
        qos[1][0] = TipoQos.TIMELY;
        qos[1][2] = TipoQos.TIMELY;
        qos[1][3] = TipoQos.UNTIMELY;
        qos[1][4] = TipoQos.UNTIMELY;
        qos[1][5] = TipoQos.UNTIMELY;
        //Canal(2,i)
        qos[2][0] = TipoQos.TIMELY;
        qos[2][1] = TipoQos.TIMELY;
        qos[2][3] = TipoQos.UNTIMELY;
        qos[2][4] = TipoQos.UNTIMELY;
        qos[2][5] = TipoQos.UNTIMELY;
        //Canal(3,i)
        qos[3][0] = TipoQos.UNTIMELY;
        qos[3][1] = TipoQos.UNTIMELY;
        qos[3][2] = TipoQos.UNTIMELY;
        qos[3][4] = TipoQos.TIMELY;
        qos[3][5] = TipoQos.TIMELY;
        //Canal(4,i)
        qos[4][0] = TipoQos.UNTIMELY;
        qos[4][1] = TipoQos.UNTIMELY;
        qos[4][2] = TipoQos.UNTIMELY;
        qos[4][3] = TipoQos.TIMELY;
        qos[4][5] = TipoQos.TIMELY;
        //Canal(5,i)
        qos[5][0] = TipoQos.UNTIMELY;
        qos[5][1] = TipoQos.UNTIMELY;
        qos[5][2] = TipoQos.UNTIMELY;
        qos[5][3] = TipoQos.TIMELY;
        qos[5][4] = TipoQos.TIMELY;
        return qos;
    }

    public Processo getProcesso() {
        return processo;
    }

    public static void main(String[] args) {
        Principal processos[] = new Principal[6];
        Integer processo_correto = null;

        for (int i = 0; i < processos.length; i++) {

            processos[i] = new Principal(i, processos.length);
            if (i == 0 || i == 3) {
                List<Integer> processos_particao = processos[i].encontrarParticao();
                processo_correto = processos_particao.get(new Random().nextInt(processos_particao.size()));
            }
            processos[i].processo.setCorreto(processo_correto);
        }
        for (Principal processo : processos) {
            try {
                processo.iniciar();
                System.out.println("Processo[" + processo.processo.getId() + "] : " + processo.processo.isCorreto());
            } catch (IOException | InterruptedException | ExecutionException ex) {
                System.err.println("Erro --");
//                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
