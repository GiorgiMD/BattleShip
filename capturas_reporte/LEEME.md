# Capturas de código para el reporte

Imágenes generadas directamente de los archivos Java del proyecto. Se conservan el código y los números de línea; las líneas largas se ajustan al ancho de la imagen. No se modificó el código fuente.

## Conexión cliente-servidor

Imagen: [01_conexion.png](01_conexion.png)

Lógica de comunicación: después de explicar ServerSocket, Socket y los flujos de entrada y salida.

Figura 1. Establecimiento de la conexión TCP y configuración de los flujos de comunicación.

## Formato e intercambio de embarcaciones

Imagen: [02_intercambio_barcos.png](02_intercambio_barcos.png)

Lógica de comunicación: después de explicar el formato x,y,tam,orientacion y los siete intercambios.

Figura 2. Conversión de los datos del barco a texto y envío y recepción durante la colocación de la flota.

## Envío de ataques y control del turno

Imagen: [03_ataques_turnos.png](03_ataques_turnos.png)

Lógica de comunicación: después de la tabla HIT, MISS y WIN. También ilustra la regla de repetición del turno en la lógica del juego.

Figura 3. Envío de coordenadas, recepción del resultado y actualización del turno del cliente.

## Composición de la flota

Imagen: [04_flota.png](04_flota.png)

Lógica del juego: después de la tabla de embarcaciones y sus tamaños.

Figura 4. Definición de los tipos de barco y cálculo de las siete embarcaciones y las 21 casillas.

## Validación y colocación de barcos

Imagen: [05_colocacion.png](05_colocacion.png)

Lógica del juego: después de explicar la validación de posiciones y el registro de casillas ocupadas.

Figura 5. Validación de límites, disponibilidad y superposición, y registro del barco en la matriz.

## Evaluación de impactos y fin de partida

Imagen: [06_impactos_victoria.png](06_impactos_victoria.png)

Lógica del juego: después de explicar los estados de la matriz y la condición de victoria.

Figura 6. Evaluación del disparo recibido, registro del daño y envío de HIT, MISS o WIN.

## Disparos automáticos del servidor

Imagen: [07_ataques_servidor.png](07_ataques_servidor.png)

Lógica del juego: después del párrafo sobre coordenadas aleatorias y la matriz tirosServidor.

Figura 7. Generación de disparos aleatorios sin repetición y envío de coordenadas al cliente.

Para insertarlas en Word: Insertar > Imágenes > Este dispositivo. Conserva la proporción y utiliza el pie de figura indicado. La numeración propuesta puede ajustarse al orden final del documento.
