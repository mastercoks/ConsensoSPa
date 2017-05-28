/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.rede;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Matheus
 */
public class NetworkService implements Callable<Pacote> {

    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    public NetworkService(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public Pacote call() throws InterruptedException, ExecutionException {
        try {
            Future<Pacote> future = pool.submit(new ServicoReceber(serverSocket.accept()));
            pool.shutdown();
            Pacote pacote = future.get();
            serverSocket.close();
            return pacote;

        } catch (IOException ex) {
            pool.shutdown();
            System.err.println("Erro ao receber o pacote.");
            return null;
        }
    }

}
