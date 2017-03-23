import java.io.Serializable;

public class Message implements Serializable {		
	int length ;
	byte type;
	byte[] payload;
	
	public Message(byte type , int payloadsize)
	{
		this.type = type;
		this.payload = new byte[payloadsize];
	}
	
	
}