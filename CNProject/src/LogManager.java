import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class LogManager implements Runnable {
	BlockingQueue<String> bql;
	Logger logger;
	PeerProcess peerProcess;

	public LogManager(BlockingQueue<String> b, Logger logger, PeerProcess peerProcess) {
		this.bql = b;
		this.logger = logger;
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
				if (!bql.isEmpty())
					logger.log(Level.INFO, bql.take());
				else {
					int peerCompleteFileReceived = 0;
					for (Peer p : peerProcess.peerList) {
						if (peerProcess.checkIfFullFileRecieved(p)) {
							peerCompleteFileReceived++;
						}
					}
					if (peerCompleteFileReceived == peerProcess.peerList.size()) {
						// check if you recievecd the whole file
						if (peerProcess.checkIfFullFileRecieved(peerProcess.currentPeer)) {
							// now terminate the process of executorService
							// exec.shutdown();

							break;
						}
					}
				}
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

	}

}