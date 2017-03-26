import java.io.Serializable;

public class Message implements Serializable {		
	int length ;
	byte type;
	byte[] payload;
	
	public Message(byte type , byte[] p)
	{
		this.type = type;
		this.payload = p;
	}
	
	
}