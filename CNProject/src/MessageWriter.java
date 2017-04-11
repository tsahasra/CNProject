import java.io.ByteArrayOutputStream;
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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (m instanceof HandShake) {
			HandShake hs = (HandShake) m;

			bos.write(hs.header, 0, hs.header.length);
			bos.write(hs.zerobits, 0, hs.zerobits.length);
			bos.write(hs.peerID, 0, hs.peerID.length);
		} else {
			bos.flush();
			bos.write(ByteBuffer.allocate(4).putInt(m.length).array());
			bos.write(new byte[]{m.type});
			if ((m.payload != null) && (m.payload.length > 0)) {
				bos.write(m.payload, 0, m.payload.length);
			}
		}
		bos.flush();
	}

}