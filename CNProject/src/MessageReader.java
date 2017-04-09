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
			byte[] ir = new byte[4];
			readFully(ir,0,4);
			int messageLength = ByteBuffer.wrap(ir).getInt() - 1;
			System.out.println(messageLength);
			byte type = readByte();
			byte[] payload = null;
			if (messageLength > 1) {
				payload = new byte[messageLength];
				readFully(payload, 0, messageLength);
			}
			m = new Message(messageLength, type, payload);
		} else {
			readFully(new byte[28], 0, 28);
			byte[] peerid = new byte[4];
			readFully(peerid, 0, 4);
			int peerID = ByteBuffer.wrap(peerid).getInt();
			m = new HandShake(peerID);
			isHandshakeDone = true;
		}

		return m;
	}

}
