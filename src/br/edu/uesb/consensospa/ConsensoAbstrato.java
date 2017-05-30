/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Matheus
 */
public abstract class ConsensoAbstrato {

    private final int id;
    private int rodada;
    private int maior_rodada;
    private TipoValor valor;
    private List<Integer> quorum;
    private final List<Integer> processos;
    private final ExecutorService executorService;
    private Eleicao eleicao;

    public ConsensoAbstrato(int id, int rodada, List<Integer> quorum, List<Integer> processos, ExecutorService executorService, Eleicao eleicao) {
        this.id = id;
        this.rodada = rodada;
        this.quorum = quorum;
        this.processos = processos;
        this.executorService = executorService;
        this.eleicao = eleicao;
    }

    public ConsensoAbstrato(int id, int rodada, int maior_rodada, List<Integer> quorum, List<Integer> processos, ExecutorService executorService, Eleicao eleicao) {
        this.id = id;
        this.rodada = rodada;
        this.maior_rodada = maior_rodada;
        this.valor = escolherValor();
        this.quorum = quorum;
        this.processos = processos;
        this.executorService = executorService;
        this.eleicao = eleicao;
    }

    public ConsensoAbstrato(int id, int rodada, int maior_rodada, TipoValor valor, List<Integer> quorum, List<Integer> processos, ExecutorService executorService) {
        this.id = id;
        this.rodada = rodada;
        this.maior_rodada = maior_rodada;
        this.valor = valor;
        this.quorum = quorum;
        this.processos = processos;
        this.executorService = executorService;
    }
    
    public ConsensoAbstrato(int id, int rodada, int maior_rodada, TipoValor valor, List<Integer> quorum, List<Integer> processos, ExecutorService executorService, Eleicao eleicao) {
        this.id = id;
        this.rodada = rodada;
        this.maior_rodada = maior_rodada;
        this.valor = valor;
        this.quorum = quorum;
        this.processos = processos;
        this.executorService = executorService;
        this.eleicao = eleicao;
    }

    public void broadcast(int porta, Pacote pacote) throws IOException, UnknownHostException, ClassNotFoundException {
        for (int processo : processos) {
            if (processo != id) {
                pacote.setId_destino(processo);
                executorService.execute(new Enviar(id, "localhost", porta + processo, pacote));
            }
        }
    }

    public int maior(int rodada, int maiorRodada) {
        if (rodada > maiorRodada) {
            return rodada;
        } else {
            return maiorRodada;
        }
    }

    private TipoValor escolherValor() {
        int num_rand = new Random().nextInt(3);
        switch (num_rand) {
            case 0:
                return TipoValor.STAR_WARS;
            case 1:
                return TipoValor.STAR_TREK;
            case 2:
                return TipoValor.NENHUMA;
        }
        return null;
    }

    public int getRodada() {
        return rodada;
    }

    public void setRodada(int rodada) {
        this.rodada = rodada;
    }

    public int getMaior_rodada() {
        return maior_rodada;
    }

    public void setMaior_rodada(int maior_rodada) {
        this.maior_rodada = maior_rodada;
    }

    public TipoValor getValor() {
        return valor;
    }

    public void setValor(TipoValor valor) {
        this.valor = valor;
    }

    public List<Integer> getQuorum() {
        return quorum;
    }

    public void setQuorum(List<Integer> quorum) {
        this.quorum = quorum;
    }

    public int getId() {
        return id;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public List<Integer> getProcessos() {
        return processos;
    }

    public Eleicao getEleicao() {
        return eleicao;
    }

}
