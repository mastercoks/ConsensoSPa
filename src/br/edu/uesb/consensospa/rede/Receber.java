/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.rede;

import br.edu.uesb.consensospa.detectorfalhas.Processo;
import br.edu.uesb.consensospa.enumerado.TipoPacote;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Matheus
 */
public class Receber implements Runnable {

    private final Processo processo;
    private final ServerSocket servidor;
    private Socket cliente;

    public Receber(Processo processo, int porta) throws IOException {
        this.processo = processo;
        this.servidor = new ServerSocket(porta);
//        System.out.println("Servidor aberto na porta: " + servidor.getLocalPort());
    }

    @Override
    public void run() {
        while (true) {
            try {
                servidor.setReuseAddress(true);
                cliente = servidor.accept();
                ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
                Pacote pacote = (Pacote) entrada.readObject();
                if (!processo.isCrash()) {
//                    System.out.println("Processo[" + pacote.getId_destino() + "]: " + "Pacote Recebido: " + pacote.getTipo() + " Origem: " + (pacote.getId_origem()));
                    switch (pacote.getTipo()) {
                        case EU_ESTOU_VIVO: //T3
                            long[] timeout = processo.getConsenso().getDetectorFalhas().getTimeout();
                            timeout[pacote.getId_origem()] = Long.MAX_VALUE;   //Cancela o Timeout
                            processo.getConsenso().getDetectorFalhas().setTimeout(timeout);
                            if (processo.getDefeituosos().contains(pacote.getId_origem())) {
                                if (processo.removeDefeituoso((Integer) pacote.getId_origem())) {
                                    System.out.println("Processo[" + processo.getId()
                                            + "]: Processo " + pacote.getId_origem() + " removido dos processos defeituosos!");
                                }
                            }
                            break;
                        case NOTIFICACAO: //T4
                            int processo_aux = (Integer) pacote.getMensagem();
                            if (!processo.getDefeituosos().contains(processo_aux)
                                    && !processo.getDefeituosos().contains((Integer) pacote.getId_origem())) {
                                processo.addDefeituoso((Integer) pacote.getMensagem());
                                // Observação: Eu alterei a estrutura do algoritmo, verificar com o professor!
                                if ((Integer) pacote.getMensagem() == processo.getEleicao().getLider()) {
                                    processo.getEleicao().novoLider();
                                }
                                // FimObservação
                            }
                            break;
                        case VOCE_ESTA_VIVO: //T5
                            new Thread(new Enviar(processo.getId(), "localhost", 9000
                                    + pacote.getId_origem(), new Pacote(processo.getId(),
                                            pacote.getId_origem(), TipoPacote.EU_ESTOU_VIVO))).start();
                            break;
                    }
                    cliente.close();
                }
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Processo[" + processo.getId() + "]: Erro no recebimento: " + ex);
            }
        }
    }
}
