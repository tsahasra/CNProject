import java.io.Serializable;

public class HandShake implements Serializable{
	
	String header ;
	String zerobits;
	int peerID;
	
	public HandShake(int peerId){
		header = "P2PFILESHARINGPROJ";
		peerID = peerId;
		zerobits = "0000000000";
	} 
		
}