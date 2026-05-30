package com.umg.comercial.modelo;

/**
 * Clase que representa la entidad MARCA de Oracle.
 */
public class Marca {
    private int idMarca;
    private String nombre;
    private String paisOrigen;

    public Marca() {}

    public Marca(int idMarca, String nombre, String paisOrigen) {
        this.idMarca = idMarca;
        this.nombre = nombre;
        this.paisOrigen = paisOrigen;
    }

    public int getIdMarca() { return idMarca; }
    public void setIdMarca(int idMarca) { this.idMarca = idMarca; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }

    @Override
    public String toString() {
        return "Marca{id=" + idMarca + ", nombre='" + nombre + "', pais='" + paisOrigen + "'}";
    }
}
