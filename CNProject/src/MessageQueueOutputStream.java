import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class MessageQueueOutputStream implements Runnable {
	BlockingQueue<MessageWriter> bqm;
	HashMap<Peer, DataOutputStream> peerObjectOutputStream;
	// ObjectOutputStream outputStream;

	public MessageQueueOutputStream(BlockingQueue<MessageWriter> b, HashMap<Peer, DataOutputStream> pos) {
		this.bqm = b;
		this.peerObjectOutputStream = pos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (true) {
				if (!bqm.isEmpty()) {
					MessageWriter ms = bqm.take();
					System.out.println(ms.m.type);
					((MessageWriter) ms.os).writeObject();
				} /*
					 * else { /*for (DataOutputStream o :
					 * peerObjectOutputStream.values()) { o.writeObject(null);
					 * o.flush(); } Thread.sleep(10000); }
					 */
			}
		} catch (InterruptedException | IOException ex) {
			ex.printStackTrace();
		}

	}

}