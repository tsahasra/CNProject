import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class PrefferedNeighborsThread implements Runnable {

	PeerProcess peerProces;

	/**
	 * @param peerProces
	 */
	public PrefferedNeighborsThread(PeerProcess peerProces) {
		super();
		this.peerProces = peerProces;
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

				Thread.sleep(peerProces.UnchokingInterval * 1000);
				if (peerProces.peerList.size() > 0) {
					if (peerProces.unchokingIntervalWisePeerDownloadingRate.size() == 0) {

						// as it is a new arraylist, this thread is run for
						// the
						// first time
						// so we do not have previous unchoking interval
						// available
						// thus select any random peers and add them to the
						// preferred neighbors list
						peerProces.PreferedNeighbours = new HashSet<Peer>();
						Random ran = new Random();
						while (peerProces.PreferedNeighbours.size() < peerProces.NumberOfPreferredNeighbors) {
							Peer p = peerProces.peerList.get(ran.nextInt(peerProces.peerList.size()));
							if (p.isHandShakeDone) {
								peerProces.PreferedNeighbours.add(p);
							}
						}
						peerProces.sendUnChokeMessage(new HashSet<>(peerProces.PreferedNeighbours));
					} else {
						// send unchoke

						// select top NumberOfPrefferedNeighbors and update
						// the
						// preferred neoighbors list
						HashSet<Peer> NewPreferedNeighbours = new HashSet<Peer>();
						Random ran = new Random();
						for (int i = 0; i < peerProces.NumberOfPreferredNeighbors; i++) {
							if (!peerProces.unchokingIntervalWisePeerDownloadingRate.isEmpty()) {
								NewPreferedNeighbours.add(peerProces.unchokingIntervalWisePeerDownloadingRate.poll().p);
							}
						}
						// if the previous downloading rates list is less
						// than
						// preffered neighbors size

						while (NewPreferedNeighbours.size() < peerProces.NumberOfPreferredNeighbors) {
							Peer p = peerProces.peerList.get(ran.nextInt(peerProces.peerList.size()));
							if (p.isHandShakeDone) {
								NewPreferedNeighbours.add(p);
							}
						}
						
						//send unchoke only to the new ones
						List<Peer> sendUnchokePrefNeig = new ArrayList<>();
						Collections.copy(sendUnchokePrefNeig,  new ArrayList<>(NewPreferedNeighbours));
						sendUnchokePrefNeig.removeAll(peerProces.PreferedNeighbours);
						
						// send choke messages to other who are not present
						// in
						// the
						// new list of preferred neighbors
						peerProces.PreferedNeighbours.removeAll(NewPreferedNeighbours);
						peerProces.sendChokeMessage(peerProces.PreferedNeighbours);
						
						
						peerProces.sendUnChokeMessage(new HashSet<>(sendUnchokePrefNeig));
						
						// change to new preferred neighbors
						peerProces.PreferedNeighbours = NewPreferedNeighbours;
						
					}

					String peerIdList = "";
					for (Peer p : peerProces.PreferedNeighbours) {
						peerIdList = p.peerID + ",";
					}
					peerProces.bql.put("Peer " + peerProces.currentPeer.peerID
							+ " has the preferred neighbors " + peerIdList.substring(0, peerIdList.length() - 1) + ".");
					// now send unchoke Messages to all the new preferred
					// neighbors
					

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}