package SHAC.protocol;

import java.util.ArrayList;

public class SHACData {
    public static final String CURRENT_VERSION = "SHC1.0";
    public String version;
    public int nodeCount;
    public NodeType nodeTypeFlag;
    public ArrayList<SHACNode> nodes;
    
    public SHACData(int nodeCount, NodeType nodeTypeFlag) {
        this.version = CURRENT_VERSION;
        this.nodeCount = nodeCount;
        this.nodeTypeFlag = nodeTypeFlag;
        if(nodeCount > 0)
            nodes = new ArrayList<SHACNode>();
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}
