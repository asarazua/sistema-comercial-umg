package com.umg.comercial.modelo;

/**
 * Clase que representa la entidad DETALLE de Oracle.
 */
public class Detalle {
    private int idDetalle;
    private int idFactura;
    private int idProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public Detalle() {}

    public Detalle(int idDetalle, int idFactura, int idProducto, int cantidad, double precioUnitario, double subtotal) {
        this.idDetalle = idDetalle;
        this.idFactura = idFactura;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return "Detalle{id=" + idDetalle + ", idFactura=" + idFactura + ", idProducto=" + idProducto + ", cantidad=" + cantidad + ", subtotal=" + subtotal + "}";
    }
}
