import java.util.*;
import java.io.*;

public class FirewallNaive {

  public Map<String, Map<String, List<RuleEntry>>> rules;

  public FirewallNaive(String file) {
    initializeFirewall();

    String line = "";

    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      // read csv files line by line
      while ((line = in.readLine()) != null) {
        addEntry(new RuleEntry(line));
      }
      in.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }

  private void initializeFirewall() {
    rules = new HashMap<>();
    rules.put("inbound", new HashMap<>());
    rules.put("outbound", new HashMap<>());

    rules.get("inbound").put("tcp", new ArrayList<>());
    rules.get("inbound").put("udp", new ArrayList<>());

    rules.get("outbound").put("tcp", new ArrayList<>());
    rules.get("outbound").put("udp", new ArrayList<>());
  }



  private void addEntry(RuleEntry entry) {
    rules.get(entry.direction).get(entry.protocol).add(entry);
  }



  public boolean acceptPacket(String direction, String protocol, int port, String ip) {
    long ipLong = ipToLong(ip);
    List<RuleEntry> ruleList = rules.get(direction).get(protocol);
    for (RuleEntry e : ruleList) {
      if (e.direction.equals(direction) && 
        e.protocol.equals(protocol) && 
        (ipLong >= e.ipRange.lo && ipLong <= e.ipRange.hi) && 
        (port >= e.portRange[0] && port <= e.portRange[1])) {return true;}
    }

    return false;

  }


  private class RuleEntry {
    String direction; 
    String protocol; 
    IPRange ipRange;
    int[] portRange;

    public RuleEntry(String entry) {
      String[] items = entry.split(",");

      direction = items[0];
      protocol = items[1];

      int dashIndex;

      // parse port ranges
      portRange = new int[2];
      String portString = items[2];
      dashIndex = portString.indexOf('-');

      // only one port
      if (dashIndex == -1) {
        portRange[0] = portRange[1] = Integer.parseInt(portString);
      } 
      // port range detected
      else {
        portRange[0] = Integer.parseInt(portString.substring(0, dashIndex));
        portRange[1] = Integer.parseInt(portString.substring(dashIndex + 1));
      }

      // parse ip range
      ipRange = new IPRange();
      String ipString = items[3];
      dashIndex = ipString.indexOf('-');

      //only one ip
      if (dashIndex == -1) {
        ipRange.lo = ipRange.hi = ipToLong(ipString);
      }
      // ip range detected
      else {
        ipRange.lo = ipToLong(ipString.substring(0, dashIndex));
        ipRange.hi = ipToLong(ipString.substring(dashIndex + 1));
      }
    }

  }

  // convert string representation of IP address to long
  public long ipToLong(String ipString) {
    String[] ipSegs = ipString.split("\\.");
    long res = 0;
    for (int i = 0, offset = 9; i < 4; i++, offset -= 3) {
      res += Long.parseLong(ipSegs[i].strip()) * (long) Math.pow(10, offset);
    }
    return res;
  }

  // IPRange class to store a range of IP addresses
  class IPRange implements Comparable<IPRange>{
    long lo, hi;

    public IPRange() {}

    public IPRange(long lo, long hi) {this.lo = lo; this.hi = hi;}

    @Override
    public int hashCode() {
      return Objects.hash(lo, hi);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof IPRange)) return false;
      IPRange range = (IPRange) o;
      return this.lo == range.lo && this.hi == range.hi;
    }

    @Override
    public int compareTo(IPRange range) {
      if (this.lo == range.lo) {
        if (this.hi == range.hi) {return 0;}
        // if lo is same, the order is defined by hi (reversed)
        else {return this.hi - range.hi < 0 ? 1 : -1;}
      }
      return this.lo - range.lo < 0 ? -1 : 1;
    }

    @Override
    public String toString() {
      return lo + "-" + hi;
    }
  }


}