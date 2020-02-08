import java.util.*;
import java.io.*;

class Test {
  public static void main(String[] args) {

    String ruleFile = "test3.csv";


    long fw_loadStart = System.currentTimeMillis();
    Firewall fw = new Firewall(ruleFile);
    long fw_loadEnd = System.currentTimeMillis();

    long fwNaive_loadStart = System.currentTimeMillis();
    FirewallNaive fwNaive = new FirewallNaive(ruleFile);
    long fwNaive_loadEnd = System.currentTimeMillis();

    System.out.println("---------------------------------------------------------------");

    System.out.println("Time spend to load rules for Firewall: " + (fw_loadEnd - fw_loadStart) + "ms");
    System.out.println("Time spend to load rules for FirewallNaive: " + (fwNaive_loadEnd - fwNaive_loadStart) + "ms");


    // System.out.println(fw.acceptPacket("inbound", "tcp", 80, "192.168.1.2"));
    // System.out.println(fw.acceptPacket("outbound", "tcp", 10234, "192.168.10.11"));
    // System.out.println(fw.acceptPacket("inbound", "udp", 53, "192.168.2.1"));
    // System.out.println(fw.acceptPacket("inbound", "tcp", 81, "192.168.1.2"));
    // System.out.println(fw.acceptPacket("inbound", "udp", 24, "52.12.48.92"));


    // System.out.println(fwNaive.acceptPacket("inbound", "tcp", 80, "192.168.1.2"));
    // System.out.println(fwNaive.acceptPacket("outbound", "tcp", 10234, "192.168.10.11"));
    // System.out.println(fwNaive.acceptPacket("inbound", "udp", 53, "192.168.2.1"));
    // System.out.println(fwNaive.acceptPacket("inbound", "tcp", 81, "192.168.1.2"));
    // System.out.println(fwNaive.acceptPacket("inbound", "udp", 24, "52.12.48.92"));


    System.out.println("---------------------------------------------------------------");
    Scanner in = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
    PrintWriter out = new PrintWriter(System.out);
    int numOfQueries = in.nextInt();
    List<Query> queryList = new ArrayList<>(numOfQueries);

    while (in.hasNext()) {
      queryList.add(new Query(in.next(), in.next(), in.nextInt(), in.next()));
    }


    System.out.println("Correctness test (comparing with the result from naive implementation): ");
    for (Query q : queryList) {
      assert fw.acceptPacket(q.direction, q.protocol, q.port, q.ip) == 
      fwNaive.acceptPacket(q.direction, q.protocol, q.port, q.ip);
    }
    System.out.println("Correctness test completed successfully!");
    System.out.println("---------------------------------------------------------------");


    

    System.out.println("Efficiency test (comparing with the naive implementation): ");

    long fwQeuryStart = System.currentTimeMillis();
    for (Query q : queryList) {
      fw.acceptPacket(q.direction, q.protocol, q.port, q.ip);
    }
    long fwQueryEnd = System.currentTimeMillis();
    System.out.println("For FireWall, the time spend to make " + numOfQueries +  " queries: " +  (fwQueryEnd - fwQeuryStart));



    long fwNaiveQueryStart = System.currentTimeMillis();
    for (Query q : queryList) {
      fwNaive.acceptPacket(q.direction, q.protocol, q.port, q.ip);
    }
    long fwNaiveQueryEnd = System.currentTimeMillis();
    System.out.println("For FirewallNaive, the time spend to make " + numOfQueries +  " queries: " +  (fwNaiveQueryEnd - fwNaiveQueryStart));
    System.out.println("---------------------------------------------------------------");
    // out.flush();
   
  }

  static class Query {
    String direction, protocol, ip;
    int port;
    public Query (String direction, String protocol, int port, String ip) {
      this.direction = direction;
      this.protocol = protocol;
      this.port = port;
      this.ip = ip;
    }
  }
}