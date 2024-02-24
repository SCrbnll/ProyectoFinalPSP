// Main.java
package samuel.carbonell;


public class Main {
    public static void main(String[] args) {
        int port = 6789;
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }
}
