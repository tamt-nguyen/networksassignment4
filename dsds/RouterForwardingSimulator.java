import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RouterForwardingSimulator {
    private static final String ROUTER_ID = "2879";
    private Map<String, String> forwardingTable = new HashMap<>();
    private PriorityQueue<Packet> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(Packet::getPriority).reversed());
    private Queue<Packet> wfqQueue = new LinkedList<>();
    private List<Packet> processingLog = new ArrayList<>();

    public RouterForwardingSimulator() {
        initializeForwardingTable();
    }

    //creates forwarding table with ip addresses and values
    private void initializeForwardingTable() {
        forwardingTable.put("192.168.1.", "Interface 1");
        forwardingTable.put("192.168.2.", "Interface 2");
        forwardingTable.put("10.0.0.", "Interface 3");
        forwardingTable.put("172.16.0.", "Interface 4");
        forwardingTable.put("192.168.1.1", "Interface 5");
        forwardingTable.put("192.168.1.2", "Interface 6");
        forwardingTable.put("10.0.0.1", "Interface 7");
        forwardingTable.put("172.16.0.1", "Interface 8");
        forwardingTable.put("192.168.", "Interface 9");
        forwardingTable.put("10.", "Interface 10");
        forwardingTable.put("172.", "Interface 11");
        forwardingTable.put("0.0.0.0", "Default Route");
        forwardingTable.put("2879.", "Special Interface");
        forwardingTable.put("2879.56.", "High Priority Interface");
        forwardingTable.put("2879.78.", "Medium Priority Interface");
    }

    public void forwardPacket(String destinationIP, int priority) throws IOException {
        String bestMatch = "";
        int maxLength = -1;
        boolean routerIDMatched = false;
        boolean holBlockingDetected = false;
        Packet frontPacket = null;
    
        for (String prefix : forwardingTable.keySet()) {
            if (destinationIP.startsWith(prefix)) {
                int prefixLength = prefix.length();
                
                //use ROUTER_ID is available
                if (prefix.startsWith(ROUTER_ID) && prefixLength >= maxLength) {
                    bestMatch = prefix;
                    maxLength = prefixLength;
                    routerIDMatched = true;
                }
                //uses the longest prefix if no ROUTER_ID match
                else if (!routerIDMatched && prefixLength > maxLength) {
                    bestMatch = prefix;
                    maxLength = prefixLength;
                }
            }
        }
    
        String interfaceName = forwardingTable.getOrDefault(bestMatch, "Default Route");
        Packet packet = new Packet(destinationIP, interfaceName, System.currentTimeMillis(), priority);
        processingLog.add(packet);
    
        //checks for HOL blocking
        if (!priorityQueue.isEmpty()) {
            frontPacket = priorityQueue.peek();
            if (frontPacket.getInterfaceName().equals(interfaceName)) {
                holBlockingDetected = true;
                logHOLBlocking(frontPacket);
            }
        }
    
        //add to packet scheduling
        priorityQueue.add(packet);
        wfqQueue.add(packet);
    
        //log forwarding decision
        try (FileWriter logWriter = new FileWriter("simulation_log.txt", true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(packet.getTimestamp()));
            logWriter.write("[" + timestamp + "] Packet to " + destinationIP + " forwarded to " + interfaceName + " with priority " + priority + "\n");
            
            if (holBlockingDetected && frontPacket != null) {
                logWriter.write("[" + timestamp + "] HOL Blocking detected for packet to " + frontPacket.getDestinationIP() + " on " + frontPacket.getInterfaceName() + "\n");
            }
        }
    
        // feedback
        System.out.println("Packet destined for " + destinationIP + " forwarded to " + interfaceName + " with priority " + priority);
        
        if (holBlockingDetected && frontPacket != null) {
            System.out.println("HOL Blocking detected for packet to " + frontPacket.getDestinationIP() + " on " + frontPacket.getInterfaceName());
        }
    }
    //time based HQL blocking logging
    private void logHOLBlocking(Packet frontPacket) throws IOException {
        try (FileWriter logWriter = new FileWriter("hol_blocking_log.txt", true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            logWriter.write("[" + timestamp + "] HOL Blocking detected for packet to " + frontPacket.getDestinationIP() + " on " + frontPacket.getInterfaceName() + "\n");
        }
    }
    //priority queue algorithm
    public void processPriorityQueue() {
        while (!priorityQueue.isEmpty()) {
            Packet packet = priorityQueue.poll();
            System.out.println("Priority Scheduling processing to " + packet.getDestinationIP() + " with priority " + packet.getPriority());
        }
    }
    //WFQ algorithm
    public void processWFQQueue() {
        int weight = 1;
        while (!wfqQueue.isEmpty()) {
            Packet packet = wfqQueue.poll();
            for (int i = 0; i < weight; i++) {
                System.out.println("WFQ Scheduling processing to " + packet.getDestinationIP() + " with priority " + packet.getPriority());
            }
            weight++;
        }
    }

    public static void main(String[] args) throws IOException {
        RouterForwardingSimulator router = new RouterForwardingSimulator();
       
        router.forwardPacket("192.168.1.15", 3);
        router.forwardPacket("192.168.1.1", 1);
        router.forwardPacket("10.0.0.5", 2);
        router.forwardPacket("2879.0.0.1", 5);
        router.forwardPacket("2879.56.1.1", 4);
        
        //process the queues
        router.processPriorityQueue();
        router.processWFQQueue();
    }
}

//this class handles the packets
class Packet {
    private String destinationIP;
    private String interfaceName;
    private long timestamp;
    private int priority;

    public Packet(String destinationIP, String interfaceName, long timestamp, int priority) {
        this.destinationIP = destinationIP;
        this.interfaceName = interfaceName;
        this.timestamp = timestamp;
        this.priority = priority;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPriority() {
        return priority;
    }
}