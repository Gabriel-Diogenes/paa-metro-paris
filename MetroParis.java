/*
 * Pontifícia Universidade Católica de Minas Gerais
 * ICEI - Ciência da Computação
 * Projeto e Análise de Algoritmos — Prof. Alexei Machado
 *
 * Alex de Castro Mendes Marques
 * Gabriel Alves da Silva Diógenes
 * Rafael Mortimer Colares
 * Rodrigo Reis do Vale
 *
 * Matrículas: 692997, 771703, 789896, 813340
 */

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MetroParis {

    static final int[][] CONJUNTOS_DE_LINHAS_POR_RESTO = {
            {1, 2, 4, 6, 7, 9, 10, 11, 12, 14},
            {1, 2, 3, 4, 6, 9, 11, 12, 13, 14},
            {1, 2, 4, 5, 6, 7, 8, 9, 11, 14},
            {1, 2, 3, 4, 5, 6, 8, 10, 12, 13},
            {1, 2, 4, 5, 6, 7, 9, 10, 12, 13}
    };
    
    static final int[] NUMEROS_DE_MATRICULA = {
            692997,
            771703,
            789896,
            813340
    };

    static int restoDaSomaDasMatriculas(int[] matriculas) {
        long soma = 0;
        for (int matricula : matriculas) {
            if (matricula > 0) {
                soma += matricula;
            }
        }
        return (int) (soma % 5);
    }

    static int numeroDoGrupo() {
        return restoDaSomaDasMatriculas(NUMEROS_DE_MATRICULA);
    }

    static long somaDasMatriculas() {
        long soma = 0;
        for (int matricula : NUMEROS_DE_MATRICULA) {
            if (matricula > 0) {
                soma += matricula;
            }
        }
        return soma;
    }

    static int[] linhasSorteadasParaOGrupo() {
        return CONJUNTOS_DE_LINHAS_POR_RESTO[numeroDoGrupo()];
    }

    static String matriculasSeparadasPorVirgula() {
        StringBuilder sb = new StringBuilder();
        for (int matricula : NUMEROS_DE_MATRICULA) {
            if (matricula <= 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(matricula);
        }
        return sb.toString();
    }

    static String descricaoDoCalculoDoGrupo() {
        int n = numeroDoGrupo();
        StringBuilder linhas = new StringBuilder();
        for (int i = 0; i < linhasSorteadasParaOGrupo().length; i++) {
            if (i > 0) {
                linhas.append(", ");
            }
            linhas.append(linhasSorteadasParaOGrupo()[i]);
        }
        return "Matrículas: " + matriculasSeparadasPorVirgula()
                + "\nSoma = " + somaDasMatriculas()
                + "\nN = " + somaDasMatriculas() + " mod 5 = " + n
                + "\nLinhas: " + linhas;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorado) {
        }
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }

    static final class ErroDeLeituraDoArquivo extends Exception {
        ErroDeLeituraDoArquivo(String mensagem) {
            super(mensagem);
        }
    }

    static final class ContagemDeNosExplorados {
        static final long MASCARA_QUE_VERIFICA_O_CANCELAMENTO_A_CADA_16384_NOS = 0x3fffL;

        long quantidadeDeNosExplorados;
        boolean calculoFoiInterrompido;

        void registrarNoExplorado() {
            quantidadeDeNosExplorados++;
            if ((quantidadeDeNosExplorados & MASCARA_QUE_VERIFICA_O_CANCELAMENTO_A_CADA_16384_NOS) == 0
                    && Thread.currentThread().isInterrupted()) {
                calculoFoiInterrompido = true;
            }
        }
    }

    static final class ResultadoDaBusca {
        final String metodo;
        final int quantidadeEncontrada;
        final int[] vertices;
        final int[] cores;
        final long duracaoEmNanossegundos;
        final long quantidadeDeNosExplorados;
        final boolean buscaTerminouPorCompleto;

        ResultadoDaBusca(String metodo, int quantidadeEncontrada, int[] vertices, int[] cores,
                  long duracaoEmNanossegundos, long quantidadeDeNosExplorados, boolean buscaTerminouPorCompleto) {
            this.metodo = metodo;
            this.quantidadeEncontrada = quantidadeEncontrada;
            this.vertices = vertices == null ? new int[0] : vertices;
            this.cores = cores;
            this.duracaoEmNanossegundos = duracaoEmNanossegundos;
            this.quantidadeDeNosExplorados = quantidadeDeNosExplorados;
            this.buscaTerminouPorCompleto = buscaTerminouPorCompleto;
        }

        String tempoFormatadoEmMilissegundos() {
            return String.format(Locale.US, "%.3f ms", duracaoEmNanossegundos / 1_000_000.0);
        }
    }

    static final class Grafo {
        final int n;
        final int[][] vizinhos;
        final boolean[][] existeLigacaoDireta;
        final String[] nomes;

        Grafo(int n, int[][] vizinhos, boolean[][] existeLigacaoDireta, String[] nomes) {
            this.n = n;
            this.vizinhos = vizinhos;
            this.existeLigacaoDireta = existeLigacaoDireta;
            this.nomes = nomes;
        }

        String nomeDaEstacao(int i) {
            if (nomes == null) {
                return Integer.toString(i);
            }
            return nomes[i];
        }

        int quantidadeDeArestas() {
            int s = 0;
            for (int i = 0; i < n; i++) {
                s += vizinhos[i].length;
            }
            return s / 2;
        }

        @SuppressWarnings("unchecked")
        static Grafo montarAPartirDasArestas(int n, List<int[]> arestas, String[] nomes) {
            boolean[][] matriz = new boolean[n][n];
            List<Integer>[] listasDeVizinhosAntesDeOrdenar = new ArrayList[n];
            for (int i = 0; i < n; i++) {
                listasDeVizinhosAntesDeOrdenar[i] = new ArrayList<>();
            }
            for (int[] e : arestas) {
                int u = e[0];
                int v = e[1];
                if (u == v || u < 0 || v < 0 || u >= n || v >= n || matriz[u][v]) {
                    continue;
                }
                matriz[u][v] = matriz[v][u] = true;
                listasDeVizinhosAntesDeOrdenar[u].add(v);
                listasDeVizinhosAntesDeOrdenar[v].add(u);
            }
            int[][] vizinhos = new int[n][];
            for (int i = 0; i < n; i++) {
                Collections.sort(listasDeVizinhosAntesDeOrdenar[i]);
                vizinhos[i] = new int[listasDeVizinhosAntesDeOrdenar[i].size()];
                for (int j = 0; j < listasDeVizinhosAntesDeOrdenar[i].size(); j++) {
                    vizinhos[i][j] = listasDeVizinhosAntesDeOrdenar[i].get(j);
                }
            }
            return new Grafo(n, vizinhos, matriz, nomes);
        }
    }

    static final class LinhaDoCatalogo {
        final int numero;
        final String cor;
        final String[][] trechos;

        LinhaDoCatalogo(int numero, String cor, String[][] trechos) {
            this.numero = numero;
            this.cor = cor;
            this.trechos = trechos;
        }
    }

    static final Map<String, int[]> COORDENADAS_DAS_ESTACOES = new LinkedHashMap<>();
    static final List<LinhaDoCatalogo> CATALOGO_DE_LINHAS = new ArrayList<>();

    static void registrarEstacaoNoCatalogo(String nome, int x, int y) {
        COORDENADAS_DAS_ESTACOES.put(nome, new int[]{x, y});
    }

    static String[][] trechosEmSequencia(String... s) {
        String[][] t = new String[s.length - 1][2];
        for (int i = 0; i < s.length - 1; i++) {
            t[i][0] = s[i];
            t[i][1] = s[i + 1];
        }
        return t;
    }

    static String[][] juntarTrechos(String[][]... partes) {
        int n = 0;
        for (String[][] p : partes) {
            n += p.length;
        }
        String[][] t = new String[n][2];
        int k = 0;
        for (String[][] p : partes) {
            for (String[] e : p) {
                t[k][0] = e[0];
                t[k][1] = e[1];
                k++;
            }
        }
        return t;
    }

    static {
        registrarEstacaoNoCatalogo("La Défense", 40, 340);
        registrarEstacaoNoCatalogo("Charles de Gaulle-Étoile", 190, 330);
        registrarEstacaoNoCatalogo("Franklin D. Roosevelt", 270, 400);
        registrarEstacaoNoCatalogo("Champs-Élysées-Clemenceau", 300, 460);
        registrarEstacaoNoCatalogo("Concorde", 360, 500);
        registrarEstacaoNoCatalogo("Palais-Royal", 450, 510);
        registrarEstacaoNoCatalogo("Châtelet", 540, 520);
        registrarEstacaoNoCatalogo("Hôtel de Ville", 620, 480);
        registrarEstacaoNoCatalogo("Bastille", 730, 510);
        registrarEstacaoNoCatalogo("Gare de Lyon", 830, 545);
        registrarEstacaoNoCatalogo("Reuilly-Diderot", 930, 500);
        registrarEstacaoNoCatalogo("Nation", 1040, 455);
        registrarEstacaoNoCatalogo("Château de Vincennes", 1180, 480);

        registrarEstacaoNoCatalogo("Porte Dauphine", 100, 270);
        registrarEstacaoNoCatalogo("Villiers", 310, 230);
        registrarEstacaoNoCatalogo("Place de Clichy", 380, 175);
        registrarEstacaoNoCatalogo("Pigalle", 470, 145);
        registrarEstacaoNoCatalogo("Barbès-Rochechouart", 555, 115);
        registrarEstacaoNoCatalogo("Stalingrad", 650, 135);
        registrarEstacaoNoCatalogo("Jaurès", 745, 125);
        registrarEstacaoNoCatalogo("Belleville", 840, 185);
        registrarEstacaoNoCatalogo("Père Lachaise", 950, 230);

        registrarEstacaoNoCatalogo("Pont de Levallois", 60, 185);
        registrarEstacaoNoCatalogo("Saint-Lazare", 420, 285);
        registrarEstacaoNoCatalogo("Havre-Caumartin", 490, 315);
        registrarEstacaoNoCatalogo("Opéra", 530, 385);
        registrarEstacaoNoCatalogo("Réaumur-Sébastopol", 590, 410);
        registrarEstacaoNoCatalogo("Arts et Métiers", 680, 345);
        registrarEstacaoNoCatalogo("République", 780, 315);
        registrarEstacaoNoCatalogo("Gallieni", 1100, 190);

        registrarEstacaoNoCatalogo("Porte de Clignancourt", 520, 25);
        registrarEstacaoNoCatalogo("Marcadet-Poissonniers", 510, 75);
        registrarEstacaoNoCatalogo("Gare du Nord", 610, 195);
        registrarEstacaoNoCatalogo("Gare de l'Est", 700, 205);
        registrarEstacaoNoCatalogo("Strasbourg-Saint-Denis", 690, 270);
        registrarEstacaoNoCatalogo("Odéon", 500, 600);
        registrarEstacaoNoCatalogo("Montparnasse-Bienvenüe", 410, 690);
        registrarEstacaoNoCatalogo("Raspail", 480, 735);
        registrarEstacaoNoCatalogo("Denfert-Rochereau", 530, 800);
        registrarEstacaoNoCatalogo("Bagneux", 490, 920);

        registrarEstacaoNoCatalogo("Bobigny-Pablo Picasso", 860, 35);
        registrarEstacaoNoCatalogo("Oberkampf", 860, 375);
        registrarEstacaoNoCatalogo("Gare d'Austerlitz", 740, 640);
        registrarEstacaoNoCatalogo("Place d'Italie", 650, 745);

        registrarEstacaoNoCatalogo("Trocadéro", 160, 445);
        registrarEstacaoNoCatalogo("La Motte-Picquet-Grenelle", 175, 590);
        registrarEstacaoNoCatalogo("Pasteur", 300, 735);
        registrarEstacaoNoCatalogo("Bercy", 880, 640);
        registrarEstacaoNoCatalogo("Daumesnil", 970, 590);

        registrarEstacaoNoCatalogo("La Courneuve", 720, 30);
        registrarEstacaoNoCatalogo("Chaussée d'Antin", 575, 300);
        registrarEstacaoNoCatalogo("Pyramides", 470, 445);
        registrarEstacaoNoCatalogo("Jussieu", 630, 610);
        registrarEstacaoNoCatalogo("Maison Blanche", 690, 845);
        registrarEstacaoNoCatalogo("Mairie d'Ivry", 820, 930);
        registrarEstacaoNoCatalogo("Villejuif-Louis Aragon", 580, 935);

        registrarEstacaoNoCatalogo("Balard", 70, 680);
        registrarEstacaoNoCatalogo("Invalides", 290, 570);
        registrarEstacaoNoCatalogo("Madeleine", 420, 415);
        registrarEstacaoNoCatalogo("Richelieu-Drouot", 610, 335);
        registrarEstacaoNoCatalogo("Pointe du Lac", 1120, 690);

        registrarEstacaoNoCatalogo("Pont de Sèvres", 25, 530);
        registrarEstacaoNoCatalogo("Michel-Ange-Molitor", 70, 560);
        registrarEstacaoNoCatalogo("Michel-Ange-Auteuil", 95, 470);
        registrarEstacaoNoCatalogo("Miromesnil", 345, 310);
        registrarEstacaoNoCatalogo("Mairie de Montreuil", 1190, 340);

        registrarEstacaoNoCatalogo("Boulogne-Pont de Saint-Cloud", 15, 430);
        registrarEstacaoNoCatalogo("Duroc", 340, 640);
        registrarEstacaoNoCatalogo("Sèvres-Babylone", 400, 590);

        registrarEstacaoNoCatalogo("Mairie des Lilas", 960, 110);

        registrarEstacaoNoCatalogo("Mairie d'Aubervilliers", 400, 15);
        registrarEstacaoNoCatalogo("Mairie d'Issy", 165, 830);

        registrarEstacaoNoCatalogo("Les Courtilles", 110, 60);
        registrarEstacaoNoCatalogo("Porte de Clichy", 210, 145);
        registrarEstacaoNoCatalogo("La Fourche", 295, 155);
        registrarEstacaoNoCatalogo("Saint-Denis-Université", 185, 25);
        registrarEstacaoNoCatalogo("Mairie de Saint-Ouen", 255, 70);
        registrarEstacaoNoCatalogo("Châtillon-Montrouge", 310, 880);

        registrarEstacaoNoCatalogo("Olympiades", 930, 730);

        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(1, "amarelo", trechosEmSequencia(
                "La Défense", "Charles de Gaulle-Étoile", "Franklin D. Roosevelt",
                "Champs-Élysées-Clemenceau", "Concorde", "Palais-Royal", "Châtelet",
                "Hôtel de Ville", "Bastille", "Gare de Lyon", "Reuilly-Diderot",
                "Nation", "Château de Vincennes")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(2, "azul", trechosEmSequencia(
                "Porte Dauphine", "Charles de Gaulle-Étoile", "Villiers",
                "Place de Clichy", "Pigalle", "Barbès-Rochechouart", "Stalingrad",
                "Jaurès", "Belleville", "Père Lachaise", "Nation")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(3, "oliva", trechosEmSequencia(
                "Pont de Levallois", "Villiers", "Saint-Lazare", "Havre-Caumartin",
                "Opéra", "Réaumur-Sébastopol", "Arts et Métiers", "République",
                "Père Lachaise", "Gallieni")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(4, "magenta", trechosEmSequencia(
                "Porte de Clignancourt", "Marcadet-Poissonniers", "Barbès-Rochechouart",
                "Gare du Nord", "Gare de l'Est", "Strasbourg-Saint-Denis",
                "Réaumur-Sébastopol", "Châtelet", "Odéon", "Montparnasse-Bienvenüe",
                "Raspail", "Denfert-Rochereau", "Bagneux")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(5, "laranja", trechosEmSequencia(
                "Bobigny-Pablo Picasso", "Jaurès", "Stalingrad", "Gare du Nord",
                "Gare de l'Est", "Oberkampf", "Bastille", "Gare d'Austerlitz",
                "Place d'Italie")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(6, "verde", trechosEmSequencia(
                "Charles de Gaulle-Étoile", "Trocadéro", "La Motte-Picquet-Grenelle",
                "Pasteur", "Montparnasse-Bienvenüe", "Raspail", "Denfert-Rochereau",
                "Place d'Italie", "Bercy", "Daumesnil", "Nation")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(7, "rosa", juntarTrechos(
                trechosEmSequencia("La Courneuve", "Stalingrad", "Gare de l'Est", "Chaussée d'Antin",
                        "Opéra", "Pyramides", "Palais-Royal", "Châtelet", "Jussieu",
                        "Place d'Italie", "Maison Blanche"),
                trechosEmSequencia("Maison Blanche", "Mairie d'Ivry"),
                trechosEmSequencia("Maison Blanche", "Villejuif-Louis Aragon"))));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(8, "lilas", trechosEmSequencia(
                "Balard", "La Motte-Picquet-Grenelle", "Invalides", "Concorde",
                "Madeleine", "Opéra", "Richelieu-Drouot", "Strasbourg-Saint-Denis",
                "République", "Bastille", "Reuilly-Diderot", "Daumesnil", "Pointe du Lac")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(9, "lima", trechosEmSequencia(
                "Pont de Sèvres", "Michel-Ange-Molitor", "Michel-Ange-Auteuil",
                "Trocadéro", "Franklin D. Roosevelt", "Miromesnil", "Havre-Caumartin",
                "Chaussée d'Antin", "Richelieu-Drouot", "Strasbourg-Saint-Denis",
                "République", "Oberkampf", "Nation", "Mairie de Montreuil")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(10, "ocre", trechosEmSequencia(
                "Boulogne-Pont de Saint-Cloud", "Michel-Ange-Molitor",
                "Michel-Ange-Auteuil", "La Motte-Picquet-Grenelle", "Duroc",
                "Sèvres-Babylone", "Odéon", "Jussieu", "Gare d'Austerlitz")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(11, "marrom", trechosEmSequencia(
                "Châtelet", "Hôtel de Ville", "Arts et Métiers", "République",
                "Belleville", "Mairie des Lilas")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(12, "verde-escuro", trechosEmSequencia(
                "Mairie d'Aubervilliers", "Marcadet-Poissonniers", "Pigalle",
                "Saint-Lazare", "Madeleine", "Concorde", "Sèvres-Babylone",
                "Montparnasse-Bienvenüe", "Pasteur", "Mairie d'Issy")));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(13, "ciano", juntarTrechos(
                trechosEmSequencia("Les Courtilles", "Porte de Clichy", "La Fourche"),
                trechosEmSequencia("Saint-Denis-Université", "Mairie de Saint-Ouen", "La Fourche"),
                trechosEmSequencia("La Fourche", "Place de Clichy", "Saint-Lazare", "Miromesnil",
                        "Champs-Élysées-Clemenceau", "Invalides", "Duroc",
                        "Montparnasse-Bienvenüe", "Châtillon-Montrouge"))));
        CATALOGO_DE_LINHAS.add(new LinhaDoCatalogo(14, "roxo", trechosEmSequencia(
                "Mairie de Saint-Ouen", "Porte de Clichy", "Saint-Lazare", "Madeleine",
                "Pyramides", "Châtelet", "Gare de Lyon", "Bercy", "Olympiades")));
    }

    static void garantirCoordenadaDeTodaEstacao() {
        for (LinhaDoCatalogo linha : CATALOGO_DE_LINHAS) {
            for (String[] e : linha.trechos) {
                if (!COORDENADAS_DAS_ESTACOES.containsKey(e[0]) || !COORDENADAS_DAS_ESTACOES.containsKey(e[1])) {
                    throw new IllegalStateException("Estação sem coordenada na linha "
                            + linha.numero + ": " + e[0] + " / " + e[1]);
                }
            }
        }
    }

    static final class TrechoColorido {
        final int linha;
        final String cor;
        final int a;
        final int b;

        TrechoColorido(int linha, String cor, int a, int b) {
            this.linha = linha;
            this.cor = cor;
            this.a = a;
            this.b = b;
        }
    }

    static final class MapaDoMetro {
        final String[] nomes;
        final int[][] coords;
        final List<TrechoColorido> trechos;
        final Grafo grafo;
        final int[] linhas;

        MapaDoMetro(String[] nomes, int[][] coords, List<TrechoColorido> trechos, Grafo grafo, int[] linhas) {
            this.nomes = nomes;
            this.coords = coords;
            this.trechos = trechos;
            this.grafo = grafo;
            this.linhas = linhas;
        }

        String descricaoResumidaDoMapa() {
            return nomes.length + " estações, " + grafo.quantidadeDeArestas()
                    + " ligações, " + linhas.length + " linhas";
        }
    }

    static MapaDoMetro montarMapaDasLinhas(int[] numeros) {
        garantirCoordenadaDeTodaEstacao();
        Set<Integer> queremos = new HashSet<>();
        for (int n : numeros) {
            queremos.add(n);
        }
        List<LinhaDoCatalogo> usadas = new ArrayList<>();
        for (LinhaDoCatalogo linha : CATALOGO_DE_LINHAS) {
            if (queremos.contains(linha.numero)) {
                usadas.add(linha);
            }
        }
        if (usadas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma linha selecionada.");
        }

        Map<String, Integer> aparicoes = new HashMap<>();
        Set<String> especial = new HashSet<>();
        for (LinhaDoCatalogo linha : usadas) {
            Map<String, Integer> grau = new HashMap<>();
            for (String[] e : linha.trechos) {
                grau.merge(e[0], 1, Integer::sum);
                grau.merge(e[1], 1, Integer::sum);
            }
            for (Map.Entry<String, Integer> en : grau.entrySet()) {
                aparicoes.merge(en.getKey(), 1, Integer::sum);
                if (en.getValue() != 2) {
                    especial.add(en.getKey());
                }
            }
        }

        Set<String> manter = new HashSet<>();
        for (String nome : aparicoes.keySet()) {
            if (aparicoes.get(nome) >= 2 || especial.contains(nome)) {
                manter.add(nome);
            }
        }

        LinkedHashMap<String, Integer> indice = new LinkedHashMap<>();
        for (String nome : COORDENADAS_DAS_ESTACOES.keySet()) {
            if (manter.contains(nome)) {
                indice.put(nome, indice.size());
            }
        }

        List<TrechoColorido> trechos = new ArrayList<>();
        List<int[]> arestas = new ArrayList<>();
        Set<Long> arestasSimples = new HashSet<>();

        for (LinhaDoCatalogo linha : usadas) {
            Map<String, List<String>> vizinhos = new HashMap<>();
            for (String[] e : linha.trechos) {
                vizinhos.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e[1]);
                vizinhos.computeIfAbsent(e[1], k -> new ArrayList<>()).add(e[0]);
            }
            for (String nome : vizinhos.keySet()) {
                if (!manter.contains(nome) && vizinhos.get(nome).size() != 2) {
                    throw new IllegalStateException("Estação '" + nome
                            + "' da linha " + linha.numero + " tem grau "
                            + vizinhos.get(nome).size() + " e não foi mantida.");
                }
            }
            Set<String> vistos = new HashSet<>();
            for (String origem : vizinhos.keySet()) {
                if (!manter.contains(origem)) {
                    continue;
                }
                for (String vizinho : vizinhos.get(origem)) {
                    String anterior = origem;
                    String atual = vizinho;
                    int guarda = 0;
                    while (!manter.contains(atual)) {
                        List<String> estacoesVizinhasNaLinha = vizinhos.get(atual);
                        if (estacoesVizinhasNaLinha == null || estacoesVizinhasNaLinha.size() != 2) {
                            throw new IllegalStateException("Falha ao contrair " + atual);
                        }
                        String proximo = estacoesVizinhasNaLinha.get(0).equals(anterior) ? estacoesVizinhasNaLinha.get(1) : estacoesVizinhasNaLinha.get(0);
                        anterior = atual;
                        atual = proximo;
                        if (++guarda > 80) {
                            throw new IllegalStateException("Ciclo ao contrair a linha " + linha.numero);
                        }
                    }
                    if (atual.equals(origem)) {
                        continue;
                    }
                    String a = origem.compareTo(atual) < 0 ? origem : atual;
                    String b = origem.compareTo(atual) < 0 ? atual : origem;
                    if (!vistos.add(linha.numero + "|" + a + "|" + b)) {
                        continue;
                    }
                    int ia = indice.get(origem);
                    int ib = indice.get(atual);
                    trechos.add(new TrechoColorido(linha.numero, linha.cor, ia, ib));
                    long chave = ia < ib ? (((long) ia) << 32) | (ib & 0xffffffffL)
                            : (((long) ib) << 32) | (ia & 0xffffffffL);
                    if (arestasSimples.add(chave)) {
                        arestas.add(new int[]{ia, ib});
                    }
                }
            }
        }

        String[] nomes = indice.keySet().toArray(new String[0]);
        int[][] coords = new int[nomes.length][2];
        for (int i = 0; i < nomes.length; i++) {
            coords[i] = COORDENADAS_DAS_ESTACOES.get(nomes[i]);
        }
        int[] linhas = new int[usadas.size()];
        for (int i = 0; i < usadas.size(); i++) {
            linhas[i] = usadas.get(i).numero;
        }
        return new MapaDoMetro(nomes, coords, trechos, Grafo.montarAPartirDasArestas(nomes.length, arestas, nomes), linhas);
    }

    static void gravarMapaNosArquivos(MapaDoMetro rede, Path arquivoEstacoes, Path arquivoLinhas) throws IOException {
        if (arquivoEstacoes.getParent() != null) {
            Files.createDirectories(arquivoEstacoes.getParent());
        }
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(arquivoEstacoes), StandardCharsets.UTF_8))) {
            for (int i = 0; i < rede.nomes.length; i++) {
                w.write(rede.nomes[i] + " " + rede.coords[i][0] + " " + rede.coords[i][1]);
                w.write('\n');
            }
        }
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(arquivoLinhas), StandardCharsets.UTF_8))) {
            for (int numero : rede.linhas) {
                String cor = null;
                List<TrechoColorido> daLinha = new ArrayList<>();
                for (TrechoColorido t : rede.trechos) {
                    if (t.linha == numero) {
                        daLinha.add(t);
                        cor = t.cor;
                    }
                }
                if (cor == null) {
                    for (LinhaDoCatalogo def : CATALOGO_DE_LINHAS) {
                        if (def.numero == numero) {
                            cor = def.cor;
                        }
                    }
                }
                w.write("Linha" + numero + " " + cor + " " + daLinha.size());
                w.write('\n');
                for (TrechoColorido t : daLinha) {
                    w.write(rede.nomes[t.a] + " " + rede.nomes[t.b]);
                    w.write('\n');
                }
            }
        }
    }

    static MapaDoMetro lerMapaDosArquivos(Path arquivoEstacoes, Path arquivoLinhas) throws IOException, ErroDeLeituraDoArquivo {
        List<String> nomes = new ArrayList<>();
        List<int[]> coords = new ArrayList<>();
        Map<String, Integer> indice = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Files.newInputStream(arquivoEstacoes), StandardCharsets.UTF_8))) {
            String linha;
            int num = 0;
            while ((linha = br.readLine()) != null) {
                num++;
                String t = linha.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                int sp2 = t.lastIndexOf(' ');
                int sp1 = sp2 > 0 ? t.lastIndexOf(' ', sp2 - 1) : -1;
                if (sp1 <= 0) {
                    throw new ErroDeLeituraDoArquivo("Linha " + num + " de estações inválida: " + t);
                }
                String nome = t.substring(0, sp1).trim();
                int x;
                int y;
                try {
                    x = Integer.parseInt(t.substring(sp1, sp2).trim());
                    y = Integer.parseInt(t.substring(sp2 + 1).trim());
                } catch (NumberFormatException e) {
                    throw new ErroDeLeituraDoArquivo("Coordenadas inválidas na linha " + num + ": " + t);
                }
                if (nome.isEmpty() || indice.containsKey(nome)) {
                    throw new ErroDeLeituraDoArquivo("Estação repetida ou vazia: " + nome);
                }
                indice.put(nome, nomes.size());
                nomes.add(nome);
                coords.add(new int[]{x, y});
            }
        }
        if (nomes.isEmpty()) {
            throw new ErroDeLeituraDoArquivo("O arquivo de estações está vazio.");
        }

        List<TrechoColorido> trechos = new ArrayList<>();
        List<int[]> arestas = new ArrayList<>();
        Set<Long> simples = new HashSet<>();
        List<Integer> linhas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Files.newInputStream(arquivoLinhas), StandardCharsets.UTF_8))) {
            String linha;
            int num = 0;
            while ((linha = br.readLine()) != null) {
                num++;
                String t = linha.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                String[] cab = t.split("\\s+");
                if (cab.length != 3 || !cab[0].matches("Linha\\d+")) {
                    throw new ErroDeLeituraDoArquivo("Cabeçalho inválido na linha " + num + ": " + t);
                }
                int numero = Integer.parseInt(cab[0].substring(5));
                String cor = cab[1];
                int qtd;
                try {
                    qtd = Integer.parseInt(cab[2]);
                } catch (NumberFormatException e) {
                    throw new ErroDeLeituraDoArquivo("Quantidade de trechos inválida: " + t);
                }
                if (qtd < 0) {
                    throw new ErroDeLeituraDoArquivo("Quantidade negativa de trechos na linha " + numero);
                }
                linhas.add(numero);
                for (int k = 0; k < qtd; k++) {
                    String seg = br.readLine();
                    num++;
                    if (seg == null) {
                        throw new ErroDeLeituraDoArquivo("Fim de arquivo antes dos " + qtd
                                + " trechos da linha " + numero);
                    }
                    seg = seg.trim();
                    String[] par = separarNomesDasDuasEstacoes(seg, indice);
                    if (par == null) {
                        throw new ErroDeLeituraDoArquivo("Trecho não reconhecido (linha " + num + "): " + seg);
                    }
                    int ia = indice.get(par[0]);
                    int ib = indice.get(par[1]);
                    if (ia == ib) {
                        throw new ErroDeLeituraDoArquivo("Trecho liga a estação a ela mesma: " + par[0]);
                    }
                    trechos.add(new TrechoColorido(numero, cor, ia, ib));
                    long chave = ia < ib ? (((long) ia) << 32) | (ib & 0xffffffffL)
                            : (((long) ib) << 32) | (ia & 0xffffffffL);
                    if (simples.add(chave)) {
                        arestas.add(new int[]{ia, ib});
                    }
                }
            }
        }
        String[] arrNomes = nomes.toArray(new String[0]);
        int[][] arrCoords = coords.toArray(new int[0][]);
        int[] arrLinhas = new int[linhas.size()];
        for (int i = 0; i < linhas.size(); i++) {
            arrLinhas[i] = linhas.get(i);
        }
        return new MapaDoMetro(arrNomes, arrCoords, trechos, Grafo.montarAPartirDasArestas(arrNomes.length, arestas, arrNomes), arrLinhas);
    }

    static String[] separarNomesDasDuasEstacoes(String texto, Map<String, Integer> indice) {
        String melhorA = null;
        String melhorB = null;
        for (String nome : indice.keySet()) {
            if (texto.startsWith(nome + " ")) {
                String resto = texto.substring(nome.length() + 1);
                if (indice.containsKey(resto) && (melhorA == null || nome.length() > melhorA.length())) {
                    melhorA = nome;
                    melhorB = resto;
                }
            }
        }
        if (melhorA == null) {
            return null;
        }
        return new String[]{melhorA, melhorB};
    }

    static void gravarArquivosDeEntradaDoGrupo(Path pasta) throws IOException {
        Files.createDirectories(pasta);
        int[][] conjuntos = {linhasSorteadasParaOGrupo(), {1, 4, 14}, {1, 2, 4, 6, 12, 14}};
        String[] nomes = {"", "3linhas_", "6linhas_"};
        for (int i = 0; i < conjuntos.length; i++) {
            MapaDoMetro rede = montarMapaDasLinhas(conjuntos[i]);
            gravarMapaNosArquivos(rede, pasta.resolve(nomes[i] + "estacoes.txt"),
                    pasta.resolve(nomes[i] + "linhas.txt"));
        }
    }

    static final class Algoritmos {

        static boolean oCicloSimplesEhValido(Grafo g, int origem, int[] cicloEncontrado) {
            if (cicloEncontrado == null || cicloEncontrado.length == 0) {
                return true;
            }
            if (cicloEncontrado.length < 3 || cicloEncontrado[0] != origem) {
                return false;
            }
            boolean[] jaVisitada = new boolean[g.n];
            for (int i = 0; i < cicloEncontrado.length; i++) {
                int v = cicloEncontrado[i];
                if (v < 0 || v >= g.n || jaVisitada[v]) {
                    return false;
                }
                jaVisitada[v] = true;
                if (i > 0 && !g.existeLigacaoDireta[cicloEncontrado[i - 1]][v]) {
                    return false;
                }
            }
            return g.existeLigacaoDireta[cicloEncontrado[cicloEncontrado.length - 1]][origem];
        }

        static boolean oConjuntoDominaTodasAsEstacoes(Grafo g, int[] conjunto) {
            boolean[] coberto = new boolean[g.n];
            for (int v : conjunto) {
                if (v < 0 || v >= g.n) {
                    return false;
                }
                coberto[v] = true;
                for (int w : g.vizinhos[v]) {
                    coberto[w] = true;
                }
            }
            for (boolean b : coberto) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }

        static int maiorCorUtilizada(int[] cores) {
            int m = 0;
            if (cores == null) {
                return 0;
            }
            for (int c : cores) {
                m = Math.max(m, c);
            }
            return m;
        }

        static final class BlocoBiconexo {
            final Grafo grafoDoBloco;
            final int[] indiceNoGrafoCompleto;

            BlocoBiconexo(Grafo grafoDoBloco, int[] indiceNoGrafoCompleto) {
                this.grafoDoBloco = grafoDoBloco;
                this.indiceNoGrafoCompleto = indiceNoGrafoCompleto;
            }

            int indiceNoBloco(int verticeOriginal) {
                for (int i = 0; i < indiceNoGrafoCompleto.length; i++) {
                    if (indiceNoGrafoCompleto[i] == verticeOriginal) {
                        return i;
                    }
                }
                return -1;
            }
        }

        static List<BlocoBiconexo> blocosBiconexosDaOrigem(Grafo g, int origem) {
            List<BlocoBiconexo> saida = new ArrayList<>();
            if (g.n == 0 || origem < 0 || origem >= g.n) {
                return saida;
            }
            int[] tempoDeDescoberta = new int[g.n];
            int[] menorAncestralAlcancavel = new int[g.n];
            int[] paiNaBusca = new int[g.n];
            Arrays.fill(paiNaBusca, -1);
            int m = Math.max(1, g.quantidadeDeArestas() + 2);
            int[][] pilha = new int[m][2];
            int[] topo = new int[1];
            int[] tempo = new int[1];
            List<List<int[]>> arestasDeCadaBloco = new ArrayList<>();
            buscarComponentesBiconexas(g, origem, tempoDeDescoberta, menorAncestralAlcancavel, paiNaBusca, tempo, pilha, topo, arestasDeCadaBloco);
            for (List<int[]> arestas : arestasDeCadaBloco) {
                BlocoBiconexo blocoBiconexo = montarBlocoAPartirDasArestas(g, arestas);
                if (blocoBiconexo.indiceNoBloco(origem) >= 0) {
                    saida.add(blocoBiconexo);
                }
            }
            return saida;
        }

        static void buscarComponentesBiconexas(Grafo g, int u, int[] tempoDeDescoberta, int[] menorAncestralAlcancavel, int[] paiNaBusca, int[] tempo,
                             int[][] pilha, int[] topo, List<List<int[]>> blocos) {
            tempoDeDescoberta[u] = menorAncestralAlcancavel[u] = ++tempo[0];
            for (int v : g.vizinhos[u]) {
                if (tempoDeDescoberta[v] == 0) {
                    paiNaBusca[v] = u;
                    pilha[topo[0]][0] = u;
                    pilha[topo[0]][1] = v;
                    topo[0]++;
                    buscarComponentesBiconexas(g, v, tempoDeDescoberta, menorAncestralAlcancavel, paiNaBusca, tempo, pilha, topo, blocos);
                    menorAncestralAlcancavel[u] = Math.min(menorAncestralAlcancavel[u], menorAncestralAlcancavel[v]);
                    if (menorAncestralAlcancavel[v] >= tempoDeDescoberta[u]) {
                        List<int[]> bloco = new ArrayList<>();
                        int a;
                        int b;
                        do {
                            topo[0]--;
                            a = pilha[topo[0]][0];
                            b = pilha[topo[0]][1];
                            bloco.add(new int[]{a, b});
                        } while (a != u || b != v);
                        blocos.add(bloco);
                    }
                } else if (v != paiNaBusca[u] && tempoDeDescoberta[v] < tempoDeDescoberta[u]) {
                    pilha[topo[0]][0] = u;
                    pilha[topo[0]][1] = v;
                    topo[0]++;
                    menorAncestralAlcancavel[u] = Math.min(menorAncestralAlcancavel[u], tempoDeDescoberta[v]);
                }
            }
        }

        static BlocoBiconexo montarBlocoAPartirDasArestas(Grafo g, List<int[]> arestas) {
            LinkedHashMap<Integer, Integer> mapa = new LinkedHashMap<>();
            for (int[] e : arestas) {
                mapa.computeIfAbsent(e[0], k -> mapa.size());
                mapa.computeIfAbsent(e[1], k -> mapa.size());
            }
            int n = mapa.size();
            int[] indiceNoGrafoCompleto = new int[n];
            for (Map.Entry<Integer, Integer> en : mapa.entrySet()) {
                indiceNoGrafoCompleto[en.getValue()] = en.getKey();
            }
            List<int[]> conv = new ArrayList<>();
            for (int[] e : arestas) {
                conv.add(new int[]{mapa.get(e[0]), mapa.get(e[1])});
            }
            String[] nomes = null;
            if (g.nomes != null) {
                nomes = new String[n];
                for (int i = 0; i < n; i++) {
                    nomes[i] = g.nomes[indiceNoGrafoCompleto[i]];
                }
            }
            return new BlocoBiconexo(Grafo.montarAPartirDasArestas(n, conv, nomes), indiceNoGrafoCompleto);
        }

        static ResultadoDaBusca calcularMaiorCicloSimples(Grafo g, int origem, boolean aplicarPodaDeBranchAndBound) {
            long inicioDaMedicaoEmNanossegundos = System.nanoTime();
            ContagemDeNosExplorados c = new ContagemDeNosExplorados();
            if (origem < 0 || origem >= g.n) {
                throw new IllegalArgumentException("Estação de origem inválida.");
            }
            int melhor = 0;
            int[] melhorCaminhoGuardado = new int[0];
            for (BlocoBiconexo blocoBiconexo : blocosBiconexosDaOrigem(g, origem)) {
                if (blocoBiconexo.grafoDoBloco.n < 3 || blocoBiconexo.indiceNoBloco(origem) < 0) {
                    continue;
                }
                int origemNoBloco = blocoBiconexo.indiceNoBloco(origem);
                int[] cicloEncontrado = buscarMaiorCicloNoBloco(blocoBiconexo.grafoDoBloco, origemNoBloco, aplicarPodaDeBranchAndBound, c);
                if (cicloEncontrado.length > melhor) {
                    melhor = cicloEncontrado.length;
                    melhorCaminhoGuardado = new int[cicloEncontrado.length];
                    for (int i = 0; i < cicloEncontrado.length; i++) {
                        melhorCaminhoGuardado[i] = blocoBiconexo.indiceNoGrafoCompleto[cicloEncontrado[i]];
                    }
                }
                if (c.calculoFoiInterrompido) {
                    break;
                }
            }
            return new ResultadoDaBusca(aplicarPodaDeBranchAndBound ? "Branch-and-bound" : "Força bruta",
                    melhor, melhorCaminhoGuardado, null, System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, !c.calculoFoiInterrompido);
        }

        static int[] buscarMaiorCicloNoBloco(Grafo g, int origem, boolean aplicarPodaDeBranchAndBound, ContagemDeNosExplorados c) {
            int[] melhorCaminhoGuardado = new int[0];
            int melhor = 0;
            if (aplicarPodaDeBranchAndBound) {
                int[] heuristica = estenderCaminhoGulosoAteFecharOCiclo(g, origem);
                if (heuristica.length > melhor) {
                    melhor = heuristica.length;
                    melhorCaminhoGuardado = heuristica;
                }
            }
            if (melhor == g.n) {
                return melhorCaminhoGuardado;
            }
            boolean[] jaVisitada = new boolean[g.n];
            int[] caminho = new int[g.n];
            jaVisitada[origem] = true;
            caminho[0] = origem;
            int[] melhorComprimentoEncontrado = new int[]{melhor};
            int[][] melhorCaminhoEncontrado = new int[][]{melhorCaminhoGuardado};
            explorarCaminhosSimplesDoPasse(g, origem, origem, 1, jaVisitada, caminho, melhorComprimentoEncontrado, melhorCaminhoEncontrado, aplicarPodaDeBranchAndBound, c);
            return melhorCaminhoEncontrado[0];
        }

        static void explorarCaminhosSimplesDoPasse(Grafo g, int origem, int atual, int len, boolean[] jaVisitada, int[] caminho,
                             int[] melhor, int[][] melhorCaminhoGuardado, boolean aplicarPodaDeBranchAndBound, ContagemDeNosExplorados c) {
            c.registrarNoExplorado();
            if (c.calculoFoiInterrompido) {
                return;
            }
            if (len >= 3 && g.existeLigacaoDireta[atual][origem] && len > melhor[0]) {
                melhor[0] = len;
                melhorCaminhoGuardado[0] = Arrays.copyOf(caminho, len);
            }
            if (melhor[0] == g.n) {
                return;
            }
            if (aplicarPodaDeBranchAndBound && limiteSuperiorDeEstacoesAindaAlcancaveis(g, atual, origem, len, jaVisitada) <= melhor[0]) {
                return;
            }
            int[] vizinhos = g.vizinhos[atual];
            if (aplicarPodaDeBranchAndBound && vizinhos.length > 1) {
                Integer[] ordem = new Integer[vizinhos.length];
                for (int i = 0; i < vizinhos.length; i++) {
                    ordem[i] = vizinhos[i];
                }
                Arrays.sort(ordem, (a, b) -> {
                    int ca = quantidadeDeVizinhosAindaNaoVisitados(g, a, jaVisitada);
                    int cb = quantidadeDeVizinhosAindaNaoVisitados(g, b, jaVisitada);
                    if (ca != cb) {
                        return Integer.compare(cb, ca);
                    }
                    return Integer.compare(a, b);
                });
                for (int v : ordem) {
                    if (v == origem || jaVisitada[v]) {
                        continue;
                    }
                    jaVisitada[v] = true;
                    caminho[len] = v;
                    explorarCaminhosSimplesDoPasse(g, origem, v, len + 1, jaVisitada, caminho, melhor, melhorCaminhoGuardado, true, c);
                    jaVisitada[v] = false;
                    if (c.calculoFoiInterrompido || melhor[0] == g.n) {
                        return;
                    }
                }
            } else {
                for (int v : vizinhos) {
                    if (v == origem || jaVisitada[v]) {
                        continue;
                    }
                    jaVisitada[v] = true;
                    caminho[len] = v;
                    explorarCaminhosSimplesDoPasse(g, origem, v, len + 1, jaVisitada, caminho, melhor, melhorCaminhoGuardado, aplicarPodaDeBranchAndBound, c);
                    jaVisitada[v] = false;
                    if (c.calculoFoiInterrompido || melhor[0] == g.n) {
                        return;
                    }
                }
            }
        }

        static int quantidadeDeVizinhosAindaNaoVisitados(Grafo g, int v, boolean[] jaVisitada) {
            int q = 0;
            for (int w : g.vizinhos[v]) {
                if (!jaVisitada[w]) {
                    q++;
                }
            }
            return q;
        }

        static int limiteSuperiorDeEstacoesAindaAlcancaveis(Grafo g, int atual, int origem, int len, boolean[] jaVisitada) {
            boolean[] visto = new boolean[g.n];
            int[] fila = new int[g.n];
            int ini = 0;
            int fim = 0;
            fila[fim++] = atual;
            visto[atual] = true;
            List<Integer> portais = new ArrayList<>();
            while (ini < fim) {
                int u = fila[ini++];
                if (u != atual && g.existeLigacaoDireta[u][origem]) {
                    portais.add(u);
                }
                for (int v : g.vizinhos[u]) {
                    if (v == origem || jaVisitada[v] || visto[v]) {
                        continue;
                    }
                    visto[v] = true;
                    fila[fim++] = v;
                }
            }
            boolean fechaAgora = len >= 3 && g.existeLigacaoDireta[atual][origem];
            if (portais.isEmpty()) {
                return fechaAgora ? len : 0;
            }
            boolean[] alcanca = new boolean[g.n];
            ini = 0;
            fim = 0;
            for (int p : portais) {
                if (!alcanca[p]) {
                    alcanca[p] = true;
                    fila[fim++] = p;
                }
            }
            while (ini < fim) {
                int u = fila[ini++];
                for (int v : g.vizinhos[u]) {
                    if (!visto[v] || alcanca[v] || jaVisitada[v]) {
                        continue;
                    }
                    alcanca[v] = true;
                    fila[fim++] = v;
                }
            }
            int extra = 0;
            for (int v = 0; v < g.n; v++) {
                if (alcanca[v] && v != atual) {
                    extra++;
                }
            }
            return len + extra;
        }

        static ResultadoDaBusca calcularCicloPelaHeuristicaGulosa(Grafo g, int origem) {
            long inicioDaMedicaoEmNanossegundos = System.nanoTime();
            int[] melhor = new int[0];
            for (BlocoBiconexo blocoBiconexo : blocosBiconexosDaOrigem(g, origem)) {
                int origemNoBloco = blocoBiconexo.indiceNoBloco(origem);
                if (origemNoBloco < 0 || blocoBiconexo.grafoDoBloco.n < 3) {
                    continue;
                }
                int[] cicloEncontrado = estenderCaminhoGulosoAteFecharOCiclo(blocoBiconexo.grafoDoBloco, origemNoBloco);
                if (cicloEncontrado.length > melhor.length) {
                    melhor = new int[cicloEncontrado.length];
                    for (int i = 0; i < cicloEncontrado.length; i++) {
                        melhor[i] = blocoBiconexo.indiceNoGrafoCompleto[cicloEncontrado[i]];
                    }
                }
            }
            return new ResultadoDaBusca("Heurística gulosa", melhor.length, melhor, null,
                    System.nanoTime() - inicioDaMedicaoEmNanossegundos, 0, true);
        }

        static int[] estenderCaminhoGulosoAteFecharOCiclo(Grafo g, int origem) {
            int[] melhor = new int[0];
            for (int primeiro : g.vizinhos[origem]) {
                boolean[] jaVisitada = new boolean[g.n];
                jaVisitada[origem] = true;
                jaVisitada[primeiro] = true;
                int[] caminho = new int[g.n];
                caminho[0] = origem;
                caminho[1] = primeiro;
                int len = 2;
                int atual = primeiro;
                while (true) {
                    if (len >= 3 && g.existeLigacaoDireta[atual][origem] && len > melhor.length) {
                        melhor = Arrays.copyOf(caminho, len);
                    }
                    int escolhido = -1;
                    int melhorScore = -1;
                    for (int v : g.vizinhos[atual]) {
                        if (v == origem || jaVisitada[v]) {
                            continue;
                        }
                        if (!oCaminhoAindaPodeVoltarAOrigem(g, origem, v, jaVisitada)) {
                            continue;
                        }
                        int score = quantidadeDeEstacoesAindaAlcancaveis(g, origem, v, jaVisitada);
                        if (score > melhorScore || (score == melhorScore && (escolhido < 0 || v < escolhido))) {
                            melhorScore = score;
                            escolhido = v;
                        }
                    }
                    if (escolhido < 0) {
                        break;
                    }
                    jaVisitada[escolhido] = true;
                    caminho[len++] = escolhido;
                    atual = escolhido;
                }
            }
            return melhor;
        }

        static boolean oCaminhoAindaPodeVoltarAOrigem(Grafo g, int origem, int partida, boolean[] jaVisitada) {
            if (g.existeLigacaoDireta[partida][origem]) {
                return true;
            }
            boolean[] visto = new boolean[g.n];
            int[] fila = new int[g.n];
            int ini = 0;
            int fim = 0;
            fila[fim++] = partida;
            visto[partida] = true;
            while (ini < fim) {
                int u = fila[ini++];
                for (int w : g.vizinhos[u]) {
                    if (w == origem) {
                        return true;
                    }
                    if (jaVisitada[w] || visto[w]) {
                        continue;
                    }
                    visto[w] = true;
                    fila[fim++] = w;
                }
            }
            return false;
        }

        static int quantidadeDeEstacoesAindaAlcancaveis(Grafo g, int origem, int partida, boolean[] jaVisitada) {
            boolean[] visto = new boolean[g.n];
            int[] fila = new int[g.n];
            int ini = 0;
            int fim = 0;
            fila[fim++] = partida;
            visto[partida] = true;
            int q = 1;
            while (ini < fim) {
                int u = fila[ini++];
                for (int w : g.vizinhos[u]) {
                    if (w == origem || jaVisitada[w] || visto[w]) {
                        continue;
                    }
                    visto[w] = true;
                    fila[fim++] = w;
                    q++;
                }
            }
            return q;
        }

        static final int ESTACAO_AINDA_NAO_DECIDIDA = 0;
        static final int ESTACAO_ESCOLHIDA_COMO_GUICHE = 1;
        static final int ESTACAO_DESCARTADA_COMO_GUICHE = 2;

        static ResultadoDaBusca calcularMenorConjuntoDominante(Grafo g, boolean aplicarPodaDeBranchAndBound) {
            long inicioDaMedicaoEmNanossegundos = System.nanoTime();
            ContagemDeNosExplorados c = new ContagemDeNosExplorados();
            if (g.n == 0) {
                return new ResultadoDaBusca(aplicarPodaDeBranchAndBound ? "Branch-and-bound" : "Força bruta",
                        0, new int[0], null, System.nanoTime() - inicioDaMedicaoEmNanossegundos, 0, true);
            }
            if (!aplicarPodaDeBranchAndBound) {
                for (int k = 0; k <= g.n && !c.calculoFoiInterrompido; k++) {
                    int[] escolhidos = new int[k];
                    int[] achou = new int[]{-1};
                    enumerarCombinacoesPorTamanho(g, 0, k, 0, escolhidos, c, achou);
                    if (achou[0] == 1) {
                        return new ResultadoDaBusca("Força bruta", k, Arrays.copyOf(escolhidos, k),
                                null, System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, true);
                    }
                }
                return new ResultadoDaBusca("Força bruta", g.n, indicesDeTodasAsEstacoes(g.n), null,
                        System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, !c.calculoFoiInterrompido);
            }

            int[] guloso = menorConjuntoEntreAsPartidasGulosas(g);
            int[] tamanhoMelhor = new int[]{guloso.length};
            int[] status = new int[g.n];
            boolean[] dominado = new boolean[g.n];
            int[][] guarda = new int[][]{guloso};
            ramificarQualEstacaoCobreADescoberta(g, status, dominado, 0, tamanhoMelhor, guarda, c);
            return new ResultadoDaBusca("Branch-and-bound", guarda[0].length, guarda[0], null,
                    System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, !c.calculoFoiInterrompido);
        }

        static int[] indicesDeTodasAsEstacoes(int n) {
            int[] v = new int[n];
            for (int i = 0; i < n; i++) {
                v[i] = i;
            }
            return v;
        }

        static void enumerarCombinacoesPorTamanho(Grafo g, int inicio, int falta, int pos, int[] escolhidos,
                             ContagemDeNosExplorados c, int[] achou) {
            if (achou[0] == 1 || c.calculoFoiInterrompido) {
                return;
            }
            c.registrarNoExplorado();
            if (falta == 0) {
                if (oConjuntoDominaTodasAsEstacoes(g, escolhidos)) {
                    achou[0] = 1;
                }
                return;
            }
            for (int i = inicio; i <= g.n - falta; i++) {
                escolhidos[pos] = i;
                enumerarCombinacoesPorTamanho(g, i + 1, falta - 1, pos + 1, escolhidos, c, achou);
                if (achou[0] == 1 || c.calculoFoiInterrompido) {
                    return;
                }
            }
        }

        static void ramificarQualEstacaoCobreADescoberta(Grafo g, int[] status, boolean[] dominado, int tomados,
                                   int[] melhor, int[][] guarda, ContagemDeNosExplorados c) {
            c.registrarNoExplorado();
            if (c.calculoFoiInterrompido) {
                return;
            }
            if (todasAsEstacoesJaForamDominadas(dominado)) {
                if (tomados < melhor[0]) {
                    melhor[0] = tomados;
                    guarda[0] = indicesDasEstacoesEscolhidas(status);
                }
                return;
            }
            if (tomados >= melhor[0]) {
                return;
            }
            if (tomados + quantidadeMinimaDeGuichesPeloAfastamento(g, dominado) >= melhor[0]) {
                return;
            }

            int u = -1;
            int menor = Integer.MAX_VALUE;
            for (int v = 0; v < g.n; v++) {
                if (dominado[v]) {
                    continue;
                }
                int q = status[v] == ESTACAO_AINDA_NAO_DECIDIDA ? 1 : 0;
                for (int w : g.vizinhos[v]) {
                    if (status[w] == ESTACAO_AINDA_NAO_DECIDIDA) {
                        q++;
                    }
                }
                if (q < menor || (q == menor && (u < 0 || v < u))) {
                    menor = q;
                    u = v;
                }
            }
            if (u < 0 || menor == 0) {
                return;
            }

            int[] cands = new int[menor];
            int t = 0;
            if (status[u] == ESTACAO_AINDA_NAO_DECIDIDA) {
                cands[t++] = u;
            }
            for (int w : g.vizinhos[u]) {
                if (status[w] == ESTACAO_AINDA_NAO_DECIDIDA) {
                    cands[t++] = w;
                }
            }
            ordenarCandidatosPelaQuantidadeQueAindaCobrem(g, cands, t, dominado);

            int[] fora = new int[t];
            int nFora = 0;
            for (int i = 0; i < t && !c.calculoFoiInterrompido; i++) {
                int w = cands[i];
                boolean[] mudou = marcarEstacaoESuaVizinhancaComoDominadas(g, w, dominado);
                status[w] = ESTACAO_ESCOLHIDA_COMO_GUICHE;
                ramificarQualEstacaoCobreADescoberta(g, status, dominado, tomados + 1, melhor, guarda, c);
                status[w] = ESTACAO_AINDA_NAO_DECIDIDA;
                desfazerDominioMarcado(dominado, mudou);
                status[w] = ESTACAO_DESCARTADA_COMO_GUICHE;
                fora[nFora++] = w;
            }
            for (int i = 0; i < nFora; i++) {
                status[fora[i]] = ESTACAO_AINDA_NAO_DECIDIDA;
            }
        }

        static int quantidadeMinimaDeGuichesPeloAfastamento(Grafo g, boolean[] dominado) {
            int n = g.n;
            boolean[] bloqueado = new boolean[n];
            boolean[] visto = new boolean[n];
            int[] fila = new int[n];
            int[] prof = new int[n];
            int lb = 0;
            for (int origem = 0; origem < n; origem++) {
                if (dominado[origem] || bloqueado[origem]) {
                    continue;
                }
                lb++;
                Arrays.fill(visto, false);
                int ini = 0;
                int fim = 0;
                fila[fim++] = origem;
                visto[origem] = true;
                prof[origem] = 0;
                bloqueado[origem] = true;
                while (ini < fim) {
                    int v = fila[ini++];
                    if (prof[v] == 2) {
                        continue;
                    }
                    for (int w : g.vizinhos[v]) {
                        if (visto[w]) {
                            continue;
                        }
                        visto[w] = true;
                        prof[w] = prof[v] + 1;
                        bloqueado[w] = true;
                        fila[fim++] = w;
                    }
                }
            }
            return lb;
        }

        static void ordenarCandidatosPelaQuantidadeQueAindaCobrem(Grafo g, int[] cands, int t, boolean[] dominado) {
            Integer[] box = new Integer[t];
            for (int i = 0; i < t; i++) {
                box[i] = cands[i];
            }
            Arrays.sort(box, (a, b) -> {
                int cmp = Integer.compare(quantidadeDeEstacoesAindaDescobertas(g, b, dominado), quantidadeDeEstacoesAindaDescobertas(g, a, dominado));
                return cmp != 0 ? cmp : Integer.compare(a, b);
            });
            for (int i = 0; i < t; i++) {
                cands[i] = box[i];
            }
        }

        static int quantidadeDeEstacoesAindaDescobertas(Grafo g, int v, boolean[] dominado) {
            int q = dominado[v] ? 0 : 1;
            for (int w : g.vizinhos[v]) {
                if (!dominado[w]) {
                    q++;
                }
            }
            return q;
        }

        static int[] indicesDasEstacoesEscolhidas(int[] status) {
            int q = 0;
            for (int s : status) {
                if (s == ESTACAO_ESCOLHIDA_COMO_GUICHE) {
                    q++;
                }
            }
            int[] r = new int[q];
            int k = 0;
            for (int i = 0; i < status.length; i++) {
                if (status[i] == ESTACAO_ESCOLHIDA_COMO_GUICHE) {
                    r[k++] = i;
                }
            }
            return r;
        }

        static boolean todasAsEstacoesJaForamDominadas(boolean[] dominado) {
            for (boolean b : dominado) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }

        
        static boolean[] marcarEstacaoESuaVizinhancaComoDominadas(Grafo g, int v, boolean[] dominado) {
            boolean[] mudou = new boolean[g.n];
            if (!dominado[v]) {
                dominado[v] = true;
                mudou[v] = true;
            }
            for (int w : g.vizinhos[v]) {
                if (!dominado[w]) {
                    dominado[w] = true;
                    mudou[w] = true;
                }
            }
            return mudou;
        }

        static void desfazerDominioMarcado(boolean[] dominado, boolean[] mudou) {
            for (int i = 0; i < dominado.length; i++) {
                if (mudou[i]) {
                    dominado[i] = false;
                }
            }
        }

        
        static ResultadoDaBusca calcularConjuntoDominantePelaHeuristica(Grafo g) {
            long inicioDaMedicaoEmNanossegundos = System.nanoTime();
            int[] conjunto = menorConjuntoEntreAsPartidasGulosas(g);
            return new ResultadoDaBusca("Heurística gulosa", conjunto.length, conjunto, null,
                    System.nanoTime() - inicioDaMedicaoEmNanossegundos, 0, true);
        }

        static int[] menorConjuntoEntreAsPartidasGulosas(Grafo g) {
            int[] melhor = montarConjuntoGulosoComecandoPor(g, -1);
            for (int s = 0; s < g.n; s++) {
                int[] cand = montarConjuntoGulosoComecandoPor(g, s);
                if (cand.length < melhor.length) {
                    melhor = cand;
                }
            }
            return melhor;
        }

        static int[] montarConjuntoGulosoComecandoPor(Grafo g, int primeiro) {
            boolean[] dominado = new boolean[g.n];
            boolean[] noConjunto = new boolean[g.n];
            List<Integer> escolhidos = new ArrayList<>();
            int faltam = g.n;
            if (primeiro >= 0) {
                faltam = acrescentarEstacaoAoConjuntoGuloso(g, primeiro, dominado, noConjunto, escolhidos, faltam);
            }
            while (faltam > 0) {
                int melhor = -1;
                int cobre = 0;
                for (int v = 0; v < g.n; v++) {
                    if (noConjunto[v]) {
                        continue;
                    }
                    int q = dominado[v] ? 0 : 1;
                    for (int w : g.vizinhos[v]) {
                        if (!dominado[w]) {
                            q++;
                        }
                    }
                    if (q > cobre || (q == cobre && q > 0 && (melhor < 0 || v < melhor))) {
                        cobre = q;
                        melhor = v;
                    }
                }
                if (melhor < 0 || cobre == 0) {
                    break;
                }
                faltam = acrescentarEstacaoAoConjuntoGuloso(g, melhor, dominado, noConjunto, escolhidos, faltam);
            }
            int[] r = new int[escolhidos.size()];
            for (int i = 0; i < r.length; i++) {
                r[i] = escolhidos.get(i);
            }
            return r;
        }

        static int acrescentarEstacaoAoConjuntoGuloso(Grafo g, int v, boolean[] dominado, boolean[] noConjunto,
                                   List<Integer> escolhidos, int faltam) {
            escolhidos.add(v);
            noConjunto[v] = true;
            if (!dominado[v]) {
                dominado[v] = true;
                faltam--;
            }
            for (int w : g.vizinhos[v]) {
                if (!dominado[w]) {
                    dominado[w] = true;
                    faltam--;
                }
            }
            return faltam;
        }

        
        static ResultadoDaBusca calcularMenorNumeroDePeriodos(Grafo g, boolean aplicarPodaDeBranchAndBound) {
            long inicioDaMedicaoEmNanossegundos = System.nanoTime();
            ContagemDeNosExplorados c = new ContagemDeNosExplorados();
            if (g.n == 0) {
                return new ResultadoDaBusca(aplicarPodaDeBranchAndBound ? "Branch-and-bound" : "Força bruta",
                        0, null, new int[0], System.nanoTime() - inicioDaMedicaoEmNanossegundos, 0, true);
            }
            if (!aplicarPodaDeBranchAndBound) {
                for (int k = 1; k <= g.n; k++) {
                    int[] cor = new int[g.n];
                    if (colorirNaOrdemFixaDasEstacoes(g, 0, k, cor, c)) {
                        return new ResultadoDaBusca("Força bruta", k, null, cor,
                                System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, !c.calculoFoiInterrompido);
                    }
                    if (c.calculoFoiInterrompido) {
                        break;
                    }
                }
                return new ResultadoDaBusca("Força bruta", g.n, null, umPeriodoDistintoParaCadaEstacao(g.n),
                        System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, !c.calculoFoiInterrompido);
            }
            int[] heur = colorirPorWelshPowell(g);
            int melhor = maiorCorUtilizada(heur);
            int[] melhorCor = heur;
            int lb = tamanhoDaCliqueParaLimiteInferior(g);
            for (int alvo = melhor - 1; alvo >= lb && !c.calculoFoiInterrompido; alvo--) {
                int[] cor = new int[g.n];
                if (!colorirEscolhendoAEstacaoComMenosCores(g, alvo, cor, 0, c)) {
                    break;
                }
                melhor = alvo;
                melhorCor = Arrays.copyOf(cor, cor.length);
            }
            return new ResultadoDaBusca("Branch-and-bound", melhor, null, melhorCor,
                    System.nanoTime() - inicioDaMedicaoEmNanossegundos, c.quantidadeDeNosExplorados, !c.calculoFoiInterrompido);
        }

        static int[] umPeriodoDistintoParaCadaEstacao(int n) {
            int[] c = new int[n];
            for (int i = 0; i < n; i++) {
                c[i] = i + 1;
            }
            return c;
        }

        static boolean colorirNaOrdemFixaDasEstacoes(Grafo g, int v, int k, int[] cor, ContagemDeNosExplorados c) {
            c.registrarNoExplorado();
            if (c.calculoFoiInterrompido) {
                return false;
            }
            if (v == g.n) {
                return true;
            }
            boolean[] usado = new boolean[k + 1];
            for (int w : g.vizinhos[v]) {
                if (cor[w] > 0) {
                    usado[cor[w]] = true;
                }
            }
            for (int col = 1; col <= k; col++) {
                if (usado[col]) {
                    continue;
                }
                cor[v] = col;
                if (colorirNaOrdemFixaDasEstacoes(g, v + 1, k, cor, c)) {
                    return true;
                }
                cor[v] = 0;
                if (c.calculoFoiInterrompido) {
                    return false;
                }
            }
            return false;
        }

        static boolean colorirEscolhendoAEstacaoComMenosCores(Grafo g, int k, int[] cor, int coloridos, ContagemDeNosExplorados c) {
            c.registrarNoExplorado();
            if (c.calculoFoiInterrompido) {
                return false;
            }
            if (coloridos == g.n) {
                return true;
            }
            int escolhido = -1;
            int menor = k + 1;
            for (int v = 0; v < g.n; v++) {
                if (cor[v] != 0) {
                    continue;
                }
                int disp = 0;
                for (int col = 1; col <= k; col++) {
                    if (aCorNaoConflitaComOsVizinhos(g, cor, v, col)) {
                        disp++;
                    }
                }
                if (disp == 0) {
                    return false;
                }
                if (disp < menor) {
                    menor = disp;
                    escolhido = v;
                }
            }
            for (int col = 1; col <= k; col++) {
                if (!aCorNaoConflitaComOsVizinhos(g, cor, escolhido, col)) {
                    continue;
                }
                cor[escolhido] = col;
                if (colorirEscolhendoAEstacaoComMenosCores(g, k, cor, coloridos + 1, c)) {
                    return true;
                }
                cor[escolhido] = 0;
                if (c.calculoFoiInterrompido) {
                    return false;
                }
            }
            return false;
        }

        static boolean aCorNaoConflitaComOsVizinhos(Grafo g, int[] cor, int v, int col) {
            for (int w : g.vizinhos[v]) {
                if (cor[w] == col) {
                    return false;
                }
            }
            return true;
        }

        
        static ResultadoDaBusca calcularPeriodosPelaHeuristica(Grafo g) {
            long inicioDaMedicaoEmNanossegundos = System.nanoTime();
            int[] cor = colorirPorWelshPowell(g);
            return new ResultadoDaBusca("Heurística Welsh-Powell", maiorCorUtilizada(cor), null, cor,
                    System.nanoTime() - inicioDaMedicaoEmNanossegundos, 0, true);
        }

        static int[] colorirPorWelshPowell(Grafo g) {
            Integer[] ordem = new Integer[g.n];
            for (int i = 0; i < g.n; i++) {
                ordem[i] = i;
            }
            Arrays.sort(ordem, (a, b) -> {
                int d = Integer.compare(g.vizinhos[b].length, g.vizinhos[a].length);
                return d != 0 ? d : Integer.compare(a, b);
            });
            int[] cor = new int[g.n];
            for (int v : ordem) {
                boolean[] usado = new boolean[g.n + 1];
                for (int w : g.vizinhos[v]) {
                    if (cor[w] > 0) {
                        usado[cor[w]] = true;
                    }
                }
                int c = 1;
                while (usado[c]) {
                    c++;
                }
                cor[v] = c;
            }
            return cor;
        }

        
        static int tamanhoDaCliqueParaLimiteInferior(Grafo g) {
            int melhor = g.n == 0 ? 0 : 1;
            for (int origem = 0; origem < g.n; origem++) {
                int[] membros = new int[g.n];
                boolean[] na = new boolean[g.n];
                int t = 0;
                membros[t++] = origem;
                na[origem] = true;
                while (true) {
                    int escolhido = -1;
                    int grau = -1;
                    for (int v = 0; v < g.n; v++) {
                        if (na[v]) {
                            continue;
                        }
                        boolean ok = true;
                        for (int i = 0; i < t; i++) {
                            if (!g.existeLigacaoDireta[v][membros[i]]) {
                                ok = false;
                                break;
                            }
                        }
                        if (!ok) {
                            continue;
                        }
                        if (g.vizinhos[v].length > grau || (g.vizinhos[v].length == grau && (escolhido < 0 || v < escolhido))) {
                            grau = g.vizinhos[v].length;
                            escolhido = v;
                        }
                    }
                    if (escolhido < 0) {
                        break;
                    }
                    na[escolhido] = true;
                    membros[t++] = escolhido;
                }
                melhor = Math.max(melhor, t);
            }
            return melhor;
        }

        static String compararOsTresMetodosDoPasse(Grafo g, int origem) {
            executarBuscaPequenaParaAquecerAMedicao();
            ResultadoDaBusca heuristica = calcularCicloPelaHeuristicaGulosa(g, origem);
            ResultadoDaBusca branchAndBound = calcularMaiorCicloSimples(g, origem, true);
            ResultadoDaBusca forcaBruta = calcularMaiorCicloSimples(g, origem, false);
            StringBuilder sb = new StringBuilder();
            sb.append("Passe a partir de ").append(g.nomeDaEstacao(origem)).append("\n");
            sb.append(formatarLinhaDoResultadoDoPasse(forcaBruta));
            sb.append(formatarLinhaDoResultadoDoPasse(branchAndBound));
            sb.append(formatarLinhaDoResultadoDoPasse(heuristica));
            if (forcaBruta.buscaTerminouPorCompleto && branchAndBound.buscaTerminouPorCompleto) {
                sb.append(forcaBruta.quantidadeEncontrada == branchAndBound.quantidadeEncontrada
                        ? "Força bruta e branch-and-bound coincidem.\n"
                        : "DIVERGÊNCIA entre força bruta e branch-and-bound.\n");
            }
            sb.append("Nós explorados: FB ").append(forcaBruta.quantidadeDeNosExplorados).append(", BB ").append(branchAndBound.quantidadeDeNosExplorados).append(".\n");
            if (forcaBruta.buscaTerminouPorCompleto && heuristica.quantidadeEncontrada <= forcaBruta.quantidadeEncontrada && oCicloSimplesEhValido(g, origem, heuristica.vertices)) {
                sb.append("A heurística devolveu um ciclo válido");
                if (forcaBruta.quantidadeEncontrada == 0) {
                    sb.append(".\n");
                } else {
                    sb.append(String.format(Locale.US, " com %.0f%% do ótimo.\n",
                            100.0 * heuristica.quantidadeEncontrada / forcaBruta.quantidadeEncontrada));
                }
            }
            sb.append("Ciclo ótimo: ").append(descreverEstacoesEmSequencia(g, branchAndBound.vertices)).append("\n");
            return sb.toString();
        }

        static String compararOsTresMetodosDosGuiches(Grafo g) {
            executarBuscaPequenaParaAquecerAMedicao();
            ResultadoDaBusca heuristica = calcularConjuntoDominantePelaHeuristica(g);
            ResultadoDaBusca branchAndBound = calcularMenorConjuntoDominante(g, true);
            ResultadoDaBusca forcaBruta = calcularMenorConjuntoDominante(g, false);
            StringBuilder sb = new StringBuilder();
            sb.append("Guichês (").append(g.n).append(" estações)\n");
            sb.append(formatarLinhaDoResultado(forcaBruta, "guichês"));
            sb.append(formatarLinhaDoResultado(branchAndBound, "guichês"));
            sb.append(formatarLinhaDoResultado(heuristica, "guichês"));
            if (forcaBruta.buscaTerminouPorCompleto && branchAndBound.buscaTerminouPorCompleto) {
                sb.append(forcaBruta.quantidadeEncontrada == branchAndBound.quantidadeEncontrada
                        ? "Força bruta e branch-and-bound coincidem.\n"
                        : "DIVERGÊNCIA entre força bruta e branch-and-bound.\n");
            }
            sb.append("Nós explorados: FB ").append(forcaBruta.quantidadeDeNosExplorados).append(", BB ").append(branchAndBound.quantidadeDeNosExplorados).append(".\n");
            sb.append("Estações com guichê (ótimo): ").append(descreverEstacoesEmSequencia(g, branchAndBound.vertices)).append("\n");
            return sb.toString();
        }

        static String compararOsTresMetodosDaManutencao(Grafo g) {
            executarBuscaPequenaParaAquecerAMedicao();
            ResultadoDaBusca heuristica = calcularPeriodosPelaHeuristica(g);
            ResultadoDaBusca branchAndBound = calcularMenorNumeroDePeriodos(g, true);
            ResultadoDaBusca forcaBruta = calcularMenorNumeroDePeriodos(g, false);
            StringBuilder sb = new StringBuilder();
            sb.append("Manutenção (").append(g.n).append(" estações)\n");
            sb.append(formatarLinhaDoResultado(forcaBruta, "períodos"));
            sb.append(formatarLinhaDoResultado(branchAndBound, "períodos"));
            sb.append(formatarLinhaDoResultado(heuristica, "períodos"));
            if (forcaBruta.buscaTerminouPorCompleto && branchAndBound.buscaTerminouPorCompleto) {
                sb.append(forcaBruta.quantidadeEncontrada == branchAndBound.quantidadeEncontrada
                        ? "Força bruta e branch-and-bound coincidem.\n"
                        : "DIVERGÊNCIA entre força bruta e branch-and-bound.\n");
            }
            sb.append("Nós explorados: FB ").append(forcaBruta.quantidadeDeNosExplorados).append(", BB ").append(branchAndBound.quantidadeDeNosExplorados).append(".\n");
            return sb.toString();
        }

        static String formatarLinhaDoResultadoDoPasse(ResultadoDaBusca r) {
            return r.metodo + ": " + r.quantidadeEncontrada + " estações em " + r.tempoFormatadoEmMilissegundos()
                    + (r.buscaTerminouPorCompleto ? "" : " (interrompido)") + "\n";
        }

        static String formatarLinhaDoResultado(ResultadoDaBusca r, String unidade) {
            return r.metodo + ": " + r.quantidadeEncontrada + " " + unidade + " em " + r.tempoFormatadoEmMilissegundos()
                    + (r.buscaTerminouPorCompleto ? "" : " (interrompido)") + "\n";
        }

        static String descreverEstacoesEmSequencia(Grafo g, int[] vertices) {
            if (vertices == null || vertices.length == 0) {
                return "(nenhuma)";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < vertices.length; i++) {
                if (i > 0) {
                    sb.append(" → ");
                }
                sb.append(g.nomeDaEstacao(vertices[i]));
            }
            return sb.toString();
        }

        static void executarBuscaPequenaParaAquecerAMedicao() {
            Grafo c = criarGrafoEmFormaDeCiclo(5);
            calcularCicloPelaHeuristicaGulosa(c, 0);
            calcularMaiorCicloSimples(c, 0, true);
            calcularMaiorCicloSimples(c, 0, false);
            calcularConjuntoDominantePelaHeuristica(c);
            calcularMenorConjuntoDominante(c, true);
            calcularPeriodosPelaHeuristica(c);
            calcularMenorNumeroDePeriodos(c, true);
        }

        static Grafo criarGrafoEmFormaDeCiclo(int n) {
            List<int[]> e = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                e.add(new int[]{i, (i + 1) % n});
            }
            return Grafo.montarAPartirDasArestas(n, e, null);
        }
    }
    
    static final class DestaqueNoMapa {
        int[] ciclo = new int[0];
        int[] guiches = new int[0];
        int[] cores;
        int selecionada = -1;
    }

    static Color corDeDesenhoDaLinha(String nome) {
        return switch (nome.toLowerCase(Locale.ROOT)) {
            case "amarelo" -> new Color(214, 158, 0);
            case "azul" -> new Color(0, 78, 184);
            case "oliva" -> new Color(112, 108, 0);
            case "magenta" -> new Color(190, 0, 140);
            case "laranja" -> new Color(222, 96, 16);
            case "verde" -> new Color(28, 150, 86);
            case "rosa" -> new Color(204, 72, 122);
            case "lilas" -> new Color(156, 78, 176);
            case "lima" -> new Color(132, 142, 0);
            case "ocre" -> new Color(176, 112, 16);
            case "marrom" -> new Color(112, 68, 28);
            case "verde-escuro" -> new Color(0, 112, 72);
            case "ciano" -> new Color(0, 132, 164);
            case "roxo" -> new Color(92, 32, 150);
            case "vermelha", "vermelho" -> new Color(176, 24, 32);
            default -> new Color(60, 60, 60);
        };
    }

    static final Color[] CORES_DOS_PERIODOS_DE_MANUTENCAO = {
            new Color(196, 48, 48),
            new Color(36, 104, 196),
            new Color(32, 150, 84),
            new Color(214, 132, 20),
            new Color(120, 52, 176),
            new Color(20, 150, 164),
            new Color(170, 50, 110),
            new Color(90, 90, 90)
    };

    
    static final class EnquadramentoDoMapa {
        final double minX;
        final double minY;
        final double s;
        final double cx;
        final double cy;

        EnquadramentoDoMapa(double minX, double minY, double s, double cx, double cy) {
            this.minX = minX;
            this.minY = minY;
            this.s = s;
            this.cx = cx;
            this.cy = cy;
        }
    }

    static EnquadramentoDoMapa calcularEnquadramentoDoMapa(MapaDoMetro rede, int largura, int altura, double zoom, double panX, double panY) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int[] c : rede.coords) {
            minX = Math.min(minX, c[0]);
            minY = Math.min(minY, c[1]);
            maxX = Math.max(maxX, c[0]);
            maxY = Math.max(maxY, c[1]);
        }
        double esquerda = 36;
        double direita = 168;
        double vertical = 36;
        double base = Math.min((largura - esquerda - direita) / Math.max(1, maxX - minX),
                (altura - 2 * vertical) / Math.max(1, maxY - minY));
        double s = base * zoom;
        double contentW = (maxX - minX) * s;
        double contentH = (maxY - minY) * s;
        double cx = esquerda + (largura - esquerda - direita - contentW) / 2.0 + panX;
        double cy = vertical + (altura - 2 * vertical - contentH) / 2.0 + panY;
        return new EnquadramentoDoMapa(minX, minY, s, cx, cy);
    }

    static void desenharMapaNaTela(Graphics2D g, int largura, int altura, MapaDoMetro rede, DestaqueNoMapa destaque,
                         double zoom, double panX, double panY) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(250, 248, 242));
        g.fillRect(0, 0, largura, altura);
        if (rede == null || rede.nomes.length == 0) {
            g.setColor(new Color(40, 40, 40));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g.drawString("Abra os arquivos de estações e de linhas, ou gere um grupo no menu Arquivo.", 40, 80);
            return;
        }

        EnquadramentoDoMapa vista = calcularEnquadramentoDoMapa(rede, largura, altura, zoom, panX, panY);
        double minX = vista.minX;
        double minY = vista.minY;
        double s = vista.s;
        double cx = vista.cx;
        double cy = vista.cy;

        Map<Long, List<Integer>> porPar = new HashMap<>();
        for (int i = 0; i < rede.trechos.size(); i++) {
            TrechoColorido t = rede.trechos.get(i);
            long chave = t.a < t.b ? (((long) t.a) << 32) | (t.b & 0xffffffffL)
                    : (((long) t.b) << 32) | (t.a & 0xffffffffL);
            porPar.computeIfAbsent(chave, k -> new ArrayList<>()).add(i);
        }

        Set<Long> cicloArestas = new HashSet<>();
        if (destaque != null && destaque.ciclo != null && destaque.ciclo.length >= 3) {
            for (int i = 0; i < destaque.ciclo.length; i++) {
                int a = destaque.ciclo[i];
                int b = destaque.ciclo[(i + 1) % destaque.ciclo.length];
                long chave = a < b ? (((long) a) << 32) | (b & 0xffffffffL)
                        : (((long) b) << 32) | (a & 0xffffffffL);
                cicloArestas.add(chave);
            }
        }

        for (Map.Entry<Long, List<Integer>> en : porPar.entrySet()) {
            List<Integer> idxs = en.getValue();
            TrechoColorido baseT = rede.trechos.get(idxs.get(0));
            double x1 = cx + (rede.coords[baseT.a][0] - minX) * s;
            double y1 = cy + (rede.coords[baseT.a][1] - minY) * s;
            double x2 = cx + (rede.coords[baseT.b][0] - minX) * s;
            double y2 = cy + (rede.coords[baseT.b][1] - minY) * s;
            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.hypot(dx, dy);
            double nx = len < 1 ? 0 : -dy / len;
            double ny = len < 1 ? 0 : dx / len;
            for (int k = 0; k < idxs.size(); k++) {
                TrechoColorido t = rede.trechos.get(idxs.get(k));
                double off = (k - (idxs.size() - 1) / 2.0) * 5.0;
                int xa = (int) Math.round(x1 + nx * off);
                int ya = (int) Math.round(y1 + ny * off);
                int xb = (int) Math.round(x2 + nx * off);
                int yb = (int) Math.round(y2 + ny * off);
                g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 255, 255, 180));
                g.drawLine(xa, ya, xb, yb);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(corDeDesenhoDaLinha(t.cor));
                g.drawLine(xa, ya, xb, yb);
            }
        }

        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(160, 30, 30));
        for (Long chave : cicloArestas) {
            int a = (int) (chave >> 32);
            int b = (int) (chave & 0xffffffffL);
            int x1 = (int) Math.round(cx + (rede.coords[a][0] - minX) * s);
            int y1 = (int) Math.round(cy + (rede.coords[a][1] - minY) * s);
            int x2 = (int) Math.round(cx + (rede.coords[b][0] - minX) * s);
            int y2 = (int) Math.round(cy + (rede.coords[b][1] - minY) * s);
            g.drawLine(x1, y1, x2, y2);
        }

        Set<Integer> noCiclo = new HashSet<>();
        if (destaque != null && destaque.ciclo != null) {
            for (int v : destaque.ciclo) {
                noCiclo.add(v);
            }
        }
        Set<Integer> guiche = new HashSet<>();
        if (destaque != null && destaque.guiches != null) {
            for (int v : destaque.guiches) {
                guiche.add(v);
            }
        }

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        for (int i = 0; i < rede.nomes.length; i++) {
            int x = (int) Math.round(cx + (rede.coords[i][0] - minX) * s);
            int y = (int) Math.round(cy + (rede.coords[i][1] - minY) * s);
            int raio = 8;
            Color preenchimento = Color.WHITE;
            if (destaque != null && destaque.cores != null && i < destaque.cores.length && destaque.cores[i] > 0) {
                preenchimento = CORES_DOS_PERIODOS_DE_MANUTENCAO[(destaque.cores[i] - 1) % CORES_DOS_PERIODOS_DE_MANUTENCAO.length];
            } else if (guiche.contains(i)) {
                preenchimento = new Color(24, 140, 78);
            } else if (noCiclo.contains(i)) {
                preenchimento = new Color(255, 196, 48);
            }
            g.setColor(preenchimento);
            g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
            g.setColor(new Color(25, 32, 48));
            g.setStroke(new BasicStroke(1.6f));
            g.drawOval(x - raio, y - raio, raio * 2, raio * 2);
            if (destaque != null && destaque.selecionada == i) {
                g.setColor(new Color(220, 100, 0));
                g.setStroke(new BasicStroke(2.4f));
                g.drawOval(x - raio - 4, y - raio - 4, (raio + 4) * 2, (raio + 4) * 2);
            }
            String rotulo = rede.nomes[i];
            if (guiche.contains(i)) {
                rotulo = rotulo + " [G]";
            }
            if (destaque != null && destaque.cores != null && i < destaque.cores.length && destaque.cores[i] > 0) {
                rotulo = rotulo + " (" + destaque.cores[i] + ")";
            }
            int tw = g.getFontMetrics().stringWidth(rotulo);
            g.setColor(new Color(250, 248, 242, 210));
            g.fillRoundRect(x + 10, y - 12, tw + 6, 16, 4, 4);
            g.setColor(new Color(20, 24, 32));
            g.drawString(rotulo, x + 13, y);
        }
    }

    static int indiceDaEstacaoProximaAoPonto(MapaDoMetro rede, int largura, int altura, double zoom, double panX, double panY,
                         int mx, int my) {
        if (rede == null || rede.nomes.length == 0) {
            return -1;
        }
        EnquadramentoDoMapa vista = calcularEnquadramentoDoMapa(rede, largura, altura, zoom, panX, panY);
        double minX = vista.minX;
        double minY = vista.minY;
        double s = vista.s;
        double cx = vista.cx;
        double cy = vista.cy;
        int melhor = -1;
        double dist = 14 * 14;
        for (int i = 0; i < rede.nomes.length; i++) {
            double x = cx + (rede.coords[i][0] - minX) * s;
            double y = cy + (rede.coords[i][1] - minY) * s;
            double d = (x - mx) * (x - mx) + (y - my) * (y - my);
            if (d <= dist) {
                dist = d;
                melhor = i;
            }
        }
        return melhor;
    }

    static final class PainelDoMapa extends JPanel {
        MapaDoMetro rede;
        DestaqueNoMapa destaque = new DestaqueNoMapa();
        double zoom = 1;
        double panX;
        double panY;
        int pressX;
        int pressY;
        double panPressX;
        double panPressY;
        boolean arrastou;
        JanelaPrincipal janela;

        PainelDoMapa(JanelaPrincipal janela) {
            this.janela = janela;
            setBackground(new Color(250, 248, 242));
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pressX = e.getX();
                    pressY = e.getY();
                    panPressX = panX;
                    panPressY = panY;
                    arrastou = false;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    panX = panPressX + (e.getX() - pressX);
                    panY = panPressY + (e.getY() - pressY);
                    if (Math.abs(e.getX() - pressX) + Math.abs(e.getY() - pressY) > 5) {
                        arrastou = true;
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!arrastou && rede != null) {
                        int i = indiceDaEstacaoProximaAoPonto(rede, getWidth(), getHeight(), zoom, panX, panY, e.getX(), e.getY());
                        if (i >= 0) {
                            destaque.selecionada = i;
                            janela.selecionarEstacaoDoPasse(i);
                            repaint();
                        }
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double fator = e.getWheelRotation() < 0 ? 1.12 : 1 / 1.12;
                    zoom = Math.max(0.35, Math.min(6, zoom * fator));
                    repaint();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            addMouseWheelListener(mouse);
        }

        void reporZoomEPosicaoDoMapa() {
            zoom = 1;
            panX = 0;
            panY = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            desenharMapaNaTela((Graphics2D) gg, getWidth(), getHeight(), rede, destaque, zoom, panX, panY);
        }
    }

    static final class JanelaPrincipal extends JFrame {
        final PainelDoMapa mapa;
        final JTextArea log = new JTextArea(9, 40);
        final JComboBox<String> estacoes = new JComboBox<>();
        final JLabel status = new JLabel("Nenhum mapa carregado.");
        final JLabel legenda = new JLabel(" ");
        SwingWorker<?, ?> tarefa;
        MapaDoMetro rede;

        JanelaPrincipal() {
            super("Metrô de Paris — grupo N=" + numeroDoGrupo());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mapa = new PainelDoMapa(this);
            log.setEditable(false);
            log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            log.setLineWrap(true);
            log.setWrapStyleWord(true);

            JMenuBar barra = new JMenuBar();
            JMenu arquivo = new JMenu("Arquivo");
            JMenuItem doGrupo = new JMenuItem("Mapa do grupo (N=" + numeroDoGrupo() + ")");
            doGrupo.addActionListener(e -> exibirMapaDoGrupo());
            arquivo.add(doGrupo);
            JMenuItem tres = new JMenuItem("3 linhas (1, 4, 14)");
            tres.addActionListener(e -> exibirMapaGeradoParaAsLinhas(new int[]{1, 4, 14}, "3 linhas"));
            arquivo.add(tres);
            JMenuItem seis = new JMenuItem("6 linhas (1, 2, 4, 6, 12, 14)");
            seis.addActionListener(e -> exibirMapaGeradoParaAsLinhas(new int[]{1, 2, 4, 6, 12, 14}, "6 linhas"));
            arquivo.add(seis);
            arquivo.addSeparator();
            JMenuItem abrirE = new JMenuItem("Abrir estações e linhas...");
            abrirE.addActionListener(e -> abrirArquivosDeEstacoesEDeLinhas());
            arquivo.add(abrirE);
            arquivo.addSeparator();
            JMenuItem sair = new JMenuItem("Sair");
            sair.addActionListener(e -> dispose());
            arquivo.add(sair);
            barra.add(arquivo);

            JMenu passe = new JMenu("Passe");
            passe.add(itemDeMenuDoMetodoExato("Força bruta", "ciclo", false));
            passe.add(itemDeMenuDoMetodoExato("Branch-and-bound", "ciclo", true));
            JMenuItem hp = new JMenuItem("Heurística gulosa");
            hp.addActionListener(e -> iniciarHeuristica("ciclo"));
            passe.add(hp);
            JMenuItem cp = new JMenuItem("Comparar os três");
            cp.addActionListener(e -> iniciarComparacaoDosTresMetodos("ciclo"));
            passe.add(cp);
            barra.add(passe);

            JMenu guiche = new JMenu("Guichês");
            guiche.add(itemDeMenuDoMetodoExato("Força bruta", "guiche", false));
            guiche.add(itemDeMenuDoMetodoExato("Branch-and-bound", "guiche", true));
            JMenuItem hg = new JMenuItem("Heurística gulosa");
            hg.addActionListener(e -> iniciarHeuristica("guiche"));
            guiche.add(hg);
            JMenuItem cg = new JMenuItem("Comparar os três");
            cg.addActionListener(e -> iniciarComparacaoDosTresMetodos("guiche"));
            guiche.add(cg);
            barra.add(guiche);

            JMenu manut = new JMenu("Manutenção");
            manut.add(itemDeMenuDoMetodoExato("Força bruta", "cor", false));
            manut.add(itemDeMenuDoMetodoExato("Branch-and-bound", "cor", true));
            JMenuItem hc = new JMenuItem("Heurística Welsh-Powell");
            hc.addActionListener(e -> iniciarHeuristica("cor"));
            manut.add(hc);
            JMenuItem cc = new JMenuItem("Comparar os três");
            cc.addActionListener(e -> iniciarComparacaoDosTresMetodos("cor"));
            manut.add(cc);
            barra.add(manut);
            setJMenuBar(barra);

            JPanel topo = new JPanel();
            topo.setLayout(new BoxLayout(topo, BoxLayout.X_AXIS));
            topo.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            topo.add(new JLabel("Estação do passe: "));
            estacoes.setMaximumSize(new Dimension(320, 28));
            estacoes.addActionListener(e -> {
                int i = estacoes.getSelectedIndex();
                if (i >= 0 && mapa.rede != null) {
                    mapa.destaque.selecionada = i;
                    mapa.repaint();
                }
            });
            topo.add(estacoes);
            topo.add(Box.createHorizontalStrut(12));
            JButton ajustar = new JButton("Ajustar mapa");
            ajustar.addActionListener(e -> mapa.reporZoomEPosicaoDoMapa());
            topo.add(ajustar);
            JButton limpar = new JButton("Limpar destaque");
            limpar.addActionListener(e -> {
                mapa.destaque.ciclo = new int[0];
                mapa.destaque.guiches = new int[0];
                mapa.destaque.cores = null;
                mapa.repaint();
            });
            topo.add(Box.createHorizontalStrut(8));
            topo.add(limpar);
            JButton cancelar = new JButton("Cancelar cálculo");
            cancelar.addActionListener(e -> {
                if (tarefa != null) {
                    tarefa.cancel(true);
                }
            });
            topo.add(Box.createHorizontalStrut(8));
            topo.add(cancelar);
            topo.add(Box.createHorizontalGlue());
            topo.add(status);

            JPanel leste = new JPanel();
            leste.setLayout(new BoxLayout(leste, BoxLayout.Y_AXIS));
            leste.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
            leste.setBackground(Color.WHITE);
            leste.add(new JLabel("Linhas"));
            leste.add(Box.createVerticalStrut(6));
            legenda.setAlignmentX(LEFT_ALIGNMENT);
            leste.add(legenda);

            add(topo, BorderLayout.NORTH);
            add(mapa, BorderLayout.CENTER);
            add(new JScrollPane(log), BorderLayout.SOUTH);
            add(leste, BorderLayout.EAST);
            setSize(1280, 860);
            setLocationRelativeTo(null);

            try {
                Path pasta = Path.of("dados");
                if (!Files.exists(pasta.resolve("estacoes.txt"))) {
                    gravarArquivosDeEntradaDoGrupo(pasta);
                }
                exibirMapaDoGrupo();
            } catch (Exception e) {
                log.setText("Não foi possível gerar o mapa inicial: " + e.getMessage());
            }
        }

        void exibirMapaDoGrupo() {
            exibirMapaGeradoParaAsLinhas(linhasSorteadasParaOGrupo(), "Grupo N=" + numeroDoGrupo());
            log.setText(descricaoDoCalculoDoGrupo() + "\n\n" + log.getText());
        }

        JMenuItem itemDeMenuDoMetodoExato(String titulo, String problema, boolean aplicarPodaDeBranchAndBound) {
            JMenuItem item = new JMenuItem(titulo);
            item.addActionListener(e -> iniciarMetodoExato(problema, aplicarPodaDeBranchAndBound));
            return item;
        }

        void selecionarEstacaoDoPasse(int i) {
            if (i >= 0 && i < estacoes.getItemCount()) {
                estacoes.setSelectedIndex(i);
            }
        }

        void exibirMapaGeradoParaAsLinhas(int[] linhas, String titulo) {
            try {
                apresentarMapaNaJanela(montarMapaDasLinhas(linhas), titulo);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao gerar", JOptionPane.ERROR_MESSAGE);
            }
        }

        void apresentarMapaNaJanela(MapaDoMetro nova, String titulo) {
            this.rede = nova;
            mapa.rede = nova;
            mapa.destaque = new DestaqueNoMapa();
            mapa.reporZoomEPosicaoDoMapa();
            estacoes.removeAllItems();
            
            for (String nome : nova.nomes) {
                estacoes.addItem(nome);
            }
            if (nova.nomes.length > 0) {
                int central = 0;
                for (int i = 1; i < nova.grafo.n; i++) {
                    if (nova.grafo.vizinhos[i].length > nova.grafo.vizinhos[central].length) {
                        central = i;
                    }
                }
                estacoes.setSelectedIndex(central);
                mapa.destaque.selecionada = central;
            }
            status.setText(titulo + " — " + nova.descricaoResumidaDoMapa());
            StringBuilder sb = new StringBuilder("<html>");
            Set<Integer> vistas = new HashSet<>();
            for (int numero : nova.linhas) {
                if (!vistas.add(numero)) {
                    continue;
                }
                String cor = null;
                for (TrechoColorido t : nova.trechos) {
                    if (t.linha == numero) {
                        cor = t.cor;
                        break;
                    }
                }
                if (cor == null) {
                    continue;
                }
                Color c = corDeDesenhoDaLinha(cor);
                String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                sb.append("<span style='color:").append(hex).append("'>●</span> Linha ")
                        .append(numero).append(" (").append(cor).append(")<br>");
            }
            sb.append("</html>");
            legenda.setText(sb.toString());
            log.setText(titulo + "\n" + nova.descricaoResumidaDoMapa());
            mapa.repaint();
        }

        void abrirArquivosDeEstacoesEDeLinhas() {
            JFileChooser est = new JFileChooser(Path.of("dados").toFile());
            est.setDialogTitle("Arquivo de estações");
            est.setFileFilter(new FileNameExtensionFilter("Texto", "txt"));
            if (est.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            JFileChooser lin = new JFileChooser(est.getSelectedFile().getParentFile());
            lin.setDialogTitle("Arquivo de linhas");
            lin.setFileFilter(new FileNameExtensionFilter("Texto", "txt"));
            if (lin.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                MapaDoMetro lida = lerMapaDosArquivos(est.getSelectedFile().toPath(), lin.getSelectedFile().toPath());
                apresentarMapaNaJanela(lida, est.getSelectedFile().getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao ler", JOptionPane.ERROR_MESSAGE);
            }
        }

        int indiceDaEstacaoDeOrigemDoPasse() {
            int i = estacoes.getSelectedIndex();
            if (i < 0 && rede != null && rede.nomes.length > 0) {
                return 0;
            }
            return i;
        }

        boolean existeMapaENaoHaCalculoEmAndamento() {
            if (rede == null || rede.grafo.n == 0) {
                JOptionPane.showMessageDialog(this, "Carregue um mapa antes de calcular.");
                return false;
            }
            if (tarefa != null && !tarefa.isDone()) {
                JOptionPane.showMessageDialog(this, "Já existe um cálculo em andamento.");
                return false;
            }
            return true;
        }

        boolean usuarioAceitouForcaBrutaEmMapaGrande(String problema, boolean aplicarPodaDeBranchAndBound) {
            if (aplicarPodaDeBranchAndBound) {
                return true;
            }
            final int limiteDeEstacoesNoBlocoParaForcaBrutaDoPasse = 14;
            final int limiteDeEstacoesParaForcaBrutaDosGuiches = 18;
            final int limiteDeEstacoesParaForcaBrutaDaManutencao = 12;
            int limite = switch (problema) {
                case "ciclo" -> limiteDeEstacoesNoBlocoParaForcaBrutaDoPasse;
                case "guiche" -> limiteDeEstacoesParaForcaBrutaDosGuiches;
                default -> limiteDeEstacoesParaForcaBrutaDaManutencao;
            };
            int n = rede.grafo.n;
            if (problema.equals("ciclo")) {
                int bloco = 0;
                int origem = indiceDaEstacaoDeOrigemDoPasse();
                if (origem >= 0) {
                    for (Algoritmos.BlocoBiconexo blocoBiconexo : Algoritmos.blocosBiconexosDaOrigem(rede.grafo, origem)) {
                        bloco = Math.max(bloco, blocoBiconexo.grafoDoBloco.n);
                    }
                }
                n = bloco;
            }
            if (n <= limite) {
                return true;
            }
            int op = JOptionPane.showConfirmDialog(this,
                    "A força bruta neste mapa pode demorar (tamanho " + n + "). Continuar?",
                    "Força bruta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            return op == JOptionPane.OK_OPTION;
        }

        void iniciarMetodoExato(String problema, boolean aplicarPodaDeBranchAndBound) {
            if (!existeMapaENaoHaCalculoEmAndamento() || !usuarioAceitouForcaBrutaEmMapaGrande(problema, aplicarPodaDeBranchAndBound)) {
                return;
            }
            int origem = indiceDaEstacaoDeOrigemDoPasse();
            log.setText("Calculando...\n");
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            tarefa = new SwingWorker<ResultadoDaBusca, Void>() {
                @Override
                protected ResultadoDaBusca doInBackground() {
                    return switch (problema) {
                        case "ciclo" -> Algoritmos.calcularMaiorCicloSimples(rede.grafo, origem, aplicarPodaDeBranchAndBound);
                        case "guiche" -> Algoritmos.calcularMenorConjuntoDominante(rede.grafo, aplicarPodaDeBranchAndBound);
                        default -> Algoritmos.calcularMenorNumeroDePeriodos(rede.grafo, aplicarPodaDeBranchAndBound);
                    };
                }

                @Override
                protected void done() {
                    concluirBuscaEMostrarResultado(this, problema);
                }
            };
            tarefa.execute();
        }

        void iniciarHeuristica(String problema) {
            if (!existeMapaENaoHaCalculoEmAndamento()) {
                return;
            }
            int origem = indiceDaEstacaoDeOrigemDoPasse();
            ResultadoDaBusca r = switch (problema) {
                case "ciclo" -> Algoritmos.calcularCicloPelaHeuristicaGulosa(rede.grafo, origem);
                case "guiche" -> Algoritmos.calcularConjuntoDominantePelaHeuristica(rede.grafo);
                default -> Algoritmos.calcularPeriodosPelaHeuristica(rede.grafo);
            };
            preencherPainelComOResultado(problema, r);
        }

        void iniciarComparacaoDosTresMetodos(String problema) {
            if (!existeMapaENaoHaCalculoEmAndamento() || !usuarioAceitouForcaBrutaEmMapaGrande(problema, false)) {
                return;
            }
            int origem = indiceDaEstacaoDeOrigemDoPasse();
            log.setText("Comparando força bruta, branch-and-bound e heurística...\n");
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            tarefa = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return switch (problema) {
                        case "ciclo" -> Algoritmos.compararOsTresMetodosDoPasse(rede.grafo, origem);
                        case "guiche" -> Algoritmos.compararOsTresMetodosDosGuiches(rede.grafo);
                        default -> Algoritmos.compararOsTresMetodosDaManutencao(rede.grafo);
                    };
                }

                @Override
                protected void done() {
                    setCursor(java.awt.Cursor.getDefaultCursor());
                    try {
                        String texto = get();
                        log.setText(texto);
                        if (problema.equals("ciclo")) {
                            ResultadoDaBusca branchAndBound = Algoritmos.calcularMaiorCicloSimples(rede.grafo, origem, true);
                            mapa.destaque.ciclo = branchAndBound.vertices;
                            mapa.destaque.guiches = new int[0];
                            mapa.destaque.cores = null;
                        } else if (problema.equals("guiche")) {
                            ResultadoDaBusca branchAndBound = Algoritmos.calcularMenorConjuntoDominante(rede.grafo, true);
                            mapa.destaque.guiches = branchAndBound.vertices;
                            mapa.destaque.ciclo = new int[0];
                            mapa.destaque.cores = null;
                        } else {
                            ResultadoDaBusca branchAndBound = Algoritmos.calcularMenorNumeroDePeriodos(rede.grafo, true);
                            mapa.destaque.cores = branchAndBound.cores;
                            mapa.destaque.ciclo = new int[0];
                            mapa.destaque.guiches = new int[0];
                        }
                        mapa.repaint();
                    } catch (Exception e) {
                        log.setText("Falha: " + e.getMessage());
                    }
                }
            };
            tarefa.execute();
        }

        void concluirBuscaEMostrarResultado(SwingWorker<ResultadoDaBusca, Void> worker, String problema) {
            setCursor(java.awt.Cursor.getDefaultCursor());
            try {
                preencherPainelComOResultado(problema, worker.get());
            } catch (Exception e) {
                Throwable causa = e.getCause() == null ? e : e.getCause();
                log.setText("Falha: " + causa.getMessage());
            }
        }

        void preencherPainelComOResultado(String problema, ResultadoDaBusca r) {
            StringBuilder sb = new StringBuilder();
            sb.append(r.metodo).append("\n");
            if (problema.equals("ciclo")) {
                sb.append("Estações no passe: ").append(r.quantidadeEncontrada).append("\n");
                sb.append("Percurso: ").append(Algoritmos.descreverEstacoesEmSequencia(rede.grafo, r.vertices));
                if (r.vertices.length >= 3) {
                    sb.append(" → ").append(rede.grafo.nomeDaEstacao(r.vertices[0]));
                }
                sb.append("\n");
                mapa.destaque.ciclo = r.vertices;
                mapa.destaque.guiches = new int[0];
                mapa.destaque.cores = null;
            } else if (problema.equals("guiche")) {
                sb.append("Guichês: ").append(r.quantidadeEncontrada).append("\n");
                sb.append("Estações: ").append(Algoritmos.descreverEstacoesEmSequencia(rede.grafo, r.vertices)).append("\n");
                mapa.destaque.guiches = r.vertices;
                mapa.destaque.ciclo = new int[0];
                mapa.destaque.cores = null;
            } else {
                sb.append("Períodos de manutenção: ").append(r.quantidadeEncontrada).append("\n");
                mapa.destaque.cores = r.cores;
                mapa.destaque.ciclo = new int[0];
                mapa.destaque.guiches = new int[0];
                if (r.cores != null) {
                    for (int p = 1; p <= r.quantidadeEncontrada; p++) {
                        sb.append("Período ").append(p).append(": ");
                        boolean primeiro = true;
                        for (int i = 0; i < r.cores.length; i++) {
                            if (r.cores[i] == p) {
                                if (!primeiro) {
                                    sb.append(", ");
                                }
                                primeiro = false;
                                sb.append(rede.nomes[i]);
                            }
                        }
                        sb.append("\n");
                    }
                }
            }
            sb.append("Tempo: ").append(r.tempoFormatadoEmMilissegundos()).append("\n");
            sb.append("Nós explorados: ").append(r.quantidadeDeNosExplorados).append("\n");
            if (!r.buscaTerminouPorCompleto) {
                sb.append("Cálculo interrompido: o valor pode não ser o ótimo.\n");
            }
            log.setText(sb.toString());
            mapa.repaint();
        }
    }
}
