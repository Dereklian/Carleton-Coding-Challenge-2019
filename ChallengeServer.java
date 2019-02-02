import java.net.ServerSocket;
import java.io.IOException;

public class ChallengeServer {

    private static int PORT = 2019;
    private static int THREAD_POOL = 10;

    private ServerSocket server;

    public ChallengeServer() throws IOException {
        server = new ServerSocket(PORT);
        System.out.println("Created server socket on port " + PORT);
    }

    public void run() {
        System.out.println("Starting server...");
        for (int i = 0; i < THREAD_POOL; i++) {
            new Thread() {
                public void run() {
                    System.out.println("New server thread created");
                    while (true) {
                        try {
                            System.out.println("Waiting for connection...");
                            ChallengeTransferProtocol ftp = new ChallengeTransferProtocol(server.accept());
                            ftp.start();
                        }
                        catch (IOException e)
                        {
                            System.err.println("Failed to accept connection");
                            System.err.println(e);
                            return;
                        }
                    }
                }
            }.start();
        }
    }

    public static void main(String[] args) {
        try {
            ChallengeServer server = new ChallengeServer();
            server.run();
        } catch (IOException e) {
            System.err.println("Failed to create server socket on port " + PORT);
            System.err.println(e);
        }
    }

}
