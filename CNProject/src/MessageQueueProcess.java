import java.io.IOException;

public class MessageQueueProcess implements Runnable {
	PeerProcess peerProcess;
	
	/**
	 * @param peerProcess
	 */
	public MessageQueueProcess(PeerProcess peerProcess) {
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
					//System.out.println(ms.m.type);
					ms.writeObject();
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