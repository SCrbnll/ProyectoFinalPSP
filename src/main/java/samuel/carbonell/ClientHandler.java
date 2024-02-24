package samuel.carbonell;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler implements Runnable {
    private Socket clientSocket; // El socket del cliente.
    private PrintWriter printWriter; // Para enviar mensajes al cliente.
    private Scanner scanner; // Para leer los mensajes del cliente.
    private ChatServer chatServer; // El servidor de chat al que está conectado este cliente.
    private String clientMessage; // El mensaje enviado por el cliente.
    private String clientName; // El nombre del cliente.

    // Constructor. Inicializa el socket del cliente, el servidor de chat, el PrintWriter y el Scanner.
    public ClientHandler(Socket clientSocket, ChatServer chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
        try {
            printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            scanner = new Scanner(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // El método que se ejecuta cuando se inicia el hilo. Maneja la comunicación con el cliente.
    @Override
    public void run() {
        // Envía un mensaje de bienvenida al cliente y solicita su nombre.
        printWriter.println ("Bienvenido al Servidor Concurrente en Java\nPor favor, ingrese su nombre: ");
        if (scanner.hasNextLine()) {
            clientName = scanner.nextLine();
            printWriter.println("Bienvenido, " + clientName + "!");
            System.out.println("Nuevo cliente conectado: " + clientName + " (" + chatServer.getConnectedClientsSize() + " usuarios conectados)");
            chatServer.sendToAllClients("se ha unido al chat.", this);
        }
        // Si hay mensajes offline para este cliente, se los envía.
        if (chatServer.offlineMessages.containsKey(clientName)) {
            ConcurrentLinkedQueue<String> messages = chatServer.offlineMessages.remove(clientName);
            for (String message : messages) {
                sendMessage(message);
            }
        }
        // Entra en un bucle infinito para leer y procesar los mensajes del cliente.
        try {
            while (true) {
                if (scanner.hasNextLine()) {
                    clientMessage = scanner.nextLine();
                    System.out.println(clientName + ": " + clientMessage);
                    if (clientMessage.equalsIgnoreCase("bye")){
                        clientSocket.close();
                        chatServer.removeClient(this);
                        System.out.println(clientName + "se ha desconectado." + " (" + chatServer.getConnectedClientsSize() + " usuarios conectados)");
                        chatServer.sendToAllClients("se ha desconectado.", this);

                    } else {
                        chatServer.sendToAllClients(clientMessage, this);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            scanner.close();
            printWriter.close();
            try {
                clientSocket.close();
                printWriter.close();
                scanner.close();
                chatServer.removeClient(this);
                System.out.println(clientName + "se ha desconectado." + " (" + chatServer.getConnectedClientsSize() + " usuarios conectados)");
                chatServer.sendToAllClients("se ha desconectado.", this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Envía un mensaje al cliente.
    public void sendMessage(String message) {
        printWriter.println(message);
    }

    // Devuelve el nombre del cliente.
    public String getClientName() {
        return clientName;
    }
}