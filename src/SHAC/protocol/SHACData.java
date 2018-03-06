package SHAC.protocol;

import java.util.ArrayList;

public class SHACData {
    public static final String CURRENT_VERSION = "SHC1.0";
    public String version;
    public int nodeCount;
    public NodeType nodeTypeFlag;
    public ArrayList<SHACNode> dataNodes;
    
    public SHACData(int nodeCount, NodeType nodeTypeFlag) {
        this.version = CURRENT_VERSION;
        this.nodeCount = nodeCount;
        this.nodeTypeFlag = nodeTypeFlag;
        if(nodeCount > 0)
            dataNodes = new ArrayList<SHACNode>();
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        String formattedData = String.format("Version: %s\n", this.version);
        formattedData += String.format("Node count: %d\n", this.nodeCount);
        formattedData += String.format("Node Type: %s\n", this.nodeTypeFlag.toString());
        if(nodeCount > 0) {
            formattedData += "Nodes:\n";
            for(SHACNode node : this.dataNodes)
                formattedData += String.format("%s\n", node.toString());
        }
        
        return formattedData;
    }
}
