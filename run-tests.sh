#!/bin/bash

echo "=================================="
echo "GTM API - Ejecutando Tests"
echo "=================================="

# Tests unitarios
echo ""
echo "1. Ejecutando tests unitarios..."
./mvnw test

# Tests de integración
echo ""
echo "2. Ejecutando tests de integración..."
./mvnw verify -P integration-tests

# Reporte de cobertura
echo ""
echo "3. Generando reporte de cobertura..."
./mvnw jacoco:report

echo ""
echo "=================================="
echo "Tests completados"
echo "Reporte de cobertura: target/site/jacoco/index.html"
echo "=================================="