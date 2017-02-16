/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class Peer {
	
	int peerID;
	String peerIP;
	int peerPort;
	boolean isHandShakeDone;
	
	public Peer(int pid, String pip, int pport)
	{
		this.peerID = pid;
		this.peerIP = pip;
		this.peerPort = pport;
	}
	
	public Peer(String pip, int pport)
	{
		this.peerIP = pip;
		this.peerPort = pport;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((peerIP == null) ? 0 : peerIP.hashCode());
		result = prime * result + peerPort;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Peer other = (Peer) obj;
		if (peerIP == null) {
			if (other.peerIP != null)
				return false;
		} else if (!peerIP.equals(other.peerIP))
			return false;
		if (peerPort != other.peerPort)
			return false;
		return true;
	}
	
	
}
