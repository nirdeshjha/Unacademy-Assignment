import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
  public static void main(String args[]) throws IOException {
    // COMMAND LINE ARGS ==> <MY_IP> <MY_PORT> <REDIS_SERVER_IP> <REDIS_SERVER_PORT>
    String host = "";
    int port = 0;

    String serverIP = "";
    int serverPort = 0;

    if (args.length != 4) {
      System.out.println("Invalid invocation. Invoke using :");
      String message = "\"java Client <MY_IP> <MY_PORT> <REDIS_SERVER_IP> <REDIS_SERVER_PORT>\"";
      System.out.println(message);
      return;
    }
    host = args[0];
    serverIP = args[2];
    try {
      port = Integer.valueOf(args[1]);
      serverPort = Integer.valueOf(args[3]);

    } catch (NumberFormatException e) {
      System.out.println("Invalid port number!");
      return;
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      System.out.print("> ");
      String command = br.readLine();
      String[] commandArgs = command.split(" ");
      // System.out.println(command);

      if (commandArgs.length == 1 && commandArgs[0].equals("EXIT")) {
        break;
      } else {
        // send the COMMAND to the REDIS_SERVER
        Socket socket = new Socket(serverIP, serverPort);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        String message = host + " " + port + " " + command;
        out.writeUTF(message);

        out.close();
        socket.close();

        // wait for the response from the server
        ServerSocket server = new ServerSocket(port);
        System.out.println("Waiting for the response....");
        socket = server.accept();

        DataInputStream input = new DataInputStream(socket.getInputStream());
        String serverResponse = input.readUTF();
        System.out.println(serverResponse);

        input.close();
        socket.close();
        server.close();
      }
    }

    br.close();
  }
}