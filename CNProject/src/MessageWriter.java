import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
		System.out.println("Sending message :"+m);
		if (m instanceof HandShake) {
			HandShake hs = (HandShake) m;

			os.write(hs.header, 0, hs.header.length);
			os.write(hs.zerobits, 0, hs.zerobits.length);
			os.write(hs.peerID, 0, hs.peerID.length);
		} else {
			System.out.println(os.size());
			os.writeInt(m.length);
			os.flush();
			os.writeByte(m.type);
			os.flush();
			System.out.println("payload length tp be sent:"+m.payload.length);
			if ((m.payload != null) && (m.payload.length > 0)) {
				System.out.println(Arrays.toString(m.payload));
				os.write(m.payload);
			}
		}
		//bos.flush();
		//os.write(bos.toByteArray());
		os.flush();
	}

}