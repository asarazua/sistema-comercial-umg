package com.umg.comercial.hash;

/**
 * Implementacion propia de una Tabla Hash con encadenamiento.
 * Funcion hash: suma de codigos ASCII del toString() de la clave, modulo tamano.
 * Tamano por defecto: 101 (numero primo).
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
@SuppressWarnings("unchecked")
public class TablaHash<K, V> {

    private static final int TAMANO_DEFAULT = 101;
    private NodoHash<K, V>[] tabla;
    private int tamano;
    private int colisiones;
    private long tiempoUltimaInsercion;
    private long tiempoUltimaBusqueda;

    /**
     * Constructor con tamano por defecto 101.
     */
    public TablaHash() {
        this(TAMANO_DEFAULT);
    }

    /**
     * Constructor con tamano personalizado.
     * @param tamano tamano de la tabla hash
     */
    public TablaHash(int tamano) {
        this.tamano = tamano;
        this.tabla = new NodoHash[tamano];
        this.colisiones = 0;
        this.tiempoUltimaInsercion = 0;
        this.tiempoUltimaBusqueda = 0;
    }

    /**
     * Calcula la posicion hash para una clave dada.
     * Suma los codigos ASCII del toString() de la clave, modulo tamano.
     * @param clave clave a hashear
     * @return posicion en la tabla
     */
    private int calcularHash(K clave) {
        String str = clave.toString();
        int suma = 0;
        for (char c : str.toCharArray()) {
            suma += (int) c;
        }
        return Math.abs(suma % tamano);
    }

    /**
     * Inserta un par clave-valor en la tabla hash.
     * Registra el tiempo de insercion y cuenta colisiones.
     * @param clave clave del elemento
     * @param valor valor asociado
     */
    public void insertar(K clave, V valor) {
        long inicio = System.nanoTime();
        int posicion = calcularHash(clave);

        if (tabla[posicion] != null) {
            colisiones++;
            // Actualizar si la clave ya existe
            NodoHash<K, V> actual = tabla[posicion];
            while (actual != null) {
                if (actual.getClave().equals(clave)) {
                    actual.setValor(valor);
                    tiempoUltimaInsercion = System.nanoTime() - inicio;
                    return;
                }
                actual = actual.getSiguiente();
            }
            // Encadenamiento: insertar al inicio
            NodoHash<K, V> nuevo = new NodoHash<>(clave, valor);
            nuevo.setSiguiente(tabla[posicion]);
            tabla[posicion] = nuevo;
        } else {
            tabla[posicion] = new NodoHash<>(clave, valor);
        }
        tiempoUltimaInsercion = System.nanoTime() - inicio;
    }

    /**
     * Busca un valor por su clave en la tabla hash. O(1) promedio.
     * @param clave clave a buscar
     * @return valor asociado o null si no existe
     */
    public V buscar(K clave) {
        long inicio = System.nanoTime();
        int posicion = calcularHash(clave);
        NodoHash<K, V> actual = tabla[posicion];
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                tiempoUltimaBusqueda = System.nanoTime() - inicio;
                return actual.getValor();
            }
            actual = actual.getSiguiente();
        }
        tiempoUltimaBusqueda = System.nanoTime() - inicio;
        return null;
    }

    /**
     * Elimina un elemento de la tabla hash por su clave.
     * @param clave clave del elemento a eliminar
     * @return true si se elimino, false si no existia
     */
    public boolean eliminar(K clave) {
        int posicion = calcularHash(clave);
        NodoHash<K, V> actual = tabla[posicion];
        NodoHash<K, V> anterior = null;
        while (actual != null) {
            if (actual.getClave().equals(clave)) {
                if (anterior == null) {
                    tabla[posicion] = actual.getSiguiente();
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }
                return true;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return false;
    }

    /**
     * Retorna la posicion hash de una clave (para reportes).
     * @param clave clave a evaluar
     * @return posicion en la tabla
     */
    public int getPosicion(K clave) {
        return calcularHash(clave);
    }

    /**
     * Retorna todos los nodos de la tabla para iteracion en reportes.
     * @return arreglo de NodoHash
     */
    public NodoHash<K, V>[] getTabla() { return tabla; }

    public int getTamano() { return tamano; }
    public int getColisiones() { return colisiones; }
    public long getTiempoUltimaInsercion() { return tiempoUltimaInsercion; }
    public long getTiempoUltimaBusqueda() { return tiempoUltimaBusqueda; }
}
