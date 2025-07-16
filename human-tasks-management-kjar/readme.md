Modelos de proceso para pruebas
================================================

Este paquete contiene el conjunto de activos de negocio creados para realizar las pruebas de la aplicación

Ha sido desarrollado en Business Central y contiene procesos simples con tareas humanas, que son las gestionadas por la aplicación.

Los procesos principales son:
* TareaARoleLeve: una instancia de este proceso crea una tarea humana para cualquier usuario con el rol wbadmin y prioridad leve
* TareaARoleMedia: una instancia de este proceso crea una tarea humana para cualquier usuario con el rol wbadmin y prioridad media
* TareaARoleUrgente: una instancia de este proceso crea una tarea humana para cualquier usuario con el rol wbadmin y prioridad alta
* TareaAUsuario: una instancia de este proceso crea una tarea humana asignada al usuario que se pasa como parámetro de entrada
