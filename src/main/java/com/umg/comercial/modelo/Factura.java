package com.umg.comercial.modelo;

import java.util.Date;

/**
 * Clase que representa la entidad FACTURA de Oracle.
 */
public class Factura {
    private int idFactura;
    private int idCliente;
    private Date fecha;
    private double total;
    private int anio;

    public Factura() {}

    public Factura(int idFactura, int idCliente, Date fecha, double total, int anio) {
        this.idFactura = idFactura;
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.total = total;
        this.anio = anio;
    }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    @Override
    public String toString() {
        return "Factura{id=" + idFactura + ", idCliente=" + idCliente + ", fecha=" + fecha + ", total=" + total + ", anio=" + anio + "}";
    }
}
