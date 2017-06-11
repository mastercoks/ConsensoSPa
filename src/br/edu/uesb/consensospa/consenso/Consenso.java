/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa.consenso;

import br.edu.uesb.consensospa.detectorfalhas.DetectorFalhas;
import br.edu.uesb.consensospa.detectorfalhas.Processo;
import br.edu.uesb.consensospa.enumerado.TipoValor;
import br.edu.uesb.consensospa.rede.Enviar;
import br.edu.uesb.consensospa.rede.Pacote;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Matheus
 */
public class Consenso implements Runnable {

    private int rodada;
    private int ultima_rodada;
    private TipoValor valor;
    private List<Integer> quorum;
    private final Processo processo;
    private final DetectorFalhas detectorFalhas;

    public Consenso(Processo processo) {
        this.processo = processo;
        this.rodada = 0;
        this.ultima_rodada = 0;
        this.valor = null;
        this.quorum = new ArrayList<>();
        detectorFalhas = new DetectorFalhas(processo, 9000 + processo.getId());
    }

    public void iniciar() throws InterruptedException, ExecutionException, IOException {
        detectorFalhas.iniciar();
        System.out.println(this);
        new Aceitador(this).iniciar();
        processo.getExecutorService().execute(this);
        processo.getExecutorService().execute(new T1(this));
    }

    private class T1 implements Runnable {

        private final Consenso consenso;

        public T1(Consenso consenso) {
            this.consenso = consenso;
        }

        @Override
        @SuppressWarnings("empty-statement")
        public void run() {
            Proponente proponente = new Proponente(consenso);
            while (consenso.getProcesso().getId() != consenso.getProcesso().getEleicao().getLider()) {
                System.out.print("");
            }
            if (consenso.getProcesso().getId() == consenso.getProcesso().getEleicao().getLider()) { //getEleicao().isLider()
                consenso.getProcesso().getExecutorService().execute(new Proponente(consenso));
            }
        }
    }

    @Override
    public void run() {
        Future<TipoValor> future = processo.getExecutorService().submit(new Aprendiz(this));
        try {
            setValor(future.get());
        } catch (InterruptedException | ExecutionException ex) {
        }
        System.out.println(this);
//        processo.getExecutorService().shutdown();
    }

    @Override
    public String toString() {
        return "--------------------\nProcesso[" + processo.getId()
                + "]\nRodada = " + getRodada() + "\nUltimaRodada = "
                + getUltima_rodada() + "\nQuorum = " + getQuorum()
                + "\nValor = " + getValor() + "\nCorreto = "
                + processo.isCorreto() + "\n--------------------\n";
    }

    public List<Integer> gerarQuorum() {

        List<Integer> q = new ArrayList<>();
        for (Integer processo_aux : processo.getProcessos()) {
            if (!detectorFalhas.getDefeituosos().contains(processo_aux)) {
                q.add(processo_aux);
            }
        }
        return q;
    }

    public TipoValor checarQuorum(List<TipoValor> respostas, int quorum) {
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
        if (maior_quant >= quorum) {
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
        for (int processo_aux : processo.getProcessos()) {
            if (processo_aux != processo.getId()) {
                pacote.setId_destino(processo_aux);
                processo.getExecutorService().execute(new Enviar(processo.getId(), "localhost", porta + processo_aux, pacote));
                System.out.println("Processo[" + processo.getId() + "]: Pacote Enviado: " + pacote + " para o processo " + processo_aux);
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

    public TipoValor escolherValor() {
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

    public boolean addQuorum(int processo) {
        return quorum.add(processo);
    }

    public boolean removeQuorum(int processo) {
        return quorum.remove((Integer) processo);
    }

    public boolean removeAllQuorum(List<Integer> processos) {
        return quorum.removeAll(processos);
    }

    public Processo getProcesso() {
        return processo;
    }

    public DetectorFalhas getDetectorFalhas() {
        return detectorFalhas;
    }
}
