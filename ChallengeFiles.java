import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.File;

// This file is for everything related to transferring files.
// This includes transferring the code for messages and transferring the cipher.

public class ChallengeFiles {

    private DataInputStream dataFromServer;
    private DataOutputStream dataToServer;
    private FileOutputStream fileCreator;
    private Scanner consoleInput;
    private File file;

    public ChallengeFiles(Socket socket, Scanner consoleInput) throws IOException {
        dataFromServer = new DataInputStream(socket.getInputStream());
        dataToServer = new DataOutputStream(socket.getOutputStream());
        this.consoleInput = consoleInput;
    }

    public void startFileTransfer(String fileName) {
        boolean shouldTransfer = false, serverReadyToTransfer = false;

        shouldTransfer = setFileName(fileName);
        if (shouldTransfer) {
            serverReadyToTransfer = requestFile();
            if (serverReadyToTransfer) {
                receiveFile();
            }
        }
    }

    private boolean setFileName(String fileName) {
        file = new File(fileName);
        if (file.exists()) {
            while (true) {
                System.out.println("WARNING: this file already exists on this computer, and will be overwritten. Proceed? (y/n)");
                String input = consoleInput.nextLine().toLowerCase();
                if (input.equals("n")) {
                    return false;
                } else if (input.equals("y")) {
                    break;
                } else {
                    System.out.println("Invalid response, please try again.");
                }
            }
        }
        return true;
    }

    private boolean requestFile() {
        try {
            dataToServer.writeUTF("FILE_REQUEST");
            dataToServer.writeUTF(file.getName());

            System.out.println("Waiting for server response...");
            String serverResponse = dataFromServer.readUTF();
            if (serverResponse.equals("SERVER_IS_READY")) {
                System.out.println("Server is ready to send " + file.getName());
            } else {
                System.err.println("Unable to download file. Please try again later.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Server is not currently available.");
            System.err.println(e);
        }
        return true;
    }

    private void receiveFile() {
        try {
            System.out.println("Receiving file...");
            dataToServer.writeUTF("CLIENT_IS_READY");
            fileCreator = new FileOutputStream(file);
            int input = -1;
            do {
                input = Integer.parseInt(dataFromServer.readUTF());
                if (input != -1) {
                    fileCreator.write(input);
                }
            } while (input != -1);
            fileCreator.close();
            System.out.println("Finished downloading file.");
        } catch (IOException e) {
            System.err.println("Failed to receive complete file from server.");
            System.err.println(e);
        }
    }

}
