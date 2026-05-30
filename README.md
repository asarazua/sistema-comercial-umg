<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:003366,100:0066CC&height=200&section=header&text=Sistema%20de%20Gesti%C3%B3n%20Comercial&fontSize=36&fontColor=ffffff&animation=fadeIn&fontAlignY=38&desc=Tablas%20Hash%20%2B%20Grafos%20%2B%20Oracle%20Database&descAlignY=55&descSize=18" width="100%"/>

<br/>

![Java](https://img.shields.io/badge/Java-25.0.2-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-21c%20XE-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.16-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.3-217346?style=for-the-badge&logo=microsoftword&logoColor=white)

<br/>

![GitHub repo size](https://img.shields.io/github/repo-size/asarazua/sistema-comercial-umg?style=flat-square&color=0066CC)
![GitHub last commit](https://img.shields.io/github/last-commit/asarazua/sistema-comercial-umg?style=flat-square&color=006633)
![GitHub contributors](https://img.shields.io/github/contributors/asarazua/sistema-comercial-umg?style=flat-square&color=CC6600)

</div>

---

## 🏫 Universidad Mariano Gálvez de Guatemala
**Centro Universitario de Chimaltenango**  
📚 Programación III | Sección B | Ciclo 2026  
👨‍🏫 Catedrático: Pablo Antonio de León Bautista  
📅 Fecha de entrega: 30 de mayo de 2026

---

## 📋 Descripción del Proyecto

> Sistema de gestión comercial desarrollado en **Java** con conexión a **Oracle Database 21c XE**, que implementa dos estructuras de datos avanzadas desde cero:
> - 🔷 **Tabla Hash** con encadenamiento para búsquedas O(1) en catálogos
> - 🔶 **Grafo Dirigido** con lista de adyacencia para representar relaciones de negocio
> - 💻 **Interfaz gráfica Swing** con 5 pestañas funcionales
> - 📄 **Reportes Word** generados con Apache POI

---

## ✨ Funcionalidades

<table>
<tr>
<td width="50%">

### 🔷 Tabla Hash
- Carga catálogos desde Oracle (Producto, Marca, TipoCliente)
- Función hash: `suma(ASCII) % 101`
- Manejo de colisiones por **encadenamiento**
- Búsqueda **O(1)** con tiempo en nanosegundos
- Visualización de posición, clave, valor y colisiones

</td>
<td width="50%">

### 🔶 Grafo Dirigido
- Representa: **Cliente → Factura → Detalle → Producto → Marca**
- Filtro por año: 2024, 2025 y 2026
- Trazabilidad inversa: Producto → Clientes
- Dibujado con colores por tipo de nodo
- Scrollbar para grafos grandes

</td>
</tr>
<tr>
<td width="50%">

### 📄 Facturas y Detalles
- Encabezado con Tipo Cliente (TH)
- Detalle al hacer clic: Producto (TH) + Marca (TH)
- Filtro por cliente y año
- Montos en Quetzales

</td>
<td width="50%">

### ➕ CRUD Completo
- Clientes, Productos y Marcas
- Insertar, Buscar, Actualizar, Eliminar
- Se refleja **inmediatamente** en Oracle
- Actualiza Hash y Grafo automáticamente

</td>
</tr>
</table>

---

## 🗄️ Modelo de Base de Datos

```
TIPO_CLIENTE ──┐
               ├──► CLIENTE ──► FACTURA ──► DETALLE ──► PRODUCTO ──► MARCA
               │                                          (Hash)        (Hash)
               └── (Catálogo Hash)
```

| Tabla | Tipo | Descripción |
|-------|------|-------------|
| `CLIENTE` | Transaccional | Datos del cliente y su clasificación |
| `TIPO_CLIENTE` | **Catálogo Hash** | Clasificación de clientes |
| `FACTURA` | Transaccional | Cabecera de factura por año |
| `DETALLE` | Transaccional | Líneas de cada factura |
| `PRODUCTO` | **Catálogo Hash** | Catálogo de productos |
| `MARCA` | **Catálogo Hash** | Marcas registradas |

---

## 🚀 Instalación y Configuración

### Prerrequisitos
- ☕ Java 11+ (probado con Java 25)
- 🏗️ Apache Maven 3.9+
- 🗄️ Oracle Database 21c XE
- 🔧 Oracle SQL Developer (opcional)

### 1️⃣ Clonar el repositorio
```bash
git clone https://github.com/asarazua/sistema-comercial-umg.git
cd sistema-comercial-umg
```

### 2️⃣ Configurar la base de datos Oracle
```bash
# Conectarse a Oracle
sqlplus system/tucontraseña@localhost:1521/XE

# Ejecutar el script de instalación
@install.sql
```

### 3️⃣ Copiar el driver JDBC de Oracle
```bash
# Windows
copy "C:\app\TU_USUARIO\product\21c\dbhomeXE\jdbc\lib\ojdbc8.jar" "lib\ojdbc8.jar"
```

### 4️⃣ Compilar el proyecto
```bash
mvn clean package
```

### 5️⃣ Ejecutar el sistema
```bash
java -cp "target/sistema-comercial.jar;lib/ojdbc8.jar" com.umg.comercial.Main
```

---

## 📦 Estructura del Proyecto

```
sistema-comercial-umg/
├── 📄 install.sql                          # DDL + datos de prueba + PL/SQL
├── 📄 pom.xml                              # Configuración Maven
├── 📁 lib/
│   └── ojdbc8.jar                          # Driver JDBC Oracle
└── 📁 src/main/java/com/umg/comercial/
    ├── 🚀 Main.java                        # Punto de entrada
    ├── 📁 ui/
    │   └── VentanaPrincipal.java           # Interfaz gráfica Swing
    ├── 📁 hash/
    │   ├── NodoHash.java                   # Nodo genérico lista enlazada
    │   └── TablaHash.java                  # Tabla Hash con encadenamiento
    ├── 📁 grafo/
    │   ├── NodoGrafo.java                  # Nodo del grafo dirigido
    │   ├── AristaGrafo.java                # Arista con peso y etiqueta
    │   └── Grafo.java                      # Lista de adyacencia + consultas
    ├── 📁 modelo/
    │   ├── Cliente.java
    │   ├── Factura.java
    │   ├── Detalle.java
    │   ├── Producto.java
    │   ├── Marca.java
    │   └── TipoCliente.java
    ├── 📁 db/
    │   └── ConexionOracle.java             # JDBC + carga Hash desde BD
    └── 📁 reporte/
        └── GeneradorReporte.java           # Reportes .docx con Apache POI
```

---

## 🧮 Función Hash Implementada

```java
// Suma de códigos ASCII de la clave, módulo 101 (número primo)
private int calcularHash(K clave) {
    String str = clave.toString();
    int suma = 0;
    for (char c : str.toCharArray()) {
        suma += (int) c;
    }
    return Math.abs(suma % tamano); // tamano = 101
}
```

> **¿Por qué 101?** Es un número primo, lo que reduce colisiones al no compartir factores con la mayoría de claves.

---

## 🔗 Grafo Dirigido

```
[CLIENTE] ──tiene──► [FACTURA 2024] ──tiene──► [DETALLE] ──es──► [PRODUCTO] ──(TH)──► [MARCA]
         ──tiene──► [FACTURA 2025]
         ──tiene──► [FACTURA 2026]
```

**IDs únicos por tipo de nodo para evitar colisiones en el Map:**
- Clientes: id original
- Marcas: 10000 + id
- Productos: 20000 + id
- Facturas: 30000 + id
- Detalles: 40000 + id

---

## 📊 Reportes Generados

| Reporte | Archivo | Contenido |
|---------|---------|-----------|
| **4.1** | `reporte_productos_hash.docx` | Productos con clave hash, posición y tiempo de búsqueda |
| **4.2** | `reporte_productos_marca.docx` | Relación Producto-Marca con tiempo Hash |
| **4.3** | `reporte_grafo_cliente_N.docx` | Recorrido del grafo por cliente y año |

---

## 👥 Integrantes del Equipo

<table align="center">
<tr>
<td align="center">
  <img src="https://github.com/asarazua.png" width="80" style="border-radius:50%"/><br/>
  <b>Andrea Sarazua</b><br/>
  <a href="https://github.com/asarazua">@asarazua</a>
</td>
</tr>
</table>

---

## 🤝 Cómo contribuir (integrantes del equipo)

```bash
# 1. Clona el repositorio
git clone https://github.com/asarazua/sistema-comercial-umg.git

# 2. Haz tus cambios

# 3. Confirma y sube
git add .
git commit -m "Descripción de lo que hiciste"
git push origin main
```

---

<div align="center">

**Universidad Mariano Gálvez de Guatemala**  
*Programación III — Sección B — 2026*

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:0066CC,100:003366&height=100&section=footer" width="100%"/>

</div>
