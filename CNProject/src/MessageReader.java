import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class MessageReader extends DataInputStream {

	/**
	 * @param in
	 * @throws IOException
	 */

	private boolean isHandshakeDone = false;

	public MessageReader(InputStream in) throws IOException {
		super(in);
	}

	public Object readObject() throws IOException {
		Message m = null;
		if (isHandshakeDone) {
			//byte[] ir;
			int messageLength = readInt();//ByteBuffer.wrap(ir).getInt();
			System.out.println(messageLength);
			byte[] b = new byte[messageLength];
			
			read(b, 0, messageLength);
			System.out.println("After reading message:"+messageLength);
			byte type = b[0];
			byte[] payload = null;
			if (messageLength > 1) {
				payload = new byte[messageLength-1];
				System.arraycopy(b, 1, payload, 0, messageLength-1);
			}
			
			
			/*byte[] ir = new byte[4];
			readFully(ir,0,4);
			int messageLength = ByteBuffer.wrap(ir).getInt();
			System.out.println(messageLength);
			byte type = readByte();
			byte[] payload = null;
			if (messageLength > 1) {
				payload = new byte[messageLength-1];
				readFully(payload, 0, messageLength-1);
			}*/
			m = new Message(messageLength, type, payload);
		} else {
			byte[] b = new byte[32];
			int messageLength = this.read(b);
			byte[] peerid = new byte[4];
			System.arraycopy(b, 28,peerid,0, 4);
			int peerID = ByteBuffer.wrap(peerid).getInt();
			m = new HandShake(peerID);
			isHandshakeDone = true;
		}

		return m;
	}

}
