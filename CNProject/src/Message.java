import java.io.Serializable;

public class Message implements Serializable {		
	int length ;
	int type;
	byte[] payload;
	
	public Message(int payloadsize)
	{
		this.payload = new byte[payloadsize];
	}
}