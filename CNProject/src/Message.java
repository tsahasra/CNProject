import java.io.Serializable;

public class Message implements Serializable {		
	int length ;
	int type;
	byte[] payload;
	
	public Message(int type , int payloadsize)
	{
		this.type = type;
		this.payload = new byte[payloadsize];
	}
}