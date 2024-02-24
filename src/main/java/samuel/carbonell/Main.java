// Main.java
package samuel.carbonell;


public class Main {
    public static void main(String[] args) {
        // Define el puerto en el que se ejecutará el servidor.
        int port = 6789;
        // Crea una nueva instancia del servidor de chat en el puerto especificado.
        ChatServer chatServer = new ChatServer(port);
        // Inicia el servidor de chat.
        chatServer.start();
    }
}
