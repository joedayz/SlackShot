# 🧪 SlackShot Testing Guide

Esta guía te ayudará a verificar que todas las funcionalidades de SlackShot funcionan correctamente, incluyendo las nuevas mejoras del pool de WebDrivers.

## 📋 Prerequisitos

1. **Aplicación ejecutándose**: Asegúrate de que SlackShot esté corriendo en `http://localhost:3030`
2. **Configuración**: Configura tu `AUTH_KEY` en los scripts de prueba
3. **Chrome**: Asegúrate de que Chrome esté instalado
4. **Herramientas opcionales**: `jq` para mejor análisis de JSON, `awk` para análisis de datos

## 🚀 Inicio Rápido

### 1. Configurar AUTH_KEY

Edita los scripts de prueba y reemplaza `your-auth-key-here` con tu clave real:

```bash
# En test-screenshots.sh, load-test.sh, y monitor-pool.sh
AUTH_KEY="tu-clave-real-aqui"
```

### 2. Ejecutar Pruebas Básicas

```bash
./test-screenshots.sh
```

Este script prueba:
- ✅ Estado del pool de WebDrivers
- ✅ Operaciones básicas de sitios
- ✅ Funcionalidad de screenshots
- ✅ Tareas programadas
- ✅ Sitios con login
- ✅ Procesamiento concurrente
- ✅ Limpieza de recursos

## 📊 Pruebas de Rendimiento

### Prueba de Carga

```bash
./load-test.sh
```

Esta prueba:
- 🚀 Ejecuta 50 requests concurrentes
- 📈 Mide tiempos de respuesta
- 📊 Analiza utilización del pool
- 💡 Proporciona recomendaciones de optimización

### Monitoreo en Tiempo Real

```bash
./monitor-pool.sh
```

Este monitor:
- 📊 Muestra estadísticas en tiempo real
- ⚠️ Alerta sobre problemas de rendimiento
- 💡 Sugiere optimizaciones
- 📈 Visualiza utilización del pool

## 🔍 Pruebas Manuales

### 1. Verificar Pool de WebDrivers

```bash
curl -X GET http://localhost:3030/api/webdriver/stats \
  -H "Authorization: tu-clave-aqui"
```

**Respuesta esperada:**
```json
{
  "totalDrivers": 0,
  "activeDrivers": 0,
  "availableDrivers": 0,
  "maxPoolSize": 5,
  "utilizationPercentage": 0,
  "timestamp": 1703123456789
}
```

### 2. Probar Screenshot Básico

```bash
# Agregar sitio
curl -X PUT http://localhost:3030/api/site \
  -H "Authorization: tu-clave-aqui" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-site",
    "url": "https://httpbin.org",
    "loginType": "NONE"
  }'

# Tomar screenshot
curl -X POST http://localhost:3030/api/site/test-site/screenshot \
  -H "Authorization: tu-clave-aqui"

# Verificar pool después del screenshot
curl -X GET http://localhost:3030/api/webdriver/stats \
  -H "Authorization: tu-clave-aqui"
```

### 3. Probar Concurrencia

```bash
# Ejecutar múltiples screenshots simultáneamente
for i in {1..5}; do
  curl -X POST http://localhost:3030/api/site/test-site/screenshot \
    -H "Authorization: tu-clave-aqui" &
done
wait

# Verificar que el pool manejó la concurrencia
curl -X GET http://localhost:3030/api/webdriver/stats \
  -H "Authorization: tu-clave-aqui"
```

## 📈 Métricas de Rendimiento

### Indicadores Clave

1. **Tasa de Éxito**: Debe ser >95% en condiciones normales
2. **Tiempo Promedio**: Screenshots simples <5 segundos
3. **Utilización del Pool**: 20-80% en carga normal
4. **Drivers Activos**: Debe volver a 0 después de las operaciones

### Umbrales de Alerta

- ⚠️ **Utilización >80%**: Considerar aumentar `max-size`
- 🚨 **Tasa de éxito <80%**: Revisar configuración del pool
- 🔥 **Drivers activos persistentes**: Posible memory leak

## 🛠️ Troubleshooting

### Problemas Comunes

#### 1. Pool Siempre Vacío
```bash
# Verificar logs
tail -f logs/application.log | grep WebDriver

# Verificar configuración
grep -A 5 "webdriver:" src/main/resources/application.yml
```

#### 2. Screenshots Fallan
```bash
# Verificar Chrome
google-chrome --version

# Verificar permisos
ls -la /usr/bin/google-chrome

# Verificar logs de Selenide
tail -f logs/application.log | grep Selenide
```

#### 3. Concurrencia Lenta
```bash
# Aumentar pool size
echo "webdriver:
  pool:
    max-size: 10" >> src/main/resources/application.yml

# Reiniciar aplicación
```

### Logs Importantes

```bash
# Logs de WebDriver
grep "WebDriver" logs/application.log

# Logs de pool
grep "pool" logs/application.log

# Logs de concurrencia
grep "async" logs/application.log
```

## 📋 Checklist de Verificación

### ✅ Funcionalidad Básica
- [ ] Pool de WebDrivers se inicializa correctamente
- [ ] Screenshots básicos funcionan
- [ ] API endpoints responden correctamente
- [ ] Base de datos guarda screenshots

### ✅ Pool Management
- [ ] Drivers se reutilizan correctamente
- [ ] Pool se expande bajo carga
- [ ] Drivers se limpian automáticamente
- [ ] Estadísticas del pool son precisas

### ✅ Concurrencia
- [ ] Múltiples screenshots simultáneos funcionan
- [ ] No hay conflictos de recursos
- [ ] Tiempos de respuesta son consistentes
- [ ] Pool maneja picos de carga

### ✅ Seguridad
- [ ] Flags de seguridad están deshabilitadas por defecto
- [ ] Configuración opcional funciona cuando se necesita
- [ ] No hay vulnerabilidades de CORS/mixed content

### ✅ Monitoreo
- [ ] Endpoint de estadísticas funciona
- [ ] Métricas son precisas
- [ ] Alertas funcionan correctamente
- [ ] Logs proporcionan información útil

## 🎯 Resultados Esperados

### Después de las Pruebas Básicas
```
✅ Test Suite Completed!

📊 Summary:
- WebDriver pool management: ✅
- Basic screenshot functionality: ✅
- Scheduled tasks: ✅
- Login support: ✅
- Concurrent processing: ✅
- Pool monitoring: ✅

🎉 All tests passed! The WebDriver pool is working correctly.
```

### Después de la Prueba de Carga
```
Success rate: 98%
Average duration: 2340ms
Maximum duration: 5670ms
Minimum duration: 1200ms

✅ Excellent performance! Pool handled load very well.
```

## 🔄 Pruebas Continuas

Para monitoreo continuo, ejecuta:

```bash
# En una terminal
./monitor-pool.sh

# En otra terminal, ejecuta carga periódica
while true; do
  ./load-test.sh
  sleep 300  # Cada 5 minutos
done
```

## 📞 Soporte

Si encuentras problemas:

1. **Revisa los logs**: `tail -f logs/application.log`
2. **Verifica configuración**: `cat src/main/resources/application.yml`
3. **Prueba endpoints básicos**: Usa los scripts de prueba
4. **Monitorea el pool**: Usa `monitor-pool.sh`

¡Con estas pruebas tendrás la certeza de que SlackShot funciona correctamente con todas sus mejoras de rendimiento! 