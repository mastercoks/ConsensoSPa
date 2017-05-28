package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoQos;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private boolean primeira_rodada;
    private int nr;
    private final TipoQos[][] qos;

    public DetectorFalhas(int id, int porta, List<Integer> processos, List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas, int quant_processos, TipoQos[][] qos) {
        this.id = id;
        this.porta = porta;
        this.processos = processos;
        this.defeituosos = new ArrayList<>();
        this.pool = Executors.newCachedThreadPool();
        this.timeout = new long[quant_processos];
        this.eleicao = new Eleicao(id, processos, (List<Integer>) defeituosos, particoes_sincronas);
        this.crash = false;
        this.primeira_rodada = true;
        this.nr = new Random().nextInt(10);
        this.qos = qos;
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
            if (new Random().nextInt(processos.size()) == 2) {
//            if (id == 3) {
                System.err.println("Processo[" + id + "]: crash...");
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
                while (true) {
                    Thread.sleep(3000);
                    if (!crash) {
                        for (int processo : processos) {
                            if (processo != id) {
                                timeout[processo] = System.currentTimeMillis() + 2000;   //Tempo atual + delay
                                pool.execute(new Enviar(id, "localhost", 9000 + processo, new Pacote(id, processo, TipoPacote.VOCE_ESTA_VIVO)));

                            }
                        }
                        System.out.println("Processo[" + id + "]: Lider: " + eleicao.getLider() + " Defeituosos: " + defeituosos + " Crash:" + crash + " Timeouts: " + getTimeouts());
                    } else {
                        Thread.sleep(2000 * nr);
                        crash = false;
                        System.out.println("Processo[" + id + "]: Se recuperou do crash.");

                    }
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
                while (true) {
                    if (!crash) {
                        for (int processoj : processos) {
                            long tempo_atual = System.currentTimeMillis();
                            if (processoj != id && !defeituosos.contains((Integer) processoj) && tempo_atual > timeout[processoj]) {
//                                if (qos[id][processoj] == TipoQos.TIMELY) {
                                    defeituosos.add((Integer) processoj);
                                    for (int processox : processos) {
                                        if (processox != id && processox != processoj) {
                                            pool.execute(new Enviar(id, "localhost", 9000 + processox, new Pacote(id, processox, TipoPacote.NOTIFICACAO, processoj)));
                                            System.out.println("Processo[" + id + "]: Notificação enviada para o processo " + processox + " que o processo " + processoj + " falhou. Timeout: " + tempo_atual);

                                        }
                                    }
                                    if (processoj == eleicao.getLider()) {
                                        eleicao.novoLider();
                                    }
//                                }

                            }
                        }
                        if (primeira_rodada) {
                            crash = future.get();
                            primeira_rodada = false;
                        }
                    } else {
                        Thread.sleep(2000 * nr);
                        crash = false;
                    }
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
            while (true) {
                try {
                    if (!crash) {
                        cliente = servidor.accept();
                        ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
//                    origem = 9000 + id_origem;
                        pacote = (Pacote) entrada.readObject();
//                    System.out.println("Processo[" + id + "]: " + "Pacote Recebido: " + pacote.getTipo() + " Origem: " + (origem));
                        switch (pacote.getTipo()) {
                            case EU_ESTOU_VIVO: //T3
                                timeout[pacote.getId_origem()] = Long.MAX_VALUE;   //Cancela o Timeout
                                if (defeituosos.contains(pacote.getId_origem())) {
                                    if (defeituosos.remove((Integer) pacote.getId_origem())) {
                                        System.out.println("Processo[" + id + "]: Processo " + pacote.getId_origem() + " removido dos processos defeituosos!");
                                    }
                                }
                                break;
                            case NOTIFICACAO: //T4
                                int processo = (Integer) pacote.getMensagem();
                                if (!defeituosos.contains(processo) && !defeituosos.contains((Integer) pacote.getId_origem())) {
                                    defeituosos.add((Integer) pacote.getMensagem());
                                    // Observação: Eu alterei a estrutura do algoritmo, verificar com o professor!
                                    if (pacote.getMensagem() == (Integer) eleicao.getLider()) {
                                        eleicao.novoLider();
                                    }
                                    // FimObservação
                                }
                                break;
                            case VOCE_ESTA_VIVO: //T5
                                new Thread(new Enviar(id, "localhost", 9000 + pacote.getId_origem(), new Pacote(id, pacote.getId_origem(), TipoPacote.EU_ESTOU_VIVO))).start();
                                break;
                        }
                        cliente.close();
                    } else {
                        Thread.sleep(2000 * nr);
                        crash = false;
                    }
                } catch (IOException ex) {
                    System.err.println("Processo[" + id + "]: Erro no recebimento: " + ex);
                } catch (ClassNotFoundException ex) {
                    System.err.println("Processo[" + id + "]: Erro no recebimento: " + ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        public long getId() {
            return id;
        }

        public Pacote getPacote() {
            return pacote;
        }

    }

}
