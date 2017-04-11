import java.io.DataOutputStream;
import java.io.IOException;

public class MessageWriter {
	public Message m;
	public DataOutputStream os;

	/**
	 * @param m
	 * @param os
	 */
	public MessageWriter(Message m, DataOutputStream os) {
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
			os.writeInt(m.length);//(ByteBuffer.allocate(4).putInt(m.length).array());
			os.writeByte(m.type);//(new byte[]{Byte.valueOf(Integer.toString(m.type))});
			if ((m.payload != null) && (m.payload.length > 0)) {
				os.write(m.payload, 0, m.payload.length);
			}
		}
		os.flush();
	}

}