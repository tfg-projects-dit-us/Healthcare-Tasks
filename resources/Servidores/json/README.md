# Recursos para el entorno de desarrollo y pruebas
Estos recursos se usarán principalmente para realizar test
## Representación
En esta carpeta se presenta la representación de los recursos tras la consulta a un servidor FHIR en los que han sido previamente creados
El nombre del recurso es XX_Tipo donde:
XX: numeración que corresponde con el id que el recurso tiene en el servidor en el que se hace la consulta, en el servidor hapi fhir el id es asignado por orden de creación, por lo que el recurso 01 fué creado antes que el recurso 02
Tipo: tipo de recurso
## Uso
Para crear estos recursos en fhir se puede usar la GUI del servidor fhir. Sería recomendable eliminar la información de meta y el id antes de la creación
## TODO
Automatizar la creación de estos recursos:
* script
* postman
* configuración spring modo dev
* Bundle4Transaction: permite crear todos a la vez en una operación de transacción (no verificado)