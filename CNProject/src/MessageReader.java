import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class MessageReader extends DataInputStream{

	/**
	 * @param in
	 * @throws IOException
	 */
	
	boolean isHandshakeDone;
	
	public MessageReader(InputStream in , boolean isHSDone) throws IOException {
		super(in);
		// TODO Auto-generated constructor stub
		this.isHandshakeDone = isHSDone;
	}
	
	
	public Object readObject() throws IOException{
		Message m = null;
		if(isHandshakeDone)
		{
		int messageLength = readInt() - 1;
		byte[] payload = null;
		readFully(payload, 5 , messageLength);
		m = new Message(messageLength,readByte(),payload);
		}
		else
		{
			skipBytes(28);
			int peerID = readInt() - 1;
			m = new HandShake(peerID); 
			
		}
	
		return m;
	}
	

}
