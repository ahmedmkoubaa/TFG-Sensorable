# TFG-Sensorable
Este es el repositorio del proyecto desarrollado por Ahmed El Moukhtari Koubaa como parte de su TFG.

# Instalación
La instalación se debe llevar a cabo en varios pasos o fases, este es un sistema distribuido que funciona
en varias plataformas.

## Android
En primer lugar se debe instalador Android Studio. Se puede descargar de este enlace:
https://developer.android.com/studio

El siguiente paso es abrir el proyecto "Sensorable" en Android Studio.
Hay que conectar un dispositivo móvil Android, para ello habrá que ir a:
ajustes > información sobre el télefono > información software > Número de compilación

Una vez aquí pulsar varias veces el número de compilación para poder usar el modo desarrollador.

Con esto ya podríamos compilar dentro del proyecto Sesorable en Android Studio el módulo mobile en el móvil asociado.

## Wear OS
Hay que seguir los mismos pasos que en el apartado anterior (Android). Ahora debemos además enlazar el 
smartwatch con el dispositivo Android, donde deberemos descargar además la app "Wear OS". En dicha app
nos iremos a ajustes avanzados > depurar mediante bluetooth y activaremos esta opción.

A continuación en el smartwatch debemos:
- meternos en ajustes de wear os > sistema > sobre > número de compilación 
- pulsamos varias veces el número de compilación para ser desarrolladores.
- Navegamos hacia atrás una vez.
- Accedemos al developer options, ponemos adb debugging a true y debug over bluetooth también.

Una vez hecho esto vamos android studio y abrimos una terminal. Vamos a ejecutar los siguientes comandos:
- adb devices // para saber los dispositivos que hay (nos quedamos con el nombre largo si hay varios)
- adb  -s <id del device que se quiere usar> forward tcp:4445 localabstract:/adb-hub
- adb connect 127.0.0.1:4445 (el puerto que se especificó en el apartado anterior>

Con esto conseguimos que se conecte el reloj a Android Studio y ahora podremos irnos a las opciones de compilación 
y compilar el módulo "Wear" en este dispositivo que acabamos de asociar.

## MySQL
Antes de levantar los servicios de Node y demás hay que lanzar una base de datos. 
Para ello hay que instalar MySQL (preferiblemente en linux). Se debe crear un usuario
con permisos para insertar, eliminar, seleccionar, actualizar, crear tablas y eliminarlas.
También se puede crear un usuario con los permisos de insertar y seleccionar y otro con los demás permisos. 
En cualquier caso debemos recordar las credenciales de dicho usuario para usarlas más tarde.
El nombre de la base de datos puede ser cualquiera, en este proyecto las credenciales son de prueba y
por eso aparecen directamente escritas en código. Pero son de prueba y deberán reemplazarse o bien usarse las mismas.
Creamos entonces una base de datos, en nuestro caso de nombre "test".

Accedemos entonces a la carpeta Database y ahí encontramos un index.SQL. Podemos ejecutar el siguiente comando
si se ha creado la base de datos con el nombre "test". En caso contrario modificar el archivo index para que use el nombre 
correcto de base de datos.

- sudo mysql < index.sql

Con este comando se crearán todas las tablas y se construirán las ADLs predefinidas en la base de datos.
Lanzamos la base de datos:
- sudo service mysql start

## Node JS
En este caso accedemos a la carpeta SensorableServicesPlatform
y una vez aquí el primer paso será acceder a packages/database-client/src/index.ts
para usar las credenciales correctas, sin ellas no habrá conexión a la base de datos. 
Asegurémonos de que la base de datos fue lanzanda antes de lanzar los servicios de Node JS.
- sudo service mysql start

El siguiente paso es ir hacia atrás y posicionarse en SensorableServicesPlatform nuevamente.
Deberemos ejecutar: 
- yarn install
- yarn build
- yarn start

Con esto lo que hacemos es instalar las dependencias, compilar los paquetes y ejecutar el fichero índice 
que lanzará todos los servicios que tengamos creados.

Con esto ya podemos lanzar todos los servicios y usar el sistema al completo.
