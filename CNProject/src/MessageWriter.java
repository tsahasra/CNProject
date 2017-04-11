import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
		//ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (m instanceof HandShake) {
			HandShake hs = (HandShake) m;

			os.write(hs.header, 0, hs.header.length);
			os.write(hs.zerobits, 0, hs.zerobits.length);
			os.write(hs.peerID, 0, hs.peerID.length);
		} else {
			//os.flush();
			os.writeInt(m.length);
			os.writeByte(m.type);
			if ((m.payload != null) && (m.payload.length > 0)) {
				os.write(m.payload, 0, m.payload.length);
			}
		}
		//bos.flush();
		//os.write(bos.toByteArray());
		os.flush();
	}

}