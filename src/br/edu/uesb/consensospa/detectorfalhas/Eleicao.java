package br.edu.uesb.consensospa.detectorfalhas;

import java.util.List;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Eleicao {

    private final int id;
    private int lider;
    private int mutex;
    private final List<Integer> processos;
    private final List<Integer> defeituosos;
    private final List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;

    public Eleicao(int id, List<Integer> processos, List<Integer> defeituosos, List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas) {
        this.id = id;
        this.processos = processos;
        this.defeituosos = defeituosos;
        this.particoes_sincronas = particoes_sincronas;
        this.lider = menorProcesso();
        this.mutex = 0;
    }

    private Integer menorProcesso() {
        for (DirectedGraph<Integer, DefaultEdge> particao_sincrona : particoes_sincronas) {
            for (int processo : processos) {
                if (particao_sincrona.containsVertex(processo)) {
                    return processo;
                }
            }
        }
        return null;
    }

    public void novoLider() {
        new NovoLider().start();
    }

    boolean isLider() {
        return id == lider;
    }

    public class NovoLider extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < processos.size(); i++) {
                if (contemVertice(processos.get(i)) && !defeituosos.contains(processos.get(i)) && lider != i) {
                    mutex = 1;
                    lider = processos.get(i);
                    mutex = 0;
                    System.out.println("Processo[" + id + "]: Processo " + lider + " é o novo lider!");
                    break;
                }
            }
        }
    }

    public boolean contemVertice(int vertice) {
        for (DirectedGraph<Integer, DefaultEdge> particao_sincrona : particoes_sincronas) {
            if (particao_sincrona.containsVertex(vertice)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"empty-statement"})
    public int getLider() {
        while (mutex != 0);
        return lider;
    }

    public List<Integer> getProcessos() {
        return processos;
    }

    public List<Integer> getDefeituosos() {
        return defeituosos;
    }

    public List<DirectedGraph<Integer, DefaultEdge>> getParticoes_sincronas() {
        return particoes_sincronas;
    }

}