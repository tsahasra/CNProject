import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HandShake extends Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String header ;
	String zerobits;
	int peerID;
	
	public HandShake(int peerId){
		header = "P2PFILESHARINGPROJ";
		peerID = peerId;
		zerobits = "0000000000";
	} 
	
	public void write(DataOutputStream outputStream) throws IOException {
        byte[] protocolId = header.getBytes();
        byte[] zero = zerobits.getBytes();
        byte[] peerIdBits = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(peerID).array();
        outputStream.write (protocolId, 0, protocolId.length);
        outputStream.write(zero, 0, zero.length);
        outputStream.write(peerIdBits, 0, peerIdBits.length);
    }
		
}