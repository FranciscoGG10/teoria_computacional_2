import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class AnalizadorLexico {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el nombre del archivo Java: ");
        String archivo = scanner.nextLine();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 1;

            while ((linea = br.readLine()) != null) {
                analizarLinea(linea, numeroLinea);
                numeroLinea++;
            }

            System.out.println("No hay errores de análisis léxico en el archivo " + archivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void analizarLinea(String linea, int numeroLinea) {
        int estado = 0;
        char[] caracteres = linea.toCharArray();

        for (int i = 0; i < caracteres.length; i++) {
            char c = caracteres[i];

            switch (estado) {
                case 0:
                    estado = estadoInicial(c);
                    break;
                case 1:
                    estado = estadoNumeroEntero(c, numeroLinea);
                    break;
                case 2:
                    estado = estadoNumeroReal(c, numeroLinea);
                    break;
                case 3:
                    estado = estadoIdentificador(c, numeroLinea);
                    break;
                case 4:
                    estado = estadoOperador(c, numeroLinea);
                    break;
                default:
                    estado = 0;
                    break;
            }
        }
    }

    public static int estadoInicial(char c) {
        if (Character.isDigit(c)) {
            return 1; // Número entero
        } else if (Character.isLetter(c)) {
            return 3; // Identificador
        } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
            return 4; // Operador aritmético
        } else {
            return 0; // Ignorar otros caracteres
        }
    }

    public static int estadoNumeroEntero(char c, int numeroLinea) {
        if (Character.isDigit(c)) {
            return 1; // Sigue siendo un número entero
        } else if (c == '.') {
            return 2; // Posible número real
        } else {
            return 0; // Fin del número
        }
    }

    public static int estadoNumeroReal(char c, int numeroLinea) {
        if (Character.isDigit(c)) {
            return 2; // Sigue siendo un número real
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Número real mal formado");
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoIdentificador(char c, int numeroLinea) {
        if (Character.isLetterOrDigit(c) || c == '_') {
            return 3; // Sigue siendo un identificador
        } else {
            return 0; // Fin del identificador
        }
    }

    public static int estadoOperador(char c, int numeroLinea) {
        if (c == '=' || c == '!' || c == '<' || c == '>') {
            return 4; // Operador compuesto
        } else {
            return 0; // Fin del operador
        }
    }
}
