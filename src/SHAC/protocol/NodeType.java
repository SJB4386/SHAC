package SHAC.protocol;

public enum NodeType {
    CLIENT(1), SERVER(2), PEER(4);
    private byte flag;
    
    private NodeType(int flag) {
        this.flag = (byte) flag;
    }
    
    public byte[] getFlag() {
        byte[] flagArr = {this.flag};
        return flagArr;
    }
}
