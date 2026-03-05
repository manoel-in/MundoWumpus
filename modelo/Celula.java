package com.mycompany.mundowumpus.modelo;

public class Celula {
    private boolean wumpus;
    private boolean poco;
    private boolean ouro;
    private boolean fedor;
    private boolean brisa;
    private boolean brilho;
    private boolean visitada; 

    public Celula() {
        this.wumpus = false;
        this.poco = false;
        this.ouro = false;
        this.fedor = false;
        this.brisa = false;
        this.brilho = false;
        this.visitada = false;
    }
    
    public Celula(Celula outra) {
        this.wumpus = outra.wumpus;
        this.poco = outra.poco;
        this.ouro = outra.ouro;
        this.fedor = outra.fedor;
        this.brisa = outra.brisa;
        this.brilho = outra.brilho;
        this.visitada = outra.visitada;
    }

    public boolean temWumpus() { return wumpus; }
    public void setWumpus(boolean wumpus) { this.wumpus = wumpus; }
    public boolean temPoco() { return poco; }
    public void setPoco(boolean poco) { this.poco = poco; }
    public boolean temOuro() { return ouro; }
    public void setOuro(boolean ouro) { this.ouro = ouro; }
    public boolean temFedor() { return fedor; }
    public void setFedor(boolean fedor) { this.fedor = fedor; }
    public boolean temBrisa() { return brisa; }
    public void setBrisa(boolean brisa) { this.brisa = brisa; }
    public boolean temBrilho() { return brilho; }
    public void setBrilho(boolean brilho) { this.brilho = brilho; }
    public boolean foiVisitada() { return visitada; }
    public void setVisitada(boolean visitada) { this.visitada = visitada; }
    // Verifica se a célula está ocupada por algum objeto
    public boolean temObjeto() {
        return wumpus || poco || ouro;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (wumpus) sb.append("W");
        else if (poco) sb.append("P");
        else if (ouro) sb.append("O");
        else sb.append(".");

        if (fedor) sb.append("F");
        if (brisa) sb.append("B");
        if (brilho) sb.append("L");
        return sb.toString();
    }
}