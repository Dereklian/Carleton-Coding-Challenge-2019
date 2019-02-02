import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.awt.Point;

public class ChallengeTransferProtocol {

    private static String ENCRYPTION_KEY = "SSSCKEY";
    private static String DECODED_MESSAGE = "SUPERCALIFRAGILISTICEXPIALIDOCIOUS";

    private DataInputStream dataFromClient;
    private DataOutputStream dataToClient;

    private static char[][] table;
    private static Point[] positions;

    public ChallengeTransferProtocol(Socket socket) throws IOException {
        System.out.println("Accepted connection");

        dataFromClient = new DataInputStream(socket.getInputStream());
        dataToClient = new DataOutputStream(socket.getOutputStream());

        table = new char[5][5];
        positions = new Point[26];
    }

    public void start() {
        System.out.println("Started transfer protocol");
        while (true) {
            try {
                System.out.println("Waiting for client command ...");

                String command = dataFromClient.readUTF();
                switch (command) {
                    case "FILE_REQUEST":
                        sendFile();
                        break;
                    case "GET_KEY":
                        sendKey();
                        break;
                    case "GET_ENCRYPTED_MESSAGE":
                        sendMessage();
                        break;
                    case "DECODED_MESSAGE":
                        receiveMessage();
                        break;
                    case "DISCONNECT":
                        System.out.println("Disconnect command received");
                        return;
                    default:
                        System.err.println("Received unknown command: " + command);
                }
            }
            catch (SocketException e) {
                System.err.println("Connection with client reset");
                System.err.println(e);
                break;
            }
            catch (EOFException e) {
                System.err.println("Failed while interacting with client.");
                System.err.println(e);
                break;
            }
            catch (IOException e) {
                System.err.println("Failed while interacting with client");
                System.err.println(e);
            }
        }
    }

    private void sendFile() {
        try{
            String fileName = dataFromClient.readUTF();
            if (fileName != null) {
                System.out.println("Sending file: " + fileName);
                File f = new File(fileName);
                if(!f.exists())
                {
                    dataToClient.writeUTF("File Not Found");
                }
                else {
                    dataToClient.writeUTF("SERVER_IS_READY");
                    String clientConfirmation = dataFromClient.readUTF();
                    if (clientConfirmation.equals("CLIENT_IS_READY")) {
                        FileInputStream fin = new FileInputStream(f);
                        int ch;
                        do {
                            ch = fin.read();
                            dataToClient.writeUTF(String.valueOf(ch));
                        }
                        while (ch != -1);
                        fin.close();
                    }
                    else{
                        System.out.println("Client aborted operation");
                    }
                }
            } else {
                System.out.println("No file name passed");
            }
    }
        catch(IOException e) {
            System.err.println("Failed while sending file");
            System.err.println(e);
        }
    }

    private void sendKey() {
        String table = generateTable(ENCRYPTION_KEY);
        try {
            dataToClient.writeUTF(table);
        } catch (IOException e) {
            System.err.println("Failed to send cipher key table.");
            System.err.println(e);
        }
    }

    private String generateTable(String key) {
        String s = prepareText(key + "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        int len = s.length();
        for (int i = 0, k = 0; i < len; i++) {
            char c = s.charAt(i);
            if (positions[c - 'A'] == null) {
                table[k / 5][k % 5] = c;
                positions[c - 'A'] = new Point(k % 5, k / 5);
                k++;
            }
        }
        return tableToString(table);
    }

    private String prepareText(String s) {
        s = s.toUpperCase().replaceAll("[^A-Z]", "");
        return s.replace("Q", "");
    }

	private String tableToString(char[][] table) {
        String stringTable = "";
    	for(int i = 0; i < 5; i++) {
    		for(int j = 0; j < 5; j++){
    			stringTable += table[i][j] + "";
        	}
    	}
		return stringTable;
    }

    private void sendMessage() {
        String message = encode(prepareText(DECODED_MESSAGE));
        try {
            dataToClient.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Failed to send cipher encrypted message.");
            System.err.println(e);
        }
    }

    private void receiveMessage() {
        try {
            String decoded = dataFromClient.readUTF();
            System.out.println("Received decryption attempt: " + decoded);
            if (DECODED_MESSAGE.equals(decoded)) {
                dataToClient.writeUTF("CORRECT");
                String id = dataFromClient.readUTF();
                for (int i = 0; i < 1000; i++) {
                    System.out.print("*");
                }
                System.out.println();
                System.out.println("CORRECT ANSWER FROM: " + id);
                for (int i = 0; i < 1000; i++) {
                    System.out.print("*");
                }
                System.out.println();
            } else {
                dataToClient.writeUTF("INCORRECT");
            }
        } catch (IOException e) {
            System.err.println("Failed to receive cipher decoded message.");
            System.err.println(e);
        }
    }

    private String encode(String s) {
        StringBuilder sb = new StringBuilder(s);

        for (int i = 0; i < sb.length(); i += 2) {
            if (i == sb.length() - 1) {
                sb.append(sb.length() % 2 == 1 ? 'X' : "");
            }
            else if (sb.charAt(i) == sb.charAt(i + 1)) {
                sb.insert(i + 1, 'X');
            }
        }
        return codec(sb, 1);
    }

    private String codec(StringBuilder text, int direction) {
        int len = text.length();
        for (int i = 0; i < len; i += 2) {
            char a = text.charAt(i);
            char b = text.charAt(i + 1);

            int row1 = positions[a - 'A'].y;
            int row2 = positions[b - 'A'].y;
            int col1 = positions[a - 'A'].x;
            int col2 = positions[b - 'A'].x;

            if (row1 == row2) {
                col1 = (col1 + direction) % 5;
                col2 = (col2 + direction) % 5;

            } else if (col1 == col2) {
                row1 = (row1 + direction) % 5;
                row2 = (row2 + direction) % 5;

            } else {
                int tmp = col1;
                col1 = col2;
                col2 = tmp;
            }

            text.setCharAt(i, table[row1][col1]);
            text.setCharAt(i + 1, table[row2][col2]);
        }
        return text.toString();
    }
}
