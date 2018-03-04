package SHAC.protocol;

import java.math.BigInteger;

public class SHACProtocol {

    public static byte[] encodePacketData(SHACData packetData) {
        byte[] encodedVer = SHACData.VERSION.getBytes();
        byte[] encodedLength = BigInteger.valueOf(packetData.nodeCount).toByteArray();
        return null;
    }

    public static SHACData decodePacketData(byte[] packetData) {
        return null;
    }
}
