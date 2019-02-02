import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.UUID;

// This file should essentially be complete
// The bulk of the work is to be done in ChallengeFiles.java and ChallengeMessaging.java.

public class ChallengeClient {

    private static String ADDRESS = "10.0.9.93";
    private static int PORT = 2019;

    private Socket socket;
    private Scanner consoleInput;

    private ChallengeFiles fileTransfers;
    private ChallengeMessaging messageTransfers;
    private ChallengeCipher cipher;

    private String cipherKey;
    private String encryptedMessage;
    private String clientId;

    public ChallengeClient() throws IOException {
        socket = new Socket(ADDRESS, PORT);
        System.out.println("Created client socket on port " + PORT + " and address " + ADDRESS);

        consoleInput = new Scanner(System.in);
        fileTransfers = new ChallengeFiles(socket, consoleInput);
        messageTransfers = new ChallengeMessaging(socket);
        cipher = new ChallengeCipher();

        cipherKey = "";
        clientId = UUID.randomUUID().toString();
    }

    public void run() {
        while (true) {
            System.out.println("\nTYPE A NUMBER TO EXECUTE A COMMAND:");
            System.out.println("\t1 - I've fixed the file transfer functionality! Send me Part 2.");
            System.out.println("\t2 - I can receive messages! Send me Part 3.");
            System.out.println("\t3 - I'd like to solve the cipher!");
            System.out.println("\t0 - Exit");
            System.out.print("\nChoice: ");

            if (consoleInput.hasNextInt()) {
                int choice = consoleInput.nextInt();
                consoleInput.nextLine();
                switch(choice) {
                    case 0:
                        System.out.println("Goodbye!");
                        try {
                            (new DataOutputStream(socket.getOutputStream())).writeUTF("DISCONNECT");
                        }
                        catch (IOException e){
                            System.err.println("Fail to properly disconnect");
                            System.err.println(e);
                        }
                        return;
                    case 1:
                        fileTransfers.startFileTransfer("ChallengeMessaging.java");
                        break;
                    case 2:
                        fileTransfers.startFileTransfer("Cipher.pdf"); // Sends cipher tutorial
                        cipherKey = messageTransfers.requestMessage("GET_KEY");
                        encryptedMessage = messageTransfers.requestMessage("GET_ENCRYPTED_MESSAGE");
                        System.out.println("\tCIPHER KEY: " + cipherKey);
                        System.out.println("\tENCODED MESSAGE: " + encryptedMessage);
                        break;
                    case 3:
                        String cipherSolution = cipher.solve(cipherKey, encryptedMessage);
                        System.out.println("\tDECRYPTED MESSAGE: " + cipherSolution);

                        String result = messageTransfers.sendMessage(cipherSolution);
                        switch (result) {
                            case "CORRECT":
                                System.out.println("\tSERVER RESPONSE: Correct decryption! YOU WIN!");
                                try {
                                    (new DataOutputStream(socket.getOutputStream())).writeUTF(clientId);
                                } catch (IOException e) {
                                    System.err.println("Failed to send client ID! PLEASE STAND UP AND SAY WHO YOU ARE!");
                                    System.err.println(e);
                                }
                                break;
                            default:
                                System.out.println("\tSERVER RESPONSE: Incorrect decryption!");
                        }
                        break;
                    default:
                        System.out.println("That's not a valid entry, try again.");
                }
            } else {
                System.out.println("That's not a valid entry, try again.");
                consoleInput.nextLine();
            }
        }
    }

    public static void main(String[] args) {
        try {
            ChallengeClient client = new ChallengeClient();
            client.run();
        } catch (IOException e) {
            System.err.println("Failed to prepare client");
            System.err.println(e);
        }
    }

}
