package br.edu.uesb.consensospa.detectorfalhas;

import br.edu.uesb.consensospa.enumerado.TipoPacote;
import br.edu.uesb.consensospa.enumerado.TipoQos;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import br.edu.uesb.consensospa.rede.Receber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DetectorFalhas {

    private final Processo processo;
    private final int porta;
    private final List<Integer> defeituosos;
    private long[] timeout;
    private Future<Boolean> future;
    private boolean primeira_rodada;
    private final int nr;

    public DetectorFalhas(Processo processo, int porta) {
        this.processo = processo;
        this.porta = porta;
        this.defeituosos = new ArrayList<>();
        this.timeout = new long[processo.getQuant_processos()];
        this.primeira_rodada = true;
        this.nr = new Random().nextInt(5);
        iniciarTimeouts();
    }

    public void iniciar() throws IOException {
        future = processo.getExecutorService().submit(new Crash());
        processo.getExecutorService().execute(new Receber(this, porta));
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

    public boolean removeDefeituoso(Integer processo) {
        return defeituosos.remove(processo);
    }

    public void addDefeituoso(Integer processo) {
        defeituosos.add(processo);
    }

    public class Crash implements Callable<Boolean> {

        @Override
        public Boolean call() throws InterruptedException {
//            if(!processo.isCorreto()) {
//                
//            }
            Thread.sleep(1100);
            if (!processo.isCorreto() && new Random().nextInt(3) == 0) {
//            if (processo.getId() == 0) {
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
                    Thread.sleep(1000);
                    if (!processo.isCrash()) {
                        for (int processo_aux : processo.getProcessos()) {
                            if (processo_aux != processo.getId()) {
                                timeout[processo_aux] = System.currentTimeMillis() + 900;   //Tempo atual + delay
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

    public Processo getProcesso() {
        return processo;
    }

    public long[] getTimeout() {
        return timeout;
    }

    public List<Integer> getDefeituosos() {
        return defeituosos;
    }

    public int getNr() {
        return nr;
    }

    public void setTimeout(long[] timeout) {
        this.timeout = timeout;
    }

}
