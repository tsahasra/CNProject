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
	
	private boolean isHandshakeDone = false;
	
	public MessageReader(InputStream in) throws IOException {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	
	public Object readObject() throws IOException{
		Message m = null;
		if(isHandshakeDone)
		{
		int messageLength = readInt() - 1;
		byte type = readByte();
		byte[] payload = null;
		readFully(payload, 0 , messageLength);
		m = new Message(messageLength,type,payload);
		}
		else
		{
			skipBytes(28);
			int peerID = readInt() - 1;
			m = new HandShake(peerID); 
			isHandshakeDone = true;
		}
	
		return m;
	}
	

}
