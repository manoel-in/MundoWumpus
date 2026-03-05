package com.mycompany.mundowumpus.agente;

import com.mycompany.mundowumpus.modelo.Ambiente;
import com.mycompany.mundowumpus.modelo.Celula;
import java.util.*;

public class AgenteV2 extends AgenteV1 implements Agente {

    // Classe interna para representar o conhecimento sobre uma célula
    private class Conhecimento {
        boolean visitado;
        boolean seguro;
        boolean suspeitoPoco;
        boolean suspeitoWumpus;
        boolean temBrisa;
        boolean temFedor;

        Conhecimento() {
            visitado = false;
            seguro = false;
            suspeitoPoco = false;
            suspeitoWumpus = false;
            temBrisa = false;
            temFedor = false;
        }
    }

    private Conhecimento[][] memoria;
    private int n;

    public AgenteV2(Ambiente ambiente) {
        super(ambiente);
        this.n = ambiente.getN();
        this.memoria = new Conhecimento[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                memoria[i][j] = new Conhecimento();
            }
        }
        // Marcar a posição inicial como segura e visitada
        memoria[0][0].visitado = true;
        memoria[0][0].seguro = true;
        ultimaMensagem = "Agente V2 iniciado na posição (0,0).";
    }
  
        public int getFlechasRestantes() {
        return flecha ? 1 : 0; // ou o número real, se houver mais de uma flecha
    }

    @Override
    public boolean passo() {
        if (!ativo) {
            ultimaMensagem = "Agente já terminou.";
            return false;
        }

        // Verificar se morreu ou venceu
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

        // Obter percepções e atualizar memória
        List<String> percepcoes = getPercepcoes();
        atualizarMemoria(percepcoes);
        
        // Escolher ação baseada na memória
        Acao acao = escolherAcao(percepcoes);
        if (acao == null) {
            ultimaMensagem = "Nenhuma ação possível. Agente parado.";
            ativo = false;
            return false;
        }

        // Executar ação
        executarAcao(acao);
        ultimaMensagem = "Ação: " + acao + " | Posição: (" + linha + "," + coluna + ")";
        return ativo;
    }
    
    @Override
    protected void executarAcao(Acao acao) {
        if (acao == Acao.ATIRAR) {
            if (flecha) {
                flecha = false;
                boolean acertou = false;
                // Tenta matar Wumpus em qualquer adjacente
                int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
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

    private void atualizarMemoria(List<String> percepcoes) {
        Conhecimento atual = memoria[linha][coluna];
        atual.visitado = true;
        atual.seguro = true;
        Celula celulaReal = ambiente.getCelula(linha, coluna);
        atual.temBrisa = celulaReal.temBrisa();
        atual.temFedor = celulaReal.temFedor();

        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] d : dirs) {
            int ni = linha + d[0];
            int nj = coluna + d[1];
            if (ambiente.coordenadaValida(ni, nj)) {
                Conhecimento adj = memoria[ni][nj];
                if (!atual.temBrisa && !atual.temFedor) {
                    adj.seguro = true;
                } else if (!atual.temBrisa) {
                    adj.suspeitoPoco = false; // Livre de poço, mas Wumpus ainda é incógnita
                } else if (!atual.temFedor) {
                    adj.suspeitoWumpus = false; // Livre de Wumpus, mas Poço ainda é incógnita
                }
                if (adj.visitado) {
                    adj.seguro = true;
                    adj.suspeitoPoco = false;
                    adj.suspeitoWumpus = false;
                }
            }
        }
    }

    @Override
    protected Acao escolherAcao(List<String> percepcoes) {
        // 1. Se há brilho e não pegou ouro, pegar
        if (percepcoes.contains("BRILHO") && !temOuro) {
            return Acao.PEGAR_OURO;
        }

        // 2. Se sente fedor e tem flecha, tentar atirar em direção suspeita
        if (percepcoes.contains("FEDOR") && flecha && wumpusVivo) {
            int[][] dirs = {{-1,0,0}, {1,0,1}, {0,-1,2}, {0,1,3}};
            
            for (int[] d : dirs) {
                int ni = linha + d[0];
                int nj = coluna + d[1];
                
                if (ambiente.coordenadaValida(ni, nj) && memoria[ni][nj].suspeitoWumpus) {
                    return Acao.ATIRAR; // atirará na direção suspeita no método executarAcao
                }
            }
            // Se não há direção suspeita, atira aleatoriamente
            return Acao.ATIRAR;
        }

        // 3. Movimentação: priorizar células seguras não visitadas
        List<Acao> movimentosSeguros = new ArrayList<>();
        List<Acao> movimentosSuspeitos = new ArrayList<>();
        List<Acao> movimentosTodos = new ArrayList<>();

        if (podeMover("NORTE")) {
            int ni = linha + 1, nj = coluna;
            if (memoria[ni][nj].seguro && !memoria[ni][nj].visitado)
                movimentosSeguros.add(Acao.MOVER_NORTE);
            else if (!memoria[ni][nj].seguro)
                movimentosSuspeitos.add(Acao.MOVER_NORTE);
            movimentosTodos.add(Acao.MOVER_NORTE);
        }
        if (podeMover("SUL")) {
            int ni = linha - 1, nj = coluna;
            if (memoria[ni][nj].seguro && !memoria[ni][nj].visitado)
                movimentosSeguros.add(Acao.MOVER_SUL);
            else if (!memoria[ni][nj].seguro)
                movimentosSuspeitos.add(Acao.MOVER_SUL);
            movimentosTodos.add(Acao.MOVER_SUL);
        }
        if (podeMover("LESTE")) {
            int ni = linha, nj = coluna + 1;
            if (memoria[ni][nj].seguro && !memoria[ni][nj].visitado)
                movimentosSeguros.add(Acao.MOVER_LESTE);
            else if (!memoria[ni][nj].seguro)
                movimentosSuspeitos.add(Acao.MOVER_LESTE);
            movimentosTodos.add(Acao.MOVER_LESTE);
        }
        
        if (podeMover("OESTE")) {
            int ni = linha, nj = coluna - 1;
            if (memoria[ni][nj].seguro && !memoria[ni][nj].visitado)
                movimentosSeguros.add(Acao.MOVER_OESTE);
            else if (!memoria[ni][nj].seguro)
                movimentosSuspeitos.add(Acao.MOVER_OESTE);
            movimentosTodos.add(Acao.MOVER_OESTE);
        }

        if (!movimentosSeguros.isEmpty())
            return movimentosSeguros.get(rand.nextInt(movimentosSeguros.size()));
        if (!movimentosSuspeitos.isEmpty())
            return movimentosSuspeitos.get(rand.nextInt(movimentosSuspeitos.size()));
        if (!movimentosTodos.isEmpty())
            return movimentosTodos.get(rand.nextInt(movimentosTodos.size()));
        return null;
    }

    
}