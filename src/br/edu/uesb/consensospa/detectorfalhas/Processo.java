/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.detectorfalhas;

import br.edu.uesb.consensospa.consenso.Consenso;
import br.edu.uesb.consensospa.enumerado.TipoQos;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;

/**
 *
 * @author Matheus
 */
public class Processo {

//    public static int[] MUTEX = {0, 0, 0, 0, 0, 0}; //Para 6 processos
    public static int[] MUTEX = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // Para 20 processos

    private final int id;
    private final int quant_processos;
    private final List<Integer> processos;
    private final List<Integer> defeituosos;
    private List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;
//    private DetectorFalhas detectorFalhas;
    private Consenso consenso;
    private Eleicao eleicao;
    private TipoQos[][] qos;
    private final ExecutorService executorService;
    private boolean correto;
    private boolean crash;
    private boolean aceitou;

    public Processo(int id, int quant_processos, List<Integer> processos, List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas, TipoQos[][] qos) {
        this.id = id;
        this.quant_processos = quant_processos;
        this.processos = processos;
        this.particoes_sincronas = particoes_sincronas;
        this.qos = qos;
        this.executorService = Executors.newCachedThreadPool();
        this.defeituosos = new ArrayList<>();
        this.crash = false;
        this.aceitou = false;
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

    public int getQuant_processos() {
        return quant_processos;
    }

    public ExecutorService getExecutorService() {
        return executorService;
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

    public void novoEleicao() {
        eleicao = new Eleicao(this);
    }

    public void novoConsenso() {
        consenso = new Consenso(this);
    }

    public Eleicao getEleicao() {
        return eleicao;
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("empty-statement")
    public boolean isCrash() {
        while (MUTEX[getId()] == 1);
        return crash;
    }

    public void setCrash(boolean crash) {
        MUTEX[getId()] = 1;
        this.crash = crash;
        MUTEX[getId()] = 0;
    }

    public List<Integer> getDefeituosos() {
        return defeituosos;
    }

    public boolean removeDefeituoso(Integer processo) {
        return defeituosos.remove(processo);
    }

    public void addDefeituoso(Integer processo) {
        defeituosos.add(processo);
    }

    public boolean isAceitou() {
        return aceitou;
    }

    public void setAceitou(boolean aceitou) {
        this.aceitou = aceitou;
    }

}
