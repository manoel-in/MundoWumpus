package com.mycompany.mundowumpus.modelo;

public class Ambiente {
    private int n; // ordem da matriz (n x n)
    private Celula[][] grade;

    public Ambiente(int n) {
        this.n = n;
        this.grade = new Celula[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grade[i][j] = new Celula();
            }
        }
    }
   
    public Ambiente(Ambiente outro) {
    this.n = outro.n;
    this.grade = new Celula[n][n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            this.grade[i][j] = new Celula(outro.grade[i][j]);
        }
    }
}
    public int getN() { return n; }

    public Celula getCelula(int linha, int coluna) {
        return grade[linha][coluna];
    }

    public void setCelula(int linha, int coluna, Celula celula) {
        grade[linha][coluna] = celula;
    }

    // Verifica se coordenadas são válidas
    public boolean coordenadaValida(int linha, int coluna) {
        return linha >= 0 && linha < n && coluna >= 0 && coluna < n;
    }

    // Propaga as percepções a partir dos objetos já posicionados
    public void atualizarPercepcoes() {
        // Primeiro, limpa todas as percepções
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grade[i][j].setFedor(false);
                grade[i][j].setBrisa(false);
                grade[i][j].setBrilho(false);
            }
        }

        // Para cada célula com objeto, adiciona percepções nas adjacentes
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grade[i][j].temWumpus()) {
                    // Fedor nas adjacentes
                    if (coordenadaValida(i-1, j)) grade[i-1][j].setFedor(true);
                    if (coordenadaValida(i+1, j)) grade[i+1][j].setFedor(true);
                    if (coordenadaValida(i, j-1)) grade[i][j-1].setFedor(true);
                    if (coordenadaValida(i, j+1)) grade[i][j+1].setFedor(true);
                }
                if (grade[i][j].temPoco()) {
                    // Brisa nas adjacentes
                    if (coordenadaValida(i-1, j)) grade[i-1][j].setBrisa(true);
                    if (coordenadaValida(i+1, j)) grade[i+1][j].setBrisa(true);
                    if (coordenadaValida(i, j-1)) grade[i][j-1].setBrisa(true);
                    if (coordenadaValida(i, j+1)) grade[i][j+1].setBrisa(true);
                }
                if (grade[i][j].temOuro()) {
                    // Brilho na própria célula
                    grade[i][j].setBrilho(true);
                }
            }
        }
    }

    public void exibir() {
        for (int i = n-1; i >= 0; i--) {
            for (int j = 0; j < n; j++) {
                System.out.print(grade[i][j] + "\t");
            }
            System.out.println();
        }
    }
}