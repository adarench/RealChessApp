import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 0; // Default to dynamic port
        int assignedPort = Server.run(port);
        System.out.println("Server running at: http://localhost:" + assignedPort);
    }
}