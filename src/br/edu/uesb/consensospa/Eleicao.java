package br.edu.uesb.consensospa;

import java.util.List;

public class Eleicao {

    private int lider;
    private int mutex;
    public List<Integer> processos;

    public Eleicao(List<Integer> processos) {
        this.processos = processos;
        lider = 0;
        mutex = 0;
    }

    @SuppressWarnings({"empty-statement"})
    public int getLider() {
        while (mutex != 0);
        return lider;
    }

    public void novoLider() {
        mutex = 1;
        new NovoLider().start();
    }

    public class NovoLider extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < processos.size(); i++) {
                if (Processo.particoes_sincronas.containsVertex(processos.get(i)) && !Processo.defeituosos.contains(processos.get(i)) && processos.get(i) > lider) {
                    lider = processos.get(i);
                    System.out.println("----------------Processo " + lider + " é o novo lider!--------------");
                    mutex = 0;
                    break;
                }
            }
        }
    }
}
