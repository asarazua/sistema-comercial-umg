package com.umg.comercial.grafo;

/**
 * Arista dirigida del grafo. Conecta dos nodos con un peso y etiqueta opcionales.
 */
public class AristaGrafo {
    private NodoGrafo destino;
    private double peso;
    private String etiqueta;

    /**
     * Constructor completo.
     * @param destino nodo destino de la arista
     * @param peso peso de la arista (puede usarse para subtotales)
     * @param etiqueta descripcion de la relacion
     */
    public AristaGrafo(NodoGrafo destino, double peso, String etiqueta) {
        this.destino = destino;
        this.peso = peso;
        this.etiqueta = etiqueta;
    }

    public NodoGrafo getDestino() { return destino; }
    public void setDestino(NodoGrafo destino) { this.destino = destino; }
    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    @Override
    public String toString() {
        return "-> " + destino.toString() + " [" + etiqueta + ", peso=" + peso + "]";
    }
}
