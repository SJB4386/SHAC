package SHAC.protocol;

public class SHACProtocol {
    public static final byte[] AVAILABLE = {1};
    public static final byte[] UNAVAILABLE = {0};

    public static byte[] encodePacketData(SHACData packetData) {
        byte[] encodedVer = SHACData.VERSION.getBytes();
        byte[] encodedLength = { (byte) packetData.nodeCount };
        byte[] encodedPacket = combineByteArrays(encodedVer, encodedLength);
        encodedPacket = combineByteArrays(encodedPacket, packetData.nodeTypeFlag.getFlag());
        if (packetData.nodeCount > 0) {
            byte[] nodeData;
            for (SHACNode node : packetData.nodes) {
                nodeData = node.ip.getAddress();
                nodeData = combineByteArrays(nodeData, longToBytes(node.timestamp.getTime()));
                if(node.isAvailable)
                    nodeData = combineByteArrays(nodeData, AVAILABLE);
                else 
                    nodeData = combineByteArrays(nodeData, UNAVAILABLE);
                encodedPacket = combineByteArrays(encodedPacket, nodeData);
            }
        }
        return encodedPacket;
    }

    public static SHACData decodePacketData(byte[] packetData) {
        return null;
    }

    /**
     * Method found here:
     * https://stackoverflow.com/questions/5683486/how-to-combine-two-byte-arrays
     */
    public static byte[] combineByteArrays(byte[] a, byte[] b) {
        byte[] combined = new byte[a.length + b.length];

        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }

    /*
     * Method found here: 
     * https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
     */
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    /*
     * Method found here: 
     * https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
     */
    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
