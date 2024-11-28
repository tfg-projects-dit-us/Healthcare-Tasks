# Servidores
## Funcionalidad
Estos ficheros permiten levantar los servidores necesarios para el funcionamiento de la aplicación en contenedores docker

Usar el comando ``docker compose up -d`` para arrancar ambos contenedores
### Servidor fhir
Estará disponible en el puerto 8888 de la máquina host

Backend para la persistencia de la información clínica en formato FHIR R5
### Servidor Business Central (BC)
Estará disponible en el puerto 8080 de la máquina host

Servidor para el control del motor kie de la aplicación en modo development. Permite desplegar contenedores en el motor (modificando procesos si es necesario), ejecutar tareas humanas, etc... desde BC en caliente, es decir, sin necesidad de reiniciar la aplicación

## Arrancar sólo el servidor fhir
TODO

Si desea arrancar sólo el servidor fhir con el comando ``docker run -d --name hapifhir -p 8888:8080 -v ./application.yaml:/app/config/application.yaml hapiproject/hapi:final`` con la configuración actual no se levanta en versión 5, esto tendría que arreglarse

Soluciones provisionales:
*Modificar docker-compose y comentar el servidor business central antes de arrancar
*Levantar los dos y luego detener el contenedor de business central

