import java.util.*;
import java.io.*;

public class Firewall {

  // nested hashmap to store the rules
  // first layer for direction
  // second layer of protocol
  // third layer is TreeMap, whose key is a range of IP, value is a list of port ranges
  public Map<String, Map<String, TreeMap<IPRange, int[][]>>> rules;

  public Firewall(String file) {
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

    rules.get("inbound").put("tcp", new TreeMap<>());
    rules.get("inbound").put("udp", new TreeMap<>());

    rules.get("outbound").put("tcp", new TreeMap<>());
    rules.get("outbound").put("udp", new TreeMap<>());
  }

  private void addEntry(RuleEntry entry) {
    TreeMap<IPRange, int[][]> curMap = rules.get(entry.direction).get(entry.protocol);

    // add new portRange to the existing ranges(if there already exists one)
    int[][] newPortRanges = addPortRange(curMap.get(entry.ipRange), entry.portRange);
    curMap.put(entry.ipRange, newPortRanges);
  }


  // add new portRange to the existing ranges(if there already exists one)
  private int[][] addPortRange (int[][] ranges, int[] range) {

    // if no previous ranges exist, directly return with single range
    if (ranges == null || ranges.length == 0) {return new int[][]{range};}
    List<int[]> res = new ArrayList<>();
    int target = lowerBound(range, ranges);
    boolean inserted = false;
    for (int i = 0; i < ranges.length; i++) {
        if (i == target && !inserted) {
            merge(res, range);
            inserted = true;
            i--;
        } else {
            merge(res, ranges[i]);
        }
    }
      
    if (!inserted) {
        merge(res, range);
    }
    
    int[][] ans = new int[res.size()][2];
    for (int i = 0; i < ans.length; i++) {
        ans[i]  = res.get(i);
    }
    return ans;
  }


  // helper function to merge the current range to res
  private void merge(List<int[]> res, int[] range) {
    if (res.size() == 0) {res.add(range); return;}
    int[] prev = res.get(res.size() - 1);
    if (range[0] > prev[1]) {res.add(range);}
    else {
      if (range[1] > prev[1]) {
        res.remove(res.size() - 1);
        res.add(new int[]{prev[0], range[1]});
      }
    }
  }
          
  private int lowerBound(int[] target, int[][] ranges) {
    int lo = 0, hi = ranges.length;
    int mid;
    while (lo < hi) {
      mid = lo + (hi - lo) / 2;
      if (ranges[mid][0] < target[0]) lo = mid + 1;
      else hi = mid;
    }
    return lo;
  }

  public boolean acceptPacket(String direction, String protocol, int port, String ip) {
    TreeMap<IPRange, int[][]> curMap = rules.get(direction).get(protocol);
    // System.out.println(curMap);
    long ipLong = ipToLong(ip);
    List<IPRange> candidateList = new ArrayList<>(curMap.headMap(new IPRange(ipLong, ipLong), true).keySet());
    Collections.sort(candidateList, (a, b) -> (b.hi - a.hi > 0 ? 1 : -1));
    for (IPRange range : candidateList) {
      if (range.hi < ipLong) {break;}

      // check port
      if (inRange(curMap.get(range), port)) {return true;}
    }
    return false;
  }


  // helper function to check if a port number is within the list of ranges
  private boolean inRange(int[][] ranges, int port) {
    int lo = 0, hi = ranges.length;
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (ranges[mid][1] < port) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo < ranges.length && ranges[lo][0] <= port;
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