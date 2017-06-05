package br.edu.uesb.consensospa.detectorfalhas;

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

    private Processo processo;
    private int porta;
    private List<Integer> defeituosos;
    private long[] timeout;
    private Future<Boolean> future;
    private boolean primeira_rodada;
    private int nr;

    public DetectorFalhas(Processo processo, int porta) {
        this.processo = processo;
        this.porta = porta;
        this.defeituosos = new ArrayList<>();
        this.timeout = new long[processo.getQuant_processos()];
        this.primeira_rodada = true;
        this.nr = new Random().nextInt(10);
        iniciarTimeouts();
    }

    public void iniciar() throws IOException {
        future = processo.getExecutorService().submit(new Crash());
        processo.getExecutorService().execute(new Receber(processo.getId(), porta, timeout));
        processo.getExecutorService().execute(new T1());
        processo.getExecutorService().execute(new T2());
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
//            if(!processo.isCorreto()) {
//                
//            }
            Thread.sleep(1000 * nr);
//            if (new Random().nextInt(processos.size()) == 2) {
            if (processo.getId() == 0) {
                System.err.println("Processo[" + processo.getId() + "]: crash...");
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
                    Thread.sleep(1500);
                    if (!processo.isCrash()) {
                        for (int processo_aux : processo.getProcessos()) {
                            if (processo_aux != processo.getId()) {
                                timeout[processo_aux] = System.currentTimeMillis() + 1000;   //Tempo atual + delay
                                processo.getExecutorService().execute(new Enviar(processo.getId(), "localhost", 9000 + processo_aux, new Pacote(processo.getId(), processo_aux, TipoPacote.VOCE_ESTA_VIVO)));

                            }
                        }
                        System.out.println("Processo[" + processo.getId() + "]: Lider: " + processo.getEleicao().getLider() + " Defeituosos: " + defeituosos);
                    } else {
                        Thread.sleep(2000 * nr);
                        processo.setCrash(false);
                        System.out.println("Processo[" + processo.getId() + "]: Se recuperou do crash.");
                        if (processo.getId() == processo.getEleicao().getLider()) {
                            processo.getEleicao().novoLider();
                        }

                    }
                }
            } catch (InterruptedException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro ao dormir a Thread: " + ex);
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro no envio do pacote VOCE_ESTA_VIVO: " + ex);
            }
        }

    }

    public class T2 implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    if (!processo.isCrash()) {
                        for (int processoj : processo.getProcessos()) {
                            long tempo_atual = System.currentTimeMillis();
                            if (processoj != processo.getId() && !defeituosos.contains((Integer) processoj) && tempo_atual > timeout[processoj]) {
                                if (processo.getQos()[processo.getId()][processoj] == TipoQos.TIMELY) {
                                    defeituosos.add((Integer) processoj);
                                    for (int processox : processo.getProcessos()) {
                                        if (processox != processo.getId() && processox != processoj) {
                                            processo.getExecutorService().execute(new Enviar(processo.getId(), "localhost", 9000 + processox, new Pacote(processo.getId(), processox, TipoPacote.NOTIFICACAO, processoj)));
                                            System.out.println("Processo[" + processo.getId() + "]: Notificação enviada para o processo "
                                                    + processox + " que o processo " + processoj + " falhou. Timeout: "
                                                    + tempo_atual + " Timeouts: " + getTimeouts());

                                        }
                                    }
                                    if (processoj == processo.getEleicao().getLider()) {
                                        processo.getEleicao().novoLider();
                                    }
                                }

                            }
                        }
                        if (primeira_rodada) {
                            processo.setCrash(future.get());
                            primeira_rodada = false;
                        }
                    } else {
                        Thread.sleep(2000 * nr);
                        processo.setCrash(false);
                    }
                }
            } catch (InterruptedException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro ao dormir a Thread: " + ex);
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro no envio do pacote NOTIFICACAO: " + ex);
            } catch (ExecutionException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro na execução do future: " + ex);
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
                    if (!processo.isCrash()) {
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
                                int processo_aux = (Integer) pacote.getMensagem();
                                if (!defeituosos.contains(processo_aux) && !defeituosos.contains((Integer) pacote.getId_origem())) {
                                    defeituosos.add((Integer) pacote.getMensagem());
                                    // Observação: Eu alterei a estrutura do algoritmo, verificar com o professor!
                                    if ((Integer) pacote.getMensagem() == processo.getEleicao().getLider()) {
                                        processo.getEleicao().novoLider();
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
                        processo.setCrash(false);
                    }
                } catch (IOException | ClassNotFoundException ex) {
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
