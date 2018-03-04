package SHAC.protocol;

import java.net.*;
import java.util.Date;

public class SHACNode {
    public InetAddress ip;
    public Date timestamp;
    public boolean isAvailable;
    
    public SHACNode(InetAddress ip, Date timestamp) {
        this.ip = ip;
        this.timestamp = timestamp;
        this.isAvailable = true;
    }
}
