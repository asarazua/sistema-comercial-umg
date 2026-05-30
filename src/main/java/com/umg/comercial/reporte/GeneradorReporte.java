package com.umg.comercial.reporte;

import com.umg.comercial.grafo.Grafo;
import com.umg.comercial.grafo.NodoGrafo;
import com.umg.comercial.grafo.AristaGrafo;
import com.umg.comercial.hash.NodoHash;
import com.umg.comercial.hash.TablaHash;
import com.umg.comercial.modelo.Marca;
import com.umg.comercial.modelo.Producto;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Genera reportes .docx usando Apache POI.
 * Reporte 4.1: Productos hash | 4.2: Productos y Marca | 4.3: Grafo por cliente
 */
public class GeneradorReporte {

    private static final String FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

    /** Agrega un parrafo con texto y negrita opcional */
    private XWPFParagraph agregarParrafo(XWPFDocument doc, String texto, boolean negrita, int tamano) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(texto);
        run.setBold(negrita);
        run.setFontSize(tamano);
        return p;
    }

    /** Agrega encabezado de tabla con fondo azul */
    private void agregarEncabezadoTabla(XWPFTable tabla, String... columnas) {
        XWPFTableRow fila = tabla.getRow(0);
        if (fila == null) fila = tabla.createRow();
        for (int i = 0; i < columnas.length; i++) {
            XWPFTableCell celda = (i < fila.getTableCells().size()) ? fila.getCell(i) : fila.addNewTableCell();
            celda.setText(columnas[i]);
            XWPFRun run = celda.getParagraphs().get(0).createRun();
            run.setText("");
            run.setBold(true);
            celda.getParagraphs().get(0).getRuns().get(0).setBold(true);
            celda.getParagraphs().get(0).getRuns().get(0).setText(columnas[i]);
        }
    }

    /** Agrega una fila de datos a la tabla */
    private void agregarFilaTabla(XWPFTable tabla, String... valores) {
        XWPFTableRow fila = tabla.createRow();
        for (int i = 0; i < valores.length; i++) {
            XWPFTableCell celda = (i < fila.getTableCells().size()) ? fila.getCell(i) : fila.addNewTableCell();
            celda.setText(valores[i]);
        }
    }

    /**
     * Reporte 4.1 - Lista de productos con clave hash, posicion y tiempo de busqueda.
     * @param hashProductos tabla hash con los productos cargados
     */
    public void generarReporteProductosHash(TablaHash<Integer, Producto> hashProductos) {
        String archivo = "reporte_productos_hash.docx";
        try (XWPFDocument doc = new XWPFDocument()) {
            agregarParrafo(doc, "UNIVERSIDAD MARIANO GALVEZ - UMG", true, 14);
            agregarParrafo(doc, "Reporte 4.1: Productos Registrados en el Sistema", true, 13);
            agregarParrafo(doc, "Fecha: " + FECHA, false, 11);
            agregarParrafo(doc, "Tabla Hash - Tamano: " + hashProductos.getTamano()
                + " | Colisiones: " + hashProductos.getColisiones(), false, 11);
            doc.createParagraph();

            XWPFTable tabla = doc.createTable(1, 5);
            agregarEncabezadoTabla(tabla,
                "ID Producto", "Nombre", "Clave Hash Calculada", "Posicion en Tabla", "Tiempo Busqueda (ns)");

            NodoHash<Integer, Producto>[] arr = hashProductos.getTabla();
            for (NodoHash<Integer, Producto> nodo : arr) {
                NodoHash<Integer, Producto> actual = nodo;
                while (actual != null) {
                    Producto p = actual.getValor();
                    long inicio = System.nanoTime();
                    hashProductos.buscar(p.getIdProducto());
                    long tiempo = System.nanoTime() - inicio;
                    int posicion = hashProductos.getPosicion(p.getIdProducto());
                    String claveHash = calcularHashStr(
    String.valueOf(p.getIdProducto())
);
                    agregarFilaTabla(tabla,
                        String.valueOf(p.getIdProducto()),
                        p.getNombre(),
                        claveHash,
                        String.valueOf(posicion),
                        String.valueOf(tiempo));
                    actual = actual.getSiguiente();
                }
            }

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                doc.write(fos);
            }
            System.out.println("[OK] Reporte generado: " + archivo);
        } catch (IOException e) {
            System.err.println("[ERROR] Al generar reporte 4.1: " + e.getMessage());
        }
    }

    /**
     * Reporte 4.2 - Relacion Producto-Marca con tiempo de busqueda en hash.
     * @param hashProductos tabla hash de productos
     * @param hashMarcas    tabla hash de marcas
     */
    public void generarReporteProductosMarca(TablaHash<Integer, Producto> hashProductos,
                                              TablaHash<Integer, Marca> hashMarcas) {
        String archivo = "reporte_productos_marca.docx";
        try (XWPFDocument doc = new XWPFDocument()) {
            agregarParrafo(doc, "UNIVERSIDAD MARIANO GALVEZ - UMG", true, 14);
            agregarParrafo(doc, "Reporte 4.2: Productos y su Marca", true, 13);
            agregarParrafo(doc, "Fecha: " + FECHA, false, 11);
            doc.createParagraph();

            XWPFTable tabla = doc.createTable(1, 4);
            agregarEncabezadoTabla(tabla,
                "ID Producto", "Nombre Producto", "Marca", "Tiempo Busqueda Marca (ns)");

            NodoHash<Integer, Producto>[] arr = hashProductos.getTabla();
            for (NodoHash<Integer, Producto> nodo : arr) {
                NodoHash<Integer, Producto> actual = nodo;
                while (actual != null) {
                    Producto p = actual.getValor();
                    long inicio = System.nanoTime();
                    Marca m = hashMarcas.buscar(p.getIdMarca());
                    long tiempo = System.nanoTime() - inicio;
                    String nombreMarca = (m != null) ? m.getNombre() : "N/A";
                    agregarFilaTabla(tabla,
                        String.valueOf(p.getIdProducto()),
                        p.getNombre(),
                        nombreMarca,
                        String.valueOf(tiempo));
                    actual = actual.getSiguiente();
                }
            }

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                doc.write(fos);
            }
            System.out.println("[OK] Reporte generado: " + archivo);
        } catch (IOException e) {
            System.err.println("[ERROR] Al generar reporte 4.2: " + e.getMessage());
        }
    }

    /**
     * Reporte 4.3 - Grafo Cliente-Facturas-Productos por cliente y anos.
     * @param grafo     grafo cargado desde Oracle
     * @param idCliente id del cliente a consultar
     * @param anios     arreglo de anos a incluir (ej. {2024,2025,2026})
     */
    public void generarReporteGrafoCliente(Grafo grafo, int idCliente, int[] anios) {
        String archivo = "reporte_grafo_cliente_" + idCliente + ".docx";
        try (XWPFDocument doc = new XWPFDocument()) {
            NodoGrafo cliente = grafo.getNodo(idCliente);
            String nombreCliente = (cliente != null) ? cliente.getEtiqueta() : "ID " + idCliente;

            agregarParrafo(doc, "UNIVERSIDAD MARIANO GALVEZ - UMG", true, 14);
            agregarParrafo(doc, "Reporte 4.3: Grafo Cliente - Facturas - Productos", true, 13);
            agregarParrafo(doc, "Fecha: " + FECHA, false, 11);
            agregarParrafo(doc, "Cliente: " + nombreCliente + " (ID: " + idCliente + ")", true, 12);
            doc.createParagraph();

            for (int anio : anios) {
                agregarParrafo(doc, "Ano: " + anio, true, 12);

                XWPFTable tabla = doc.createTable(1, 4);
                agregarEncabezadoTabla(tabla, "N Factura", "Total (Q)", "Detalle", "Producto");

                if (cliente != null) {
                    for (AristaGrafo aFac : cliente.getAdyacentes()) {
                        NodoGrafo fac = aFac.getDestino();
                        if (!"FACTURA".equals(fac.getTipo())) continue;
                        if (!fac.getEtiqueta().contains("(" + anio + ")")) continue;

                        for (AristaGrafo aDet : fac.getAdyacentes()) {
                            NodoGrafo det = aDet.getDestino();
                            if (!"DETALLE".equals(det.getTipo())) continue;
                            for (AristaGrafo aProd : det.getAdyacentes()) {
                                NodoGrafo prod = aProd.getDestino();
                                if (!"PRODUCTO".equals(prod.getTipo())) continue;
                                agregarFilaTabla(tabla,
                                    fac.getEtiqueta(),
                                    String.valueOf(aFac.getPeso()),
                                    det.getEtiqueta(),
                                    prod.getEtiqueta());
                            }
                        }
                    }
                }
                doc.createParagraph();
            }

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                doc.write(fos);
            }
            System.out.println("[OK] Reporte generado: " + archivo);
        } catch (IOException e) {
            System.err.println("[ERROR] Al generar reporte 4.3: " + e.getMessage());
        }
    }

    /** Calcula la clave hash como suma ASCII modulo 101 */
    private String calcularHashStr(String clave) {
        int suma = 0;
        for (char c : clave.toCharArray()) suma += (int) c;
        return String.valueOf(Math.abs(suma % 101));
    }
}
