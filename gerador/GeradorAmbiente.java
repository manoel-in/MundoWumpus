package com.mycompany.mundowumpus.gerador;

import com.mycompany.mundowumpus.modelo.Ambiente;
import java.util.Random;
import java.util.Scanner;

public class GeradorAmbiente {
    private Random rand = new Random();
    public Ambiente gerarPorUsuario() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Digite o tamanho n (n > 3): ");
        int n = sc.nextInt();
        while (n < 3) {
            System.out.print("n deve ser maior que 3. Digite novamente: ");
            n = sc.nextInt();
        }

        System.out.print("Quantidade de poços (p > 0): ");
        int p = sc.nextInt();
        while (p <= 0 || p > n*n - 1) { // não pode ocupar (0,0) e nem todas as células
            System.out.print("p inválido. Digite novamente: ");
            p = sc.nextInt();
        }

        System.out.print("Quantidade de Wumpus (W > 0): ");
        int w = sc.nextInt();
        while (w <= 0 || w > n*n - 1 - p) {
            System.out.print("W inválido. Digite novamente: ");
            w = sc.nextInt();
        }

        System.out.print("Quantidade de ouro (o > 0): ");
        int o = sc.nextInt();
        while (o <= 0 || o > n*n - 1 - p - w) {
            System.out.print("o inválido. Digite novamente: ");
            o = sc.nextInt();
        }

        return gerarAmbiente(n, p, w, o);
    }


    // Gera ambiente com quantidades automáticas baseadas em n
    public Ambiente gerarAutomatico(int n) {
        // Regra
        int p = n;
        int w = 1;
        int o = 1;
        // Ajuste para não exceder células disponíveis
        int maxObjetos = n*n - 1; // excluindo (0,0)

        while (p + w + o > maxObjetos) {
            if (p > 1) p--;
            else if (w > 1) w--;
            else if (o > 1) o--;
        }
        return gerarAmbiente(n, p, w, o);
    }


    private Ambiente gerarAmbiente(int n, int p, int w, int o) {
        Ambiente ambiente = new Ambiente(n);
        // Posicionar objetos aleatoriamente, exceto em (0,0)
        int totalObjetos = p + w + o;
        int[] linhas = new int[totalObjetos];
        int[] colunas = new int[totalObjetos];
        int count = 0;

        while (count < totalObjetos) {
            int i = rand.nextInt(n);
            int j = rand.nextInt(n);
            if (i == 0 && j == 0) continue;

            // Verifica se já não foi escolhido
            boolean jaEscolhido = false;
            for (int k = 0; k < count; k++) {
                if (linhas[k] == i && colunas[k] == j) {
                    jaEscolhido = true;
                    break;
                }
            }
            if (!jaEscolhido) {
                linhas[count] = i;
                colunas[count] = j;
                count++;
            }
        }

        //poços
        for (int k = 1; k < p; k++) {
            int i = linhas[k];
            int j = colunas[k];
            ambiente.getCelula(i, j).setPoco(true);
        }
        // Wumpus
        for (int k = p; k < p + w; k++) {
            int i = linhas[k];
            int j = colunas[k];
            ambiente.getCelula(i, j).setWumpus(true);
        }
        // Depois ouro
        for (int k = p + w; k < p + w + o; k++) {
            int i = linhas[k];
            int j = colunas[k];
            ambiente.getCelula(i, j).setOuro(true);
        }

        // Atualizar percepções
        ambiente.atualizarPercepcoes();

        return ambiente;
    }

 
    public Ambiente gerarAutomaticoComSemente(int n, long seed) {
        Random rand = new Random(seed);
        int p = n; // número de poços = tamanho da matriz
        int w = 1;
        int o = 1;
        int maxObjetos = n * n - 1;
        while (p + w + o > maxObjetos) {
            p--; // reduz se necessário
        }
        if (p < 1) p = 1;

        // Chama um método privado que aceita um Random personalizado
        return gerarAmbienteComRandom(n, p, w, o, rand);
    }
    
    private Ambiente gerarAmbienteComRandom(int n, int p, int w, int o, Random rand) {
        Ambiente ambiente = new Ambiente(n);
        int totalObjetos = p + w + o;
        int[] linhas = new int[totalObjetos];
        int[] colunas = new int[totalObjetos];
        int count = 0;

        while (count < totalObjetos) {
            int i = rand.nextInt(n);
            int j = rand.nextInt(n);
            if (i == 0 && j == 0) continue; // (0,0) proibido

            boolean jaEscolhido = false;
            for (int k = 0; k < count; k++) {
                if (linhas[k] == i && colunas[k] == j) {
                    jaEscolhido = true;
                    break;
                }
            }
            if (!jaEscolhido) {
                linhas[count] = i;
                colunas[count] = j;
                count++;
            }
        }

        // Distribuir objetos
        for (int k = 0; k < p; k++) {
            ambiente.getCelula(linhas[k], colunas[k]).setPoco(true);
        }
        for (int k = p; k < p + w; k++) {
            ambiente.getCelula(linhas[k], colunas[k]).setWumpus(true);
        }
        for (int k = p + w; k < p + w + o; k++) {
            ambiente.getCelula(linhas[k], colunas[k]).setOuro(true);
        }

        ambiente.atualizarPercepcoes();
        return ambiente;
    }
    
}