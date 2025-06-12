import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Simulator {
    int logicalMemorySize;
    int physicalMemorySize;
    int pageSize;
    int[] physicalMemory;
    Scanner in = new Scanner(System.in);

    void run(String addressesPath) {
        System.out.print("Digite, em bits, o tamanho do espaço de endereços virtuais: ");// tem q ser menor que 25 bits
        logicalMemorySize = getScannerInt();
        System.out.print("Digite, em bits, o tamanho da memória principal: ");
        physicalMemorySize = getPhysicalMemorySize();
        System.out.print("Digite, em bits, o tamanho das páginas: ");
        pageSize = getScannerInt();
        System.out.println("----------------------------------------------------------");
        initializeMainMemory();
        read(addressesPath);

    }
    void read(String addressesPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(addressesPath))) {
            String line;
            while ((line = reader.readLine()) != null) {//entrada do texto
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
    void initializeMainMemory() {
        physicalMemory = new int[(int) Math.pow(2,physicalMemorySize)];
        for (int i = 0; i < physicalMemorySize; i++) {
            physicalMemory[i] = -1;
        }
    }
    int getPhysicalMemorySize() {
        int aux = getScannerInt();
        while (aux < logicalMemorySize) {
                System.out.println("Valor invalido. A memória física deve ser maior ou igual a memória virtual. Digite novamente.");
                aux = getScannerInt();
        }
        return aux;
    }
    int getScannerInt() {
        int aux;
        while(true) {
            if (in.hasNextInt()) {
                aux = in.nextInt();
                if (aux > 0) return aux;
            } else {
                System.out.print("Valor invalido. Digite um inteiro positivo: ");
                in.nextLine();
            }
        }
    }
}
