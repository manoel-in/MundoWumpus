package com.mycompany.mundowumpus.algoritmogenetico;

import com.mycompany.mundowumpus.agente.Acao;
import java.util.Random;

public class Cromossomo {
    private int[] genes;
    private double fitness;
    private static final int TAMANHO = 200; 
    private static final Random rand = new Random();

    public Cromossomo() {
        genes = new int[TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            genes[i] = rand.nextInt(6); 
        }
        fitness = 0;
    }

    public Cromossomo(int[] genes) {
        this.genes = genes.clone();
    }

    public int getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, int valor) {
        genes[index] = valor;
    }

    public int[] getGenes() {
        return genes;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public static int getTamanho() {
        return TAMANHO;
    }

    public static Acao codigoParaAcao(int codigo) {
        switch (codigo) {
            case 0: return Acao.MOVER_NORTE;
            case 1: return Acao.MOVER_SUL;
            case 2: return Acao.MOVER_LESTE;
            case 3: return Acao.MOVER_OESTE;
            case 4: return Acao.PEGAR_OURO;
            case 5: return Acao.ATIRAR;
            default: return Acao.MOVER_NORTE;
        }
    }
}