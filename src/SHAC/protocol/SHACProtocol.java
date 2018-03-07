package SHAC.protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

public class SHACProtocol {
    public static final int SHAC_SOCKET = 9746;
    public static final byte[] AVAILABLE = {1};
    public static final byte[] UNAVAILABLE = {0};
    public static final int VER_FIELD_LENGTH = 6;
    public static final int NODE_COUNT_LENGTH = 1;
    public static final int FLAG_FIELD_LENGTH = 1;
    public static final int IP_LENGTH = 4;
    public static final int TIMESTAMP_LENGTH = 8;

    public static byte[] encodePacketData(SHACData packetData) {
        byte[] encodedVer = SHACData.CURRENT_VERSION.getBytes();
        byte[] encodedLength = { (byte) packetData.nodeCount };
        byte[] encodedPacket = combineByteArrays(encodedVer, encodedLength);
        encodedPacket = combineByteArrays(encodedPacket, packetData.nodeTypeFlag.getFlag());
        if (packetData.nodeCount > 0) {
            byte[] nodeData;
            for (SHACNode node : packetData.dataNodes) {
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
        SHACData decodedPacket;
        int packetDataIndex = 0;
        byte[] ver = Arrays.copyOfRange(packetData, packetDataIndex, VER_FIELD_LENGTH);
        packetDataIndex += VER_FIELD_LENGTH;
        int nodeCount = packetData[packetDataIndex];
        packetDataIndex += NODE_COUNT_LENGTH;
        int flags = packetData[packetDataIndex];
        NodeType nodeType;
        switch (flags) {
            case 1:
                nodeType = NodeType.CLIENT;
                break;
            case 2:
                nodeType = NodeType.SERVER;
                break;
            case 4:
                nodeType = NodeType.PEER;
                break;
            default:
                nodeType = null;
                break;
        }
        packetDataIndex += FLAG_FIELD_LENGTH;
        
        decodedPacket = new SHACData(nodeCount, nodeType);
        decodedPacket.setVersion(new String(ver, StandardCharsets.UTF_8));
        InetAddress nodeAddress;
        Date nodeTimestamp;
        boolean nodeAvailability;
        for(int i = 0; i < nodeCount; i++) {
            try {
                nodeAddress = InetAddress.getByAddress(Arrays.copyOfRange(packetData, packetDataIndex, packetDataIndex + IP_LENGTH));
                packetDataIndex += IP_LENGTH;
                nodeTimestamp = new Date(bytesToLong(Arrays.copyOfRange(packetData, packetDataIndex, packetDataIndex + TIMESTAMP_LENGTH)));
                packetDataIndex += TIMESTAMP_LENGTH;
                if(packetData[packetDataIndex] > 0) {
                    nodeAvailability = true;
                    packetDataIndex += AVAILABLE.length;
                }
                else{
                    nodeAvailability = false;
                    packetDataIndex += UNAVAILABLE.length;
                }
                decodedPacket.dataNodes.add(new SHACNode(nodeAddress, nodeTimestamp));
                decodedPacket.dataNodes.get(i).isAvailable = nodeAvailability;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return decodedPacket;
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

    /**
     * Method found here: 
     * https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
     */
    public static byte[] longToBytes(long inputLong) {
        byte[] result = new byte[Long.BYTES];
        for (int i = Byte.SIZE - 1; i >= 0; i--) {
            result[i] = (byte) (inputLong & 0xFF);
            inputLong >>= Byte.SIZE;
        }
        return result;
    }

    /**
     * Method found here: 
     * https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java/29132118#29132118
     */
    public static long bytesToLong(byte[] inputBytes) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (inputBytes[i] & 0xFF);
        }
        return result;
    }
}
