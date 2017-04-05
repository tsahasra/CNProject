import java.io.File;
import java.io.IOException;

/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class test {

	/**
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			//File testFile = new File(args[0]);
			byte[] b = {(byte)4,(byte)128};
			
			System.out.println(getBit(b,8) + " " + Byte.toUnsignedInt(b[1]));
			
			setBit(b,8);
			
			System.out.println(getBit(b,8) + " " + Byte.toUnsignedInt(b[1]));
			
			clearBit(b,8);
			
			System.out.println(getBit(b,8) + " " + Byte.toUnsignedInt(b[1]));
			//testFile.createNewFile();
	}
	
	public static void setBit(byte[] b , int index)
	{
		b[index/8] = (byte) (b[index/8] | 1 << ((index) % 8));
		
	}
	
	public static int getBit(byte[] b , int index)
	{
		byte b1 = b[index/8];
		
		if((b1 & (1 << ((index) % 2))) != 0)
			return 1;
		else 
			return 0;
		
	}
	
	public static void clearBit(byte[] b , int index)
	{
		b[index/8] = (byte) (b[index/8] & (~(1 << ((index) % 8))));
		
		
	}
	

}
