package samuel.carbonell;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private ServerSocket serverSocket; // Socket del servidor para aceptar conexiones de clientes.
    private ExecutorService executorService; // Pool de hilos para manejar múltiples clientes
    private List<ClientHandler> connectedClients;  // Lista de clientes conectados al servidor.
    private Timer timer; // Temporizador para apagar el servidor después de un cierto tiempo.
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> offlineMessages = new ConcurrentHashMap<>(); // Almacena los mensajes offline para cada cliente.

    // Constructor del servidor. Inicializa el socket del servidor y el pool de hilos.
    public ChatServer(int port) {
        connectedClients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            System.out.println("Servidor iniciado y escuchando en el puerto " + port);
            System.out.println("Cliente conectados: " + connectedClients.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inicia el servidor. Acepta conexiones de clientes y crea un nuevo hilo para cada uno.
    public void start() {
        // Hilo leer entrada del usuario desde la consola
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String userInput = scanner.nextLine();
                if ("stop".equalsIgnoreCase(userInput.trim())) {
                    stopServer(5);
                }
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();
        // SIGTERM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando servidor de manera ordenada...");
            stopServer(5);
        }));
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                connectedClients.add(clientHandler);
                executorService.execute(clientHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Devuelve el número de clientes conectados.
    public int getConnectedClientsSize() {
        return connectedClients.size();
    }

    // Envía un mensaje a todos los clientes conectados. Si se proporciona un cliente, no se envía el mensaje a ese cliente.
    public void sendToAllClients(String message, ClientHandler clientHandler) {
        if (clientHandler != null){
            for (ClientHandler client : connectedClients) {
                if (!client.equals(clientHandler)){
                    client.sendMessage("[" + clientHandler.getClientName() +  "]:  " + message);
                }
            }
            // Añade los mensajes a la cola de mensajes offline para los clientes desconectados
            for (String offlineClient : offlineMessages.keySet()) {
                if (!offlineClient.equals(clientHandler.getClientName())) {
                    offlineMessages.get(offlineClient).add("[" + clientHandler.getClientName() + "]:  " + message);
                }
            }
        } else {
            for (ClientHandler client : connectedClients) {
                client.sendMessage( message);
            }
            // Añade los mensajes a la cola de mensajes offline para los clientes desconectados
            for (String offlineClient : offlineMessages.keySet()) {
                offlineMessages.get(offlineClient).add(message);
            }
        }

    }

    // Elimina un cliente de la lista de clientes conectados y añade sus mensajes a la cola de mensajes offline.
    public void removeClient(ClientHandler client) {
        connectedClients.remove(client);
        offlineMessages.put(client.getClientName(), new ConcurrentLinkedQueue<>());
    }

    // Detiene el servidor después de un cierto retraso. Envía un mensaje a todos los clientes antes de detenerse.
    public void stopServer(int delayInSeconds) {
        if (timer != null) {
            return;
        }
        // Crear un temporizador para apagar servidor
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    serverSocket.close();
                    executorService.shutdown();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, delayInSeconds * 1000L);

        sendToAllClients("[SERVER] : El servidor se desconectará en " + delayInSeconds + " segundos.", null);
    }

}