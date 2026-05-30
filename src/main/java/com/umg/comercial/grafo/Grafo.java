package com.umg.comercial.grafo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Grafo dirigido con lista de adyacencia.
 * Representa las relaciones: CLIENTE -> FACTURA -> DETALLE -> PRODUCTO -> MARCA
 */
public class Grafo {

    private Map<Integer, NodoGrafo> nodos;

    /** Constructor que inicializa el mapa de nodos. */
    public Grafo() {
        this.nodos = new HashMap<>();
    }

    /**
     * Agrega un nodo al grafo.
     * @param id identificador unico
     * @param etiqueta descripcion del nodo
     * @param tipo tipo de entidad
     */
    public void agregarNodo(int id, String etiqueta, String tipo) {
        if (!nodos.containsKey(id)) {
            nodos.put(id, new NodoGrafo(id, etiqueta, tipo));
        }
    }

    /**
     * Agrega una arista dirigida entre dos nodos.
     * @param idOrigen nodo origen
     * @param idDestino nodo destino
     * @param peso peso de la arista
     * @param etiqueta descripcion de la relacion
     */
    public void agregarArista(int idOrigen, int idDestino, double peso, String etiqueta) {
        NodoGrafo origen  = nodos.get(idOrigen);
        NodoGrafo destino = nodos.get(idDestino);
        if (origen != null && destino != null) {
            origen.agregarAdyacente(new AristaGrafo(destino, peso, etiqueta));
        }
    }

    /**
     * Retorna un nodo por su id.
     * @param id identificador del nodo
     * @return NodoGrafo o null si no existe
     */
    public NodoGrafo getNodo(int id) {
        return nodos.get(id);
    }

    /**
     * Carga el grafo completo desde Oracle Database.
     * Construye nodos y aristas para CLIENTE->FACTURA->DETALLE->PRODUCTO->MARCA.
     * @param conn conexion activa a Oracle
     * @throws SQLException si ocurre error de BD
     */
    public void cargarDesdeOracle(Connection conn) throws SQLException {
        nodos.clear();

        // Cargar CLIENTES
        String sqlCli = "SELECT ID_CLIENTE, NOMBRE || ' ' || APELLIDO AS NOMBRE FROM CLIENTE";
        try (PreparedStatement ps = conn.prepareStatement(sqlCli);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                agregarNodo(rs.getInt("ID_CLIENTE"), rs.getString("NOMBRE"), "CLIENTE");
            }
        }

        // Cargar MARCAS
        String sqlMarca = "SELECT ID_MARCA, NOMBRE FROM MARCA";
        try (PreparedStatement ps = conn.prepareStatement(sqlMarca);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                agregarNodo(10000 + rs.getInt("ID_MARCA"), rs.getString("NOMBRE"), "MARCA");
            }
        }

        // Cargar PRODUCTOS y arista PRODUCTO->MARCA
        String sqlProd = "SELECT ID_PRODUCTO, NOMBRE, ID_MARCA FROM PRODUCTO";
        try (PreparedStatement ps = conn.prepareStatement(sqlProd);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idProd  = 20000 + rs.getInt("ID_PRODUCTO");
                int idMarca = 10000 + rs.getInt("ID_MARCA");
                agregarNodo(idProd, rs.getString("NOMBRE"), "PRODUCTO");
                agregarArista(idProd, idMarca, 0, "TIENE_MARCA");
            }
        }

        // Cargar FACTURAS y arista CLIENTE->FACTURA
        String sqlFac = "SELECT ID_FACTURA, ID_CLIENTE, TOTAL, ANIO FROM FACTURA";
        try (PreparedStatement ps = conn.prepareStatement(sqlFac);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idFac = 30000 + rs.getInt("ID_FACTURA");
                int idCli = rs.getInt("ID_CLIENTE");
                agregarNodo(idFac, "Factura#" + rs.getInt("ID_FACTURA") + " (" + rs.getInt("ANIO") + ")", "FACTURA");
                agregarArista(idCli, idFac, rs.getDouble("TOTAL"), "TIENE_FACTURA");
            }
        }

        // Cargar DETALLES y aristas FACTURA->DETALLE y DETALLE->PRODUCTO
        String sqlDet = "SELECT ID_DETALLE, ID_FACTURA, ID_PRODUCTO, CANTIDAD, SUBTOTAL FROM DETALLE";
        try (PreparedStatement ps = conn.prepareStatement(sqlDet);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idDet  = 40000 + rs.getInt("ID_DETALLE");
                int idFac  = 30000 + rs.getInt("ID_FACTURA");
                int idProd = 20000 + rs.getInt("ID_PRODUCTO");
                agregarNodo(idDet, "Det#" + rs.getInt("ID_DETALLE") + " x" + rs.getInt("CANTIDAD"), "DETALLE");
                agregarArista(idFac, idDet, rs.getDouble("SUBTOTAL"), "TIENE_DETALLE");
                agregarArista(idDet, idProd, rs.getDouble("SUBTOTAL"), "ES_PRODUCTO");
            }
        }
    }

    /**
     * Retorna la lista de productos comprados por un cliente en un rango de anos.
     * @param idCliente id del cliente
     * @param anioInicio ano de inicio
     * @param anioFin ano de fin
     * @return lista de nombres de productos
     */
    public List<String> productosCompradosPorCliente(int idCliente, int anioInicio, int anioFin) {
        List<String> productos = new ArrayList<>();
        NodoGrafo cliente = nodos.get(idCliente);
        if (cliente == null) return productos;

        for (AristaGrafo aFac : cliente.getAdyacentes()) {
            NodoGrafo factura = aFac.getDestino();
            if (!"FACTURA".equals(factura.getTipo())) continue;
            String etiq = factura.getEtiqueta();
            int anio = extraerAnio(etiq);
            if (anio < anioInicio || anio > anioFin) continue;

            for (AristaGrafo aDet : factura.getAdyacentes()) {
                NodoGrafo detalle = aDet.getDestino();
                if (!"DETALLE".equals(detalle.getTipo())) continue;
                for (AristaGrafo aProd : detalle.getAdyacentes()) {
                    NodoGrafo prod = aProd.getDestino();
                    if ("PRODUCTO".equals(prod.getTipo()) && !productos.contains(prod.getEtiqueta())) {
                        productos.add(prod.getEtiqueta());
                    }
                }
            }
        }
        return productos;
    }

    /**
     * Trazabilidad inversa: dado un producto, retorna todos los clientes que lo compraron.
     * @param idProducto id del producto
     * @return lista de nombres de clientes
     */
    public List<String> clientesQueCompraronProducto(int idProducto) {
        List<String> clientes = new ArrayList<>();
        int idProdNodo = 20000 + idProducto;

        for (NodoGrafo cli : nodos.values()) {
            if (!"CLIENTE".equals(cli.getTipo())) continue;
            for (AristaGrafo aFac : cli.getAdyacentes()) {
                NodoGrafo fac = aFac.getDestino();
                if (!"FACTURA".equals(fac.getTipo())) continue;
                for (AristaGrafo aDet : fac.getAdyacentes()) {
                    NodoGrafo det = aDet.getDestino();
                    if (!"DETALLE".equals(det.getTipo())) continue;
                    for (AristaGrafo aProd : det.getAdyacentes()) {
                        if (aProd.getDestino().getId() == idProdNodo) {
                            if (!clientes.contains(cli.getEtiqueta())) {
                                clientes.add(cli.getEtiqueta());
                            }
                        }
                    }
                }
            }
        }
        return clientes;
    }

    /**
     * Recorre el grafo completo para un cliente: Cliente->Factura->Detalle->Producto->Marca.
     * @param idCliente id del cliente
     * @return representacion textual del recorrido completo
     */
    public String relacionCompleta(int idCliente) {
        StringBuilder sb = new StringBuilder();
        NodoGrafo cliente = nodos.get(idCliente);
        if (cliente == null) return "Cliente no encontrado en el grafo.";

        sb.append("=== RELACION COMPLETA ===\n");
        sb.append("CLIENTE: ").append(cliente.getEtiqueta()).append("\n");

        for (AristaGrafo aFac : cliente.getAdyacentes()) {
            NodoGrafo fac = aFac.getDestino();
            if (!"FACTURA".equals(fac.getTipo())) continue;
            sb.append("  FACTURA: ").append(fac.getEtiqueta())
              .append(" | Total: Q").append(aFac.getPeso()).append("\n");

            for (AristaGrafo aDet : fac.getAdyacentes()) {
                NodoGrafo det = aDet.getDestino();
                if (!"DETALLE".equals(det.getTipo())) continue;
                sb.append("    DETALLE: ").append(det.getEtiqueta())
                  .append(" | Subtotal: Q").append(aDet.getPeso()).append("\n");

                for (AristaGrafo aProd : det.getAdyacentes()) {
                    NodoGrafo prod = aProd.getDestino();
                    if (!"PRODUCTO".equals(prod.getTipo())) continue;
                    sb.append("      PRODUCTO: ").append(prod.getEtiqueta()).append("\n");

                    for (AristaGrafo aMarca : prod.getAdyacentes()) {
                        NodoGrafo marca = aMarca.getDestino();
                        if ("MARCA".equals(marca.getTipo())) {
                            sb.append("        MARCA: ").append(marca.getEtiqueta()).append("\n");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /** Extrae el ano de la etiqueta de una factura. Formato: "Factura#X (YYYY)" */
    private int extraerAnio(String etiqueta) {
        try {
            int inicio = etiqueta.indexOf('(');
            int fin    = etiqueta.indexOf(')');
            if (inicio >= 0 && fin > inicio) {
                return Integer.parseInt(etiqueta.substring(inicio + 1, fin).trim());
            }
        } catch (NumberFormatException e) { /* ignorar */ }
        return 0;
    }

    public Map<Integer, NodoGrafo> getNodos() { return nodos; }
}
