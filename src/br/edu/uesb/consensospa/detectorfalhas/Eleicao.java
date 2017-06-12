package br.edu.uesb.consensospa.detectorfalhas;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Eleicao {

    private final Processo processo;
    private int lider;
    private int mutex;

    public Eleicao(Processo processo) {
        this.processo = processo;
        this.lider = menorProcesso();
        this.mutex = 0;
    }

    private Integer menorProcesso() {
        for (DirectedGraph<Integer, DefaultEdge> particao_sincrona : processo.getParticoes_sincronas()) {
            for (int processo : processo.getProcessos()) {
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
        return processo.getId() == lider;
    }

    public class NovoLider extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < processo.getQuant_processos(); i++) {
                if (contemVertice(processo.getProcessos().get(i)) && !processo.getDefeituosos().contains(processo.getProcessos().get(i)) && lider != i) {
                    mutex = 1;
                    lider = processo.getProcessos().get(i);
                    mutex = 0;
                    System.out.println("Processo[" + processo.getId() + "]: Processo " + lider + " é o novo lider!");
                    break;
                }
            }
        }
    }

    public boolean contemVertice(int vertice) {
        for (DirectedGraph<Integer, DefaultEdge> particao_sincrona : processo.getParticoes_sincronas()) {
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

}
