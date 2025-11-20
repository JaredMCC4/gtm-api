#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MVNW="./mvnw"
COVERAGE_FILE="target/jacoco-merged.exec"
HTML_REPORT="target/site/jacoco/index.html"
XML_REPORT="target/site/jacoco/jacoco.xml"

echo "=================================="
echo "GTM API - Ejecutando tests"
echo "=================================="

echo ""
echo "0. Limpiando y ejecutando pruebas (unit + integration) con cobertura..."
"$MVNW" --batch-mode clean verify -P integration-tests

echo ""
echo "=================================="
echo "Verificando generación de reportes..."
echo "=================================="

# Verificar archivo de cobertura
if [[ -f "$COVERAGE_FILE" ]]; then
    SIZE=$(wc -c < "$COVERAGE_FILE")
    if (( SIZE > 100 )); then
        echo "✓ Cobertura consolidada disponible (${SIZE} bytes) en $COVERAGE_FILE"
    else
        echo "⚠ ADVERTENCIA: $COVERAGE_FILE es demasiado pequeño (${SIZE} bytes)"
    fi
else
    echo "✗ ERROR: $COVERAGE_FILE no se generó"
fi

if [[ -f "$HTML_REPORT" ]]; then
    echo "✓ Reporte HTML generado: $HTML_REPORT"
else
    echo "⚠ Reporte HTML NO encontrado en: $HTML_REPORT"
    FOUND_REPORT=$(find target -name "index.html" -path "*/jacoco/*" 2>/dev/null | head -n 1)
    if [[ -n "$FOUND_REPORT" ]]; then
        echo "✓ Reporte encontrado en: $FOUND_REPORT"
        HTML_REPORT="$FOUND_REPORT"
    else
        echo "✗ No se encontró ningún reporte HTML de Jacoco"
    fi
fi

if [[ -f "$XML_REPORT" ]]; then
    echo "✓ Reporte XML generado: $XML_REPORT"
else
    echo "⚠ Reporte XML NO encontrado en: $XML_REPORT"
fi

echo ""
echo "=================================="
echo "Tests completados"
echo "Reporte HTML: $HTML_REPORT"
echo "Reporte XML:  $XML_REPORT"
echo "=================================="

open_report() {
    local report_path="$1"
    
    if [[ ! -f "$report_path" ]]; then
        echo "✗ No se puede abrir: archivo no existe ($report_path)"
        return 1
    fi
    
    echo "Abriendo reporte en navegador..."
    local uname_out
    uname_out="$(uname -s 2>/dev/null || echo unknown)"
    case "$uname_out" in
        CYGWIN*|MINGW*|MSYS*)
            if command -v cmd.exe >/dev/null 2>&1; then
                local win_path="$report_path"
                if command -v cygpath >/dev/null 2>&1; then
                    win_path="$(cygpath -w "$report_path")"
                elif command -v wslpath >/dev/null 2>&1; then
                    win_path="$(wslpath -w "$report_path")"
                fi
                cmd.exe /c start "" "$win_path" >/dev/null 2>&1 || true
            fi
            ;;
        Darwin*)
            if command -v open >/dev/null 2>&1; then
                open "$report_path" >/dev/null 2>&1 || true
            fi
            ;;
        *)
            if command -v xdg-open >/dev/null 2>&1; then
                xdg-open "$report_path" >/dev/null 2>&1 &
            fi
            ;;
    esac
}

if [[ -f "$HTML_REPORT" ]]; then
    open_report "$HTML_REPORT"
else
    echo ""
    echo "=================================="
    echo "⚠ No se pudo abrir el reporte automáticamente"
    echo "Ejecuta manualmente: open $HTML_REPORT"
    echo "O busca en: target/site/jacoco/index.html"
    echo "=================================="
fi