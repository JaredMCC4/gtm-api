#!/bin/bash
set -euo pipefail

echo "=================================="
echo "GTM API - Ejecutando Tests"
echo "=================================="

# Limpiar build anterior
echo ""
echo "0. Limpiando builds anteriores..."
./mvnw clean

echo ""
echo "1. Ejecutando tests unitarios CON instrumentación de JaCoCo..."
./mvnw test

echo ""
echo "2. Ejecutando tests de integración..."
./mvnw verify -P integration-tests

echo ""
echo "3. Generando reporte de cobertura consolidado..."
./mvnw jacoco:report

# Verificar que jacoco.exec no esté vacío
if [ -f target/jacoco.exec ]; then
    SIZE=$(wc -c < target/jacoco.exec)
    if [ $SIZE -gt 100 ]; then
        echo "Archivo jacoco.exec generado correctamente ($SIZE bytes)"
    else
        echo "ADVERTENCIA: jacoco.exec está vacío o muy pequeño ($SIZE bytes)"
    fi
else
    echo "ERROR: jacoco.exec NO fue generado"
fi

echo ""
echo "=================================="
echo "Tests completados"
echo "Reporte HTML: target/site/jacoco/index.html"
echo "Reporte XML:  target/site/jacoco/jacoco.xml"
echo "=================================="

# Abrir reporte automáticamente (opcional)
if command -v start &> /dev/null; then
    start target/site/jacoco/index.html
elif command -v xdg-open &> /dev/null; then
    xdg-open target/site/jacoco/index.html
fi