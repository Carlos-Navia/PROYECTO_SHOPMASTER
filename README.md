# ShopMaster

Proyecto de consola para el Parcial 2 de Programación II. Implementa Composite,
Decorator, Adapter, Proxy y Facade con Java estándar. Los tres escenarios del
enunciado se ejecutan automáticamente desde `Main`, sin entrada por teclado.

## Ejecución en PowerShell

Se validó con JDK 25.0.2. `java` y `javac` deben estar disponibles en `PATH`.
Desde esta carpeta:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar.ps1
```

Para compilar y ejecutar las seis suites de pruebas:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar.ps1 -Pruebas
```

El script compila en UTF-8, trata las advertencias como errores y guarda los
archivos compilados en `salida/clases`. No requiere Maven, Gradle ni descargas.
Las fuentes permanecen en `src` y las verificaciones en `pruebas`.
La opción `-ExecutionPolicy Bypass` se aplica únicamente al proceso que ejecuta
este script local; no cambia la política permanente de PowerShell del equipo.

## Ejecución en IntelliJ IDEA

1. Abrir esta carpeta como proyecto Java y seleccionar el JDK 25 disponible.
2. Marcar `src` como **Sources Root**.
3. Para ejecutar las verificaciones desde el IDE, marcar `pruebas` como
   **Test Sources Root**. Son programas con `main`, sin JUnit.
4. Seleccionar UTF-8 como codificación del proyecto y de la consola.
5. Abrir `src/Main.java` y ejecutar su método `main`.

La ejecución en IntelliJ queda a cargo de la demostración del estudiante; la
validación automatizada se realiza mediante `javac` y `java`.

## Escenarios y resultados

| Caso | Importe final | Resultado |
|---|---:|---|
| Combo Setup Gamer, garantía y empaque | $1.159.000 COP / USD 289,75 | EXITOSA |
| Servidor Empresarial con manejo frágil | $6.025.000 COP | RECHAZADA por monto |
| Mouse para BLOQUEADO | $150.000 COP | RECHAZADA por usuario |

La garantía se aplica al importe acumulado. El orden inverso, empaque y después
garantía, produce $1.160.500 COP. La lista de servicios conserva el orden y las
repeticiones.

## Arquitectura y alcance

`Main` proporciona datos a `FachadaCompra`. Esta construye los productos y combos,
aplica los decoradores y solicita el pago a `ProxyPagoSeguro`. El Proxy valida
primero; solo después pide al proveedor crear el adaptador y la pasarela.

`StripeUSA` es una simulación académica, sin cobros reales. La clave de `Main` es
un dato de demostración, y el usuario se transmite como identificador al parámetro
externo `customerEmail`, sin validación de correo. Los importes usan `double`,
conforme al diseño aprobado; no se redondean antes de enviar el pago simulado.

Consulta [la guía de sustentación y trazabilidad](docs/SUSTENTACION.md) para el UML,
la explicación de los patrones, las pruebas y el recorrido de diez minutos.
