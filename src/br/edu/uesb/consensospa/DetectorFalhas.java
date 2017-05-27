package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DetectorFalhas {

    private final int id;
    private int porta;
    private List<Integer> processos;
    private List<Integer> defeituosos;
    private final ExecutorService pool;
    private long[] timeout;
    private final Eleicao eleicao;
    private boolean crash;
    private Future<Boolean> future;

    public DetectorFalhas(int id, int porta, List<Integer> processos, DirectedGraph<Integer, DefaultEdge> particoes_sincronas, int quant_processos) {
        this.id = id;
        this.porta = porta;
        this.processos = processos;
        this.defeituosos = new ArrayList<>();
        this.pool = Executors.newCachedThreadPool();
        this.timeout = new long[quant_processos];
        this.eleicao = new Eleicao(id, processos, (List<Integer>) defeituosos, particoes_sincronas);
        this.crash = false;
        iniciarTimeouts();
    }

    public void iniciar() throws IOException {
        future = pool.submit(new Crash());
        pool.execute(new Receber(id, porta, timeout));
        pool.execute(new T1());
        pool.execute(new T2());
    }

    private void iniciarTimeouts() {
        for (int i = 0; i < timeout.length; i++) {
            timeout[i] = Long.MAX_VALUE;
        }
    }

    private String getTimeouts() {
        String t = "[";
        for (int i = 0; i < timeout.length - 1; i++) {
            t += timeout[i] + ", ";
        }
        t += timeout[timeout.length - 1] + "]";
        return t;
    }

    public class Crash implements Callable<Boolean> {

        @Override
        public Boolean call() throws InterruptedException {
            Thread.sleep(5000);
            if (new Random().nextInt(processos.size()) == 3) {
//            if (id == 0) {
                System.out.println("Processo[" + id + "]: crash...");
                return true;
            } else {
                return false;
            }
        }

    }

    public class T1 implements Runnable {

        @Override
        public void run() {
            try {
                while (!future.isDone() || !crash) {
                    Thread.sleep(3000);
                    for (int processo : processos) {
                        if (processo != id) {
                            timeout[processo] = System.currentTimeMillis() + 2000;   //Tempo atual + delay
                            pool.execute(new Enviar(id, "localhost", 9000 + processo, new Pacote(TipoPacote.VOCE_ESTA_VIVO)));

                        }
                    }
                    System.out.println("Processo[" + id + "]: Lider: " + eleicao.getLider() + " Defeituosos: " + defeituosos + " Crash:" + crash + " Timeouts: " + getTimeouts());

                }
            } catch (InterruptedException ex) {
                System.err.println("Processo[" + id + "]: Erro ao dormir a Thread: " + ex);
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Processo[" + id + "]: Erro no envio do pacote VOCE_ESTA_VIVO: " + ex);
            }
        }

    }

    public class T2 implements Runnable {

        @Override
        public void run() {
            try {
                while (!future.isDone() || !crash) {
                    for (int processoj : processos) {
                        long tempo_atual = System.currentTimeMillis();
                        if (processoj != id && !defeituosos.contains((Integer) processoj) && tempo_atual > timeout[processoj]) {
                            //checar if(QoS(canal(processo, id)) == T)
                            defeituosos.add((Integer) processoj);
                            for (int processox : processos) {
                                if (processox != id && processox != processoj) {
                                    pool.execute(new Enviar(id, "localhost", 9000 + processox, new Pacote(TipoPacote.NOTIFICACAO, processoj)));
                                    System.out.println("Processo[" + id + "]: Notificação enviada para o processo " + processox + " que o processo " + processoj + " falhou. Timeout: " + tempo_atual);

                                }
                            }
                            if (processoj == eleicao.getLider()) {
                                eleicao.novoLider();
                            }

                        }
                    }
                    crash = future.get();
                }
            } catch (InterruptedException ex) {
                System.err.println("Processo[" + id + "]: Erro ao dormir a Thread: " + ex);
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Processo[" + id + "]: Erro no envio do pacote NOTIFICACAO: " + ex);
            } catch (ExecutionException ex) {
                System.err.println("Processo[" + id + "]: Erro na execução do future: " + ex);
            }
        }

    }

    public class Receber implements Runnable {

        private final int id;
        private final ServerSocket servidor;
        private Socket cliente;
        private int origem;
        private Pacote pacote;
        private long[] timeout;

        public Receber(int id, int porta, long[] timeout) throws IOException {
            this.id = id;
            this.timeout = timeout;
            servidor = new ServerSocket(porta);
            System.out.println("Servidor aberto na porta: " + servidor.getLocalPort());
        }

        public Receber(int id, int porta) throws IOException {
            this.id = id;
            servidor = new ServerSocket(porta);
            System.out.println("Servidor aberto na porta: " + servidor.getLocalPort());
        }

        @Override
        public void run() {
            while (!future.isDone() || !crash) {
                try {
                    cliente = servidor.accept();
                    ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
                    int id_origem = entrada.read();
                    origem = 9000 + id_origem;
                    pacote = (Pacote) entrada.readObject();
//                    System.out.println("Processo[" + id + "]: " + "Pacote Recebido: " + pacote.getTipo() + " Origem: " + (origem));
                    switch (pacote.getTipo()) {
                        case EU_ESTOU_VIVO: //T3
                            timeout[id_origem] = Long.MAX_VALUE;   //Cancela o Timeout
                            if (defeituosos.contains(id_origem)) {
                                if (defeituosos.remove((Integer) id_origem)) {
                                    System.out.println("Processo " + id_origem + " reinicializado!");
                                }
                            }
                            break;
                        case NOTIFICACAO: //T4
                            if (!defeituosos.contains((Integer) pacote.getMensagem())) {
                                defeituosos.add((Integer) pacote.getMensagem());
                                // Observação: Eu alterei a estrutura do algoritmo, verificar com o professor!
                                if (pacote.getMensagem() == eleicao.getLider()) {
                                    eleicao.novoLider();
                                }
                                // FimObservação
                            }
                            break;
                        case VOCE_ESTA_VIVO: //T5
                            new Thread(new Enviar(id, "localhost", getOrigem(), new Pacote(TipoPacote.EU_ESTOU_VIVO))).start();
                            break;
                    }
                    cliente.close();
                } catch (IOException ex) {
                    System.err.println("Processo[" + id + "]: Erro no recebimento: " + ex);
                } catch (ClassNotFoundException ex) {
                    System.err.println("Processo[" + id + "]: Erro no recebimento: " + ex);
                }
            }
        }

        public long getId() {
            return id;
        }

        public Pacote getPacote() {
            return pacote;
        }

        public int getOrigem() {
            return origem;
        }

    }

}
