import java.io.Serializable;

public class HandShake extends Message implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8799977982265952720L;
	String header ;
	String zerobits;
	int peerID;
	
	public HandShake(int peerId){
		header = "P2PFILESHARINGPROJ";
		peerID = peerId;
		zerobits = "0000000000";
	}
		
}