/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.rede;

import br.edu.uesb.consensospa.detectorfalhas.DetectorFalhas;
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

    private final DetectorFalhas detectorFalhas;
    private final ServerSocket servidor;
    private Socket cliente;

    public Receber(DetectorFalhas detectorFalhas, int porta) throws IOException {
        this.detectorFalhas = detectorFalhas;
        this.servidor = new ServerSocket(porta);
//        System.out.println("Servidor aberto na porta: " + servidor.getLocalPort());
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!detectorFalhas.getProcesso().isCrash()) {
                    cliente = servidor.accept();
                    ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
//                    origem = 9000 + id_origem;
                    Pacote pacote = (Pacote) entrada.readObject();
//                    System.out.println("Processo[" + id + "]: " + "Pacote Recebido: " + pacote.getTipo() + " Origem: " + (origem));
                    switch (pacote.getTipo()) {
                        case EU_ESTOU_VIVO: //T3
                            long[] timeout = detectorFalhas.getTimeout();
                            timeout[pacote.getId_origem()] = Long.MAX_VALUE;   //Cancela o Timeout
                            detectorFalhas.setTimeout(timeout);
                            if (detectorFalhas.getDefeituosos().contains(pacote.getId_origem())) {
                                if (detectorFalhas.removeDefeituoso((Integer) pacote.getId_origem())) {
                                    System.out.println("Processo[" + detectorFalhas.getProcesso().getId()
                                            + "]: Processo " + pacote.getId_origem() + " removido dos processos defeituosos!");
                                }
                            }
                            break;
                        case NOTIFICACAO: //T4
                            int processo_aux = (Integer) pacote.getMensagem();
                            if (!detectorFalhas.getDefeituosos().contains(processo_aux)
                                    && !detectorFalhas.getDefeituosos().contains((Integer) pacote.getId_origem())) {
                                detectorFalhas.addDefeituoso((Integer) pacote.getMensagem());
                                // Observação: Eu alterei a estrutura do algoritmo, verificar com o professor!
                                if ((Integer) pacote.getMensagem() == detectorFalhas.getProcesso().getEleicao().getLider()) {
                                    detectorFalhas.getProcesso().getEleicao().novoLider();
                                }
                                // FimObservação
                            }
                            break;
                        case VOCE_ESTA_VIVO: //T5
                            new Thread(new Enviar(detectorFalhas.getProcesso().getId(), "localhost", 9000
                                    + pacote.getId_origem(), new Pacote(detectorFalhas.getProcesso().getId(),
                                            pacote.getId_origem(), TipoPacote.EU_ESTOU_VIVO))).start();
                            break;
                    }
                    cliente.close();
                } else {
                    Thread.sleep(2000 * detectorFalhas.getNr());
                    detectorFalhas.getProcesso().setCrash(false);
                }
            } catch (IOException | InterruptedException | ClassNotFoundException ex) {
                System.err.println("Processo[" + detectorFalhas.getProcesso().getId() + "]: Erro no recebimento: " + ex);
            }
        }
    }
}
