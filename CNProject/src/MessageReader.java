import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
			int messageLength = readInt() - 1;
			System.out.println(messageLength);
			byte type = readByte();
			byte[] payload = new byte[messageLength];
			readFully(payload, 0, messageLength);
			m = new Message(messageLength, type, payload);
		} else {
			skipBytes(28);
			int peerID = readInt();
			m = new HandShake(peerID);
			isHandshakeDone = true;
		}

		return m;
	}

}
