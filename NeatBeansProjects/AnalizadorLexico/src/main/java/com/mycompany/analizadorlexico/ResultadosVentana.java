/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.analizadorlexico;

import javax.swing.*;
import java.awt.*;

public class ResultadosVentana {

    public static void mostrarResultado(String resultado) {
        JFrame frame = new JFrame("Resultados del Análisis Léxico");
        JTextArea textArea = new JTextArea(resultado);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}