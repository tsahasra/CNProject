public class message {		
		byte[] length ;
		byte type;
		byte[] payload;
		
		public message(int payloadsize)
		{
			this.payload = new byte[payloadsize];
		}
	}