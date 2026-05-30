package com.umg.comercial.grafo;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del grafo dirigido. Representa una entidad del sistema
 * (CLIENTE, FACTURA, DETALLE, PRODUCTO, MARCA).
 */
public class NodoGrafo {
    private int id;
    private String etiqueta;
    private String tipo;
    private List<AristaGrafo> adyacentes;

    /**
     * Constructor completo.
     * @param id identificador unico del nodo
     * @param etiqueta descripcion visible del nodo
     * @param tipo tipo de entidad: CLIENTE, FACTURA, DETALLE, PRODUCTO, MARCA
     */
    public NodoGrafo(int id, String etiqueta, String tipo) {
        this.id = id;
        this.etiqueta = etiqueta;
        this.tipo = tipo;
        this.adyacentes = new ArrayList<>();
    }

    /** Agrega una arista saliente desde este nodo. */
    public void agregarAdyacente(AristaGrafo arista) {
        adyacentes.add(arista);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public List<AristaGrafo> getAdyacentes() { return adyacentes; }
    public void setAdyacentes(List<AristaGrafo> adyacentes) { this.adyacentes = adyacentes; }

    @Override
    public String toString() {
        return "[" + tipo + "] " + etiqueta + " (id=" + id + ")";
    }
}
