# Servidor de Chat en Java
## Descripción
> Este es un servidor de chat concurrente implementado en Java. Utiliza sockets para la comunicación entre el servidor y los clientes. Cada cliente se maneja en un hilo separado para permitir múltiples conexiones simultáneas. Los mensajes de los clientes se retransmiten a todos los demás clientes conectados. Cuando un cliente se desconecta, sus mensajes se almacenan y se le envían cuando se vuelve a conectar.

## Ejecución
> Para ejecutar el servidor de chat, ejecuta la clase `Main.java`, al hacerlo iniciará el servidor de chat en el puerto 6789.

## Características Principales
>  - **Concurrencia**: El servidor puede manejar múltiples clientes simultáneamente.
> 
>  - **Mensajes Offline**: Cuando un cliente se desconecta, sus mensajes se almacenan y se le envían cuando se vuelve a conectar.
>    
>  - **Comandos del Servidor**: El servidor puede ser detenido de manera segura mediante el comando “stop”.
