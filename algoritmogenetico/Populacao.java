package com.mycompany.mundowumpus.algoritmogenetico;

import java.util.Arrays;
import java.util.Comparator;

public class Populacao {
    private Cromossomo[] individuos;
    private int tamanhoPopulacao;

    public Populacao(int tamanhoPopulacao) {
        this.tamanhoPopulacao = tamanhoPopulacao;
        individuos = new Cromossomo[tamanhoPopulacao];
        for (int i = 0; i < tamanhoPopulacao; i++) {
            individuos[i] = new Cromossomo();
        }
    }

    public Cromossomo getIndividuo(int index) {
        return individuos[index];
    }

    public void setIndividuo(int index, Cromossomo c) {
        individuos[index] = c;
    }
    public int getTamanho() {
        return tamanhoPopulacao;
    }

    // Ordena por fitness decrescente
    public void ordenarPorFitness() {
        Arrays.sort(individuos, Comparator.comparingDouble(Cromossomo::getFitness).reversed());
    }

    // Retorna o melhor indivíduo
    public Cromossomo getMelhor() {
        ordenarPorFitness();
        return individuos[0];
    }
}