package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectorFalhas {

    private static int mutex = 0;

    private long[] timeout;
    private final int id;
    private int porta;
    private final Eleicao eleicao;
    private boolean crash;
    private List<Integer> processos;
    private final ExecutorService pool;
    Future<Boolean> future;

    public DetectorFalhas(int id, int porta, List<Integer> processos) {
        pool = Executors.newCachedThreadPool();
        timeout = new long[6];
        zerarTimeouts();
        this.porta = porta;
        this.id = id;
        eleicao = new Eleicao(processos);
        crash = false;
        this.processos = processos;
    }

    public void iniciar() throws IOException {
        future = pool.submit(new Crash());
        pool.execute(new Receber(id, porta, timeout));
        pool.execute(new T1());
        pool.execute(new T2());
    }

    private void zerarTimeouts() {
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
                System.out.println("---------Crash no processo " + id + "---------");
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
                    System.out.println("Processo[" + id + "]: Lider: " + eleicao.getLider() + " Defeituosos: " + Processo.defeituosos + " Crash:" + crash + " Timeouts: " + getTimeouts());
                    crash = future.get();
                }
            } catch (InterruptedException | ExecutionException | IOException | ClassNotFoundException ex) {
                Logger.getLogger(DetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public class T2 implements Runnable {

        @Override
        public void run() {
            try {
                while (!future.isDone() || !crash) {
                    for (int processoj : processos) {
                        while (mutex != 0);
                        if (processoj != id && !Processo.defeituosos.contains((Integer) processoj) && System.currentTimeMillis() > timeout[processoj]) {
                            //checar if(QoS(canal(processo, id)) == T)
                            Processo.defeituosos.add((Integer) processoj);
                            for (int processox : processos) {
                                if (processox != id && processox != processoj) {
                                    pool.execute(new Enviar(id, "localhost", 9000 + processox, new Pacote(TipoPacote.NOTIFICACAO, processoj)));

                                }
                            }
                            if (processoj == eleicao.getLider()) {
                                eleicao.novoLider();
                            }

                        }
                    }
                    crash = future.get();
                }
            } catch (InterruptedException | ExecutionException | IOException | ClassNotFoundException ex) {
                Logger.getLogger(DetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
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
                    try (ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream())) {
                        int id_origem = entrada.read();
                        origem = 9000 + id_origem;
                        pacote = (Pacote) entrada.readObject();
//                        System.out.println("Processo[" + id + "]: " + "Pacote Recebido: " + pacote.getTipo() + " Origem: " + (origem));
                        switch (pacote.getTipo()) {
                            case EU_ESTOU_VIVO: //T3
                                timeout[id_origem] = Long.MAX_VALUE;   //Cancela o Timeout
                                if (Processo.defeituosos.contains(id_origem)) {
                                    if (Processo.defeituosos.remove((Integer) id_origem)) {
                                        System.out.println("Processo " + id_origem + " reinicializado!");
                                    }
                                }
                                break;
                            case NOTIFICACAO: //T4
                                if (!Processo.defeituosos.contains((Integer) pacote.getMensagem())) {
                                    while(mutex != 0);
                                    mutex = 1;
                                    Processo.defeituosos.add((Integer) pacote.getMensagem());
                                    mutex = 0;
                                }
                                break;
                            case VOCE_ESTA_VIVO: //T5
                                new Thread(new Enviar(id, "localhost", getOrigem(), new Pacote(TipoPacote.EU_ESTOU_VIVO))).start();
                                break;
                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Receber.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    cliente.close();
                } catch (IOException ex) {
                    Logger.getLogger(Receber.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public long getId() {
            return id;
        }

        @SuppressWarnings("empty-statement")
        public Pacote getPacote() {
            return pacote;
        }

        public int getOrigem() {
            return origem;
        }

    }

}
