import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class startPeerProcesses {

	/**
	 * @param args
	 * 
	 */
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BufferedReader pireader ;
		String line , tokens[];
		try
		{
			pireader =   new BufferedReader(new FileReader("peerInfo.cfg"));
		while((line = pireader.readLine()) !=null)
		{
			tokens = line.split(" ");
			String user = "cyguser";
			String workingDir = System.getProperty("user.dir");
			File batchFile = new File("startPeerProcessBatch.bat");
			String startPeerProcessBatch = "startPeerProcessBatch.bat " + "testFile.txt";			
			Runtime.getRuntime().exec(new String("ssh " + user + "@" + tokens[1] +" && " + " cd " + workingDir + " && " + startPeerProcessBatch));
			pireader.close();
		}
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		finally
		{
		
		}
		
		
	}

}
