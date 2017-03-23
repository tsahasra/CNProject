public class Piece
	{
		int index;
		Object content;
		
		public Piece( int index , Object o)
		{
			this.index =  index;
			this.content = o;
			
		}
		
		int getIndex()
		{
			return index;
		}
		
		Object getContent()
		{
			return content;
		}
	}