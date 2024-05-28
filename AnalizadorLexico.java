import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorLexico {

    private static boolean hayErrores = false;
    private static final Set<String> palabrasReservadas;

    static {
        palabrasReservadas = new HashSet<>();
        String[] palabras = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"};
        for (String palabra : palabras) {
            palabrasReservadas.add(palabra);
        }
    }

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

            if (!hayErrores) {
                System.out.println("No hay errores de análisis léxico en el archivo " + archivo);
            }
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
                    estado = estadoInicial(c, caracteres, i);
                    break;
                case 1:
                    estado = estadoNumeroEntero(c, numeroLinea, caracteres, i);
                    break;
                case 2:
                    estado = estadoNumeroReal(c, numeroLinea, caracteres, i);
                    break;
                case 3:
                    estado = estadoIdentificador(c, numeroLinea);
                    break;
                case 4:
                    estado = estadoOperadorAritmetico(c, numeroLinea);
                    break;
                case 5:
                    estado = estadoOperadorComparacion(c, numeroLinea);
                    break;
                case 6:
                    estado = estadoOperadorAsignacion(c, numeroLinea);
                    break;
                case 7:
                    estado = estadoHexadecimal(c, numeroLinea, caracteres, i);
                    break;
                case 8:
                    estado = estadoOctal(c, numeroLinea, caracteres, i);
                    break;
                case 9:
                    estado = estadoRealConExponente(c, numeroLinea, caracteres, i);
                    break;
                case 10:
                    estado = estadoComentario(c, numeroLinea, caracteres, i);
                    i = estado == 0 ? i : caracteres.length; // Si se sale de comentario, continuar desde ahí
                    break;
                default:
                    estado = 0;
                    break;
            }
        }

        // Verificar estructuras de iteradores y impresiones
        verificarEstructuras(linea, numeroLinea);
    }

    public static int estadoInicial(char c, char[] caracteres, int i) {
        if (Character.isDigit(c) || c == '+' || c == '-') {
            if (c == '0') {
                if (i + 1 < caracteres.length && (caracteres[i + 1] == 'x' || caracteres[i + 1] == 'X')) {
                    return 7; // Posible número hexadecimal
                } else {
                    return 8; // Posible número octal
                }
            }
            return 1; // Número entero
        } else if (Character.isLetter(c) || c == '_' || c == "$") {
            return 3; // Identificador
        } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
            return 4; // Operador aritmético
        } else if (c == '<' || c == '>' || c == '!' || c == '=') {
            return 5; // Operador de comparación
        } else if (c == '/') {
            return 10; // Posible inicio de comentario
        } else {
            return 0; // Ignorar otros caracteres
        }
    }

    public static int estadoNumeroEntero(char c, int numeroLinea, char[] caracteres, int i) {
        if (Character.isDigit(c)) {
            return 1; // Sigue siendo un número entero
        } else if (c == '.') {
            return 2; // Posible número real
        } else if (c == 'E' || c == 'e') {
            return 9; // Posible notación científica
        } else if (esDelimitador(c)) {
            return 0; // Fin del número entero
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Número entero mal formado");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoNumeroReal(char c, int numeroLinea, char[] caracteres, int i) {
        if (Character.isDigit(c)) {
            return 2; // Sigue siendo un número real
        } else if (c == 'E' || c == 'e') {
            return 9; // Notación científica
        } else if (esDelimitador(c)) {
            return 0; // Fin del número real
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Número real mal formado");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoRealConExponente(char c, int numeroLinea, char[] caracteres, int i) {
        //comprobar si sea 7.(nada) marque error
        if (Character.isDigit(c)) {
            return 9; // Sigue siendo un número real con exponente
        } else if (c == '+' || c == '-') {
            return 9; // Puede tener signo después de 'E' o 'e'
        } else if (){
            //comprobar si tiene un numero despues del + o -
        }else if (esDelimitador(c)) {
            return 0; // Fin del número real con exponente
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Número real con exponente mal formado");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoIdentificador(char c, int numeroLinea) {
        if (Character.isLetterOrDigit(c) || c == '_' || c == "$") {
            return 3; // Sigue siendo un identificador
        } else if (esDelimitador(c)) {
            return 0; // Fin del identificador
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Identificador mal formado");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoOperadorAritmetico(char c, int numeroLinea) {
        if (esDelimitador(c)) {
            return 0; // Fin del operador aritmético
        } else {
            return 4; // Sigue siendo un operador aritmético
        }
    }

    public static int estadoOperadorComparacion(char c, int numeroLinea) {
        if (c == '=') {
            return 5; // Operador de comparación compuesto
        } else {
            return 0; // Fin del operador de comparación
        }
    }

    public static int estadoOperadorAsignacion(char c, int numeroLinea) {
        if (esDelimitador(c)) {
            return 0; // Fin del operador de asignación
        } else {
            return 6; // Sigue siendo un operador de asignación
        }
    }

    public static int estadoHexadecimal(char c, int numeroLinea, char[] caracteres, int i) {
        if (i == 1 && (c == 'x' || c == 'X')) {
            return 7; // Confirmar hexadecimal
        } else if (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
            return 7; // Sigue siendo un número hexadecimal
        } else if (esDelimitador(c)) {
            return 0; // Fin del número hexadecimal
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Número hexadecimal mal formado");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoOctal(char c, int numeroLinea, char[] caracteres, int i) {
        //agregar si exite un 0.X lo mando a reales
        if (Character.isDigit(c) && c >= '0' && c <= '7') {
            return 8; // Sigue siendo un número octal
        } else if (esDelimitador(c)) {
            return 0; // Fin del número octal
        } else {
            System.out.println("Error en línea " + numeroLinea + ": Número octal mal formado");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoComentario(char c, int numeroLinea, char[] caracteres, int i) {
        if (c == '/') {
            if (i + 1 < caracteres.length && caracteres[i + 1] == '/') {
                return caracteres.length; // Comentario de línea, ignorar hasta el final
            } else if (i + 1 < caracteres.length && caracteres[i + 1] == '*') {
                return 11; // Comentario de bloque
            } else {
                return 4; // Operador aritmético '/'
            }
        } else {
            return 0; // No es comentario, regresar al estado inicial
        }
    }

    public static void verificarEstructuras(String linea, int numeroLinea) {
        // Verificar iteradores y condiciones
        String[] iteradores = {"for", "if", "while", "do"};
        for (String iterador : iteradores) {
            if (linea.matches(".*\\b" + iterador + "\\b.*")) {
                Pattern pattern = Pattern.compile("\\b" + iterador + "\\b\\s*\\(.*\\).*\\{.*\\}");
                Matcher matcher = pattern.matcher(linea);
                if (!matcher.find()) {
                    System.out.println("Error en línea " + numeroLinea + ": Estructura de " + iterador + " mal formada");
                    hayErrores = true;
                }
                return; // Una vez verificada la estructura del iterador, no continuar verificando más
            }
        }

        // Verificar System.out.println
        if (linea.contains("System.out.println")) {
            Pattern pattern = Pattern.compile("System\\.out\\.println\\s*\\(\\s*\".*\"\\s*(\\+\\s*\\(?.*\\)?\\s*)?\\)\\s*;");
            Matcher matcher = pattern.matcher(linea);
            if (!matcher.find()) {
                System.out.println("Error en línea " + numeroLinea + ": Impresión mal formada");
                hayErrores = true;
            }
        }
    }

    private static boolean esDelimitador(char c) {
        return c == ' ' || c == ',' || c == ';' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']';
    }
}
