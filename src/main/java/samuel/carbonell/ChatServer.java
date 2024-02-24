package samuel.carbonell;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private List<ClientHandler> connectedClients;
    private Timer timer;


    public ChatServer(int port) {
        connectedClients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            System.out.println("Servidor iniciado y escuchando en el puerto " + port);
            System.out.println("Cliente conectadoss: " + connectedClients.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            stopServer(2);
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


    public List<ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    public void sendToAllClients(String message, ClientHandler clientHandler) {
        if (clientHandler != null){
            for (ClientHandler client : connectedClients) {
                if (!client.equals(clientHandler)){
                    client.sendMessage("[" + clientHandler.getClientName() +  "]:  " + message);
                }
            }
        } else {
            for (ClientHandler client : connectedClients) {
                client.sendMessage( message);
            }
        }

    }

    public void removeClient(ClientHandler client) {
        connectedClients.remove(client);
    }

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
