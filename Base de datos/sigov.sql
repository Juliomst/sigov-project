-- Creación de la base de datos
CREATE DATABASE sigovdb;
USE sigovdb;

-- Tabla MESEROS
CREATE TABLE MESEROS (
    id_mesero INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(25) NOT NULL,
    apellido VARCHAR(25) NOT NULL,
    nickname VARCHAR(25) NOT NULL,
    contrasena VARCHAR(25) NOT NULL
);

-- Tabla VENTAS
CREATE TABLE VENTAS (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fechaRegistro DATE NOT NULL,
    monto FLOAT
);

-- Tabla PAGOS
CREATE TABLE PAGOS (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    montoEfectuado FLOAT,
    id_venta INT,
    FOREIGN KEY (id_venta) REFERENCES VENTAS(id_venta)
);

-- Tabla EFECTIVOS
CREATE TABLE EFECTIVOS (
    id_efectivo INT AUTO_INCREMENT PRIMARY KEY,
    divisa ENUM('MXN','USD','EUR'),
    id_pago INT,
    FOREIGN KEY (id_pago) REFERENCES PAGOS(id_pago)
);

-- Tabla TARJETAS
CREATE TABLE TARJETAS (
    id_tarjeta INT AUTO_INCREMENT PRIMARY KEY,
    banca VARCHAR(50) NOT NULL,
    folio VARCHAR(50) NOT NULL,
    id_pago INT,
    FOREIGN KEY (id_pago) REFERENCES PAGOS(id_pago)
);

-- Tabla ORDENES
CREATE TABLE ORDENES (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    fechaRegistro DATE NOT NULL,
    descripcion VARCHAR(140),
    noMesa INT NOT NULL,
    id_mesero INT,
   -- id_venta INT,
    FOREIGN KEY (id_mesero) REFERENCES MESEROS(id_mesero)
   -- FOREIGN KEY (id_venta) REFERENCES VENTAS(id_venta)
);

-- Tabla PRODUCTOS
CREATE TABLE PRODUCTOS (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    precio FLOAT NOT NULL,
    nombre VARCHAR(25) NOT NULL,
    tipoProducto ENUM('Comida', 'Bebida', 'Postre'),
    descripcion VARCHAR(140) NOT NULL
);

-- Tabla ORDEN_PRODUCTO
CREATE TABLE ORDEN_PRODUCTO (
    id_ordenProducto INT AUTO_INCREMENT PRIMARY KEY,
    cantidadProducto INT,
    id_orden INT,
    id_producto INT,
    FOREIGN KEY (id_orden) REFERENCES ORDENES(id_orden),
    FOREIGN KEY (id_producto) REFERENCES PRODUCTOS(id_producto)
);