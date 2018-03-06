package SHAC.protocol;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;

public class SHACProtocolTest {

    @Test
    public void testEncodePacketData() {
        SHACData testData = new SHACData(0, NodeType.CLIENT);
        byte[] actualPacketData = SHACProtocol.encodePacketData(testData);
        byte[] expectedPacketData = {83, 72, 67, 49, 46, 48, 0, 1};
        assertEquals(expectedPacketData[0], actualPacketData[0]);
        assertEquals(expectedPacketData[1], actualPacketData[1]);
        assertEquals(expectedPacketData[2], actualPacketData[2]);
        assertEquals(expectedPacketData[3], actualPacketData[3]);
        assertEquals(expectedPacketData[4], actualPacketData[4]);
        assertEquals(expectedPacketData[5], actualPacketData[5]);
        assertEquals(expectedPacketData[6], actualPacketData[6]);
        assertEquals(expectedPacketData[7], actualPacketData[7]);
    }
    
    @Test
    public void testEncodePacketDataWithNode() throws UnknownHostException {
        SHACData testData = new SHACData(1, NodeType.SERVER);
        SHACNode testNode = new SHACNode(InetAddress.getByName("192.168.0.1"), new Date(1520312475950L));
        testData.nodes.add(testNode);
        byte[] actualPacketData = SHACProtocol.encodePacketData(testData);
        byte[] expectedPacketData = {83, 72, 67, 49, 46, 48, 1, 2, 
                                        (byte) 192, (byte) 168, 0, 1,
                                        0, 0, 1, 97, (byte) 249, (byte) 175, 97, 46,
                                        1};
        assertEquals(expectedPacketData[0], actualPacketData[0]);
        assertEquals(expectedPacketData[1], actualPacketData[1]);
        assertEquals(expectedPacketData[2], actualPacketData[2]);
        assertEquals(expectedPacketData[3], actualPacketData[3]);
        assertEquals(expectedPacketData[4], actualPacketData[4]);
        assertEquals(expectedPacketData[5], actualPacketData[5]);
        assertEquals(expectedPacketData[6], actualPacketData[6]);
        assertEquals(expectedPacketData[7], actualPacketData[7]);
        assertEquals(expectedPacketData[8], actualPacketData[8]);
        assertEquals(expectedPacketData[9], actualPacketData[9]);
        assertEquals(expectedPacketData[10], actualPacketData[10]);
        assertEquals(expectedPacketData[11], actualPacketData[11]);
        assertEquals(expectedPacketData[12], actualPacketData[12]);
        assertEquals(expectedPacketData[13], actualPacketData[13]);
        assertEquals(expectedPacketData[14], actualPacketData[14]);
        assertEquals(expectedPacketData[15], actualPacketData[15]);
        assertEquals(expectedPacketData[16], actualPacketData[16]);
        assertEquals(expectedPacketData[17], actualPacketData[17]);
        assertEquals(expectedPacketData[18], actualPacketData[18]);
        assertEquals(expectedPacketData[19], actualPacketData[19]);
        assertEquals(expectedPacketData[20], actualPacketData[20]);
    }
    
    @Test
    public void testDecodePacketData() {
        byte[] testData = {83, 72, 67, 49, 46, 48, 0, 1};
        SHACData actualPacketData = SHACProtocol.decodePacketData(testData);
        SHACData expectedPacketData = new SHACData(0, NodeType.CLIENT);
        assertEquals(expectedPacketData.version, actualPacketData.version);
        assertEquals(expectedPacketData.nodeCount, actualPacketData.nodeCount);
        assertEquals(expectedPacketData.nodeTypeFlag, actualPacketData.nodeTypeFlag);
    }
    
    @Test
    public void testDecodePacketDataWithNode() throws UnknownHostException {
        byte[] testData = {83, 72, 67, 49, 46, 48, 1, 2, 
                            (byte) 192, (byte) 168, 0, 1,
                            0, 0, 1, 97, (byte) 249, (byte) 175, 97, 46,
                            1};
        SHACData actualPacketData = SHACProtocol.decodePacketData(testData);
        SHACData expectedPacketData = new SHACData(1, NodeType.SERVER);
        expectedPacketData.nodes.add(new SHACNode(InetAddress.getByName("192.168.0.1"), new Date(1520312475950L)));
        assertEquals(expectedPacketData.version, actualPacketData.version);
        assertEquals(expectedPacketData.nodeCount, actualPacketData.nodeCount);
        assertEquals(expectedPacketData.nodeTypeFlag, actualPacketData.nodeTypeFlag);
        assertEquals(expectedPacketData.nodes.get(0).ip.toString(), actualPacketData.nodes.get(0).ip.toString());
        assertEquals(expectedPacketData.nodes.get(0).timestamp.getTime(), actualPacketData.nodes.get(0).timestamp.getTime());
        assertEquals(expectedPacketData.nodes.get(0).isAvailable, actualPacketData.nodes.get(0).isAvailable);
    }
    
    @Test
    public void testCombineByteArrays() {
        byte[] a = {1};
        byte[] b = {2};
        byte[] expectedArray = {1, 2};
        byte[] actualArray = SHACProtocol.combineByteArrays(a, b);
        assertEquals(expectedArray[0], actualArray[0]);
        assertEquals(expectedArray[1], actualArray[1]);
    }
}
