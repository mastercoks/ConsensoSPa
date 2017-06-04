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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Matheus
 */
public class Consenso extends ConsensoAbstrato implements Runnable {
    
    private final List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas;
    
    public Consenso(int id, ExecutorService executorService, Eleicao eleicao, List<DirectedGraph<Integer, DefaultEdge>> particoes_sincronas) {
        super(id, 0, 0, new ArrayList<>(), executorService, eleicao);
        this.particoes_sincronas = particoes_sincronas;
        System.out.println(this);
    }
    
    public void iniciar() throws InterruptedException, ExecutionException {
        new Aceitador(getId(), getRodada(), getUltima_rodada(), getValor(), getQuorum(), getProcessos(), getExecutorService()).iniciar();
        getExecutorService().execute(this);
        if (getId() == 0) { //getEleicao().isLider()
            getExecutorService().execute(new Proponente(getId(), getRodada(), getUltima_rodada(), getValor(), getQuorum(), getExecutorService(), getEleicao(), particoes_sincronas));
        }
//        Future<TipoValor> future = getExecutorService().submit(new Aprendiz(getId(), getRodada(), getQuorum(), getExecutorService(), getEleicao()));
//        setValor(future.get());
//        System.out.println(this);
//        getExecutorService().shutdown();
    }
    
    @Override
    public void run() {
        Future<TipoValor> future = getExecutorService().submit(new Aprendiz(getId(), getRodada(), getQuorum(), getExecutorService(), getEleicao()));
        try {
            setValor(future.get());
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Consenso.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(this);
        getExecutorService().shutdown();
        
    }
    
    @Override
    public String toString() {
        return "--------------------\nProcesso[" + getId() + "]\nRodada = " + getRodada() + "\nUltimaRodada = " + getUltima_rodada() + "\nQuorum = " + getQuorum() + "\nValor = " + getValor() + "\n--------------------\n";
    }
    
}
