
CREATE DATABASE reservas_recursos;

USE reservas_recursos;

-- Usuarios
CREATE TABLE usuarios (id VARCHAR(50) PRIMARY KEY, clave VARCHAR(255) NOT NULL, rol VARCHAR(30) NOT NULL);

-- Funcionarios
CREATE TABLE funcionarios (id VARCHAR(50) PRIMARY KEY, nombre VARCHAR(120) NOT NULL, telefono VARCHAR(30));

-- Categoria de recurso
CREATE TABLE categorias_recurso (id VARCHAR(50) PRIMARY KEY, descripcion VARCHAR(150) NOT NULL);

-- Recursos
CREATE TABLE recursos (id VARCHAR(50) PRIMARY KEY, categoria_id VARCHAR(50) NOT NULL, descripcion VARCHAR(200) NOT NULL,
    CONSTRAINT fk_recurso_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_recurso(id));

-- Reservas
CREATE TABLE reservas (id VARCHAR(50) PRIMARY KEY, actividad VARCHAR(200) NOT NULL, fecha DATE NOT NULL, hora_inicio TIME NOT NULL, hora_fin TIME NOT NULL,
    funcionario_id VARCHAR(50) NOT NULL, estado VARCHAR(30) NOT NULL,
	CONSTRAINT fk_reserva_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id));

-- Detalle de reserva
CREATE TABLE detalle_reserva (reserva_id VARCHAR(50) NOT NULL, recurso_id VARCHAR(50) NOT NULL, categoria_id VARCHAR(50) NOT NULL,
	PRIMARY KEY (reserva_id, recurso_id), 
    CONSTRAINT fk_detalle_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(id),
	CONSTRAINT fk_detalle_recurso  FOREIGN KEY (recurso_id) REFERENCES recursos(id),
	CONSTRAINT fk_detalle_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_recurso(id));

SHOW TABLES;