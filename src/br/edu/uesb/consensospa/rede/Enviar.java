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

/**
 *
 * @author Matheus
 */
public final class Enviar implements Runnable {

    private final int id;
    private final Socket cliente;
    private final Pacote pacote;

    public Enviar(int id, String host, int porta, Pacote pacote) throws UnknownHostException, IOException, ClassNotFoundException {
        this.id = id;
        this.pacote = pacote;
        this.cliente = new Socket(host, porta);
    }

    @Override
    public void run() {
        try {
//            System.out.println("Processo[" + id + "]: " + "Cliente " + cliente.getInetAddress() + ":" + cliente.getPort() + " conectado.");
            ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
            saida.writeObject(pacote);
            cliente.close();
//            System.out.println("Processo[" + id + "]: " + "Cliente " + cliente.getInetAddress() + ":" + cliente.getPort() + " desconectado.");
        } catch (IOException ex) {
            System.err.println("Processo[" + id + "]: Falha no envio: " + ex);
        }
    }

    public long getId() {
        return id;
    }

}
