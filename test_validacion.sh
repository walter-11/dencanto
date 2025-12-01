#!/bin/bash

# Script para probar la validación de forma manual

echo "==================================="
echo "PRUEBA MANUAL DE VALIDACIÓN"
echo "==================================="
echo ""

# Enviar JSON vacío al servidor
echo "Enviando datos VACÍOS al servidor..."
echo ""

curl -X POST http://localhost:8080/carrito/api/enviar-cotizacion \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCliente": "",
    "email": "",
    "telefono": "",
    "direccion": "",
    "fechaDeseada": "",
    "productosJson": "[]",
    "total": 0,
    "estado": "Pendiente"
  }' | jq

echo ""
echo "==================================="
echo "FIN DE PRUEBA"
echo "==================================="
