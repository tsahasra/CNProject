import java.io.Serializable;

public class HandShake extends Message implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8799977982265952720L;
	byte[] header;
	byte[] zerobits;
	int peerID;
	
	public HandShake(int peerId){
		String h = "P2PFILESHARINGPROJ";
		
		header = new byte[18];
		for(int i=0;i<h.length();i++){
			header[i]=(byte)(h.charAt(i));
		}
		peerID = peerId;
		zerobits = new byte[10];
		for(int i =0 ; i<10 ; i++)
			zerobits[i] = (byte)0;
	}
		
}