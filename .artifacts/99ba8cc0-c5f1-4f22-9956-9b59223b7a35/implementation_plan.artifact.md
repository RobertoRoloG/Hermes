# Plan de Inserción de Datos de Prueba

Este plan detalla los pasos para insertar datos de prueba en la base de datos de Hermes. Esto permitirá verificar el funcionamiento del motor de planificación (CSP), la visualización en la línea de tiempo y la gestión de tareas.

## Cambios Propuestos

Se añadirá una función de "seeding" en `MainActivity` que se ejecutará al iniciar la aplicación si la base de datos está vacía.

### [Componente de Datos y UI]

#### [MODIFICAR] [MainActivity.kt](file:///C:/Users/usuario/Desktop/Roberto/Hermes/app/src/main/java/com/hermes/app/ui/main/MainActivity.kt)
- Añadir una función privada `seedTestData()` que inserte una variedad de tareas (fijas, flexibles y completadas).
- Llamar a `seedTestData()` en `onCreate` dentro de una corrutina.

## Verificación Plan

### Verificación Manual
- Abrir la aplicación.
- Comprobar que en la pestaña "Tareas" aparecen las tareas de prueba.
- Comprobar que en la pestaña "Línea de Tiempo" (o Calendario) se visualizan las tareas planificadas.
- Verificar que el motor de planificación asigna horas a las tareas flexibles que no tienen horario inicial.
