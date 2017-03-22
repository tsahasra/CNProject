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
			pireader =   new BufferedReader(new FileReader("peerProp.cfg"));
		while((line = pireader.readLine()) !=null)
		{
			tokens = line.split(" ");
			String user = "cyguser";
			String workingDir = tokens[2];
			String startPeerProcessBatch = "startPeerProcessBatch.bat " + workingDir + " " + tokens[0];
			String command = "ssh " + user + "@" + tokens[1] + " " + workingDir + "\\" + startPeerProcessBatch;
			Runtime.getRuntime().exec(command);
			
		}
		pireader.close();
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
