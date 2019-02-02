import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

public class ChallengeMessaging {

    private DataOutputStream dataToServer;
    private DataInputStream dataFromServer;

    public ChallengeMessaging(Socket socket) throws IOException {
        dataFromServer = new DataInputStream(socket.getInputStream());
        dataToServer = new DataOutputStream(socket.getOutputStream());
    }

	//Either rm everything from this method or leave try catch (for challenge)
    public String requestMessage(String type) {
        try {
            dataToServer.writeUTF(type);
            return dataFromServer.readUTF();
        } catch (IOException e) {
            System.err.println("Failed to retrieve key from server.");
            System.err.println(e);
            return "";
        }
    }

	// Rm everything (participants have to write this function)
    public String sendMessage(String message) {
        try {
            dataToServer.writeUTF("DECODED_MESSAGE");
            dataToServer.writeUTF(message);
            return dataFromServer.readUTF();
        } catch (IOException e) {
            System.err.println("Failed to send decoded message to server.");
            System.err.println(e);
            return "INCORRECT";
        }
    }
}
