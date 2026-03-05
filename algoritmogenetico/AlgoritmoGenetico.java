package com.mycompany.mundowumpus.algoritmogenetico;

import com.mycompany.mundowumpus.modelo.Ambiente;
import com.mycompany.mundowumpus.modelo.Celula;
import com.mycompany.mundowumpus.agente.Acao;
import java.util.*;

public class AlgoritmoGenetico {
    private Ambiente ambiente;
    private int tamanhoPopulacao;
    private double taxaCruzamento;
    private double taxaMutacao;
    private int numeroGeracoes;
    private Random rand = new Random();

    public AlgoritmoGenetico(Ambiente ambiente, int tamanhoPopulacao, double taxaCruzamento, 
                             double taxaMutacao, int numeroGeracoes) {
        this.ambiente = ambiente;
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.taxaCruzamento = taxaCruzamento;
        this.taxaMutacao = taxaMutacao;
        this.numeroGeracoes = numeroGeracoes;
    }

    // Método principal de execução do AG
    public Cromossomo executar() {
        Populacao pop = new Populacao(tamanhoPopulacao);
        for (int geracao = 0; geracao < numeroGeracoes; geracao++) {
            // Avaliar fitness
            for (int i = 0; i < pop.getTamanho(); i++) {
                double fit = calcularFitness(pop.getIndividuo(i));
                pop.getIndividuo(i).setFitness(fit);
            }
            pop.ordenarPorFitness();
            System.out.println("Geração " + geracao + " Melhor fitness: " + pop.getMelhor().getFitness());

            // Nova população
            Populacao novaPop = new Populacao(tamanhoPopulacao);
            // Elitismo: manter o melhor
            novaPop.setIndividuo(0, pop.getMelhor());

            // Preencher o resto
            for (int i = 1; i < tamanhoPopulacao; i += 2) {
                Cromossomo pai1 = selecionar(pop);
                Cromossomo pai2 = selecionar(pop);
                Cromossomo[] filhos = cruzar(pai1, pai2);
                mutar(filhos[0]);
                mutar(filhos[1]);
                novaPop.setIndividuo(i, filhos[0]);
                if (i+1 < tamanhoPopulacao) {
                    novaPop.setIndividuo(i+1, filhos[1]);
                }
            }
            pop = novaPop;
        }
        return pop.getMelhor();
    }

    // Função de avaliação (fitness)
    private double calcularFitness(Cromossomo c) {
        Ambiente copia = new Ambiente(ambiente);
        int linha = 0, coluna = 0;
        boolean temOuro = false;
        boolean flecha = true;
        boolean wumpusVivo = existeWumpus(copia);
        int passos = 0;
        double fitness = 0;
        boolean vivo = true;

        boolean[][] visitadas = new boolean[copia.getN()][copia.getN()];
        visitadas[0][0] = true;
        int celulasVisitadas = 1;

        final int MAX_HIST = 5;
        List<String> historico = new ArrayList<>();

        for (int i = 0; i < Cromossomo.getTamanho() && vivo; i++) {
            Acao acao = Cromossomo.codigoParaAcao(c.getGene(i));
            passos++;

            String estado = linha + "," + coluna + "," + acao;
            historico.add(estado);
            if (historico.size() > MAX_HIST) historico.remove(0);
            if (historico.size() == MAX_HIST && historico.stream().distinct().count() == 1) {
                fitness -= 200;
            }

            boolean acaoInvalida = false;
            switch (acao) {
                case MOVER_NORTE:
                    if (linha >= copia.getN() - 1) acaoInvalida = true;
                    else linha++;
                    break;
                case MOVER_SUL:
                    if (linha <= 0) acaoInvalida = true;
                    else linha--;
                    break;
                case MOVER_LESTE:
                    if (coluna >= copia.getN() - 1) acaoInvalida = true;
                    else coluna++;
                    break;
                case MOVER_OESTE:
                    if (coluna <= 0) acaoInvalida = true;
                    else coluna--;
                    break;
                case PEGAR_OURO:
                    Celula cel = copia.getCelula(linha, coluna);
                    if (cel.temOuro() && !temOuro) {
                        temOuro = true;
                        cel.setOuro(false);
                        cel.setBrilho(false);
                    } else {
                        acaoInvalida = true;
                    }
                    break;
                case ATIRAR:
                    if (!flecha) {
                        acaoInvalida = true;
                    } else {
                        flecha = false;
                        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                        for (int[] d : dirs) {
                            int alvoL = linha + d[0];
                            int alvoC = coluna + d[1];
                            if (copia.coordenadaValida(alvoL, alvoC) && copia.getCelula(alvoL, alvoC).temWumpus()) {
                                copia.getCelula(alvoL, alvoC).setWumpus(false);
                                wumpusVivo = false;
                                fitness += 500; // recompensa por matar Wumpus
                            }
                        }
                    }
                    break;
            }

            if (acaoInvalida) {
                fitness -= 100;
            }

            if (!visitadas[linha][coluna]) {
                visitadas[linha][coluna] = true;
                celulasVisitadas++;
                fitness += 20;
            }

            Celula celAtual = copia.getCelula(linha, coluna);
            if (celAtual.temPoco() || (celAtual.temWumpus() && wumpusVivo)) {
                vivo = false;
                fitness -= 1000;
            }

            if (temOuro && linha == 0 && coluna == 0) {
                fitness += 5000;
                fitness += (Cromossomo.getTamanho() - passos) * 50;
                break;
            }
        }

        if (temOuro) fitness += 1000;
        fitness -= passos * 5;
        fitness += celulasVisitadas * 10;

        return fitness;
    }

    // Verifica se ainda existe Wumpus no ambiente
    private boolean existeWumpus( Ambiente amb) {
        for (int i = 0; i < amb.getN(); i++)
            for (int j = 0; j < amb.getN(); j++)
                if (amb.getCelula(i, j).temWumpus()) return true;
        return false;
    }

    // Seleção por torneio
    private Cromossomo selecionar(Populacao pop) {
        int k = 3;
        Cromossomo melhor = null;
        for (int i = 0; i < k; i++) {
            int idx = rand.nextInt(pop.getTamanho());
            Cromossomo candidato = pop.getIndividuo(idx);
            if (melhor == null || candidato.getFitness() > melhor.getFitness()) {
                melhor = candidato;
            }
        }
        return melhor;
    }

    // Cruzamento de um ponto
    private Cromossomo[] cruzar(Cromossomo pai1, Cromossomo pai2) {
        if (rand.nextDouble() > taxaCruzamento) {
            return new Cromossomo[] {pai1, pai2};
        }
        int ponto = rand.nextInt(Cromossomo.getTamanho());
        int[] genes1 = pai1.getGenes();
        int[] genes2 = pai2.getGenes();
        int[] filho1 = new int[Cromossomo.getTamanho()];
        int[] filho2 = new int[Cromossomo.getTamanho()];
        for (int i = 0; i < ponto; i++) {
            filho1[i] = genes1[i];
            filho2[i] = genes2[i];
        }
        for (int i = ponto; i < Cromossomo.getTamanho(); i++) {
            filho1[i] = genes2[i];
            filho2[i] = genes1[i];
        }
        return new Cromossomo[] {new Cromossomo(filho1), new Cromossomo(filho2)};
    }

    // Mutação
    private void mutar(Cromossomo c) {
        for (int i = 0; i < Cromossomo.getTamanho(); i++) {
            if (rand.nextDouble() < taxaMutacao) {
                c.setGene(i, rand.nextInt(6));
            }
        }
    }
}