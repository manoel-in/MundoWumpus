/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mundowumpus;

import com.mycompany.mundowumpus.agente.*;
import com.mycompany.mundowumpus.algoritmogenetico.AlgoritmoGenetico;
import com.mycompany.mundowumpus.algoritmogenetico.Cromossomo;
import com.mycompany.mundowumpus.gerador.GeradorAmbiente;
import com.mycompany.mundowumpus.modelo.Ambiente;
import com.mycompany.mundowumpus.modelo.Celula;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

public class MundoWumpus extends JFrame {
    private Ambiente ambiente;
    private Agente agente;
    private JButton[][] botoes;
    private JTextArea logArea;
    private JTextArea historicoArea;
    private JButton btnPasso, btnReiniciar, btnNovoAmbiente;
    private JComboBox<String> comboTipoAgente;
    private int n;
    private int pontuacao = 0;
    private boolean ouroPego = false;
    private boolean wumpusVivoAnterior = true;
    private int contadorPassos = 0;
    private JLabel lblPontuacao;
    private JLabel lblFlechas;
    private JLabel lblOuro;
    private JLabel lblWumpus;
    private JLabel lblPassos;
    private JLabel lblOurosRestantes;

    public MundoWumpus() {
        setTitle("Mundo de Wumpus");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel superior com controles
        JPanel painelControles = new JPanel();
        comboTipoAgente = new JComboBox<>(new String[]{"Agente V1", "Agente V2", "Agente V3"});
        btnNovoAmbiente = new JButton("Novo Ambiente");
        btnPasso = new JButton("Passo");
        btnReiniciar = new JButton("Reiniciar Agente");
        painelControles.add(new JLabel("Agente:"));
        painelControles.add(comboTipoAgente);
        painelControles.add(btnNovoAmbiente);
        painelControles.add(btnPasso);
        painelControles.add(btnReiniciar);
        add(painelControles, BorderLayout.NORTH);
        // Painel central com tabuleiro
        painelTabuleiro = new JPanel(new GridLayout(1,1));
        add(painelTabuleiro, BorderLayout.CENTER);
        // Painel de informações (lado direito)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informações"));
        
        lblPontuacao = new JLabel("Pontuação: 0");
        lblFlechas = new JLabel("Flechas: 1");
        lblOuro = new JLabel("Ouro: Não");
        lblWumpus = new JLabel("Wumpus: Vivo");
        lblPassos = new JLabel("Passos: 0");
        lblOurosRestantes = new JLabel("Ouros Restantes: ?");
        
        infoPanel.add(lblPontuacao);
        infoPanel.add(lblFlechas);
        infoPanel.add(lblOuro);
        infoPanel.add(lblWumpus);
        infoPanel.add(lblPassos);
        infoPanel.add(lblOurosRestantes);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("Histórico:"));
     
        historicoArea = new JTextArea(8, 20);
        historicoArea.setEditable(false);
        JScrollPane scrollHist = new JScrollPane(historicoArea);
        infoPanel.add(scrollHist);
        add(infoPanel, BorderLayout.EAST);

        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        // Ações dos botões
        btnNovoAmbiente.addActionListener(this::novoAmbiente);
        btnPasso.addActionListener(this::passo);
        btnReiniciar.addActionListener(this::reiniciarAgente);

        pack();
        setLocationRelativeTo(null);

        // Iniciar com um ambiente padrão
        novoAmbiente(null);
    }

    private JPanel painelTabuleiro;

    private void novoAmbiente(ActionEvent e) {
        String[] opcoes = {"Automático", "Manual"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Como deseja gerar o ambiente?",
                "Gerar Ambiente",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[0]);
        GeradorAmbiente gerador = new GeradorAmbiente();

        if (escolha == 0) { // Automático
            String input = JOptionPane.showInputDialog(this, "Digite o tamanho n (n>3):", "5");
            if (input == null) return;
            try {
                n = Integer.parseInt(input);
                if (n <= 3) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido. Usando n=5.");
                n = 5;
            }
            ambiente = gerador.gerarAutomatico(n);
        } else if (escolha == 1) { // Manual
            ambiente = gerador.gerarPorUsuario();
            n = ambiente.getN();
        } else {
            return;
        }

        // Criar botões do tabuleiro
        painelTabuleiro.removeAll();
        painelTabuleiro.setLayout(new GridLayout(n, n));
        botoes = new JButton[n][n];
        for (int i = n-1; i >= 0; i--) {
            for (int j = 0; j < n; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(60, 60));
                btn.setBackground(Color.LIGHT_GRAY);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                final int linha = i;
                final int coluna = j;
                btn.addActionListener(ev -> mostrarInfoCelula(linha, coluna));
                botoes[i][j] = btn;
                painelTabuleiro.add(btn);
            }
        }
        painelTabuleiro.revalidate();
        painelTabuleiro.repaint();

       // Reiniciar pontuação e contadores
        pontuacao = 0;
        contadorPassos = 0;
        historicoArea.setText("");
        reiniciarAgente(null);
    }

    private void reiniciarAgente(ActionEvent e) {
        if (ambiente == null) return;
        int tipo = comboTipoAgente.getSelectedIndex();
        switch (tipo) {
            case 0:
                agente = new AgenteV1(ambiente);
                break;
            case 1:
                agente = new AgenteV2(ambiente);
                break;
            case 2:
                logArea.setText("Executando Algoritmo Genético (parâmetros fixos)... Aguarde.\n");
                SwingWorker<Cromossomo, Void> worker = new SwingWorker<Cromossomo, Void>() {
                    @Override
                    protected Cromossomo doInBackground() throws Exception {
                        int popSize = 50;
                        int geracoes = 1000;
                        double taxaCrossover = 0.85;
                        double taxaMutacao = 0.05;
                        AlgoritmoGenetico ag = new AlgoritmoGenetico(ambiente, popSize, taxaCrossover, taxaMutacao, geracoes);
                        return ag.executar();
                    }
                    @Override
                    protected void done() {
                        try {
                            Cromossomo melhor = get();
                            agente = new AgenteV3(ambiente, melhor);
                            atualizarTabuleiro();
                            logArea.append("AG concluído. Melhor fitness: " + melhor.getFitness() + "\n");
                            logArea.append("Tamanho do mundo: " + ambiente.getN() + "x" + ambiente.getN() + "\n");
                            logArea.append("Agente V3 pronto. Clique em Passo.\n");
                        } catch (Exception ex) {
                            logArea.append("Erro no AG: " + ex.getMessage() + "\n");
                        }
                    }
                };
                worker.execute();
                return;
            }    

        atualizarTabuleiro();

        pontuacao = 0;
        ouroPego = false;
        wumpusVivoAnterior = true;
        contadorPassos = 0;
        historicoArea.setText("");
        atualizarInfo();

        logArea.setText("Agente reiniciado.\n");

    }



    private void passo(ActionEvent e) {
        if (agente == null || ambiente == null) return;
        if (!agente.isAtivo()) {
            logArea.append("O agente já terminou. Reinicie ou gere novo ambiente.\n");
            return;
        }

        // Guarda estado anterior
        boolean tinhaOuro = agente.getTemOuro();
        boolean wumpusVivoAntes = agente.getWumpusVivo();

        boolean continua = agente.passo();

        // Atualiza pontuação
        pontuacao -= 1; // cada passo custa 1

        if (agente.getTemOuro() && !tinhaOuro) {
            pontuacao += 1000;
        }
        if (!agente.getWumpusVivo() && wumpusVivoAntes) {
            pontuacao += 1000;
        }
        if (!continua && !(agente.getTemOuro() && agente.getLinha() == 0 && agente.getColuna() == 0)) {
            pontuacao -= 1000; // morreu
        }

        atualizarTabuleiro();
        atualizarInfo();

        String msg = agente.getMensagem();
        logArea.append(msg + "\n");
        historicoArea.append("Passo " + (++contadorPassos) + ": " + msg + "\n");
        historicoArea.setCaretPosition(historicoArea.getDocument().getLength());

        if (!continua) {
            logArea.append("Fim de jogo.\n");
        }
    }



    private void atualizarInfo() {
        if (agente != null) {
            lblPontuacao.setText("Pontuação: " + pontuacao);
            lblFlechas.setText("Flechas: " + (agente.getFlecha() ? "1" : "0"));
            lblOuro.setText("Ouro: " + (agente.getTemOuro() ? "Sim" : "Não"));
            lblWumpus.setText("Wumpus: " + (agente.getWumpusVivo() ? "Vivo" : "Morto"));
            lblPassos.setText("Passos: " + agente.getPassos());
        }
    }

    private void atualizarTabuleiro() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                JButton btn = botoes[i][j];
                Celula cel = ambiente.getCelula(i, j);

                if (i == agente.getLinha() && j == agente.getColuna()) {
                    btn.setIcon(carregarImagem("logo.png"));
                    btn.setText(null);
                } else if (cel.temWumpus()) {
                    btn.setIcon(carregarImagem("unnamed.png"));
                    btn.setText(null);
                } else if (cel.temPoco()) {
                    btn.setIcon(carregarImagem("hole.png"));
                    btn.setText(null);
                } else if (cel.temOuro()) {
                    btn.setIcon(carregarImagem("gold-icon.png"));
                    btn.setText(null);
                } else {
                    btn.setIcon(null);
                    btn.setText("");
                }

                if (cel.foiVisitada()) {
                    btn.setBackground(new Color(200, 230, 200));
                } else {
                    btn.setBackground(Color.LIGHT_GRAY);
                }

                int borderThickness = 0;
                Color borderColor = null;
                if (cel.temFedor()) {
                    borderThickness = 3;
                    borderColor = Color.RED;
                } else if (cel.temBrisa()) {
                    borderThickness = 3;
                    borderColor = Color.BLUE;
                } else if (cel.temBrilho()) {
                    borderThickness = 3;
                    borderColor = Color.YELLOW;
                }

                if (borderThickness > 0) {
                    btn.setBorder(BorderFactory.createLineBorder(borderColor, borderThickness));
                } else {
                    btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                }
            }
        }
    }

    private ImageIcon carregarImagem(String nome) {
        String caminho = "/com/mycompany/mundowumpus/pacote/" + nome;
        URL url = getClass().getResource(caminho);

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("Imagem não encontrada: " + caminho);
            return null;
        }
    }

    private void mostrarInfoCelula(int linha, int coluna) {
        Celula cel = ambiente.getCelula(linha, coluna);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Célula (%d,%d):\n", linha, coluna));
        if (cel.temWumpus()) sb.append("- Wumpus\n");
        if (cel.temPoco()) sb.append("- Poço\n");
        if (cel.temOuro()) sb.append("- Ouro\n");
        if (cel.temFedor()) sb.append("- Fedor\n");
        if (cel.temBrisa()) sb.append("- Brisa\n");
        if (cel.temBrilho()) sb.append("- Brilho\n");
        if (cel.foiVisitada()) sb.append("- Visitada\n");
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MundoWumpus().setVisible(true);
        });
    }
    
 }