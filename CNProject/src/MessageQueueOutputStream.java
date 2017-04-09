import java.io.IOException;

public class MessageQueueOutputStream implements Runnable {
	PeerProcess peerProcess;
	
	/**
	 * @param peerProcess
	 */
	public MessageQueueOutputStream(PeerProcess peerProcess) {
		super();
		this.peerProcess = peerProcess;
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
				if (!peerProcess.bqm.isEmpty()) {
					MessageWriter ms = peerProcess.bqm.take();
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