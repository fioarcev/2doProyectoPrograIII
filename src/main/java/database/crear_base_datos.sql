CREATE DATABASE IF NOT EXISTS reservas_recursos;
USE reservas_recursos;

CREATE TABLE IF NOT EXISTS usuarios (
                                        id VARCHAR(50) PRIMARY KEY,
    clave VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'FUNCIONARIO') NOT NULL
    );

CREATE TABLE IF NOT EXISTS funcionarios (
                                            id VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(30)
    );

CREATE TABLE IF NOT EXISTS categorias (
                                          id VARCHAR(50) PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS recursos (
                                        id VARCHAR(50) PRIMARY KEY,
    categoria_id VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    CONSTRAINT fk_recurso_categoria
    FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    CONSTRAINT uq_recurso_categoria
    UNIQUE (id, categoria_id)
    );

CREATE TABLE IF NOT EXISTS reservas (
                                        id VARCHAR(50) PRIMARY KEY,
    actividad VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    funcionario_id VARCHAR(50) NOT NULL,
    estado ENUM('ACTIVA', 'CANCELADA', 'FINALIZADA') NOT NULL,
    CONSTRAINT fk_reserva_funcionario
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
    );

CREATE TABLE IF NOT EXISTS detalles_reserva (
                                                reserva_id VARCHAR(50) NOT NULL,
    recurso_id VARCHAR(50) NOT NULL,
    categoria_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (reserva_id, recurso_id),
    CONSTRAINT fk_detalle_reserva
    FOREIGN KEY (reserva_id)
    REFERENCES reservas(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_recurso_categoria
    FOREIGN KEY (recurso_id, categoria_id)
    REFERENCES recursos(id, categoria_id)
    );

INSERT IGNORE INTO usuarios (id, clave, rol)
VALUES ('admin', 'admin', 'ADMINISTRADOR');