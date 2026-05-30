package com.umg.comercial.modelo;

/**
 * Clase que representa la entidad CLIENTE de Oracle.
 */
public class Cliente {
    private int idCliente;
    private String nombre;
    private String apellido;
    private String correo;
    private int idTipoCliente;

    public Cliente() {}

    public Cliente(int idCliente, String nombre, String apellido, String correo, int idTipoCliente) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.idTipoCliente = idTipoCliente;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public int getIdTipoCliente() { return idTipoCliente; }
    public void setIdTipoCliente(int idTipoCliente) { this.idTipoCliente = idTipoCliente; }

    @Override
    public String toString() {
        return "Cliente{id=" + idCliente + ", nombre='" + nombre + " " + apellido + "', correo='" + correo + "'}";
    }
}
