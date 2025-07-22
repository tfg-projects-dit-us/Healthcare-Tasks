Modelos de proceso para pruebas
================================================

Este paquete contiene el conjunto de activos de negocio creados para realizar las pruebas de la aplicación

Ha sido desarrollado en Business Central y contiene procesos simples con tareas humanas, que son las gestionadas por la aplicación.

Los procesos principales son:
* TareaARoleLeve: una instancia de este proceso crea una tarea humana con prioridad leve para cualquier usuario con el rol wbadmin
* TareaARoleMedia: una instancia de este proceso crea una tarea humana con prioridad media para cualquier usuario con el rol wbadmin
* TareaARoleUrgente: una instancia de este proceso crea una tarea humana con prioridad urgente para cualquier usuario con el rol wbadmin
* TareaAUsuario: una instancia de este proceso crea una tarea humana asignada al usuario que se pasa como parámetro de entrada

Estos procesos contienen temporizadores que finalizarán el proceso si no se ha completado antes de que se cumpla la fecha de expiración de la tarea humana.
