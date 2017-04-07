import java.io.Serializable;

public class Message implements Serializable {		
	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	//int length ;
	byte type;
	byte[] payload;
	
	public Message(byte type , byte[] p)
	{
		this.type = type;
		this.payload = p;
	}
	
	
}