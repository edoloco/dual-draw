# DUAL Draw — guía para IAs (y personas) que trabajan en este repositorio

Responde y comenta siempre **en español** (el dueño escribe en español, a menudo desde el teléfono).
Actualiza este archivo con cada cambio que mueva piezas.

## Qué es

Programa de **dibujo** (pinceles, pluma vectorial estilo FreeHand, capas, PSD) repartido en **dos teléfonos que
funcionan como uno** (sistema ChronOS, carcasa 3D). Nació dentro de ChronOS ("DUAL-Draw") y en nov. 2026 pasó a
ser una app aparte, con su propia conexión, igual que DUAL Motion.

| Papel (pref `role` en "dd") | Teléfono | Qué muestra |
|---|---|---|
| **Principal** (`view`, arriba) | Redmi Note 8 | Paneles **fijos** como DUAL Motion: color (izq., 214 dp), barra de archivo + la obra (centro), capas (der., 226 dp). |
| **Extendido** (`edit`, abajo) | Poco M8 Pro | El lienzo con zoom, pinceles y herramientas (paneles de cristal), ☰ menú. |
| **Un solo teléfono** (`solo`) | cualquiera | Arriba la obra (paneles flotantes escondidos), abajo el lienzo. |

La primera vez se elige el papel (`chooseRole`; mientras tanto `onResume`/`onPause` no tocan el dibujo, que aún no existe); se cambia en ☰ › Cambiar el papel.
Si la app se cae, `catchCrashes` guarda `files/error.txt` y al abrirla de nuevo lo muestra con "Copiar".

## Reglas que no se rompen

1. **Paquete `cl.dualdraw`**: no cambiarlo. App aparte de ChronOS (`cl.mesadoble`) y de DUAL Motion (`cl.dualmotion`).
2. **Java, sin AndroidX ni librerías externas.** `minSdk 28`, `targetSdk 34`.
3. **Cada envío a `main` compila y publica** `DualDraw.apk` en Releases (`.github/workflows/build.yml`).
   Antes de subir: `git fetch` + `rebase` sobre `main`. Cambios solo en `.md` no publican.
4. La llave `app/debug.keystore` es fija (la misma de ChronOS y DUAL Motion).
5. Textos para el usuario en español de Chile, cortos. Comentarios del código en español.

## Mapa del código (`app/src/main/java/cl/dualdraw/`)

- `DualDrawActivity` — todo lo de pantalla: papel, principal con paneles fijos (`buildViewer` → `fixedRow`),
  extendido (`buildEditor`), herramientas, pluma, balde, texto, transformar, PNG/PSD, ☰ (`appMenu`).
- `DrawDoc` — el documento (capas de bits + formas vectoriales `VShape`), órdenes `apply`, deshacer, PSD.
- `Net` — TCP por WiFi (puerto **47832**, anuncio UDP **47833**, "DUALDRAW"), una sola fila de envío, latido.
- `LinkService` — servicio en primer plano dueño de `Net`; atiende `dwOpen` (abre la otra mitad: `startActivity`
  y, si no aparece, aviso a pantalla completa), `ddUpdate`, `ddMsg`; lo demás va a `Hub`.
- `Hub` / `Link` — puente con la misma forma que tenía en ChronOS (`hub.send(t, k, v…)`, `Link.Listener`).
- `Updater` — Releases de `edoloco/dual-draw`, `DualDraw.apk`; "Actualizar los dos" manda `ddUpdate`.
- `Ui`, `Glass`, `CardTheme`, `Aero`, `ColorWheel`, `Knob` — copiados de ChronOS (hojas, cristal, rueda, perilla).

## Sincronía

- El **extendido es dueño del documento**: aplica cada orden y la reenvía `{t:"dw", op…, n, sid}` por UNA fila
  (`outQ`). `n` = número de orden (1, 2, 3…), `sid` = sesión del extendido.
- `dwFull` lleva el dibujo entero con el `n` de la última orden que incluye. El principal (`inOrder`) descarta lo
  ya incluido y, si ve un salto (o otra `sid`), pide `dwHello` → el extendido manda `dwFull`. Así nunca quedan
  distintos aunque se corte la conexión un instante.
- El principal pide cambios: `dwReq {op…}`, `dwSet {k, v}`; el extendido responde con `dwState`.
- Al conectarse o volver al frente: `dwOpen` (que la otra mitad se abra) + `dwFull`/`dwHello`.
- `dwClose` cierra la otra mitad. `dwPick`: importar imagen se elige en el principal.
- ChronOS la abre en el extendido sola al abrirla en el principal (meta-data `chronos.dual`).
