-- ============================================================
--  Sistema de Inventario - Script de base de datos (MySQL 8+)
-- ============================================================
CREATE DATABASE IF NOT EXISTS inventario_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventario_db;

CREATE TABLE IF NOT EXISTS productos (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    codigo_producto   VARCHAR(30)    NOT NULL UNIQUE,
    nombre            VARCHAR(120)   NOT NULL,
    categoria         VARCHAR(60)    NOT NULL,
    precio_compra     DECIMAL(10,2)  NOT NULL CHECK (precio_compra >= 0),
    precio_venta      DECIMAL(10,2)  NOT NULL CHECK (precio_venta  >= 0),
    stock             INT            NOT NULL DEFAULT 0 CHECK (stock >= 0),
    fecha_vencimiento DATE           NULL,
    creado_en         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_productos_nombre    ON productos (nombre);
CREATE INDEX idx_productos_categoria ON productos (categoria);

-- Datos de ejemplo (opcional)
INSERT INTO productos (codigo_producto, nombre, categoria, precio_compra, precio_venta, stock, fecha_vencimiento) VALUES
('CON-001', 'Concentrado para perro adulto 20 kg', 'Concentrados', 38.50, 49.90, 25, '2027-03-15'),
('CON-002', 'Concentrado para gato adulto 8 kg',   'Concentrados', 18.00, 24.50, 12, '2027-01-20'),
('GRA-001', 'Maiz amarillo (quintal)',             'Granos',       14.00, 19.00, 40, '2026-12-30'),
('ABA-001', 'Arroz 5 lb',                          'Abarrotes',     2.10,  3.25, 60, '2027-06-01'),
('ABA-002', 'Aceite vegetal 1 L',                  'Abarrotes',     1.80,  2.75,  4, '2026-11-10');
