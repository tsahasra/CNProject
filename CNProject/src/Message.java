public class Message {		
	int length ;
	int type;
	byte[] payload;
	
	public Message(int payloadsize)
	{
		this.payload = new byte[payloadsize];
	}
}