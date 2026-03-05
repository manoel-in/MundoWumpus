package com.mycompany.mundowumpus.agente;

public interface Agente {
    boolean passo();
    int getLinha();
    int getColuna();
    boolean isAtivo();
    String getMensagem();
    boolean getFlecha();
    boolean getTemOuro();
    boolean getWumpusVivo();
    int getPassos();
}
