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

	//InputStream inputStream;
	/**
	 * @param in
	 * @throws IOException
	 */

	private boolean isHandshakeDone = false;

	public MessageReader(InputStream in) throws IOException {
		super(in);
		//inputStream = in;
	}

	public Object readObject() throws Exception {
		Message m = null;
		if (isHandshakeDone) {
			int messageLength = readInt();
            Byte type = readByte();
            byte[] payload = null;
            if ((messageLength) > 1) {
            	payload = new byte[messageLength -1];
                readFully(payload, 0, messageLength -1);
            }
            
            m = new Message(messageLength, type,payload);
			System.out.println("message read: "  +type.intValue());
			/*byte[] ir = new byte[4];
			try {
				while (inputStream.available() == 0)
					;
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception();
			}
			int hsmessage = 0;
			int messageLength = 0;

			try {
				hsmessage = inputStream.read(ir, 0, 4);
				messageLength = ByteBuffer.wrap(ir).getInt();

				System.out.println(messageLength);
				byte[] b = new byte[messageLength];
				while (inputStream.available() < messageLength)
					;
				inputStream.read(b, 0, messageLength);
				System.out.println("After reading message:" + messageLength);
				byte type = b[0];
				byte[] payload = null;
				if (messageLength > 1) {
					payload = new byte[messageLength - 1];
					System.arraycopy(b, 1, payload, 0, messageLength - 1);
				}
				m = new Message(messageLength, type, payload);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception();
			}
			/*
			 * byte[] ir = new byte[4]; readFully(ir,0,4); int messageLength =
			 * ByteBuffer.wrap(ir).getInt(); System.out.println(messageLength);
			 * byte type = readByte(); byte[] payload = null; if (messageLength
			 * > 1) { payload = new byte[messageLength-1]; readFully(payload, 0,
			 * messageLength-1); }
			 */

			/*
			 * int messageLength = readInt(); byte type = readByte(); byte[]
			 * payload = null; if (messageLength > 1) { payload = new
			 * byte[messageLength-1]; readFully(payload, 0, messageLength-1); }
			 */

			
		} else {
			byte[] b = new byte[32];
			read(b);
			byte[] peerid = new byte[4];
			System.arraycopy(b, 28, peerid, 0, 4);
			int peerID = ByteBuffer.wrap(peerid).getInt();
			m = new HandShake(peerID);
			isHandshakeDone = true;
			System.out.println("Handshake sent");
		}

		return m;
	}

}
