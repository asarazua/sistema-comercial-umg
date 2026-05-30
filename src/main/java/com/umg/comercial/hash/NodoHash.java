package com.umg.comercial.hash;

/**
 * Nodo generico para la lista enlazada de cada bucket en la TablaHash.
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
public class NodoHash<K, V> {
    private K clave;
    private V valor;
    private NodoHash<K, V> siguiente;

    /**
     * Constructor con clave y valor.
     * @param clave clave del nodo
     * @param valor valor asociado a la clave
     */
    public NodoHash(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
        this.siguiente = null;
    }

    public K getClave() { return clave; }
    public void setClave(K clave) { this.clave = clave; }
    public V getValor() { return valor; }
    public void setValor(V valor) { this.valor = valor; }
    public NodoHash<K, V> getSiguiente() { return siguiente; }
    public void setSiguiente(NodoHash<K, V> siguiente) { this.siguiente = siguiente; }
}
