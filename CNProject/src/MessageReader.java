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
public class MessageReader {

	InputStream inputStream;
	/**
	 * @param in
	 * @throws IOException
	 */

	private boolean isHandshakeDone = false;

	public MessageReader(InputStream in) throws IOException {
		//super(in);
		inputStream = in;
	}

	public Object readObject() throws Exception {
		if(isHandshakeDone){
			while(inputStream.available()<4);
			DataInputStream din = new DataInputStream(inputStream);
			int length;
			try {
				length = din.readInt();
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			byte type;
			try {
				type = din.readByte();
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			byte[] payload = null;//new byte[1];
			System.out.println("payload length available to be recieved:"+inputStream.available());
			//Arrays.fill(payload, Byte.valueOf(Integer.toString(0)));
			if(length>1){
				payload = new byte[length-1];
				try {
					din.read(payload);
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
			Message m = new Message(length, type, payload);
			return m;
		}else{
			byte[] header = new byte[18];
			try {
				inputStream.read(header,0,18);
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			byte[] zerobits = new byte[10];
			try {
				inputStream.read(zerobits,0,10);
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			byte[] peerId = new byte[4];
			try {
				inputStream.read(peerId);
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			HandShake h = new HandShake(ByteBuffer.wrap(peerId).getInt());
			isHandshakeDone=true;
			return h;
		}
		
		/*Message m = null;
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
			byte[] ir = new byte[4];
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
			 

			
			 * int messageLength = readInt(); byte type = readByte(); byte[]
			 * payload = null; if (messageLength > 1) { payload = new
			 * byte[messageLength-1]; readFully(payload, 0, messageLength-1); }
			 

			
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

		return m;*/
	}

}
