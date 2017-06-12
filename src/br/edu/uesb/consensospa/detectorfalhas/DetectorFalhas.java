package br.edu.uesb.consensospa.detectorfalhas;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoQos;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import br.edu.uesb.consensospa.rede.Receber;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectorFalhas {

    private final Processo processo;
    private final int porta;
    private long[] timeout;
    private final int nr;

    public DetectorFalhas(Processo processo, int porta) {
        this.processo = processo;
        this.porta = porta;
        this.timeout = new long[processo.getQuant_processos()];
        this.nr = new Random().nextInt(5);
        iniciarTimeouts();
    }

    public void iniciar() throws IOException {
        processo.getExecutorService().execute(new Crash());
        processo.getExecutorService().execute(new Receber(processo, porta));
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

    public class Crash implements Runnable {

        @Override
        public void run() {
            try {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DetectorFalhas.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!processo.isCorreto() && new Random().nextInt(3) == 0) {
//            if (processo.getId() == 0) {
                System.err.println("Processo[" + processo.getId() + "]: crash...");
                processo.setCrash(true);
            } else {
                processo.setCrash(false);
            }
        }

    }

    public class T1 implements Runnable {

        @Override
        public void run() {
            try {
                int delay;
                if (processo.getQuant_processos() == 20) {
                    delay = 3000; // Para 20 processos.
                } else {
                    delay = 1000; // Para 6 processos.
                }
                while (!processo.isAceitou()) {
                    Thread.sleep(delay);
                    if (!processo.isCrash()) {
                        for (int processo_aux : processo.getProcessos()) {
                            if (processo_aux != processo.getId()) {
                                timeout[processo_aux] = System.currentTimeMillis() + delay;   //Tempo atual + delay
                                Pacote pacote = new Pacote(processo.getId(), processo_aux, TipoPacote.VOCE_ESTA_VIVO);
                                processo.getExecutorService().execute(new Enviar(processo.getId(), "localhost", 9000 + processo_aux, pacote));

                            }
                        }
                        /*
                        System.out.println("Processo[" + processo.getId() + "]: Lider: "
                                + processo.getEleicao().getLider() + " Defeituosos: " + processo.getDefeituosos());
                        */
                    } else {
                        Thread.sleep((delay + 2000) * (nr + 1));
                        processo.setCrash(false);
                        System.out.println("Processo[" + processo.getId() + "]: Se recuperou do crash.");
                        if (processo.getId() == processo.getEleicao().getLider()) {
                            processo.getEleicao().novoLider();
                        }
                        processo.getExecutorService().execute(new Crash());
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
                while (!processo.isAceitou()) {
                    if (!processo.isCrash()) {
                        for (Integer processoj : processo.getProcessos()) {
                            long tempo_atual = System.currentTimeMillis();
                            if (processoj != processo.getId() && !processo.getDefeituosos().contains(processoj) && tempo_atual > timeout[processoj]) {
                                if (processo.getQos()[processo.getId()][processoj] == TipoQos.TIMELY) {
                                    processo.addDefeituoso(processoj);
                                    for (int processox : processo.getProcessos()) {
                                        if (processox != processo.getId() && processox != processoj) {
                                            processo.getExecutorService().execute(new Enviar(processo.getId(), "localhost", 9000 + processox, new Pacote(processo.getId(), processox, TipoPacote.NOTIFICACAO, processoj)));
                                            /*
                                            System.out.println("Processo[" + processo.getId() + "]: Notificação enviada para o processo "
                                                    + processox + " que o processo " + processoj + " falhou. " + processo.getDefeituosos());
                                             */
                                        }
                                    }
                                    if (processoj == processo.getEleicao().getLider()) {
                                        processo.getEleicao().novoLider();
                                    }
                                }

                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro no envio do pacote NOTIFICACAO: " + ex);
            }
        }

    }

    public Processo getProcesso() {
        return processo;
    }

    public long[] getTimeout() {
        return timeout;
    }

    public int getNr() {
        return nr;
    }

    public void setTimeout(long[] timeout) {
        this.timeout = timeout;
    }

}
