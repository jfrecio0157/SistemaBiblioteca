#!/bin/bash

echo "⏹️ Parando contenedor de sistemabiblioteca si está activo..."
docker ps --filter "name=sistemabiblioteca" --format "{{.Names}}" | grep -q "sistemabiblioteca"
if [ $? -eq 0 ]; then
  docker stop sistemabiblioteca
  echo "✅ Contenedor sistemabiblioteca detenido."
else
  echo "ℹ️ Contenedor sistemabiblioteca no estaba activo."
fi

echo "⏹️ Parando contenedores de MariaDB y SonarQube..."
docker-compose down

echo "✅ Todos los contenedores han sido detenidos correctamente."
