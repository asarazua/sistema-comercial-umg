package com.umg.comercial;

import com.umg.comercial.ui.VentanaPrincipal;
import javax.swing.*;

/**
 * Clase principal — lanza la interfaz gráfica del Sistema de Gestión Comercial UMG.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* usar look por defecto */ }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
