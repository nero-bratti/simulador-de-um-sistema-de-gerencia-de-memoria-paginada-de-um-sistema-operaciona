import java.util.*;

// Interface comum para os tipos de tabela de páginas
interface TabelaPaginas {
    void mapear(int paginaVirtual);
    int traduzir(int enderecoVirtual);
}

public class GerenciadorMemoriaPaginada {

    // Configuração geral
    static int virtualAddressBits;
    static int physicalAddressBits;
    static int pageSizeBits;

    static int pageSize;
    static int numPaginasVirtuais;
    static int numMoldurasFisicas;

    // Segmentos
    static int tamanhoTextBits;
    static int tamanhoDataBits;
    static int tamanhoStackBits;
    static int tamanhoBssBits;

    // Memória física representada por vetor
    static int[] memoriaFisica;

    // Tabela de páginas (qualquer tipo)
    static TabelaPaginas tabelaPaginas;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. Configuração básica
        System.out.print("Bits do espaço de endereços virtuais: ");
        virtualAddressBits = scanner.nextInt();

        System.out.print("Bits da memória física: ");
        physicalAddressBits = scanner.nextInt();

        System.out.print("Bits do tamanho da página: ");
        pageSizeBits = scanner.nextInt();

        if (virtualAddressBits >= physicalAddressBits) {
            System.out.println("Erro: o espaço de endereços virtuais deve ser MENOR que o da memória física.");
            return;
        }

        // 2. Segmentos
        System.out.print("Bits do segmento .text: ");
        tamanhoTextBits = scanner.nextInt();

        System.out.print("Bits do segmento .data: ");
        tamanhoDataBits = scanner.nextInt();

        System.out.print("Bits do segmento .stack: ");
        tamanhoStackBits = scanner.nextInt();

        int somaSegmentos = (int) (Math.pow(2, tamanhoTextBits)
                                 + Math.pow(2, tamanhoDataBits)
                                 + Math.pow(2, tamanhoStackBits));
        int tamanhoBssBytes = 3 * somaSegmentos;
        tamanhoBssBits = (int) (Math.log(tamanhoBssBytes) / Math.log(2));

        // 3. Cálculos derivados
        pageSize = (int) Math.pow(2, pageSizeBits);
        numPaginasVirtuais = (int) Math.pow(2, virtualAddressBits - pageSizeBits);
        numMoldurasFisicas = (int) Math.pow(2, physicalAddressBits - pageSizeBits);
        memoriaFisica = new int[numMoldurasFisicas];
        Arrays.fill(memoriaFisica, -1); // todas as molduras começam livres

        // 4. Escolha do tipo de tabela
        System.out.println("\nEscolha o tipo de tabela de páginas:");
        System.out.println("1 - Tabela de 1 nível");
        System.out.println("2 - Tabela de 2 níveis");
        System.out.println("3 - Tabela invertida");
        int tipo = scanner.nextInt();

        switch (tipo) {
            case 1 -> tabelaPaginas = new TabelaPaginaSimples(numPaginasVirtuais);
            case 2 -> tabelaPaginas = new TabelaPaginaDoisNiveis(numPaginasVirtuais);
            case 3 -> tabelaPaginas = new TabelaInvertida(numMoldurasFisicas);
            default -> {
                System.out.println("Tipo inválido.");
                return;
            }
        }

        // 5. Entrada de endereços virtuais para tradução
        System.out.println("\nDigite endereços virtuais para tradução (número inteiro). Digite -1 para sair.");
        while (true) {
            System.out.print("Endereço virtual: ");
            int enderecoVirtual = scanner.nextInt();
            if (enderecoVirtual == -1) break;

            int enderecoFisico = tabelaPaginas.traduzir(enderecoVirtual);
            System.out.println("→ Endereço físico: " + enderecoFisico);
        }
    }

    // Encontra uma moldura livre ou retorna -1
    public static int encontrarMolduraLivre() {
        for (int i = 0; i < memoriaFisica.length; i++) {
            if (memoriaFisica[i] == -1) return i;
        }
        return -1;
    }
}

// ------------------------------
// Tabela de 1 nível
class TabelaPaginaSimples implements TabelaPaginas {
    private final int[] tabela;

    public TabelaPaginaSimples(int numPaginas) {
        tabela = new int[numPaginas];
        int valor = -1;
        for (int i = 0; i < tabela.length; i++) {
            tabela[i] = valor;
            valor = (valor - 1 < -42) ? -1 : valor - 1;
        }
    }

    public void mapear(int paginaVirtual) {
        int moldura = GerenciadorMemoriaPaginada.encontrarMolduraLivre();
        if (moldura == -1) {
            System.out.println("Memória física lotada! Encerrando o programa.");
            System.exit(1);
        }
        tabela[paginaVirtual] = moldura;
        GerenciadorMemoriaPaginada.memoriaFisica[moldura] = paginaVirtual;
    }

    public int traduzir(int enderecoVirtual) {
        int pagina = enderecoVirtual / GerenciadorMemoriaPaginada.pageSize;
        int deslocamento = enderecoVirtual % GerenciadorMemoriaPaginada.pageSize;

        if (tabela[pagina] < 0) {
            mapear(pagina);
        }

        int moldura = tabela[pagina];
        return moldura * GerenciadorMemoriaPaginada.pageSize + deslocamento;
    }
}

// ------------------------------
// Tabela de 2 níveis (básica com 2 blocos)
class TabelaPaginaDoisNiveis implements TabelaPaginas {
    private final int[][] tabela;

    public TabelaPaginaDoisNiveis(int numPaginasVirtuais) {
        int nivel1 = (int) Math.sqrt(numPaginasVirtuais);
        tabela = new int[nivel1][nivel1];
        int val = -1;
        for (int i = 0; i < nivel1; i++) {
            for (int j = 0; j < nivel1; j++) {
                tabela[i][j] = val;
                val = (val - 1 < -42) ? -1 : val - 1;
            }
        }
    }

    public void mapear(int paginaVirtual) {
        int i = paginaVirtual / tabela.length;
        int j = paginaVirtual % tabela.length;

        int moldura = GerenciadorMemoriaPaginada.encontrarMolduraLivre();
        if (moldura == -1) {
            System.out.println("Memória física lotada! Encerrando o programa.");
            System.exit(1);
        }

        tabela[i][j] = moldura;
        GerenciadorMemoriaPaginada.memoriaFisica[moldura] = paginaVirtual;
    }

    public int traduzir(int enderecoVirtual) {
        int pagina = enderecoVirtual / GerenciadorMemoriaPaginada.pageSize;
        int deslocamento = enderecoVirtual % GerenciadorMemoriaPaginada.pageSize;

        int i = pagina / tabela.length;
        int j = pagina % tabela.length;

        if (tabela[i][j] < 0) {
            mapear(pagina);
        }

        int moldura = tabela[i][j];
        return moldura * GerenciadorMemoriaPaginada.pageSize + deslocamento;
    }
}

// ------------------------------
// Tabela invertida (usa vetor direto)
class TabelaInvertida implements TabelaPaginas {
    private final int[] tabela;

    public TabelaInvertida(int numMolduras) {
        tabela = new int[numMolduras];
        Arrays.fill(tabela, -1);
    }

    public void mapear(int paginaVirtual) {
        int moldura = GerenciadorMemoriaPaginada.encontrarMolduraLivre();
        if (moldura == -1) {
            System.out.println("Memória física lotada! Encerrando o programa.");
            System.exit(1);
        }
        tabela[moldura] = paginaVirtual;
        GerenciadorMemoriaPaginada.memoriaFisica[moldura] = paginaVirtual;
    }

    public int traduzir(int enderecoVirtual) {
        int pagina = enderecoVirtual / GerenciadorMemoriaPaginada.pageSize;
        int deslocamento = enderecoVirtual % GerenciadorMemoriaPaginada.pageSize;

        for (int i = 0; i < tabela.length; i++) {
            if (tabela[i] == pagina) {
                return i * GerenciadorMemoriaPaginada.pageSize + deslocamento;
            }
        }

        // Página não encontrada → mapear
        mapear(pagina);

        // Após mapear, encontrar novamente
        for (int i = 0; i < tabela.length; i++) {
            if (tabela[i] == pagina) {
                return i * GerenciadorMemoriaPaginada.pageSize + deslocamento;
            }
        }

        throw new RuntimeException("Erro ao mapear página.");
    }
}
