#!/bin/bash

# Script para arrancar SistemaBiblioteca con Docker
# Levanta MariaDB y SonarQube en segundo plano
# y ejecuta sistemabiblioteca en modo interactivo

# 1. Levantar servicios de soporte
echo ">>> Levantando MariaDB y SonarQube en segundo plano..."
docker-compose up -d mariadb sonarqube

# 2. Construir la imagen de sistemabiblioteca
echo ">>> Construyendo la imagen de sistemabiblioteca..."
docker build -t sistemabiblioteca .

# 3. Ejecutar sistemabiblioteca en modo interactivo
echo ">>> Arrancando sistemabiblioteca en modo interactivo..."
docker run -it --rm \
  --name sistemabiblioteca \
  --network=sistemabiblioteca_default \
  sistemabiblioteca
