package com.umg.comercial.modelo;

/**
 * Clase que representa la entidad PRODUCTO de Oracle.
 */
public class Producto {
    private int idProducto;
    private String nombre;
    private double precio;
    private int stock;
    private int idMarca;

    public Producto() {}

    public Producto(int idProducto, String nombre, double precio, int stock, int idMarca) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.idMarca = idMarca;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getIdMarca() { return idMarca; }
    public void setIdMarca(int idMarca) { this.idMarca = idMarca; }

    @Override
    public String toString() {
        return "Producto{id=" + idProducto + ", nombre='" + nombre + "', precio=" + precio + ", idMarca=" + idMarca + "}";
    }
}
