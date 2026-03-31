Para COMPILAR este proyecto (./gradlew build)
---------------------------------------------
Solo necesitas el JAR de cpmodel_entity (mod id: customcpmodel), p. ej. copiado como:
  libs/cpmodel_entity.jar
Porque el código importa y extiende clases de ese mod (CPModelEntity, renderer).

NO hace falta poner en libs/ ni en Gradle:
  super_mario, super_block_world, CustomPlayerModels
Esos mods se resuelven solo cuando el juego está en marcha (items/entidades por ResourceLocation).

Luma (NPC): el renderer CPM busca assets/customcpmodel/cpmmodels/luma.cpmmodel. Si super_block_world
incluye ese recurso (mismo path), el modelo se combina sin cambiar el código. Si solo existe bajo
otro namespace, hay que copiar el .cpmmodel a ese path en un resource pack o en este mod.

Para JUGAR o runClient
----------------------
Copia todos los mods que uses (incluido este, customcpmodel, super_mario, super_block_world, CPM, etc.)
en la carpeta mods del cliente, por ejemplo:
  run/mods/   (desarrollo con Gradle)
  o la carpeta .minecraft/mods del launcher.
