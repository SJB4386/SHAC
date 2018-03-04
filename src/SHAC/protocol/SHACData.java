package SHAC.protocol;

import java.util.ArrayList;

public class SHACData {
    public static final String VERSION = "SHC1.0";
    public int nodeCount;
    public NodeType nodeTypeFlag;
    public ArrayList<SHACNode> nodes;
    
    public SHACData(int nodeCount, NodeType nodeTypeFlag) {
        this.nodeCount = nodeCount;
        this.nodeTypeFlag = nodeTypeFlag;
        if(nodeCount > 0)
            nodes = new ArrayList<SHACNode>();
    }
}
