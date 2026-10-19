package com.inventario;

import com.inventario.vista.InventarioFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada principal de la aplicación. */
public class Main {
    public static void main(String[] args) {
        
        // Bloque para intentar aplicar un diseño visual moderno llamado "Nimbus" a la interfaz
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Si el estilo Nimbus no está disponible, el programa usa el diseño por defecto sin detenerse
        }
        
        // Inicializa y muestra la ventana principal de forma segura en el hilo de la interfaz gráfica (EDT)
        SwingUtilities.invokeLater(() -> new InventarioFrame().setVisible(true));
    }
}