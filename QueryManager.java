import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class QueryManager extends Thread {
  private Redis cache;
  private String command;
  private String clientHost;
  private int clientPort;

  public QueryManager(Redis cacheInstance, String command) {
    cache = cacheInstance;
    /**
     * MESSAGE FORMAT
     * 
     * <CLIENT_HOST> <CLIENT_IP> COMMAND
     * 
     * COMMANDS ==>
     * 
     * ***********************************************
     * 
     * ZADD KEY SCORE_1 MEMBER_1 SCORE_2 MEMBER_2 ....
     * 
     * **********************************************
     * 
     * ZRANGE KEY L R WITHSCORES(?)
     * 
     * ***********************************************
     * 
     * ZRANK KEY MEMBER
     * 
     * ***********************************************
     * 
     */
    String info[] = command.split(" ", 3);
    this.clientHost = info[0];
    try {
      this.clientPort = Integer.valueOf(info[1]);

    } catch (NumberFormatException e) {
      // TODO: add some neccessary detail

    }
    this.command = info[2];
  }

  private void sendResponse(String message) {
    Socket socket;

    DataOutputStream output;
    try {
      socket = new Socket(clientHost, clientPort);
      output = new DataOutputStream(socket.getOutputStream());
      output.writeUTF(message);

      output.close();
      socket.close();
    } catch (IOException e) {
      System.out.println("Invalid CLIENT_IP<" + clientHost + "> or CLIENT_PORT<" + clientPort + ">");
    }
  }

  @Override
  public void run() {
    /**
     * ***********************************************
     * 
     * ZADD KEY SCORE_1 MEMBER_1 SCORE_2 MEMBER_2 ....
     * 
     * **********************************************
     * 
     * ZRANGE KEY L R WITHSCORES(?)
     * 
     * ***********************************************
     * 
     * ZRANK KEY MEMBER
     * 
     * ***********************************************
     * 
     * PUT KEY VALUE
     * 
     * ***********************************************
     * 
     * GET KEY
     * 
     * ***********************************************
     * 
     * EXPIRE KEY TIME
     * 
     * ***********************************************
     */

    String[] commandArgs = command.split(" ");
    boolean invalidCommand = false;

    if (commandArgs.length < 1) {
      invalidCommand = true;
    } else if (commandArgs[0].equals("ZADD")) {

      // ZADD KEY SCORE_1 MEMBER_1 SCORE_2 MEMBER_2 ...
      if (commandArgs.length % 2 != 0) {
        // invalid arguments provided
        invalidCommand = true;
      } else {

        String key = commandArgs[1];
        int successfulInsert = 0;

        for (int j = 2; j < commandArgs.length; j += 2) {
          String score = commandArgs[j];
          String member = commandArgs[j + 1];

          try {
            successfulInsert += cache.insert(key, Double.valueOf(score), member);
          } catch (NumberFormatException e) {
            e.printStackTrace();
            invalidCommand = true;
            break;
          }
        }

        this.sendResponse("Succesfully inserted " + successfulInsert + " members.");
      }
    } else if (commandArgs[0].equals("ZRANGE")) {

      // ZRANGE KEY L R WITHSCORES?
      int l = 0, r = 0;
      String key = commandArgs[1];

      boolean validWithScoreQuery = (commandArgs.length == 5) && (commandArgs[4].compareTo("WITHSCORES") == 0);

      if (commandArgs.length != 4 && !validWithScoreQuery) {
        invalidCommand = true;
      } else {

        try {
          l = Integer.valueOf(commandArgs[2]);
          r = Integer.valueOf(commandArgs[3]);
        } catch (NumberFormatException e) {
          invalidCommand = true;
        }

        if (!invalidCommand) {
          boolean withScores = validWithScoreQuery;
          String response = "";

          ArrayList<String> result = cache.getInRange(key, l, r, withScores);
          for (String s : result) {
            response += s + "\n";
          }
          response += "\n";

          this.sendResponse(response);
        }
      }

    } else if (commandArgs[0].equals("ZRANK")) {

      // ZRANK KEY MEMBER
      if (commandArgs.length != 3) {
        invalidCommand = true;
      } else {
        String key = commandArgs[1];
        String member = commandArgs[2];
        this.sendResponse("\"" + cache.getRank(key, member) + "\"");
      }
    } else if (commandArgs[0].equals("PUT")) {
      // PUT KEY VALUE
      String key = commandArgs[1];
      String value = commandArgs[2];
      String response = cache.put(key, value);
      this.sendResponse("Successfully " + response + "!");
    } else if (commandArgs[0].equals("GET")) {
      // GET <KEY>
      String key = commandArgs[1];
      this.sendResponse(cache.get(key));
    } else if (commandArgs[0].equals("EXPIRE")) {
      // EXPIRE <KEY> <TIME in seconds>
      String key = commandArgs[1];
      long time = 0;
      try {
        time = Long.valueOf(commandArgs[2]);
      } catch (NumberFormatException e) {
        this.sendResponse("Invalid query format or invalid time!");
        return;
      }

      this.sendResponse(cache.setExpiry(key, time));
    } else {
      invalidCommand = true;
    }

    if (invalidCommand) {
      this.sendResponse("Invalid operation :(. Try again!");
    }
  }
}