/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * @author Matheus
 */
public class Processo {

    public List<Integer> processos;
    public static DirectedGraph<Integer, DefaultEdge> particoes_sincronas;
    public static List<Integer> defeituosos;
    private final int id;
    private final DetectorFalhas detectorFalhas;

    public Processo(int id) throws IOException {
        this.id = id;
        processos = new ArrayList<>();
        particoes_sincronas = new SimpleDirectedGraph<>(DefaultEdge.class);
        defeituosos = new ArrayList<>();
        detectorFalhas = new DetectorFalhas(id, 9000 + id, processos);
    }

    public void iniciarDetectorFalhas(int quant_processos) throws IOException {
        addProcessos(quant_processos);
        addParticoesSincronas();
        detectorFalhas.iniciar();
    }

    public int getId() {
        return id;
    }

    public void addProcessos(int quant_processos) {
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
