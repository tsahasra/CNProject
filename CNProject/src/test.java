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
			File testFile = new File(args[0]);
			try {
				System.out.println("Im in in");
				testFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
