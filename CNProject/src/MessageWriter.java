import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MessageWriter {
	public Message m;
	public OutputStream os;

	/**
	 * @param m
	 * @param os
	 */
	public MessageWriter(Message m, OutputStream os) {
		this.m = m;
		this.os = os;
	}

	public void writeObject() throws IOException {
		if (m instanceof HandShake) {
			HandShake hs = (HandShake) m;

			os.write(hs.header, 0, hs.header.length);
			os.write(hs.zerobits, 0, hs.zerobits.length);
			os.write(hs.peerID, 0, hs.peerID.length);
		} else {
			os.write(ByteBuffer.allocate(4).putInt(m.length).array());
			os.write(new byte[]{Byte.valueOf(Integer.toString(m.type))});
			if ((m.payload != null) && (m.payload.length > 0)) {
				os.write(m.payload, 0, m.payload.length);
			}
		}
		os.flush();
	}

}