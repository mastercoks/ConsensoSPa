/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.detectorfalhas;

import br.edu.uesb.consensospa.consenso.Consenso;
import br.edu.uesb.consensospa.enumerado.TipoQos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * @author Matheus
 */
public class Processo {

    private final int id;
    private final int quant_processos;
    private final List<Integer> processos;
    private final List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;
    private DetectorFalhas detectorFalhas;
    private Consenso consenso;
    private final TipoQos[][] qos;
    private final ExecutorService executorService;

    public Processo(int id, int quant_processos) throws IOException {
        this.id = id;
        this.quant_processos = quant_processos;
        this.processos = new ArrayList<>();
        this.particoes_sincronas = new ArrayList<>();
        this.qos = new TipoQos[quant_processos][quant_processos];
        this.executorService = Executors.newCachedThreadPool();
    }

    public void iniciarConsenso() throws InterruptedException, ExecutionException {
        addProcessos();
        addParticoesSincronas();
        preencherQoS();
        consenso = new Consenso(id, executorService, new Eleicao(id, processos, new ArrayList<>(), particoes_sincronas));
        consenso.iniciar();
    }

    public void iniciarDetectorFalhas() throws IOException {
        addProcessos();
        addParticoesSincronas();
        preencherQoS();
        detectorFalhas = new DetectorFalhas(id, 9000 + id, processos, particoes_sincronas, quant_processos, qos);
        detectorFalhas.iniciar();
    }

    private void preencherQoS() {
        //Processos
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

    }

    public void addProcessos() {
        for (int i = 0; i < quant_processos; i++) {
            processos.add(i);
        }
    }

    public void addParticoesSincronas() {
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
    }

    public Consenso getConsenso() {
        return consenso;
    }

    public List<Integer> getProcessos() {
        return processos;
    }

    public List<DirectedGraph<Integer, DefaultEdge>> getParticoes_sincronas() {
        return particoes_sincronas;
    }

    public DetectorFalhas getDetectorFalhas() {
        return detectorFalhas;
    }

}