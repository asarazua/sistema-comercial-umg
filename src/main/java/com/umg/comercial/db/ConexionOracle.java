package com.umg.comercial.db;

import com.umg.comercial.hash.TablaHash;
import com.umg.comercial.modelo.Marca;
import com.umg.comercial.modelo.Producto;
import com.umg.comercial.modelo.TipoCliente;

import java.sql.*;

/**
 * Clase que gestiona la conexion JDBC a Oracle Database.
 * Permite cargar los catalogos PRODUCTO, MARCA y TIPO_CLIENTE en tablas hash.
 */
public class ConexionOracle {

    private String host;
    private String port;
    private String sid;
    private String usuario;
    private String password;
    private Connection conexion;

    /**
     * Constructor con parametros de conexion.
     * @param host     servidor Oracle (ej. localhost)
     * @param port     puerto (ej. 1521)
     * @param sid      SID de la base de datos (ej. XE)
     * @param usuario  usuario Oracle
     * @param password contrasena Oracle
     */
    public ConexionOracle(String host, String port, String sid, String usuario, String password) {
        this.host     = host;
        this.port     = port;
        this.sid      = sid;
        this.usuario  = usuario;
        this.password = password;
    }

    /**
     * Establece la conexion JDBC con Oracle.
     * @return Connection activa
     * @throws SQLException si falla la conexion
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver Oracle no encontrado: " + e.getMessage());
            }
            conexion = DriverManager.getConnection(url, usuario, password);
            System.out.println("[OK] Conexion a Oracle establecida: " + url);
        }
        return conexion;
    }

    /**
     * Carga los catalogos PRODUCTO, MARCA y TIPO_CLIENTE desde Oracle hacia las tablas hash.
     * Muestra el tiempo de carga y numero de colisiones de cada catalogo.
     * @param hashProductos   tabla hash destino para productos
     * @param hashMarcas      tabla hash destino para marcas
     * @param hashTipoCliente tabla hash destino para tipos de cliente
     */
    public void cargarHashDesdeBD(TablaHash<Integer, Producto> hashProductos,
                                   TablaHash<Integer, Marca> hashMarcas,
                                   TablaHash<Integer, TipoCliente> hashTipoCliente) {
        try {
            Connection conn = getConexion();

            // Cargar PRODUCTOS
            System.out.println("\n>> Cargando PRODUCTO a tabla hash...");
            long t1 = System.nanoTime();
            String sqlProd = "SELECT ID_PRODUCTO, NOMBRE, PRECIO, STOCK, ID_MARCA FROM PRODUCTO";
            try (PreparedStatement ps = conn.prepareStatement(sqlProd);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                        rs.getInt("ID_PRODUCTO"), rs.getString("NOMBRE"),
                        rs.getDouble("PRECIO"), rs.getInt("STOCK"), rs.getInt("ID_MARCA")
                    );
                    hashProductos.insertar(p.getIdProducto(), p);
                }
            }
            long t2 = System.nanoTime();
            System.out.println("   Tiempo: " + (t2 - t1) + " ns | Colisiones: " + hashProductos.getColisiones());

            // Cargar MARCAS
            System.out.println(">> Cargando MARCA a tabla hash...");
            t1 = System.nanoTime();
            String sqlMarca = "SELECT ID_MARCA, NOMBRE, PAIS_ORIGEN FROM MARCA";
            try (PreparedStatement ps = conn.prepareStatement(sqlMarca);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Marca m = new Marca(rs.getInt("ID_MARCA"), rs.getString("NOMBRE"), rs.getString("PAIS_ORIGEN"));
                    hashMarcas.insertar(m.getIdMarca(), m);
                }
            }
            t2 = System.nanoTime();
            System.out.println("   Tiempo: " + (t2 - t1) + " ns | Colisiones: " + hashMarcas.getColisiones());

            // Cargar TIPO_CLIENTE
            System.out.println(">> Cargando TIPO_CLIENTE a tabla hash...");
            t1 = System.nanoTime();
            String sqlTipo = "SELECT ID_TIPO_CLIENTE, NOMBRE, DESCRIPCION FROM TIPO_CLIENTE";
            try (PreparedStatement ps = conn.prepareStatement(sqlTipo);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TipoCliente tc = new TipoCliente(rs.getInt("ID_TIPO_CLIENTE"),
                        rs.getString("NOMBRE"), rs.getString("DESCRIPCION"));
                    hashTipoCliente.insertar(tc.getIdTipoCliente(), tc);
                }
            }
            t2 = System.nanoTime();
            System.out.println("   Tiempo: " + (t2 - t1) + " ns | Colisiones: " + hashTipoCliente.getColisiones());
            System.out.println("\n[OK] Catalogos cargados exitosamente en tablas hash.");

        } catch (SQLException e) {
            System.err.println("[ERROR] Al cargar catalogos: " + e.getMessage());
        }
    }

    /**
     * Cierra la conexion a Oracle y libera recursos.
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("[OK] Conexion a Oracle cerrada.");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Al cerrar conexion: " + e.getMessage());
            }
        }
    }
}
