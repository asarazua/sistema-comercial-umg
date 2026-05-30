package com.umg.comercial.ui;

import com.umg.comercial.db.ConexionOracle;
import com.umg.comercial.grafo.Grafo;
import com.umg.comercial.grafo.NodoGrafo;
import com.umg.comercial.grafo.AristaGrafo;
import com.umg.comercial.hash.NodoHash;
import com.umg.comercial.hash.TablaHash;
import com.umg.comercial.modelo.*;
import com.umg.comercial.reporte.GeneradorReporte;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    static final Color AZUL_UMG   = new Color(0, 51, 102);
    static final Color AZUL_CLARO = new Color(0, 102, 204);
    static final Color VERDE      = new Color(0, 153, 76);
    static final Color NARANJA    = new Color(204, 102, 0);
    static final Color ROJO       = new Color(180, 0, 0);
    static final Color GRIS_FONDO = new Color(245, 245, 250);
    static final Color BLANCO     = Color.WHITE;

    TablaHash<Integer, Producto>    hashProductos   = new TablaHash<>();
    TablaHash<Integer, Marca>       hashMarcas      = new TablaHash<>();
    TablaHash<Integer, TipoCliente> hashTipoCliente = new TablaHash<>();
    Grafo grafo = new Grafo();
    ConexionOracle conexion;
    Connection conn;
    boolean cargado = false;

    JTabbedPane tabbedPane;
    JLabel lblEstado;
    PanelGrafo panelGrafo;
    JTable tablaHash, tablaFacturas, tablaDetalles;
    DefaultTableModel modelHash, modelFacturas, modelDetalles;
    JComboBox<String> cbClientes, cbAnioGrafo;

    public VentanaPrincipal() {
        setTitle("Sistema de Gestión Comercial — UMG | Programación III");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_UMG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel titulo = new JLabel("SISTEMA DE GESTIÓN COMERCIAL — UMG");
        titulo.setForeground(BLANCO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel sub = new JLabel("Programación III | Tablas Hash + Grafos | Sección B 2026");
        sub.setForeground(new Color(180, 210, 255));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel titulos = new JPanel(new GridLayout(2, 1));
        titulos.setOpaque(false);
        titulos.add(titulo); titulos.add(sub);
        header.add(titulos, BorderLayout.WEST);

        JButton btnConectar = crearBoton("🔌 Conectar Oracle", new Color(0,180,90));
        btnConectar.addActionListener(e -> mostrarDialogoConexion());
        JButton btnCargar = crearBoton("⬇ Cargar Datos", new Color(30,130,220));
        btnCargar.addActionListener(e -> cargarDatos());
        JPanel bots = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bots.setOpaque(false);
        bots.add(btnConectar); bots.add(btnCargar);
        header.add(bots, BorderLayout.EAST);

        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(new Color(230, 230, 240));
        status.setBorder(new EmptyBorder(4, 16, 4, 16));
        lblEstado = new JLabel("⚪ Sin conexión — Haga clic en 'Conectar Oracle'");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.add(lblEstado, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("🔷 Tabla Hash",       crearPanelHash());
        tabbedPane.addTab("🔶 Grafo Visual",     crearPanelGrafo());
        tabbedPane.addTab("📄 Facturas",         crearPanelFacturas());
        tabbedPane.addTab("➕ CRUD",             crearPanelCRUD());
        tabbedPane.addTab("📊 Reportes",         crearPanelReportes());

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    // ==================== TABLA HASH ====================
    private JPanel crearPanelHash() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBackground(GRIS_FONDO);
        top.add(new JLabel("Catálogo:"));
        JComboBox<String> cbCat = new JComboBox<>(new String[]{"Productos", "Marcas", "Tipo Cliente"});
        cbCat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        top.add(cbCat);
        JButton btnMostrar = crearBoton("Mostrar en Hash", AZUL_CLARO);
        top.add(btnMostrar);
        JLabel lblInfo = new JLabel("");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(AZUL_UMG);
        top.add(lblInfo);

        modelHash = new DefaultTableModel(
            new String[]{"Posición Hash", "Clave (ID)", "Valor", "¿Colisión?", "Tiempo Búsqueda (ns)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHash = new JTable(modelHash);
        estilizarTabla(tablaHash);
        ajustarAnchos(tablaHash, new int[]{110, 90, 300, 100, 160});

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busq.setBackground(GRIS_FONDO);
        busq.add(new JLabel("Buscar ID:"));
        JTextField txtB = new JTextField(8);
        busq.add(txtB);
        JButton btnB = crearBoton("Buscar O(1)", VERDE);
        JLabel lblR = new JLabel("");
        lblR.setFont(new Font("Segoe UI", Font.BOLD, 12));
        busq.add(btnB); busq.add(lblR);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaHash), BorderLayout.CENTER);
        panel.add(busq, BorderLayout.SOUTH);

        btnMostrar.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Cargue los datos primero."); return; }
            modelHash.setRowCount(0);
            int s = cbCat.getSelectedIndex();
            if (s == 0) llenarHashProductos(lblInfo);
            else if (s == 1) llenarHashMarcas(lblInfo);
            else llenarHashTipos(lblInfo);
        });

        btnB.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Cargue los datos primero."); return; }
            try {
                int id = Integer.parseInt(txtB.getText().trim());
                int s = cbCat.getSelectedIndex();
                String res; long t1 = System.nanoTime();
                if (s == 0) { Producto p = hashProductos.buscar(id); res = p != null ? "✅ " + p.getNombre() + " — Q" + p.getPrecio() : "❌ No encontrado"; }
                else if (s == 1) { Marca m = hashMarcas.buscar(id); res = m != null ? "✅ " + m.getNombre() + " (" + m.getPaisOrigen() + ")" : "❌ No encontrado"; }
                else { TipoCliente tc = hashTipoCliente.buscar(id); res = tc != null ? "✅ " + tc.getNombre() : "❌ No encontrado"; }
                long t = System.nanoTime() - t1;
                lblR.setText(res + "  |  " + t + " ns");
                lblR.setForeground(res.startsWith("✅") ? VERDE : ROJO);
            } catch (NumberFormatException ex) { mostrarAlerta("ID inválido."); }
        });
        return panel;
    }

    private void llenarHashProductos(JLabel l) {
        for (NodoHash<Integer, Producto> n : hashProductos.getTabla()) {
            boolean p = true;
            while (n != null) {
                long t = System.nanoTime(); hashProductos.buscar(n.getClave()); long t2 = System.nanoTime() - t;
                modelHash.addRow(new Object[]{hashProductos.getPosicion(n.getClave()), n.getClave(), n.getValor().getNombre() + " — Q" + n.getValor().getPrecio(), p ? "" : "⚠ COLISIÓN", t2});
                p = false; n = n.getSiguiente();
            }
        }
        l.setText("  Productos: " + modelHash.getRowCount() + " registros | Colisiones: " + hashProductos.getColisiones());
    }

    private void llenarHashMarcas(JLabel l) {
        for (NodoHash<Integer, Marca> n : hashMarcas.getTabla()) {
            boolean p = true;
            while (n != null) {
                long t = System.nanoTime(); hashMarcas.buscar(n.getClave()); long t2 = System.nanoTime() - t;
                modelHash.addRow(new Object[]{hashMarcas.getPosicion(n.getClave()), n.getClave(), n.getValor().getNombre() + " (" + n.getValor().getPaisOrigen() + ")", p ? "" : "⚠ COLISIÓN", t2});
                p = false; n = n.getSiguiente();
            }
        }
        l.setText("  Marcas: " + modelHash.getRowCount() + " registros | Colisiones: " + hashMarcas.getColisiones());
    }

    private void llenarHashTipos(JLabel l) {
        for (NodoHash<Integer, TipoCliente> n : hashTipoCliente.getTabla()) {
            boolean p = true;
            while (n != null) {
                long t = System.nanoTime(); hashTipoCliente.buscar(n.getClave()); long t2 = System.nanoTime() - t;
                modelHash.addRow(new Object[]{hashTipoCliente.getPosicion(n.getClave()), n.getClave(), n.getValor().getNombre() + " — " + n.getValor().getDescripcion(), p ? "" : "⚠ COLISIÓN", t2});
                p = false; n = n.getSiguiente();
            }
        }
        l.setText("  TipoCliente: " + modelHash.getRowCount() + " registros | Colisiones: " + hashTipoCliente.getColisiones());
    }

    // ==================== GRAFO ====================
    private JPanel crearPanelGrafo() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane grafoTabs = new JTabbedPane();
        grafoTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // ---- Tab 1: Grafo por Cliente ----
        JPanel tabCliente = new JPanel(new BorderLayout(8, 8));
        tabCliente.setBackground(GRIS_FONDO);
        tabCliente.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBackground(GRIS_FONDO);
        top.add(new JLabel("Cliente:"));
        cbClientes = new JComboBox<>();
        cbClientes.setPreferredSize(new Dimension(210, 28));
        top.add(cbClientes);
        top.add(new JLabel("Año:"));
        cbAnioGrafo = new JComboBox<>(new String[]{"Todos", "2024", "2025", "2026"});
        top.add(cbAnioGrafo);
        JButton btnVer = crearBoton("Ver Grafo", NARANJA);
        top.add(btnVer);

        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        leyenda.setBackground(GRIS_FONDO);
        agregarLeyenda(leyenda, "CLIENTE",  new Color(0, 102, 204));
        agregarLeyenda(leyenda, "FACTURA",  new Color(153, 0, 153));
        agregarLeyenda(leyenda, "DETALLE",  new Color(204, 102, 0));
        agregarLeyenda(leyenda, "PRODUCTO", new Color(0, 153, 76));
        agregarLeyenda(leyenda, "MARCA",    new Color(204, 0, 0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(GRIS_FONDO);
        topBar.add(top, BorderLayout.WEST);
        topBar.add(leyenda, BorderLayout.EAST);

        panelGrafo = new PanelGrafo();
        JScrollPane scrollGrafo = new JScrollPane(panelGrafo,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JTextArea txtRel = new JTextArea(7, 40);
        txtRel.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtRel.setEditable(false);
        txtRel.setBackground(new Color(20, 20, 30));
        txtRel.setForeground(new Color(0, 255, 128));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollGrafo, new JScrollPane(txtRel));
        split.setDividerLocation(420);

        tabCliente.add(topBar, BorderLayout.NORTH);
        tabCliente.add(split, BorderLayout.CENTER);

        btnVer.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Cargue los datos primero."); return; }
            String sel = (String) cbClientes.getSelectedItem();
            if (sel == null) return;
            int idCli = Integer.parseInt(sel.split(" - ")[0].trim());
            String anio = (String) cbAnioGrafo.getSelectedItem();
            int anioF = "Todos".equals(anio) ? 0 : Integer.parseInt(anio);
            panelGrafo.cargarGrafo(grafo, idCli, anioF);
            txtRel.setText(grafo.relacionCompleta(idCli));
        });

        // ---- Tab 2: Trazabilidad Inversa ----
        JPanel tabInversa = new JPanel(new BorderLayout(8, 8));
        tabInversa.setBackground(GRIS_FONDO);
        tabInversa.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topInv = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topInv.setBackground(GRIS_FONDO);
        topInv.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ROJO, 1),
            "Trazabilidad Inversa — dado un Producto, ver todos los Clientes que lo compraron",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), ROJO));

        topInv.add(new JLabel("ID Producto:"));
        JTextField txtIdProd = new JTextField(8);
        txtIdProd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topInv.add(txtIdProd);
        JLabel lblNomProd = new JLabel("  ");
        lblNomProd.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNomProd.setForeground(AZUL_UMG);
        topInv.add(lblNomProd);
        JButton btnInversa = crearBoton("Buscar Clientes", ROJO);
        topInv.add(btnInversa);

        String[] colsInv = {"#", "ID Cliente", "Nombre Cliente", "Correo", "Tipo Cliente (TH)"};
        DefaultTableModel modelInv = new DefaultTableModel(colsInv, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaInv = new JTable(modelInv);
        estilizarTabla(tablaInv);
        ajustarAnchos(tablaInv, new int[]{40, 90, 220, 260, 160});

        JTextArea txtInvDetalle = new JTextArea(5, 40);
        txtInvDetalle.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtInvDetalle.setEditable(false);
        txtInvDetalle.setBackground(new Color(20, 20, 30));
        txtInvDetalle.setForeground(new Color(255, 200, 50));

        JSplitPane splitInv = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tablaInv), new JScrollPane(txtInvDetalle));
        splitInv.setDividerLocation(280);

        tabInversa.add(topInv, BorderLayout.NORTH);
        tabInversa.add(splitInv, BorderLayout.CENTER);

        btnInversa.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Cargue los datos primero."); return; }
            try {
                int idProd = Integer.parseInt(txtIdProd.getText().trim());
                long t1 = System.nanoTime();
                Producto prod = hashProductos.buscar(idProd);
                long tiempoHash = System.nanoTime() - t1;
                if (prod == null) { mostrarAlerta("Producto no encontrado en Hash."); return; }
                lblNomProd.setText("-> " + prod.getNombre() + " | Hash O(1): " + tiempoHash + " ns");
                List<String> clientes = grafo.clientesQueCompraronProducto(idProd);
                modelInv.setRowCount(0);
                StringBuilder sb = new StringBuilder();
                sb.append("=== TRAZABILIDAD INVERSA ===\n");
                sb.append("Producto buscado: ").append(prod.getNombre()).append(" (ID: ").append(idProd).append(")\n");
                sb.append("Tiempo busqueda Hash O(1): ").append(tiempoHash).append(" ns\n");
                sb.append("Total clientes que compraron: ").append(clientes.size()).append("\n\n");
                int num = 1;
                for (String nombreCli : clientes) {
                    try {
                        PreparedStatement ps = conn.prepareStatement(
                            "SELECT C.ID_CLIENTE,C.NOMBRE||' '||C.APELLIDO,C.CORREO,TC.NOMBRE " +
                            "FROM CLIENTE C JOIN TIPO_CLIENTE TC ON C.ID_TIPO_CLIENTE=TC.ID_TIPO_CLIENTE " +
                            "WHERE C.NOMBRE||' '||C.APELLIDO=?");
                        ps.setString(1, nombreCli);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            modelInv.addRow(new Object[]{num, rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)+" (TH)"});
                            sb.append("  ").append(num).append(". ").append(rs.getString(2)).append(" | ").append(rs.getString(4)).append("\n");
                        } else {
                            modelInv.addRow(new Object[]{num, "-", nombreCli, "-", "-"});
                            sb.append("  ").append(num).append(". ").append(nombreCli).append("\n");
                        }
                        num++;
                    } catch (SQLException ex) {
                        modelInv.addRow(new Object[]{num++, "-", nombreCli, "-", "-"});
                    }
                }
                if (clientes.isEmpty()) sb.append("  Ningun cliente ha comprado este producto.\n");
                txtInvDetalle.setText(sb.toString());
            } catch (NumberFormatException ex) { mostrarAlerta("Ingrese un ID de producto valido."); }
        });

        grafoTabs.addTab("Grafo por Cliente", tabCliente);
        grafoTabs.addTab("Trazabilidad Inversa", tabInversa);
        panel.add(grafoTabs, BorderLayout.CENTER);
        return panel;
    }

    private void agregarLeyenda(JPanel p, String txt, Color c) {
        JLabel cuadro = new JLabel("  "); cuadro.setOpaque(true); cuadro.setBackground(c);
        cuadro.setPreferredSize(new Dimension(14, 14));
        JLabel lbl = new JLabel(txt); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); item.setBackground(p.getBackground());
        item.add(cuadro); item.add(lbl); p.add(item);
    }

    // ==================== FACTURAS ====================
    private JPanel crearPanelFacturas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Encabezado
        JPanel topFac = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        topFac.setBackground(new Color(235, 240, 255));
        topFac.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(AZUL_UMG, 2), "  Encabezado de Factura",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), AZUL_UMG));
        topFac.add(etiqueta("Cliente:"));
        JComboBox<String> cbCliFac = new JComboBox<>();
        cbCliFac.setPreferredSize(new Dimension(220, 30));
        cbCliFac.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topFac.add(cbCliFac);
        topFac.add(etiqueta("Año:"));
        JComboBox<String> cbAnioFac = new JComboBox<>(new String[]{"Todos", "2024", "2025", "2026"});
        cbAnioFac.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topFac.add(cbAnioFac);
        JButton btnVerFac = crearBoton("Ver Facturas", AZUL_CLARO);
        topFac.add(btnVerFac);

        // Tabla facturas
        modelFacturas = new DefaultTableModel(
            new String[]{"ID Factura", "Cliente", "Tipo Cliente (TH)", "Fecha", "Año", "Total (Q)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaFacturas = new JTable(modelFacturas);
        estilizarTabla(tablaFacturas);
        ajustarAnchos(tablaFacturas, new int[]{80, 180, 140, 110, 60, 100});
        JScrollPane scrollFac = new JScrollPane(tablaFacturas);
        scrollFac.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(AZUL_CLARO, 2), "  Facturas del Cliente",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), AZUL_CLARO));

        // Tabla detalles
        modelDetalles = new DefaultTableModel(
            new String[]{"ID Det.", "Producto (TH)", "Marca (TH)", "Cantidad", "Precio Unit. (Q)", "Subtotal (Q)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDetalles = new JTable(modelDetalles);
        estilizarTabla(tablaDetalles);
        ajustarAnchos(tablaDetalles, new int[]{70, 220, 130, 80, 130, 110});
        JScrollPane scrollDet = new JScrollPane(tablaDetalles);
        scrollDet.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(NARANJA, 2), "  Detalle de Factura Seleccionada  ← haga clic en una factura",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), NARANJA));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollFac, scrollDet);
        split.setDividerLocation(280);
        split.setResizeWeight(0.5);

        panel.add(topFac, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        // Eventos
        btnVerFac.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Cargue los datos primero."); return; }
            String sel = (String) cbCliFac.getSelectedItem();
            if (sel == null) return;
            int id = Integer.parseInt(sel.split(" - ")[0].trim());
            String anio = (String) cbAnioFac.getSelectedItem();
            int anioF = "Todos".equals(anio) ? 0 : Integer.parseInt(anio);
            cargarFacturas(id, anioF);
            modelDetalles.setRowCount(0);
        });

        tablaFacturas.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = tablaFacturas.getSelectedRow();
            if (row >= 0) {
                int idFac = (int) modelFacturas.getValueAt(row, 0);
                cargarDetalles(idFac);
            }
        });

        // Sincronizar combo clientes
        tabbedPane.addChangeListener(ce -> {
            if (tabbedPane.getSelectedIndex() == 2 && cargado && cbCliFac.getItemCount() == 0) {
                for (int i = 0; i < cbClientes.getItemCount(); i++)
                    cbCliFac.addItem(cbClientes.getItemAt(i));
            }
        });
        return panel;
    }

    private void cargarFacturas(int idCli, int anio) {
        modelFacturas.setRowCount(0);
        try {
            String sql = "SELECT F.ID_FACTURA, C.NOMBRE||' '||C.APELLIDO, TC.NOMBRE, F.FECHA, F.ANIO, F.TOTAL " +
                "FROM FACTURA F JOIN CLIENTE C ON F.ID_CLIENTE=C.ID_CLIENTE " +
                "JOIN TIPO_CLIENTE TC ON C.ID_TIPO_CLIENTE=TC.ID_TIPO_CLIENTE " +
                "WHERE F.ID_CLIENTE=?" + (anio > 0 ? " AND F.ANIO=?" : "") + " ORDER BY F.ANIO, F.FECHA";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCli);
            if (anio > 0) ps.setInt(2, anio);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelFacturas.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3) + " (TH)",
                    rs.getDate(4), rs.getInt(5), "Q " + String.format("%.2f", rs.getDouble(6))
                });
            }
        } catch (SQLException ex) { mostrarAlerta("Error facturas: " + ex.getMessage()); }
    }

    private void cargarDetalles(int idFac) {
        modelDetalles.setRowCount(0);
        try {
            String sql = "SELECT D.ID_DETALLE, P.NOMBRE, M.NOMBRE, D.CANTIDAD, D.PRECIO_UNITARIO, D.SUBTOTAL " +
                "FROM DETALLE D JOIN PRODUCTO P ON D.ID_PRODUCTO=P.ID_PRODUCTO " +
                "JOIN MARCA M ON P.ID_MARCA=M.ID_MARCA WHERE D.ID_FACTURA=? ORDER BY D.ID_DETALLE";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idFac);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Busca producto y marca via Hash para mostrar TH
                Producto pHash = hashProductos.buscar(rs.getInt(1) > 0 ? 1 : 1);
                modelDetalles.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2) + " (TH)",
                    rs.getString(3) + " (TH)",
                    rs.getInt(4),
                    "Q " + String.format("%.2f", rs.getDouble(5)),
                    "Q " + String.format("%.2f", rs.getDouble(6))
                });
            }
        } catch (SQLException ex) { mostrarAlerta("Error detalles: " + ex.getMessage()); }
    }

    // ==================== CRUD ====================
    private JPanel crearPanelCRUD() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.addTab("👤 Clientes",  crearCRUDClientes());
        tabs.addTab("📦 Productos", crearCRUDProductos());
        tabs.addTab("🏷 Marcas",    crearCRUDMarcas());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearCRUDClientes() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(GRIS_FONDO);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BLANCO);
        form.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10); g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = new JTextField(8), txtNom = new JTextField(18), txtApe = new JTextField(18), txtCorreo = new JTextField(22);
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"1 - VIP", "2 - Regular", "3 - Nuevo", "4 - Corporativo", "5 - Mayorista"});
        cbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        String[] lbs = {"ID:", "Nombre:", "Apellido:", "Correo:", "Tipo Cliente:"};
        JComponent[] fs = {txtId, txtNom, txtApe, txtCorreo, cbTipo};
        for (int i = 0; i < lbs.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.15; form.add(etiqueta(lbs[i]), g);
            g.gridx = 1; g.weightx = 0.85; form.add(fs[i], g);
        }

        JPanel bots = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bots.setBackground(GRIS_FONDO);
        JButton btnI = crearBoton("➕ Insertar", VERDE), btnA = crearBoton("✏ Actualizar", AZUL_CLARO);
        JButton btnE = crearBoton("🗑 Eliminar", ROJO), btnB = crearBoton("🔍 Buscar", NARANJA), btnL = crearBoton("🔄 Limpiar", Color.GRAY);
        bots.add(btnI); bots.add(btnA); bots.add(btnE); bots.add(btnB); bots.add(btnL);

        String[] cols = {"ID", "Nombre", "Apellido", "Correo", "Tipo Cliente"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabla = new JTable(model); estilizarTabla(tabla);
        ajustarAnchos(tabla, new int[]{50, 140, 140, 200, 120});

        JPanel top = new JPanel(new BorderLayout()); top.setBackground(GRIS_FONDO);
        top.add(form, BorderLayout.CENTER); top.add(bots, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        Runnable cargar = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = conn.prepareStatement(
                    "SELECT C.ID_CLIENTE,C.NOMBRE,C.APELLIDO,C.CORREO,TC.NOMBRE FROM CLIENTE C " +
                    "JOIN TIPO_CLIENTE TC ON C.ID_TIPO_CLIENTE=TC.ID_TIPO_CLIENTE ORDER BY C.ID_CLIENTE").executeQuery();
                while (rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            } catch (Exception ex) { if (cargado) mostrarAlerta("Error: " + ex.getMessage()); }
        };

        tabla.getSelectionModel().addListSelectionListener(ev -> {
            int r = tabla.getSelectedRow();
            if (r >= 0) { txtId.setText(model.getValueAt(r,0).toString()); txtNom.setText(model.getValueAt(r,1).toString());
                txtApe.setText(model.getValueAt(r,2).toString()); txtCorreo.setText(model.getValueAt(r,3).toString()); }
        });

        btnI.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Conecte primero."); return; }
            try {
                int tipo = Integer.parseInt(cbTipo.getSelectedItem().toString().split(" - ")[0].trim());
                PreparedStatement ps = conn.prepareStatement("INSERT INTO CLIENTE VALUES(?,?,?,?,?)");
                ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
                ps.setString(2, txtNom.getText().trim()); ps.setString(3, txtApe.getText().trim());
                ps.setString(4, txtCorreo.getText().trim()); ps.setInt(5, tipo);
                ps.executeUpdate(); conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Cliente insertado en Oracle."); cargar.run(); actualizarHash();
            } catch (Exception ex) { mostrarAlerta("Error: " + ex.getMessage()); }
        });

        btnA.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Conecte primero."); return; }
            try {
                int tipo = Integer.parseInt(cbTipo.getSelectedItem().toString().split(" - ")[0].trim());
                PreparedStatement ps = conn.prepareStatement("UPDATE CLIENTE SET NOMBRE=?,APELLIDO=?,CORREO=?,ID_TIPO_CLIENTE=? WHERE ID_CLIENTE=?");
                ps.setString(1, txtNom.getText().trim()); ps.setString(2, txtApe.getText().trim());
                ps.setString(3, txtCorreo.getText().trim()); ps.setInt(4, tipo); ps.setInt(5, Integer.parseInt(txtId.getText().trim()));
                ps.executeUpdate(); conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Cliente actualizado en Oracle."); cargar.run(); actualizarHash();
            } catch (Exception ex) { mostrarAlerta("Error: " + ex.getMessage()); }
        });

        btnE.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Conecte primero."); return; }
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar cliente ID " + txtId.getText() + "?\nEsto eliminará sus facturas y detalles.", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                conn.prepareStatement("DELETE FROM DETALLE WHERE ID_FACTURA IN (SELECT ID_FACTURA FROM FACTURA WHERE ID_CLIENTE=" + id + ")").executeUpdate();
                conn.prepareStatement("DELETE FROM FACTURA WHERE ID_CLIENTE=" + id).executeUpdate();
                conn.prepareStatement("DELETE FROM CLIENTE WHERE ID_CLIENTE=" + id).executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Cliente eliminado de Oracle."); cargar.run(); actualizarHash();
            } catch (Exception ex) { mostrarAlerta("Error: " + ex.getMessage()); }
        });

        btnB.addActionListener(e -> {
            if (!cargado) { mostrarAlerta("Conecte primero."); return; }
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT C.*,TC.NOMBRE FROM CLIENTE C JOIN TIPO_CLIENTE TC ON C.ID_TIPO_CLIENTE=TC.ID_TIPO_CLIENTE WHERE C.ID_CLIENTE=?");
                ps.setInt(1, Integer.parseInt(txtId.getText().trim())); ResultSet rs = ps.executeQuery();
                if (rs.next()) { txtNom.setText(rs.getString(2)); txtApe.setText(rs.getString(3)); txtCorreo.setText(rs.getString(4));
                    JOptionPane.showMessageDialog(this, "✅ Encontrado: " + rs.getString(2) + " " + rs.getString(3));
                } else mostrarAlerta("Cliente no encontrado.");
            } catch (Exception ex) { mostrarAlerta("Error: " + ex.getMessage()); }
        });

        btnL.addActionListener(e -> { txtId.setText(""); txtNom.setText(""); txtApe.setText(""); txtCorreo.setText(""); });
        tabbedPane.addChangeListener(ce -> { if (tabbedPane.getSelectedIndex() == 3 && cargado) cargar.run(); });
        return panel;
    }

    private JPanel crearCRUDProductos() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(GRIS_FONDO); panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(BLANCO);
        form.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(6,10,6,10); g.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtId=new JTextField(8), txtNom=new JTextField(22), txtPrecio=new JTextField(10), txtStock=new JTextField(8), txtMarca=new JTextField(8);
        String[] lbs={"ID:","Nombre:","Precio (Q):","Stock:","ID Marca:"};
        JTextField[] fs={txtId,txtNom,txtPrecio,txtStock,txtMarca};
        for(int i=0;i<lbs.length;i++){ g.gridx=0;g.gridy=i;g.weightx=0.15;form.add(etiqueta(lbs[i]),g);g.gridx=1;g.weightx=0.85;form.add(fs[i],g); }
        JPanel bots=new JPanel(new FlowLayout(FlowLayout.CENTER,10,8)); bots.setBackground(GRIS_FONDO);
        JButton btnI=crearBoton("➕ Insertar",VERDE),btnA=crearBoton("✏ Actualizar",AZUL_CLARO),btnE=crearBoton("🗑 Eliminar",ROJO),btnB=crearBoton("🔍 Buscar",NARANJA);
        bots.add(btnI); bots.add(btnA); bots.add(btnE); bots.add(btnB);
        DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Nombre","Precio (Q)","Stock","Marca"},0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable tabla=new JTable(model); estilizarTabla(tabla); ajustarAnchos(tabla,new int[]{50,220,90,70,150});
        JPanel top=new JPanel(new BorderLayout()); top.setBackground(GRIS_FONDO); top.add(form,BorderLayout.CENTER); top.add(bots,BorderLayout.SOUTH);
        panel.add(top,BorderLayout.NORTH); panel.add(new JScrollPane(tabla),BorderLayout.CENTER);
        Runnable cargar=()->{
            model.setRowCount(0);
            try { ResultSet rs=conn.prepareStatement("SELECT P.ID_PRODUCTO,P.NOMBRE,P.PRECIO,P.STOCK,M.NOMBRE FROM PRODUCTO P JOIN MARCA M ON P.ID_MARCA=M.ID_MARCA ORDER BY P.ID_PRODUCTO").executeQuery();
                while(rs.next()) model.addRow(new Object[]{rs.getInt(1),rs.getString(2)+" (TH)",rs.getDouble(3),rs.getInt(4),rs.getString(5)+" (TH)"});
            } catch(Exception ex){ if(cargado) mostrarAlerta("Error: "+ex.getMessage()); }
        };
        tabla.getSelectionModel().addListSelectionListener(ev->{ int r=tabla.getSelectedRow();
            if(r>=0){ txtId.setText(model.getValueAt(r,0).toString()); txtNom.setText(model.getValueAt(r,1).toString().replace(" (TH)",""));
                txtPrecio.setText(model.getValueAt(r,2).toString()); txtStock.setText(model.getValueAt(r,3).toString()); }});
        btnI.addActionListener(e->{
            if(!cargado){mostrarAlerta("Conecte primero.");return;}
            try { PreparedStatement ps=conn.prepareStatement("INSERT INTO PRODUCTO VALUES(?,?,?,?,?)");
                ps.setInt(1,Integer.parseInt(txtId.getText().trim())); ps.setString(2,txtNom.getText().trim());
                ps.setDouble(3,Double.parseDouble(txtPrecio.getText().trim())); ps.setInt(4,Integer.parseInt(txtStock.getText().trim())); ps.setInt(5,Integer.parseInt(txtMarca.getText().trim()));
                ps.executeUpdate(); conn.commit(); JOptionPane.showMessageDialog(this,"✅ Producto insertado en Oracle y Hash."); cargar.run(); actualizarHash();
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }
        });
        btnA.addActionListener(e->{
            if(!cargado){mostrarAlerta("Conecte primero.");return;}
            try { PreparedStatement ps=conn.prepareStatement("UPDATE PRODUCTO SET NOMBRE=?,PRECIO=?,STOCK=?,ID_MARCA=? WHERE ID_PRODUCTO=?");
                ps.setString(1,txtNom.getText().trim()); ps.setDouble(2,Double.parseDouble(txtPrecio.getText().trim()));
                ps.setInt(3,Integer.parseInt(txtStock.getText().trim())); ps.setInt(4,Integer.parseInt(txtMarca.getText().trim())); ps.setInt(5,Integer.parseInt(txtId.getText().trim()));
                ps.executeUpdate(); conn.commit(); JOptionPane.showMessageDialog(this,"✅ Producto actualizado en Oracle y Hash."); cargar.run(); actualizarHash();
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }
        });
        btnE.addActionListener(e->{
            if(!cargado){mostrarAlerta("Conecte primero.");return;}
            if(JOptionPane.showConfirmDialog(this,"¿Eliminar producto ID "+txtId.getText()+"?","Confirmar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { conn.prepareStatement("DELETE FROM DETALLE WHERE ID_PRODUCTO="+Integer.parseInt(txtId.getText().trim())).executeUpdate();
                conn.prepareStatement("DELETE FROM PRODUCTO WHERE ID_PRODUCTO="+Integer.parseInt(txtId.getText().trim())).executeUpdate();
                conn.commit(); JOptionPane.showMessageDialog(this,"✅ Producto eliminado de Oracle y Hash."); cargar.run(); actualizarHash();
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }
        });
        btnB.addActionListener(e->{
            if(!cargado){mostrarAlerta("Conecte primero.");return;}
            try { PreparedStatement ps=conn.prepareStatement("SELECT P.*,M.NOMBRE FROM PRODUCTO P JOIN MARCA M ON P.ID_MARCA=M.ID_MARCA WHERE P.ID_PRODUCTO=?");
                ps.setInt(1,Integer.parseInt(txtId.getText().trim())); ResultSet rs=ps.executeQuery();
                if(rs.next()){ txtNom.setText(rs.getString(2)); txtPrecio.setText(rs.getString(3)); txtStock.setText(rs.getString(4));
                    JOptionPane.showMessageDialog(this,"✅ Producto: "+rs.getString(2)+" — Marca: "+rs.getString(6));
                } else mostrarAlerta("Producto no encontrado.");
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }
        });
        tabbedPane.addChangeListener(ce->{ if(tabbedPane.getSelectedIndex()==3 && cargado) cargar.run(); });
        return panel;
    }

    private JPanel crearCRUDMarcas() {
        JPanel panel=new JPanel(new BorderLayout(8,8)); panel.setBackground(GRIS_FONDO); panel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(BLANCO); form.setBorder(BorderFactory.createTitledBorder("Datos de Marca"));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(6,10,6,10); g.fill=GridBagConstraints.HORIZONTAL;
        JTextField txtId=new JTextField(8),txtNom=new JTextField(22),txtPais=new JTextField(22);
        String[] lbs={"ID:","Nombre:","País Origen:"}; JTextField[] fs={txtId,txtNom,txtPais};
        for(int i=0;i<lbs.length;i++){ g.gridx=0;g.gridy=i;g.weightx=0.15;form.add(etiqueta(lbs[i]),g);g.gridx=1;g.weightx=0.85;form.add(fs[i],g); }
        JPanel bots=new JPanel(new FlowLayout(FlowLayout.CENTER,10,8)); bots.setBackground(GRIS_FONDO);
        JButton btnI=crearBoton("➕ Insertar",VERDE),btnA=crearBoton("✏ Actualizar",AZUL_CLARO),btnE=crearBoton("🗑 Eliminar",ROJO),btnB=crearBoton("🔍 Buscar",NARANJA);
        bots.add(btnI); bots.add(btnA); bots.add(btnE); bots.add(btnB);
        DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Nombre","País Origen"},0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable tabla=new JTable(model); estilizarTabla(tabla); ajustarAnchos(tabla,new int[]{50,220,200});
        JPanel top=new JPanel(new BorderLayout()); top.setBackground(GRIS_FONDO); top.add(form,BorderLayout.CENTER); top.add(bots,BorderLayout.SOUTH);
        panel.add(top,BorderLayout.NORTH); panel.add(new JScrollPane(tabla),BorderLayout.CENTER);
        Runnable cargar=()->{
            model.setRowCount(0);
            try { ResultSet rs=conn.prepareStatement("SELECT * FROM MARCA ORDER BY ID_MARCA").executeQuery();
                while(rs.next()) model.addRow(new Object[]{rs.getInt(1),rs.getString(2)+" (TH)",rs.getString(3)});
            } catch(Exception ex){ if(cargado) mostrarAlerta("Error: "+ex.getMessage()); }
        };
        tabla.getSelectionModel().addListSelectionListener(ev->{ int r=tabla.getSelectedRow();
            if(r>=0){ txtId.setText(model.getValueAt(r,0).toString()); txtNom.setText(model.getValueAt(r,1).toString().replace(" (TH)",""));  txtPais.setText(model.getValueAt(r,2).toString()); }});
        btnI.addActionListener(e->{ if(!cargado){mostrarAlerta("Conecte primero.");return;}
            try { PreparedStatement ps=conn.prepareStatement("INSERT INTO MARCA VALUES(?,?,?)");
                ps.setInt(1,Integer.parseInt(txtId.getText().trim())); ps.setString(2,txtNom.getText().trim()); ps.setString(3,txtPais.getText().trim());
                ps.executeUpdate(); conn.commit(); JOptionPane.showMessageDialog(this,"✅ Marca insertada en Oracle y Hash."); cargar.run(); actualizarHash();
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }});
        btnA.addActionListener(e->{ if(!cargado){mostrarAlerta("Conecte primero.");return;}
            try { PreparedStatement ps=conn.prepareStatement("UPDATE MARCA SET NOMBRE=?,PAIS_ORIGEN=? WHERE ID_MARCA=?");
                ps.setString(1,txtNom.getText().trim()); ps.setString(2,txtPais.getText().trim()); ps.setInt(3,Integer.parseInt(txtId.getText().trim()));
                ps.executeUpdate(); conn.commit(); JOptionPane.showMessageDialog(this,"✅ Marca actualizada."); cargar.run(); actualizarHash();
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }});
        btnE.addActionListener(e->{ if(!cargado){mostrarAlerta("Conecte primero.");return;}
            if(JOptionPane.showConfirmDialog(this,"¿Eliminar marca "+txtId.getText()+"?","Confirmar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { conn.prepareStatement("DELETE FROM MARCA WHERE ID_MARCA="+Integer.parseInt(txtId.getText().trim())).executeUpdate();
                conn.commit(); JOptionPane.showMessageDialog(this,"✅ Marca eliminada."); cargar.run(); actualizarHash();
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }});
        btnB.addActionListener(e->{ if(!cargado){mostrarAlerta("Conecte primero.");return;}
            try { PreparedStatement ps=conn.prepareStatement("SELECT * FROM MARCA WHERE ID_MARCA=?");
                ps.setInt(1,Integer.parseInt(txtId.getText().trim())); ResultSet rs=ps.executeQuery();
                if(rs.next()){ txtNom.setText(rs.getString(2)); txtPais.setText(rs.getString(3)); JOptionPane.showMessageDialog(this,"✅ Marca: "+rs.getString(2));
                } else mostrarAlerta("Marca no encontrada.");
            } catch(Exception ex){ mostrarAlerta("Error: "+ex.getMessage()); }});
        tabbedPane.addChangeListener(ce->{ if(tabbedPane.getSelectedIndex()==3 && cargado) cargar.run(); });
        return panel;
    }

    // ==================== REPORTES ====================
    private JPanel crearPanelReportes() {
        JPanel panel=new JPanel(new GridBagLayout()); panel.setBackground(GRIS_FONDO); panel.setBorder(new EmptyBorder(20,30,20,30));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(10,10,10,10); g.fill=GridBagConstraints.HORIZONTAL;
        JLabel tit=new JLabel("Generación de Reportes (.docx)"); tit.setFont(new Font("Segoe UI",Font.BOLD,16)); tit.setForeground(AZUL_UMG);
        g.gridx=0;g.gridy=0;g.gridwidth=2; panel.add(tit,g); g.gridwidth=1;
        g.gridx=0;g.gridy=1; panel.add(crearTarjetaReporte("Reporte 4.1","Productos en Tabla Hash","Productos con clave hash, posición y tiempo de búsqueda.",AZUL_CLARO,e->{
            if(!cargado){mostrarAlerta("Cargue primero.");return;}
            new GeneradorReporte().generarReporteProductosHash(hashProductos);
            JOptionPane.showMessageDialog(this,"✅ reporte_productos_hash.docx generado.");
        }),g);
        g.gridx=1;g.gridy=1; panel.add(crearTarjetaReporte("Reporte 4.2","Productos y Marca (TH)","Relación Producto-Marca con tiempo de búsqueda en Hash.",VERDE,e->{
            if(!cargado){mostrarAlerta("Cargue primero.");return;}
            new GeneradorReporte().generarReporteProductosMarca(hashProductos,hashMarcas);
            JOptionPane.showMessageDialog(this,"✅ reporte_productos_marca.docx generado.");
        }),g);
        g.gridx=0;g.gridy=2;g.gridwidth=2;
        JPanel r3=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); r3.setBackground(GRIS_FONDO);
        r3.add(etiqueta("ID Cliente para Reporte 4.3:"));
        JTextField txtIdR=new JTextField(6); r3.add(txtIdR);
        JButton btnR3=crearBoton("Generar Reporte 4.3 — Grafo Cliente",NARANJA); r3.add(btnR3);
        panel.add(r3,g);
        btnR3.addActionListener(e->{ if(!cargado){mostrarAlerta("Cargue primero.");return;}
            try { int id=Integer.parseInt(txtIdR.getText().trim());
                new GeneradorReporte().generarReporteGrafoCliente(grafo,id,new int[]{2024,2025,2026});
                JOptionPane.showMessageDialog(this,"✅ reporte_grafo_cliente_"+id+".docx generado.");
            } catch(NumberFormatException ex){ mostrarAlerta("ID inválido."); }
        });
        return panel;
    }

    private JPanel crearTarjetaReporte(String tit,String sub,String desc,Color c,ActionListener al){
        JPanel card=new JPanel(new BorderLayout(6,6)); card.setBackground(BLANCO); card.setPreferredSize(new Dimension(320,150));
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(c,2,true),new EmptyBorder(12,12,12,12)));
        JLabel lt=new JLabel(tit); lt.setFont(new Font("Segoe UI",Font.BOLD,13)); lt.setForeground(c);
        JLabel ls=new JLabel(sub); ls.setFont(new Font("Segoe UI",Font.BOLD,11));
        JLabel ld=new JLabel("<html>"+desc+"</html>"); ld.setFont(new Font("Segoe UI",Font.PLAIN,11));
        JButton btn=crearBoton("Generar",c); btn.addActionListener(al);
        JPanel txt=new JPanel(new GridLayout(3,1,3,3)); txt.setOpaque(false); txt.add(lt); txt.add(ls); txt.add(ld);
        card.add(txt,BorderLayout.CENTER); card.add(btn,BorderLayout.SOUTH);
        return card;
    }

    // ==================== CONEXION ====================
    private void mostrarDialogoConexion(){
        JDialog dlg=new JDialog(this,"Conexión a Oracle",true); dlg.setSize(390,310); dlg.setLocationRelativeTo(this); dlg.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,12,7,12); g.fill=GridBagConstraints.HORIZONTAL;
        JTextField tH=new JTextField("localhost",15),tP=new JTextField("1521",15),tS=new JTextField("XE",15),tU=new JTextField("system",15);
        JPasswordField tPw=new JPasswordField(15);
        String[] lbs={"Host:","Puerto:","SID:","Usuario:","Contraseña:"}; JComponent[] fs={tH,tP,tS,tU,tPw};
        for(int i=0;i<lbs.length;i++){ g.gridx=0;g.gridy=i;g.weightx=0.3;dlg.add(new JLabel(lbs[i]),g);g.gridx=1;g.weightx=0.7;dlg.add(fs[i],g); }
        JButton btnOk=crearBoton("Conectar",VERDE); g.gridx=0;g.gridy=5;g.gridwidth=2;dlg.add(btnOk,g);
        btnOk.addActionListener(e->{
            try { conexion=new ConexionOracle(tH.getText(),tP.getText(),tS.getText(),tU.getText(),new String(tPw.getPassword()));
                conn=conexion.getConexion(); conn.setAutoCommit(false);
                lblEstado.setText("🟢 Conectado: "+tH.getText()+":"+tP.getText()+"/"+tS.getText());
                lblEstado.setForeground(VERDE); dlg.dispose();
                JOptionPane.showMessageDialog(this,"✅ Conexión exitosa. Haga clic en 'Cargar Datos'.");
            } catch(SQLException ex){ JOptionPane.showMessageDialog(dlg,"❌ "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
        });
        dlg.setVisible(true);
    }

    private void cargarDatos(){
        if(conn==null){mostrarAlerta("Conéctese primero.");return;}
        SwingWorker<Void,String> w=new SwingWorker<>(){
            protected Void doInBackground() throws Exception {
                publish("⏳ Cargando Tablas Hash (Producto, Marca, TipoCliente)...");
                hashProductos=new TablaHash<>(); hashMarcas=new TablaHash<>(); hashTipoCliente=new TablaHash<>();
                conexion.cargarHashDesdeBD(hashProductos,hashMarcas,hashTipoCliente);
                publish("⏳ Construyendo Grafo desde Oracle...");
                grafo=new Grafo(); grafo.cargarDesdeOracle(conn);
                publish("⏳ Cargando lista de clientes...");
                cargarComboClientes();
                return null;
            }
            protected void process(List<String> c){ lblEstado.setText(c.get(c.size()-1)); }
            protected void done(){
                try{ get(); cargado=true;
                    lblEstado.setText("🟢 Hash listo ("+hashProductos.getTamano()+" buckets) | Grafo listo | "+hashProductos.getColisiones()+" colisiones en productos");
                    lblEstado.setForeground(VERDE);
                } catch(Exception ex){ lblEstado.setText("❌ "+ex.getMessage()); lblEstado.setForeground(ROJO); }
            }
        };
        w.execute();
    }

    private void actualizarHash(){
        try {
            hashProductos=new TablaHash<>(); hashMarcas=new TablaHash<>(); hashTipoCliente=new TablaHash<>();
            conexion.cargarHashDesdeBD(hashProductos,hashMarcas,hashTipoCliente);
            grafo=new Grafo(); grafo.cargarDesdeOracle(conn);
            cargarComboClientes();
        } catch(Exception ex){ mostrarAlerta("Error actualizando Hash/Grafo: "+ex.getMessage()); }
    }

    private void cargarComboClientes() throws SQLException {
        cbClientes.removeAllItems();
        ResultSet rs=conn.prepareStatement("SELECT ID_CLIENTE,NOMBRE||' '||APELLIDO FROM CLIENTE ORDER BY ID_CLIENTE").executeQuery();
        while(rs.next()) cbClientes.addItem(rs.getInt(1)+" - "+rs.getString(2));
    }

    // ==================== UTILIDADES ====================
    private JButton crearBoton(String t,Color c){
        JButton b=new JButton(t); b.setBackground(c); b.setForeground(BLANCO);
        b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setBorder(new EmptyBorder(8,14,8,14));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setOpaque(true);
        return b;
    }
    private JLabel etiqueta(String t){
        JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,12)); return l;
    }
    private void estilizarTabla(JTable t){
        t.setFont(new Font("Segoe UI",Font.PLAIN,13)); t.setRowHeight(28);
        t.setSelectionBackground(new Color(200,220,255)); t.setGridColor(new Color(210,210,220));
        t.setShowGrid(true); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Renderer personalizado para header — garantiza texto blanco sobre azul oscuro
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(AZUL_UMG);
                lbl.setForeground(BLANCO);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,0,1,new Color(80,120,180)),
                    BorderFactory.createEmptyBorder(4,6,4,6)));
                lbl.setOpaque(true);
                return lbl;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        t.getTableHeader().setPreferredSize(new Dimension(0, 34));
        t.getTableHeader().setReorderingAllowed(false);
        // Filas alternas
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col){
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
                if(!isSelected) c.setBackground(row%2==0 ? BLANCO : new Color(235,240,255));
                return c;
            }
        });
    }
    private void ajustarAnchos(JTable t,int[] anchos){
        for(int i=0;i<anchos.length && i<t.getColumnCount();i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
    }
    private void mostrarAlerta(String m){ JOptionPane.showMessageDialog(this,m,"Aviso",JOptionPane.WARNING_MESSAGE); }
}

class PanelGrafo extends JPanel {
    private List<NodoVisual> nodos=new ArrayList<>();
    private List<AristaVisual> aristas=new ArrayList<>();
    static final Color C_CLI=new Color(0,102,204),C_FAC=new Color(153,0,153),C_DET=new Color(204,102,0),C_PROD=new Color(0,153,76),C_MARCA=new Color(204,0,0);

    public PanelGrafo(){ setBackground(new Color(20,20,35)); setPreferredSize(new Dimension(1400,700)); }

    public void cargarGrafo(Grafo grafo, int idCli, int anioF){
        nodos.clear(); aristas.clear();
        NodoGrafo cli=grafo.getNodo(idCli);
        if(cli==null){repaint();return;}
        Map<Integer,Point> pos=new HashMap<>();
        agregarNodo(cli,60,300,pos);
        List<NodoGrafo> facts=new ArrayList<>();
        for(AristaGrafo a:cli.getAdyacentes()){
            NodoGrafo f=a.getDestino();
            if(!"FACTURA".equals(f.getTipo())) continue;
            if(anioF>0 && !f.getEtiqueta().contains("("+anioF+")")) continue;
            facts.add(f);
        }
        int facY=Math.max(60,300-facts.size()*45);
        for(NodoGrafo fac:facts){
            agregarNodo(fac,260,facY,pos); agregarArista(cli.getId(),fac.getId(),"tiene");
            int detY=facY-15;
            for(AristaGrafo ad:fac.getAdyacentes()){
                NodoGrafo det=ad.getDestino(); if(!"DETALLE".equals(det.getTipo())) continue;
                agregarNodo(det,460,detY,pos); agregarArista(fac.getId(),det.getId(),"");
                for(AristaGrafo ap:det.getAdyacentes()){
                    NodoGrafo prod=ap.getDestino(); if(!"PRODUCTO".equals(prod.getTipo())) continue;
                    if(!pos.containsKey(prod.getId())) agregarNodo(prod,660,detY,pos);
                    agregarArista(det.getId(),prod.getId(),"");
                    for(AristaGrafo am:prod.getAdyacentes()){
                        NodoGrafo marca=am.getDestino(); if(!"MARCA".equals(marca.getTipo())) continue;
                        if(!pos.containsKey(marca.getId())) agregarNodo(marca,870,detY,pos);
                        agregarArista(prod.getId(),marca.getId(),"(TH)");
                    }
                }
                detY+=55;
            }
            facY+=Math.max(90,detY-facY+30);
        }
        int maxY=nodos.stream().mapToInt(n->n.y).max().orElse(600)+80;
        setPreferredSize(new Dimension(1100,Math.max(600,maxY)));
        revalidate(); repaint();
    }

    private void agregarNodo(NodoGrafo n,int x,int y,Map<Integer,Point> p){p.put(n.getId(),new Point(x,y));nodos.add(new NodoVisual(n,x,y));}
    private void agregarArista(int d,int h,String e){aristas.add(new AristaVisual(d,h,e));}

    @Override protected void paintComponent(Graphics g){
        super.paintComponent(g); Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Map<Integer,Point> pos=new HashMap<>();
        for(NodoVisual n:nodos) pos.put(n.nodo.getId(),new Point(n.x,n.y));
        g2.setStroke(new BasicStroke(1.8f));
        for(AristaVisual a:aristas){
            Point p1=pos.get(a.desde),p2=pos.get(a.hasta); if(p1==null||p2==null) continue;
            g2.setColor(new Color(150,150,180)); g2.drawLine(p1.x+128,p1.y+15,p2.x,p2.y+15);
            dibujarFlecha(g2,p1.x+128,p1.y+15,p2.x,p2.y+15);
            if(!a.etiqueta.isEmpty()){ g2.setColor(new Color(220,220,100)); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                g2.drawString(a.etiqueta,(p1.x+128+p2.x)/2,(p1.y+p2.y)/2+10); }
        }
        for(NodoVisual n:nodos){
            Color c=color(n.nodo.getTipo()); g2.setColor(c); g2.fillRoundRect(n.x,n.y,128,30,10,10);
            g2.setColor(c.brighter()); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(n.x,n.y,128,30,10,10);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,10));
            String e=n.nodo.getEtiqueta(); if(e.length()>17) e=e.substring(0,16)+"…";
            g2.drawString("["+n.nodo.getTipo().charAt(0)+"] "+e,n.x+5,n.y+20);
        }
    }
    private void dibujarFlecha(Graphics2D g2,int x1,int y1,int x2,int y2){
        double a=Math.atan2(y2-y1,x2-x1); int l=8;
        g2.fillPolygon(new int[]{x2,(int)(x2-l*Math.cos(a-0.4)),(int)(x2-l*Math.cos(a+0.4))},
                       new int[]{y2,(int)(y2-l*Math.sin(a-0.4)),(int)(y2-l*Math.sin(a+0.4))},3);
    }
    private Color color(String t){ switch(t){ case "CLIENTE":return C_CLI; case "FACTURA":return C_FAC; case "DETALLE":return C_DET; case "PRODUCTO":return C_PROD; case "MARCA":return C_MARCA; default:return Color.GRAY; } }
    static class NodoVisual{NodoGrafo nodo;int x,y;NodoVisual(NodoGrafo n,int x,int y){this.nodo=n;this.x=x;this.y=y;}}
    static class AristaVisual{int desde,hasta;String etiqueta;AristaVisual(int d,int h,String e){desde=d;hasta=h;etiqueta=e;}}
}
