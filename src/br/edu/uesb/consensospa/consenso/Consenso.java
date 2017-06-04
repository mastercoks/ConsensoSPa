/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.consenso;

import br.edu.uesb.consensospa.detectorfalhas.Eleicao;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 * @author Matheus
 */
public class Consenso implements Runnable {

    private final int id;
    private int rodada;
    private int ultima_rodada;
    private TipoValor valor;
    private List<Integer> quorum;
    private final List<Integer> processos;
    private final ExecutorService executorService;
    private final Eleicao eleicao;

    public Consenso(int id, ExecutorService executorService, Eleicao eleicao) {
        this.id = id;
        this.rodada = 0;
        this.ultima_rodada = 0;
        this.valor = escolherValor();
        this.quorum = new ArrayList<>();
        this.processos = eleicao.getProcessos();
        this.executorService = executorService;
        this.eleicao = eleicao;
    }

    public void iniciar() throws InterruptedException, ExecutionException {
        System.out.println(this);
        new Aceitador(this).iniciar();
        getExecutorService().execute(this);
        if (getId() == 0) { //getEleicao().isLider()
            getExecutorService().execute(new Proponente(this));
        }
    }

    @Override
    public void run() {
        Future<TipoValor> future = getExecutorService().submit(new Aprendiz(this));
        try {
            setValor(future.get());
        } catch (InterruptedException | ExecutionException ex) {
//            System.err.println("error: " + ex);
        }
        System.out.println(this);
        getExecutorService().shutdown();

    }

    @Override
    public String toString() {
        return "--------------------\nProcesso[" + getId() + "]\nRodada = " + getRodada() + "\nUltimaRodada = " + getUltima_rodada() + "\nQuorum = " + getQuorum() + "\nValor = " + getValor() + "\n--------------------\n";
    }

    public List<Integer> gerarQuorum() {
        List<Integer> q = new ArrayList<>();
        getProcessos().stream().filter((processo) -> (!getEleicao().getDefeituosos().contains(processo) && getEleicao().contemVertice(processo))).forEach((processo) -> {
            q.add(processo);
        });
        return q;
    }

    public TipoValor checarQuorum(List<TipoValor> respostas, int k) {
        int quant_sw = 0, quant_st = 0, quant_n = 0;
        for (TipoValor resposta : respostas) {
            switch (resposta) {
                case STAR_WARS:
                    quant_sw++;
                    break;
                case STAR_TREK:
                    quant_st++;
                    break;
                case NENHUMA:
                    quant_n++;
                    break;
            }
        }
        int maior_quant = maior(quant_sw, maior(quant_st, quant_n));
        TipoValor valor_tmp = null;
        if (maior_quant >= k) {
            if (maior_quant == quant_sw) {
                valor_tmp = TipoValor.STAR_WARS;
            } else if (maior_quant == quant_st) {
                valor_tmp = TipoValor.STAR_TREK;
            } else {
                valor_tmp = TipoValor.NENHUMA;
            }
        }
        return valor_tmp;
    }

    public void broadcast(int porta, Pacote pacote) throws IOException, UnknownHostException, ClassNotFoundException {
        for (int processo : processos) {
            if (processo != id) {
                pacote.setId_destino(processo);
                executorService.execute(new Enviar(id, "localhost", porta + processo, pacote));
                System.out.println("Processo[" + getId() + "]: Pacote Enviado: " + pacote + " para o processo " + processo);
            }
        }
    }

    public int maior(int rodada, int ultima_rodada) {
        if (rodada > ultima_rodada) {
            return rodada;
        } else {
            return ultima_rodada;
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

    public int getUltima_rodada() {
        return ultima_rodada;
    }

    public void setUltima_rodada(int ultima_rodada) {
        this.ultima_rodada = ultima_rodada;
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

    public boolean addQuorum(int processo) {
        return quorum.add(processo);
    }

    public boolean removeQuorum(int processo) {
        return quorum.remove((Integer) processo);
    }

    public boolean removeAllQuorum(List<Integer> processos) {
        return quorum.removeAll(processos);
    }
}
