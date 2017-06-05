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
    private List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;
    private DetectorFalhas detectorFalhas;
    private Consenso consenso;
    private TipoQos[][] qos;
    private final ExecutorService executorService;
    private boolean correto;

    public Processo(int id, int quant_processos, List<Integer> processos, List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas, TipoQos[][] qos) {
        this.id = id;
        this.quant_processos = quant_processos;
        this.processos = processos;
        this.particoes_sincronas = particoes_sincronas;
        this.qos = qos;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void setCorreto(int processo_correto) {
        this.correto = (id == processo_correto);
    }

    public boolean isCorreto() {
        return correto;
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

    public void addProcesso(int processo) {
        processos.add(processo);
    }

    public void setParticoes_sincronas(List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas) {
        this.particoes_sincronas = particoes_sincronas;
    }

    public TipoQos[][] getQos() {
        return qos;
    }

    public void setQos(TipoQos[][] qos) {
        this.qos = qos;
    }

    public void novoConsenso() {
        consenso = new Consenso(id, executorService, new Eleicao(id, processos, new ArrayList<>(), particoes_sincronas));
    }

    public void novoDetectorFalhas() {
        detectorFalhas = new DetectorFalhas(id, 9000 + id, processos, particoes_sincronas, quant_processos, qos);
    }

    public int getId() {
        return id;
    }

}
