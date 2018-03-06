package SHAC.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SHACProtocolTest {

    @Test
    void testEncodePacketData() {
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
    void testEncodePacketDataWithNodes() {
        
    }
    
    @Test
    void testCombineByteArrays() {
        byte[] a = {1};
        byte[] b = {2};
        byte[] expectedArray = {1, 2};
        byte[] actualArray = SHACProtocol.combineByteArrays(a, b);
        assertEquals(expectedArray[0], actualArray[0]);
        assertEquals(expectedArray[1], actualArray[1]);
    }
}
