package br.edu.uesb.consensospa;

import java.util.List;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Eleicao {

    private final int id;
    private int lider;
    private int mutex;
    private final List<Integer> processos;
    private final List<Integer> defeituosos;
    private final DirectedGraph<Integer, DefaultEdge> particoes_sincronas;

    public Eleicao(int id, List<Integer> processos, List<Integer> defeituosos, DirectedGraph<Integer, DefaultEdge> particoes_sincronas) {
        this.id = id;
        this.processos = processos;
        this.defeituosos = defeituosos;
        this.particoes_sincronas = particoes_sincronas;
        this.lider = 0;
        this.mutex = 0;
    }

    @SuppressWarnings({"empty-statement"})
    public int getLider() {
        while (mutex != 0);
        return lider;
    }

    public void novoLider() {
        new NovoLider().start();
    }

    public class NovoLider extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < processos.size(); i++) {
                if (particoes_sincronas.containsVertex(processos.get(i)) && !defeituosos.contains(processos.get(i)) && processos.get(i) > lider) {
                    mutex = 1;
                    lider = processos.get(i);
                    mutex = 0;
                    System.out.println("Processo[" + id + "]: Processo " + lider + " é o novo lider!");
                    break;
                }
            }
        }
    }
}
