/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.analizadorlexico;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AnalizadorLexico {

    private static boolean hayErrores = false;
    private static final Set<String> palabrasReservadas;
    private static final StringBuilder resultados = new StringBuilder();

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
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Seleccione el archivo Java a analizar");
    int userSelection = fileChooser.showOpenDialog(null);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File archivo = fileChooser.getSelectedFile();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 1;

            while ((linea = br.readLine()) != null) {
                analizarLinea(linea, numeroLinea);
                numeroLinea++;
            }

            if (!hayErrores) {
                resultados.append("No hay errores de análisis léxico en el archivo ").append(archivo.getName()).append("\n");
            }

            ResultadosVentana.mostrarResultado(resultados.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        System.out.println("No se seleccionó ningún archivo.");
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
                    estado = estadoIdentificador(c, numeroLinea, caracteres, i, linea);
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
        if (Character.isDigit(c)) {
            if (c == '0') {
                if (i + 1 < caracteres.length && (caracteres[i + 1] == 'x' || caracteres[i + 1] == 'X')) {
                    return 7; // Posible número hexadecimal
                } else if (i + 1 < caracteres.length && caracteres[i + 1] == '.') {
                    return 1; // Posible número real
                } else if (i + 1 < caracteres.length && Character.isDigit(caracteres[i + 1]) && caracteres[i + 1] >= '0' && caracteres[i + 1] <= '7') {
                    return 8; // Posible número octal
                }
            }
            return 1; // Número entero
        } else if (Character.isLetter(c) || c == '_' || c == '$') {
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

    public static int estadoIdentificador(char c, int numeroLinea, char[] caracteres, int i, String linea) {
        if (Character.isLetterOrDigit(c) || c == '_' || c == '$' || c == '"') {
            return 3; // Sigue siendo un identificador
        } else if (linea.contains("System.out.println")) {
            return 3; // Ignorar identificadores si la línea contiene "System.out.println"
        } else if (esDelimitador(c)) {
            return 0; // Fin del identificador
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Identificador mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }    

    public static int estadoNumeroEntero(char c, int numeroLinea, char[] caracteres, int i) {
        if (Character.isDigit(c)) {
            return 1; // Sigue siendo un número entero
        } else if (c == '.') {
            if (i + 1 >= caracteres.length || !Character.isDigit(caracteres[i + 1])) {
                resultados.append("Error en línea ").append(numeroLinea).append(": Número real mal formado\n");
                hayErrores = true;
                return 0; // Error, regresar al estado inicial
            }
            return 2; // Posible número real
        } else if (c == 'E' || c == 'e') {
            return 9; // Posible notación científica
        } else if (esDelimitador(c)) {
            return 0; // Fin del número entero
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número entero mal formado\n");
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
            resultados.append("Error en línea ").append(numeroLinea).append(": Número real mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoRealConExponente(char c, int numeroLinea, char[] caracteres, int i) {
        if (Character.isDigit(c)) {
            return 9; // Sigue siendo un número real con exponente
        } else if (c == '+' || c == '-') {
            if (i + 1 < caracteres.length && Character.isDigit(caracteres[i + 1])) {
                return 9; // Puede tener signo después de 'E' o 'e' y seguido de un dígito
            } else {
                resultados.append("Error en línea ").append(numeroLinea).append(": Número real con exponente mal formado\n");
                hayErrores = true;
                return 0; // Error, regresar al estado inicial
            }
        } else if (esDelimitador(c)) {
            return 0; // Fin del número real con exponente
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número real con exponente mal formado\n");
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
        if (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
            return 7; // Sigue siendo un número hexadecimal
        } else if (esDelimitador(c)) {
            return 0; // Fin del número hexadecimal
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número hexadecimal mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoOctal(char c, int numeroLinea, char[] caracteres, int i) {
        if (c >= '0' && c <= '7') {
            return 8; // Sigue siendo un número octal
        } else if (esDelimitador(c)) {
            return 0; // Fin del número octal
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número octal mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoComentario(char c, int numeroLinea, char[] caracteres, int i) {
        if (c == '/') {
            return 0; // Comentario de una sola línea, terminar análisis
        } else if (c == '*') {
            if (i + 1 < caracteres.length && caracteres[i + 1] == '/') {
                return 0; // Comentario de varias líneas, terminado
            } else {
                return 10; // Comentario de varias líneas, continuar
            }
        } else {
            return 10; // Sigue siendo un comentario
        }
    }

    private static boolean esDelimitador(char c) {
        return c == ' ' || c == ',' || c == ';' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || 
               c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '<' || c == '>' || 
               c == '=' || c == '!' || c == '&' || c == '|' || c == '^' || c == '~';
    }

    public static void verificarEstructuras(String linea, int numeroLinea) {
        // Verificar estructuras de iteración
        if (linea.contains("for (") || linea.contains("while (")) {
            if (!linea.contains("{") && !linea.trim().endsWith(";")) {
                resultados.append("Error en línea ").append(numeroLinea).append(": Estructura de iteración mal formada\n");
                hayErrores = true;
            }
        }
    
        // Verificar System.out.println
        if (linea.contains("System.out.println")) {
            int indexInicio = linea.indexOf("System.out.println") + "System.out.println".length();
            if (indexInicio < linea.length() && (linea.charAt(indexInicio) != '(' || !linea.trim().endsWith(");"))) {
                resultados.append("Error en línea ").append(numeroLinea).append(": Estructura de impresión mal formada\n");
                hayErrores = true;
            } else {
                // Analizar lo que está dentro de los paréntesis
                int indexFin = linea.lastIndexOf(")");
                if (indexInicio < indexFin) {
                    String contenido = linea.substring(indexInicio + 1, indexFin);
                    analizarContenidoImpresion(contenido, numeroLinea);
                }
            }
        } 
    }

    public static void analizarContenidoImpresion(String contenido, int numeroLinea) {
        char[] caracteres = contenido.toCharArray();
        int estado = 0;
    
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
                    estado = estadoIdentificador(c, numeroLinea, caracteres, i, contenido);
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
    }
    
}