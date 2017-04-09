import java.io.Serializable;

public class Message implements Serializable {		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 985205199122584865L;
	//int length ;
	byte type;
	byte[] payload;
	
	public Message(){
		super();
	}
	public Message(byte type , byte[] p)
	{
		this.type = type;
		this.payload = p;
	}
	
	
}