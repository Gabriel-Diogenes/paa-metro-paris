# Metrô de Paris

Trabalho prático de Projeto e Análise de Algoritmos, PUC Minas, ICEI, Ciência da Computação. Professor Alexei Machado.
## Integrantes

- Alex de Castro Mendes Marques
- Gabriel Alves da Silva Diógenes
- Rafael Mortimer Colares
- Rodrigo Reis do Vale

Matrículas: 692997, 771703, 789896 e 813340.

## O que o programa resolve

O metrô é um grafo não direcionado. Cada estação é um vértice e cada ligação direta é uma aresta. Se duas linhas passam pelo mesmo par de estações, o desenho mostra os dois traços coloridos, mas os algoritmos tratam esse par como uma única aresta.

Há três problemas. Cada um tem força bruta, branch-and-bound e uma heurística polinomial.

### Passe

Maior ciclo simples que sai de uma estação escolhida e volta a ela, sem repetir estação. O tamanho do ciclo é o número de estações visitadas. Um ciclo precisa de pelo menos três estações.

- Força bruta: percorre todos os caminhos simples a partir da origem.
- Branch-and-bound: a mesma busca, com um limite superior. Esse limite soma o caminho já percorrido às estações ainda não visitadas que conseguem voltar a um vizinho livre da origem. Se o limite não supera o melhor ciclo já achado, o ramo é cortado. A busca começa com o ciclo da heurística. Antes disso, o grafo é reduzido aos blocos biconexos da origem, porque um ciclo simples não atravessa uma articulação.
- Heurística gulosa: para cada vizinho da origem, constrói um único caminho. Em cada passo escolhe o vizinho livre que ainda permite voltar e que alcança mais estações livres. Não há retrocesso, então o ótimo não é garantido.

### Guichês

Menor conjunto de estações tal que toda estação seja um guichê ou vizinha de um guichê. O programa informa a quantidade e a lista.

- Força bruta: testa combinações por tamanho crescente e para no primeiro conjunto que domina o grafo. Esse tamanho é o mínimo.
- Branch-and-bound: escolhe uma estação ainda descoberta e decide qual vértice da vizinhança fechada vai cobri-la. O limite superior vem de um guloso com várias partidas. O limite inferior conta estações descobertas afastadas demais para compartilhar o mesmo guichê.
- Heurística gulosa: cobertura gulosa repetida, forçando cada estação como primeira escolha e ficando com o menor conjunto. O tempo é polinomial e o mínimo não é garantido.

### Manutenção

Menor número de períodos para que duas estações ligadas diretamente não fechem ao mesmo tempo. É o número cromático do grafo.

- Força bruta: tenta 1 cor, depois 2, e assim por diante, com backtracking na ordem dos índices.
- Branch-and-bound: o limite superior é a coloração de Welsh–Powell e o inferior é o tamanho de uma clique. A busca escolhe a estação com menos cores ainda possíveis e corta o ramo em que alguma estação ficou sem cor.
- Heurística Welsh–Powell: ordena as estações por grau e dá a menor cor que não aparece nos vizinhos. Não garante o mínimo.

## Grupo e linhas

O enunciado define cinco conjuntos de linhas. A equipe usa só um, dado por

```text
N = (soma das matrículas) mod 5
```

692997 + 771703 + 789896 + 813340 = 3067936

3067936 mod 5 = 1

O mapa do grupo usa as linhas 1, 2, 3, 4, 6, 9, 11, 12, 13 e 14. Esse mapa tem 48 estações e 73 ligações. Na estação de maior grau, o passe ótimo visita 32 estações, o menor número de guichês é 16 e a manutenção precisa de 3 períodos.

Os outros valores de N existem no código apenas para a fórmula. A interface abre o N=1.

| N | Linhas |
| --- | --- |
| 0 | 1, 2, 4, 6, 7, 9, 10, 11, 12, 14 |
| 1 | 1, 2, 3, 4, 6, 9, 11, 12, 13, 14 |
| 2 | 1, 2, 4, 5, 6, 7, 8, 9, 11, 14 |
| 3 | 1, 2, 3, 4, 5, 6, 8, 10, 12, 13 |
| 4 | 1, 2, 4, 5, 6, 7, 9, 10, 12, 13 |

Esse N não é o número do arquivo `Gnn.zip`. O `nn` é o número do grupo na planilha do professor.

## De onde vêm as estações

`MetroParis.java` guarda o catálogo das 14 linhas: nome, coordenada e cada trecho real, inclusive estações intermediárias.

`montarMapaDasLinhas` fica só com as linhas pedidas e mantém uma estação quando ela atende duas ou mais dessas linhas, ou quando é ponta ou bifurcação. As estações intermediárias de grau 2 somem, e o trecho passa a ligar as duas estações mantidas.

Os arquivos em `dados` são esse mapa já filtrado, no formato que o enunciado manda ler. O desenho inicial não depende deles: a janela monta o mapa a partir do catálogo. Se `dados\estacoes.txt` não existir na abertura, o programa recria os seis arquivos.

| Arquivo | Conteúdo |
| --- | --- |
| `dados\estacoes.txt` | Estações do N=1 |
| `dados\linhas.txt` | Trechos do N=1 |
| `dados\3linhas_estacoes.txt` e `dados\3linhas_linhas.txt` | Linhas 1, 4 e 14 |
| `dados\6linhas_estacoes.txt` e `dados\6linhas_linhas.txt` | Linhas 1, 2, 4, 6, 12 e 14 |

Estações, uma por linha, com o nome e as coordenadas a partir do canto superior esquerdo:

```text
Châtelet 540 520
Nation 1040 455
```

Linhas, em seções. O cabeçalho traz o número, a cor e a quantidade de trechos:

```text
Linha1 amarelo 9
Nation Gare de Lyon
Concorde Châtelet
```

O nome da estação pode ter espaços. O leitor separa as duas estações pelo maior nome conhecido que encaixa no começo da linha.

Apagar a pasta `dados`, ou só o `estacoes.txt`, faz a próxima execução gravar os seis arquivos de novo. Apagar apenas `linhas.txt` ou os arquivos de 3 e 6 linhas, deixando o `estacoes.txt`, não dispara essa gravação.

## Como executar

É preciso o JDK 17 ou mais recente. Nenhum biblioteca gráfica de fora do Java é usada. Na pasta do projeto:

```text
javac -encoding UTF-8 MetroParis.java
java MetroParis
```

O `-encoding UTF-8` é necessário por causa dos acentos nos nomes das estações.

## Como usar a janela

O título mostra o grupo. O painel inferior mostra as matrículas, a soma, o N e as linhas. A estação de maior grau já vem selecionada como origem do passe.

- Arrastar o mapa move a vista.
- A roda do mouse amplia e reduz.
- Clicar numa estação, ou escolhê-la na lista **Estação do passe**, define a origem do passe. A estação fica com um anel laranja.
- **Ajustar mapa** volta ao enquadramento inicial.
- **Limpar destaque** apaga o ciclo, os guichês e as cores da manutenção.
- **Cancelar cálculo** interrompe uma busca longa. O resultado interrompido não é tratado como ótimo.

### Menu Arquivo

- **Mapa do grupo (N=1)** carrega as dez linhas do grupo.
- **3 linhas (1, 4, 14)** e **6 linhas (1, 2, 4, 6, 12, 14)** carregam subconjuntos dessas linhas, para o teste pedido com quantidades diferentes.
- **Abrir estações e linhas...** lê um par de arquivos no formato acima.
- **Sair** fecha a janela.

### Menus Passe, Guichês e Manutenção

Cada menu tem **Força bruta**, **Branch-and-bound**, a heurística e **Comparar os três**.

A comparação mede os três métodos e informa se força bruta e branch-and-bound chegaram ao mesmo número. A heurística pode ser menor que o ótimo no passe, ou maior no número de guichês e de períodos.

No mapa completo, a força bruta do passe pode não terminar em tempo hábil, porque o maior bloco biconexo tem 32 estações. A janela pede confirmação antes de uma força bruta grande. A comparação dos dois métodos exatos deve ser feita nos mapas de 3 e de 6 linhas.

O resultado aparece no painel inferior e no desenho:

- Passe: estações e trechos do ciclo em destaque.
- Guichês: estações escolhidas em verde, com `[G]` no nome.
- Manutenção: cada período com uma cor, e o número do período ao lado do nome.

## Organização do código

Tudo está em `MetroParis.java`.

| Parte | Função |
| --- | --- |
| `NUMEROS_DE_MATRICULA` e `CONJUNTOS_DE_LINHAS_POR_RESTO` | Cálculo do N e escolha das linhas |
| Catálogo `COORDENADAS_DAS_ESTACOES` e `CATALOGO_DE_LINHAS` | Metrô completo usado para gerar o mapa |
| `MapaDoMetro`, `Grafo`, `TrechoColorido` | Estações, trechos coloridos e grafo simples |
| `montarMapaDasLinhas`, `gravarMapaNosArquivos`, `lerMapaDosArquivos` | Geração, gravação e leitura |
| `Algoritmos` | Os nove métodos, a comparação e os blocos biconexos |
| `desenharMapaNaTela` e `PainelDoMapa` | Desenho, zoom, arraste e clique |
| `JanelaPrincipal` | Menus e execução das buscas fora da thread da interface |

`trab.pdf` e `trab.txt` são o enunciado.

## Entrega

O enunciado pede, além deste programa, um relatório em PDF no padrão da SBC, com no máximo 15 páginas: introdução, solução em pseudocódigo, implementação, testes com tempos e mapas, conclusão comparando complexidade e tempo medido, e bibliografia ABNT. O ZIP deve se chamar `Gnn.zip`, ter no máximo 10 MB, e trazer o fonte e o relatório na raiz. O relatório ainda não faz parte desta pasta.
