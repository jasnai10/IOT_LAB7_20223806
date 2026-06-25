# ARQUITECTURA.md — Laboratorio 7 BiciPUCP

**Alumno:** Jair Sneider Aguilera Inca

**Código:** 20223806

---

## 1. Patrón arquitectónico global aplicado

De los seis patrones de arquitectura presentados en clase, el patrón arquitectónico general que aplica esta solución es el de **Microservicios**. Esto se debe a que la aplicación se ha implementado mediante tres servicios autónomos que cooperan a través de la red, cada uno con una responsabilidad delimitada:

* **eureka-server (puerto 8761):** se encarga de mantener un registro de las instancias activas y permitir que los servicios se localicen entre sí dinámicamente.
* **pucp-validador-service (puerto 8001):** microservicio que ejecuta las reglas de validación de la aplicación (código PUCP y PIN del candado IoT).
* **orquestador-service (puerto 8080):** se encarga de recibir las solicitudes del cliente Android, invocar al validador vía RestTemplate y Feign, agregar los resultados y enviar la respuesta final con el token de desbloqueo.

---

## 2. Cumplimiento de la restricción "Stateless" del estándar RESTful

La restricción **Stateless** de REST establece que cada petición del cliente debe contener toda la información necesaria para ser procesada, sin que el servidor guarde estado de sesión asociado al cliente entre peticiones.

El microservicio `pucp-validador-service` cumple con esta restricción porque no utiliza ninguna base de datos ni mantiene sesiones para registrar información sobre las solicitudes; únicamente utiliza la información (código y PIN) que recibe en cada llamada como parte de la URL (path variables) para procesar la respuesta. No existe la necesidad de consultar una petición previa ni de mantener un paso anterior en el mismo microservicio como ocurriría en una aplicación stateful.
