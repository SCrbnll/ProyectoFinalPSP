package samuel.carbonell;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter printWriter;
    private Scanner scanner;
    private ChatServer chatServer;
    private String clientMessage;
    private String clientName;

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

    @Override
    public void run() {
        printWriter.println ("Bienvenido al Servidor Concurrente en Java\nPor favor, ingrese su nombre: ");
        if (scanner.hasNextLine()) {
            clientName = scanner.nextLine();
            printWriter.println("Bienvenido, " + clientName + "!");
            System.out.println("Nuevo cliente conectado: " + clientName + " (" + chatServer.getConnectedClientsSize() + " usuarios conectados)");
            chatServer.sendToAllClients("se ha unido al chat.", this);
        }
        if (chatServer.offlineMessages.containsKey(clientName)) {
            ConcurrentLinkedQueue<String> messages = chatServer.offlineMessages.remove(clientName);
            for (String message : messages) {
                sendMessage(message);
            }
        }
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

    public void sendMessage(String message) {
        printWriter.println(message);
    }

    public String getClientName() {
        return clientName;
    }
}