/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.uesb.consensospa;

import br.edu.uesb.consensospa.enumerado.TipoValor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 * @author Matheus
 */
public class Consenso extends ConsensoAbstrato {

    

    public Consenso(int id, ExecutorService executorService, Eleicao eleicao) {
        super(id, 0, 0, new ArrayList<>(), eleicao.getProcessos(), executorService, eleicao);
    }

    public void iniciar() throws InterruptedException, ExecutionException {
        getExecutorService().execute(new Proponente(getId(), getRodada(), getMaior_rodada(), getValor(), getQuorum(), getProcessos(), getExecutorService(), getEleicao()));
        new Aceitador(getId(), getRodada(), getMaior_rodada(), getValor(), getQuorum(), getProcessos(), getExecutorService()).iniciar();
        Future<TipoValor> future = getExecutorService().submit(new Aprendiz(getId(), getRodada(), getQuorum(), getProcessos(), getExecutorService(), getEleicao()));
        setValor(future.get());
        System.out.println(getValor());
    }
}
