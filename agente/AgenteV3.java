package com.mycompany.mundowumpus.agente;

import com.mycompany.mundowumpus.modelo.Ambiente;
import com.mycompany.mundowumpus.modelo.Celula;
import com.mycompany.mundowumpus.algoritmogenetico.Cromossomo;

public class AgenteV3 extends AgenteV1 {
    private Cromossomo plano;
    private int indiceAcao;
    
    public AgenteV3(Ambiente ambiente, Cromossomo plano) {
        super(ambiente);
        this.plano = plano;
        this.indiceAcao = 0;
        this.passosContador = 0;
    }

    @Override
    public boolean passo() {
        if (!ativo) {
            ultimaMensagem = "Agente já terminou.";
            return false;
        }

        // Verificar morte/vencimento
        Celula atual = ambiente.getCelula(linha, coluna);
        if (atual.temPoco()) {
            ativo = false;
            ultimaMensagem = "Caiu em um poço! Fim de jogo.";
            return false;
        }
        if (atual.temWumpus() && wumpusVivo) {
            ativo = false;
            ultimaMensagem = "Foi devorado pelo Wumpus! Fim de jogo.";
            return false;
        }
        if (temOuro && linha == 0 && coluna == 0) {
            ativo = false;
            ultimaMensagem = "Voltou com o ouro! Vitória!";
            return false;
        }

        // Se ainda há ações no plano
        if (indiceAcao < Cromossomo.getTamanho()) {
            Acao acao = Cromossomo.codigoParaAcao(plano.getGene(indiceAcao));
            indiceAcao++;
            executarAcao(acao);
            passosContador++;
            ultimaMensagem = "Ação: " + acao + " | Posição: (" + linha + "," + coluna + ")";
        } else {
            // Plano esgotado, agente para
            ativo = false;
            ultimaMensagem = "Plano esgotado. Agente parou.";
        }
        return ativo;
    }

    @Override
    protected void executarAcao(Acao acao) {
        if (acao == Acao.ATIRAR) {
            if (flecha) {
                flecha = false;
                // Tenta matar Wumpus em qualquer adjacente (tiro em todas direções)
                int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                boolean acertou = false;
                for (int[] d : dirs) {
                    int alvoL = linha + d[0];
                    int alvoC = coluna + d[1];
                    if (ambiente.coordenadaValida(alvoL, alvoC) && ambiente.getCelula(alvoL, alvoC).temWumpus()) {
                        ambiente.getCelula(alvoL, alvoC).setWumpus(false);
                        wumpusVivo = false;
                        ambiente.atualizarPercepcoes();
                        acertou = true;
                    }
                }
                if (acertou) {
                    ultimaMensagem = "Atirou e acertou o Wumpus!";
                } else {
                    ultimaMensagem = "Atirou mas errou.";
                }
            } else {
                ultimaMensagem = "Não tem flecha.";
            }
        } else {
            super.executarAcao(acao);
        }
    }
}