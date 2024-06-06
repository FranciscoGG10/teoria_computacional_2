/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.analizadorlexico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AnalizadorLexico {

    private static boolean hayErrores = false;
    private static boolean comentarioBloque = false;
    private static final Set<String> palabrasReservadas;
    private static final StringBuilder resultados = new StringBuilder();

    static {
        palabrasReservadas = new HashSet<>();
        String[] palabras = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "int"};
        for (String palabra : palabras) {
            palabrasReservadas.add(palabra);
        }
    }

    public static void main(String[] args) {
        // Crear la ventana principal
        JFrame frame = new JFrame("Analizador Léxico");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Crear panel superior con botón y etiqueta
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Seleccione un archivo Java para analizar:");
        JButton button = new JButton("Buscar archivo");

        topPanel.add(label);
        topPanel.add(button);
        frame.add(topPanel, BorderLayout.NORTH);

        // Crear área de texto para mostrar resultados
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Añadir funcionalidad al botón
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccione el archivo Java a analizar");
            int userSelection = fileChooser.showOpenDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                analizarArchivo(archivo, textArea);
            } else {
                textArea.setText("No se seleccionó ningún archivo.");
            }
        });

        // Hacer visible la ventana
        frame.setVisible(true);
    }

    private static void analizarArchivo(File archivo, JTextArea textArea) {
        resultados.setLength(0);  // Limpiar resultados anteriores
        hayErrores = false;
        comentarioBloque = false;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 1;
            resultados.append("Analizando el archivo: ").append(archivo.getName()).append("\n");

            while ((linea = br.readLine()) != null) {
                analizarLinea(linea, numeroLinea);
                numeroLinea++;
            }

            if (!hayErrores) {
                resultados.append("No hay errores de análisis léxico en el archivo ").append(archivo.getName()).append("\n");
            }

            if (comentarioBloque) {
                resultados.append("Error en comentario de bloque sin cierre en el archivo ").append(archivo.getName()).append("\n");
            }

            // Muestra los resultados en el área de texto
            textArea.setText(resultados.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            textArea.setText("Ocurrió un error al leer el archivo: " + ex.getMessage());
        }
    }


    public static void analizarLinea(String linea, int numeroLinea) {
        int estado = 0;
        char[] caracteres = linea.toCharArray();
    
        if (comentarioBloque) {
            estado = 11;
        }
    
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
                    estado = estadoOperadorAritmetico(c, numeroLinea, caracteres, i);
                    break;
                case 5:
                    estado = estadoOperadorComparacion(c, numeroLinea, caracteres, i);
                    break;
                case 6:
                    estado = estadoOperadorAsignacion(c, numeroLinea, caracteres, i);
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
                    i = estado == 10 ? i : caracteres.length; // Si se sale de comentario, continuar desde ahí
                    break;
                case 11: 
                    estado = estadoComentarioBloque(c, numeroLinea, caracteres, i);
                    break;
                default:
                    estado = 0;
                    break;
            }
        }
    
        // Verificar estructuras de iteradores y impresiones solo si no estamos en un comentario de bloque
        if (!comentarioBloque) {
            verificarEstructuras(linea, numeroLinea);
        }
    }

    public static int estadoInicial(char c, char[] caracteres, int i) {
        if (Character.isDigit(c)) {
            if (c == '0') {
                if (i + 1 < caracteres.length && (caracteres[i + 1] == 'x' || caracteres[i + 1] == 'X')) {
                    return 7; // Posible número hexadecimal
                } else {
                    return 8; //Posible octal
                }
            }
            return 1; // Número entero
        } else if (Character.isLetter(c) || c == '_') {
            return 3; // Identificador
        } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
            return 4; // Operador aritmético
        } else if (c == '<' || c == '>' || c == '!') {
            return 5; // Operador de comparación
        } else if (c == '=') {
            return 6; // Operador de asignación
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
            resultados.append("Error en línea ").append(numeroLinea).append(": Número entero mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

     public static int estadoNumeroReal(char c, int numeroLinea, char[] caracteres, int i) {
        if (Character.isDigit(c)) {
            return 2; // Sigue siendo un número real
        } else if ((c == 'E' || c == 'e')) {
            if (!Character.isDigit(caracteres[i - 1])){
                resultados.append("Error en línea ").append(numeroLinea).append(": Número real con exponente mal formado\n");
                hayErrores = true;
                return 0; // Error, regresar al estado inicial
            }
            return 9; // Notación científica
        } else if (esDelimitador(c) && Character.isDigit(caracteres[i - 1])) {
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
        } else if (esDelimitador(c) && Character.isDigit(caracteres[i - 1])) {
            return 0; // Fin del número realEjemploPracticaAnalizador.java
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número real con exponente mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }
    
    public static int estadoIdentificador(char c, int numeroLinea, char[] caracteres, int i, String linea) {
        if (Character.isLetterOrDigit(c) || c == '_') {
            return 3; // Sigue siendo un identificador
        } else if (linea.contains("System.out.println")) {
            return 3; // Ignorar identificadores si la línea contiene "System.out.println"
        } else if (i + 1 < caracteres.length && (caracteres[i+1] == '+' || caracteres[i+1] == '-')){
            return 4;//identificador con operador aritmetico        
        }else if (esDelimitador(c)) {
            return 0; // Fin del identificador
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Identificador mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }    

    public static int estadoOperadorAritmetico(char c, int numeroLinea, char[] caracteres, int i) {
        if (caracteres[i - 1] == '/' && (c == '/')) {
            return 10; // Comentario de linea
        } else if (caracteres[i - 1] == '/' && (c == '*')) {
            comentarioBloque = true;
            return 11; // Comentario de bloque
        } else if (c == ' ') {
            if (i + 1 < caracteres.length && !(Character.isLetter(caracteres[i + 1]) || Character.isDigit(caracteres[i + 1]) || caracteres[i + 1] == '_' || caracteres[i + 1] == '(' || caracteres[i + 1] == '"')) {
                resultados.append("Error en línea ").append(numeroLinea).append(": Operador aritmético mal formado\n");
                hayErrores = true;
                return 0; //Operador aritmetico mal formado
            }
            return 0; // Operacion Aritmetica bien formada
        } else if (esDelimitador(c)) {
            resultados.append("Error en línea ").append(numeroLinea).append(": Operador aritmético mal formado\n");
            hayErrores = true;
            return 0; //Operador aritmetico mal formado
        } else {
            return 0;
        }
    } 

    public static int estadoOperadorComparacion(char c, int numeroLinea, char[] caracteres, int i) {
        if(c == '='){
            return 5; // Sigue siendo de comparacion
        } else if (c == ' ' && caracteres[i - 1] != '!'){
            if(i + 1 < caracteres.length && esDelimitador(caracteres[i + 1])){
                resultados.append("Error en línea ").append(numeroLinea).append(": Operador de comparacion mal formado\n");
                hayErrores = true;
                return 0; //Operador mal formado
            }
            else {
                return 5; //Operador bien formado
            }
        } else if (c == ' ') {
            if(i + 1 < caracteres.length && !(Character.isLetter(caracteres[i+1]) || Character.isDigit(caracteres[i+1]) || caracteres[i+1] == '_')){
                resultados.append("Error en línea ").append(numeroLinea).append(": Operador aritmético mal formado\n");
                hayErrores = true;
                return 0; //Operador aritmetico mal formado
            }
            return 0; // Operacion Aritmetica bien formada
        } else if (esDelimitador(c)){
            resultados.append("Error en línea ").append(numeroLinea).append(": Operador de comparacion mal formado\n");
            hayErrores = true;
            return 0; //Operador mal formado
        } else {
            return 0; //Operador bien formado
        }
    }

    public static int estadoOperadorAsignacion(char c, int numeroLinea, char[] caracteres, int i) {
        if (c == '=') {
            return 5; // Operador de comparación
        } else if (c == ' ') {
            if(i + 1 < caracteres.length && esCierre(caracteres[i + 1])){
                resultados.append("Error en línea ").append(numeroLinea).append(": Operador de asignacion mal formado\n");
                hayErrores = true;
                return 0; //Operador mal formado
            } else {
                return 0; //Operador bien formado
            }
        } else if (esCierre(c)){
                resultados.append("Error en línea ").append(numeroLinea).append(": Operador de asignacion mal formado\n");
                hayErrores = true;
                return 0; //Operador mal formado
        } else {
            return 0; //Operador mal formado
        }
    }

    public static int estadoHexadecimal(char c, int numeroLinea, char[] caracteres, int i) {
        if (i == 1 && (c == 'x' || c == 'X')) {
            return 7; // Confirmar hexadecimal
        } else if (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
            return 7; // Sigue siendo un número hexadecimal
        } else if (esDelimitador(c) && Character.isDigit(caracteres[i - 1])) {
            return 0; // Fin del número hexadecimal
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número hexadecimal mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoOctal(char c, int numeroLinea, char[] caracteres, int i) {
        if (Character.isDigit(c) && c >= '0' && c <= '7') {
            return 8; // Sigue siendo un número octal
        } else if (c == '.'){
            return 2; // Posible numero real
        } else if (esDelimitador(c)) {
            return 0; // Fin del número octal
        } else {
            resultados.append("Error en línea ").append(numeroLinea).append(": Número octal mal formado\n");
            hayErrores = true;
            return 0; // Error, regresar al estado inicial
        }
    }

    public static int estadoComentario(char c, int numeroLinea, char[] caracteres, int i) {
        if (caracteres[i - 1] == '/') {
            return 10; // comentario de bloque 
        } else {
            return 0;
        }
    }
    
    public static int estadoComentarioBloque(char c, int numeroLinea, char[] caracteres, int i) {
        if (comentarioBloque) {
            if (c == '*' && i + 1 < caracteres.length && caracteres[i + 1] == '/') {
                comentarioBloque = false;
                return 0; // Fin del comentario de bloque
            } else {
                return 11; // Sigue dentro del comentario de bloque
            }
        } else {
            return 0;
        }
    }        
    
    private static boolean esCierre(char c) {
        return c == ',' || c == ';' || c == '{' || c == '}';
    }
    
    private static boolean esDelimitador(char c) {
        return c == ' ' || c == ',' || c == ';' || c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '"';
    }

    public static void verificarEstructuras(String linea, int numeroLinea) {
        // Verificar estructuras de iteración
        if (linea.contains("for (") || linea.contains("while (")) {
            if (!linea.contains("{") && !linea.trim().endsWith(";")) {
                resultados.append("Error en línea ").append(numeroLinea).append(": Estructura de iteración mal formada\n");
                hayErrores = true;
            }
        }
    
        // Verificar System.out.println solo si no estamos en un comentario de bloque
        if (!comentarioBloque && linea.contains("System.out.println")) {
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
                    estado = estadoOperadorAritmetico(c, numeroLinea, caracteres, i);
                    break;
                case 5:
                    estado = estadoOperadorComparacion(c, numeroLinea, caracteres, i);
                    break;
                case 6:
                    estado = estadoOperadorAsignacion(c, numeroLinea, caracteres, i);
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
                    i = estado == 10 ? i : caracteres.length; // Si se sale de comentario, continuar desde ahí
                    break;
                case 11: 
                    estado = estadoComentarioBloque(c, numeroLinea, caracteres, i);
                    break;
                default:
                    estado = 0;
                    break;
            }
        }
    }
    
}