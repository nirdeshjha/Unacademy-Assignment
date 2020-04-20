import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Redis {
  private ConcurrentHashMap<String, BalancedTree> meSet;
  private ConcurrentHashMap<String, String> keyValue;
  private ConcurrentHashMap<String, Long> expiry;

  Redis() {
    meSet = new ConcurrentHashMap<String, BalancedTree>();
    keyValue = new ConcurrentHashMap<>();
    expiry = new ConcurrentHashMap<>();
  }

  // *********** Z_OPERATIONS HERE ***************

  public int insert(String key, double score, String member) {
    if (!meSet.containsKey(key)) {
      meSet.put(key, new BalancedTree());
    }

    BalancedTree avl = meSet.get(key);
    return avl.insert(score, member);
  }

  public ArrayList<String> getInRange(String key, int l, int r, boolean withScores) {
    if (!meSet.containsKey(key)) {
      return new ArrayList<String>();
    }

    BalancedTree avl = meSet.get(key);
    return avl.getInRange(l, r, withScores);
  }

  public int getRank(String key, String member) {
    if (!meSet.containsKey(key)) {
      return -1;
    }

    BalancedTree avl = meSet.get(key);
    return avl.rank(member);
  }
  // *********** Z_OPERATIONS ENDED HERE ***************

  public String get(String key) {
    if (!keyValue.containsKey(key))
      return "-NIL-";

    long curTime = System.currentTimeMillis();
    long expireAt = expiry.get(key);

    if (expireAt == -1 || expireAt > curTime)
      return keyValue.get(key);
    else
      return "-NIL-";
  }

  public String put(String key, String value) {
    if (!keyValue.containsKey(key)) {

      keyValue.put(key, value);
      long expireAt = -1;
      expiry.put(key, expireAt);
      return "INSERTED";

    } else {

      keyValue.replace(key, value);
      long expireAt = -1;
      expiry.replace(key, expireAt);
      return "UPDATED";
    }
  }

  public String setExpiry(String key, long timeInSeconds) {
    if (keyValue.containsKey(key)) {
      long expireAt = expiry.get(key);
      long curTime = System.currentTimeMillis();
      if (expireAt != -1 && curTime >= expireAt)
        return "Invalid Key!";

      expireAt = curTime + timeInSeconds * 1000;
      expiry.replace(key, expireAt);
      return "SUCCESS";

    } else
      return "Invalid key!";
  }

  private void initiate(String host, int port) {

    try {
      ServerSocket server = new ServerSocket(port);
      while (true) {
        System.out.println("Waiting for client. Listening on port " + port + " ...");
        Socket socket = server.accept();

        DataInputStream input = new DataInputStream(socket.getInputStream());
        String command = input.readUTF();
        new QueryManager(this, command).start();
        input.close();

        socket.close();
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public static void main(String args[]) {
    /**
     * args[0] --> <host> args[1] --> <port>
     */

    if (args.length < 2) {
      System.out.println("Pass host and port as command line arguments!\n\nRun like \"java Redis <host> <port>\"\n");
      return;
    }

    String host = args[0];
    int port = 0;
    try {
      port = Integer.valueOf(args[1]);
    } catch (NumberFormatException e) {
      System.out.println("Enter a valid port please ...");
      return;
    }

    Redis cache = new Redis();
    cache.initiate(host, port);
  }
}