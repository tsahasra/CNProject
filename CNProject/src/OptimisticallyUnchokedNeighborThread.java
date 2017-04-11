import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class OptimisticallyUnchokedNeighborThread implements Runnable {
	List<Peer> interestedPeers;
	PeerProcess peerProcess;
	/**
	 * @param peerProcess
	 */
	public OptimisticallyUnchokedNeighborThread(PeerProcess peerProcess) {
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
		while (true) {
			try {

				Thread.sleep(peerProcess.OptimisticUnchokingInterval * 1000);

				interestedPeers = new ArrayList<>();
				for (Peer p : peerProcess.peerSocketMap.keySet()) {
					if (p.interestedInPieces) {
						interestedPeers.add(p);
					}
				}
				if (interestedPeers.size() > 0) {
					Random ran = new Random();
					if (peerProcess.optimisticallyUnchokedNeighbor != null) {
						// check if not a preferred neighbor then only
						// send
						// choke message
						if (!peerProcess.PreferedNeighbours.contains(peerProcess.optimisticallyUnchokedNeighbor)) {
							// send a choke message to the previous
							// neighbor
							peerProcess.sendChokeMessage(new HashSet<>(Arrays.asList(peerProcess.optimisticallyUnchokedNeighbor)));
						}
					}
					peerProcess.optimisticallyUnchokedNeighbor = interestedPeers.get(ran.nextInt(interestedPeers.size()));
					peerProcess.sendUnChokeMessage(new HashSet<>(Arrays.asList(peerProcess.optimisticallyUnchokedNeighbor)));
					peerProcess.bql.put(
							"Peer " + peerProcess.currentPeer.peerID + " has the optimistically unchoked neighbor "
									+ peerProcess.optimisticallyUnchokedNeighbor.peerID + ".");
				}

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
