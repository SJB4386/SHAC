package SHAC.protocol;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SHACNode {
    private final String datePattern = "yyyy-MM-dd HH:mm:ss.SSS";
    public InetAddress ip;
    public Date timestamp;
    public boolean isAvailable;
    private SimpleDateFormat dateFormat;
    
    
    public SHACNode(InetAddress ip, Date timestamp) {
        this.ip = ip;
        this.timestamp = timestamp;
        this.isAvailable = true;
        dateFormat = new SimpleDateFormat(this.datePattern);
    }
    
    @Override
    public String toString() {
        String formattedDate = dateFormat.format(this.timestamp);
        String formattedNode = String.format("IP: %s %s Available: %s", this.ip.getHostAddress(), formattedDate,
                isAvailable);
        return formattedNode;
    }
}
