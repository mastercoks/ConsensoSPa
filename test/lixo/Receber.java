/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lixo;

import br.edu.uesb.consensospa.detectorfalhas.Processo;
import br.edu.uesb.consensospa.enumerado.TipoPacote;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
//public class Receber implements Runnable {

//    public static int mutex = 0;
//    private final int id;
//    private final ServerSocket servidor;
//    private Socket cliente;
//    private int origem;
//    private Pacote pacote;
//    private long[] timeout;
//    private boolean crash;
////    private List<Integer> defeituosos;
//    
//    public Receber(int id, int porta, long[] timeout, boolean crash) throws IOException {
//        this.id = id;
//        this.timeout = timeout;
//        this.crash = crash;
////        this.defeituosos = defeituosos;
//        servidor = new ServerSocket(porta);
//        System.out.println("Servidor aberto na porta: " + servidor.getLocalPort());
//    }
//    
//    public Receber(int id, int porta) throws IOException {
//        this.id = id;
//        servidor = new ServerSocket(porta);
//        System.out.println("Servidor aberto na porta: " + servidor.getLocalPort());
//    }
//
//    @Override
//    public void run() {
//        while (!crash) {
//            try {
//                cliente = servidor.accept();
////                    System.out.println("Processo[" + id + "]: " + "Cliente " + cliente.getInetAddress() + ":" + cliente.getLocalPort() + " conectado.");
//                try (ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream())) {
//                    int id_origem = entrada.read();
//                    origem = 9000 + id_origem;
//                    pacote = (Pacote) entrada.readObject();
//                    System.out.println("Processo[" + id + "]: " + "Pacote Recebido: " + pacote.getTipo() + " Origem: " + (origem));
//                    switch (pacote.getTipo()) {
//                        case EU_ESTOU_VIVO: //T3
//                            timeout[id_origem] = Long.MAX_VALUE;   //Cancela o Timeout
//                            if (Processo.defeituosos.contains(id_origem)) {
//                                if (Processo.defeituosos.remove((Integer) id_origem)) {
//                                    System.out.println("Processo " + id_origem + " reinicializado!");
//                                }
//                            }
//                            break;
//                        case NOTIFICACAO: //T4
//                            if (!Processo.defeituosos.contains((Integer) pacote.getMensagem())) {
//                                while(mutex == 1);
//                                mutex = 1;
//                                Processo.defeituosos.add((Integer) pacote.getMensagem());
//                                mutex = 0;
//                            }
//                            break;
//                        case VOCE_ESTA_VIVO: //T5
//                            new Thread(new Enviar(id, "localhost", getOrigem(), new Pacote(TipoPacote.EU_ESTOU_VIVO))).start();
//                            break;
//                    }
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(Receber.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                cliente.close();
////                    System.out.println("Processo[" + id + "]: " + "Cliente " + cliente.getInetAddress() + ":" + cliente.getLocalPort() + " desconectado.");
//            } catch (IOException ex) {
//                Logger.getLogger(Receber.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    @SuppressWarnings("empty-statement")
//    public Pacote getPacote() {
//        return pacote;
//    }
//
//    public int getOrigem() {
//        return origem;
//    }

//}
