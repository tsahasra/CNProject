import java.io.Serializable;

public class HandShake extends Message implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8799977982265952720L;
	String header ;
	byte[] zerobits;
	int peerID;
	
	public HandShake(int peerId){
		header = "P2PFILESHARINGPROJ";
		peerID = peerId;
		zerobits = new byte[10];
		for(int i =0 ; i<10 ; i++)
			zerobits[i] = (byte)0;
	}
		
}