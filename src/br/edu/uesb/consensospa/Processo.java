/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private final DirectedGraph<Integer, DefaultEdge> particoes_sincronas;
    private final DetectorFalhas detectorFalhas;

    public Processo(int id, int quant_processos) throws IOException {
        this.id = id;
        this.quant_processos = quant_processos;
        this.processos = new ArrayList<>();
        this.particoes_sincronas = new SimpleDirectedGraph<>(DefaultEdge.class);
        this.detectorFalhas = new DetectorFalhas(id, 9000 + id, processos, particoes_sincronas, quant_processos);
    }

    public void iniciarDetectorFalhas() throws IOException {
        addProcessos();
        addParticoesSincronas();
        detectorFalhas.iniciar();
    }

    public void addProcessos() {
        for (int i = 0; i < quant_processos; i++) {
            processos.add(i);
        }
    }

    public void addParticoesSincronas() {
        particoes_sincronas.addVertex(0);
        particoes_sincronas.addVertex(1);
        particoes_sincronas.addVertex(2);
        particoes_sincronas.addVertex(3);
        particoes_sincronas.addVertex(4);
        particoes_sincronas.addVertex(5);
        particoes_sincronas.addEdge(0, 1);
        particoes_sincronas.addEdge(0, 2);
        particoes_sincronas.addEdge(1, 2);
        particoes_sincronas.addEdge(3, 4);
        particoes_sincronas.addEdge(3, 5);
        particoes_sincronas.addEdge(4, 5);
    }

}
