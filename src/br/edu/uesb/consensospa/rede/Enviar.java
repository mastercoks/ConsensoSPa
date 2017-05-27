/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.rede;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matheus
 */
public final class Enviar implements Runnable{

    private final int id;
    private final Socket cliente;
    Pacote pacote;

    public Enviar(int id, String host, int porta, Pacote pacote) throws UnknownHostException, IOException, ClassNotFoundException {
        this.id = id;
        this.pacote = pacote;
        cliente = new Socket(host, porta);
    }

    @Override
    public void run() {
        try {
//            System.out.println("Processo[" + id + "]: " + "Cliente " + cliente.getInetAddress() + ":" + cliente.getPort() + " conectado.");
            try (ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream())) {
                saida.write(id);
                saida.writeObject(pacote);
//                System.out.println("Processo[" + id + "]: " + "Enviando Pacote: " + pacote.getTipo() + " Destido: " + cliente.getPort());
            }
            cliente.close();
//            System.out.println("Processo[" + id + "]: " + "Cliente " + cliente.getInetAddress() + ":" + cliente.getPort() + " desconectado.");
        } catch (IOException ex) {
            Logger.getLogger(Enviar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public long getId() {
        return id;
    }

}
