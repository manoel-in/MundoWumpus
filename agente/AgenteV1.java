package com.mycompany.mundowumpus.agente;

import com.mycompany.mundowumpus.modelo.Ambiente;
import com.mycompany.mundowumpus.modelo.Celula;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AgenteV1 implements Agente {
    protected Ambiente ambiente;
    protected int linha, coluna;
    protected boolean temOuro;
    protected boolean flecha;
    protected boolean wumpusVivo;
    protected boolean ativo;
    protected Random rand;
    protected String ultimaMensagem;
    protected int passos = 0;
    protected int passosContador;
   
    public AgenteV1(Ambiente ambiente) {
        this.ambiente = ambiente;
        this.linha = 0;
        this.coluna = 0;
        this.temOuro = false;
        this.flecha = true;
        this.wumpusVivo = true;
        this.ativo = true;  
        this.rand = new Random();
        this.ultimaMensagem = "Agente V1 iniciado.";
        this.passosContador = 0;
        ambiente.getCelula(0, 0).setVisitada(true);
    }

    @Override
    public boolean passo() {
        passos++;
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

        // Percepções
        List<String> percepcoes = getPercepcoes();
        // Escolher ação
        Acao acao = escolherAcao(percepcoes);
        if (acao == null) {
            ultimaMensagem = "Nenhuma ação possível. Parado.";
            ativo = false;
            return false;
        }

        // Executar ação
        executarAcao(acao);
        passosContador++;
        ultimaMensagem = "Ação: " + acao + " | Posição: (" + linha + "," + coluna + ")";
        System.out.println(ultimaMensagem);
        return ativo;
    }

   
    protected void executarAcao(Acao acao) {
        switch (acao) {
            case MOVER_NORTE:
                if (podeMover("NORTE")) linha++;
                break;
            case MOVER_SUL:
                if (podeMover("SUL")) linha--;
                break;
            case MOVER_LESTE:
                if (podeMover("LESTE")) coluna++;
                break;
            case MOVER_OESTE:
                if (podeMover("OESTE")) coluna--;
                break;
            case PEGAR_OURO:
                Celula atual = ambiente.getCelula(linha, coluna);
                if (atual.temOuro() && !temOuro) {
                    temOuro = true;
                    atual.setOuro(false);
                    atual.setBrilho(false);
                    ambiente.atualizarPercepcoes();
                }
                break;
            case ATIRAR:
                // lógica simples do V1 (atirar aleatório)
                if (flecha) {
                    flecha = false;
                    String[] dirs = {"NORTE", "SUL", "LESTE", "OESTE"};
                    String dir = dirs[rand.nextInt(4)];
                    int alvoLinha = linha, alvoColuna = coluna;
                    switch (dir) {
                        case "NORTE": alvoLinha++; break;
                        case "SUL": alvoLinha--; break;
                        case "LESTE": alvoColuna++; break;
                        case "OESTE": alvoColuna--; break;
                    }
                    if (ambiente.coordenadaValida(alvoLinha, alvoColuna)) {
                        Celula c = ambiente.getCelula(alvoLinha, alvoColuna);
                        if (c.temWumpus()) {
                            c.setWumpus(false);
                            wumpusVivo = false;
                            ambiente.atualizarPercepcoes();
                            ultimaMensagem = "Atirou e acertou o Wumpus!";
                        } else {
                            ultimaMensagem = "Atirou mas errou.";
                        }
                    }
                } else {
                    ultimaMensagem = "Não tem flecha.";
                }
                break;
        }
        // Marcar nova posição como visitada se moveu
        if (acao == Acao.MOVER_NORTE || acao == Acao.MOVER_SUL ||
            acao == Acao.MOVER_LESTE || acao == Acao.MOVER_OESTE) {
            ambiente.getCelula(linha, coluna).setVisitada(true);
        }
    }

    protected List<String> getPercepcoes() {
        List<String> percepcoes = new ArrayList<>();
        Celula atual = ambiente.getCelula(linha, coluna);
        if (atual.temFedor()) percepcoes.add("FEDOR");
        if (atual.temBrisa()) percepcoes.add("BRISA");
        if (atual.temBrilho()) percepcoes.add("BRILHO");
        return percepcoes;
    }

    protected boolean podeMover(String direcao) {
        int n = ambiente.getN();
        switch (direcao) {
            case "NORTE": return linha < n - 1; // Norte = aumentar linha
            case "SUL":   return linha > 0;
            case "LESTE": return coluna < n - 1;
            case "OESTE": return coluna > 0;
            default: return false;
        }
    }

    protected Acao escolherAcao(List<String> percepcoes) {
        // PRIORIDADE 1: Se vir o brilho, ignore tudo e pegue o ouro
        if (percepcoes.contains("BRILHO") && !temOuro) {
            return Acao.PEGAR_OURO;
        }

        // PRIORIDADE 2: Se sentir fedor e tiver flecha, atire na direção provável
        // No V1 ele atira aleatório, mas só se NÃO houver brilho
        if (percepcoes.contains("FEDOR") && flecha && wumpusVivo) {
            return Acao.ATIRAR;
        }

        // PRIORIDADE 3: Movimentação
        return movimentoAleatorio();
    }

    private Acao movimentoAleatorio() {
        List<Acao> movimentos = new ArrayList<>();
        if (podeMover("NORTE")) movimentos.add(Acao.MOVER_NORTE);
        if (podeMover("SUL"))   movimentos.add(Acao.MOVER_SUL);
        if (podeMover("LESTE")) movimentos.add(Acao.MOVER_LESTE);
        if (podeMover("OESTE")) movimentos.add(Acao.MOVER_OESTE);
        return movimentos.get(rand.nextInt(movimentos.size()));
    }
    
        public int getFlechasRestantes() {
            return flecha ? 1 : 0;
        }

    @Override public int getLinha() { return linha; }
    @Override public int getColuna() { return coluna; }
    @Override public boolean isAtivo() { return ativo; }
    @Override public String getMensagem() { return ultimaMensagem; }
    @Override public boolean getFlecha() { return flecha; }
    @Override public boolean getTemOuro() { return temOuro; }
    @Override public boolean getWumpusVivo() { return wumpusVivo; }
    @Override public int getPassos() { return passos; }
    
}