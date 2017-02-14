import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class peerProcess {

	/**
	 * @param args
	 * 
	 */
	
	List <Peer> peerList;
	int NumberOfPreferredNeighbors;
	int UnchokingInterval;
	int OptimisticUnchokingInterval;
	int FileName;
	int FileSize;
	int PieceSize;
	int noOfPieces;
	
	peerProcess(){
		
	}
	
	private void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	private void initializePeerList(peerProcess p, String peerID) throws IOException
	{
		BufferedReader pireader =  new BufferedReader(new FileReader("peerInfo.cfg"));
		String line , tokens[];
		try
		{
		while((line = pireader.readLine()) !=null)
		{
			tokens = line.split(" ");
			if(! tokens[0].equals(peerID))
				p.peerList.add(new Peer(tokens[0],tokens[1],tokens[2]));		
		}
		}
		finally
		{
		pireader.close();
		}
		
	}
	
	private void initializePeerParams(peerProcess p) throws IOException
	{
		BufferedReader commonreader = new BufferedReader(new FileReader("common.cfg"));
		String line , tokens[];
		int i = 0;
		
		try{
			
		while((line = commonreader.readLine()) !=null)
		{
			tokens = line.split(" ");
			p.NumberOfPreferredNeighbors = Integer.parseInt(tokens[1]);
			p.UnchokingInterval = Integer.parseInt(tokens[1]);
			p.OptimisticUnchokingInterval = Integer.parseInt(tokens[1]);
			p.FileName = Integer.parseInt(tokens[1]);
			p.FileSize = Integer.parseInt(tokens[1]);
			p.PieceSize = Integer.parseInt(tokens[1]);
			p.noOfPieces= Integer.parseInt(tokens[1]);
			
		}
		}
		finally{
		commonreader.close();
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/***  Creates a new process instance with the supplied peerID and initializes the peer list   ***/
		peerProcess proc = new peerProcess();
		proc.peerList = new ArrayList<Peer>();
		
		try
		{
		
		new File("peer_"+args[0]).mkdir();
		File peerLogFile = new File("log_peer_"+args[0]+".log");
		peerLogFile.createNewFile();
				
		proc.copyFileUsingStream(new File("File.txt"), new File("\\peer_"+args[0]+"\\File.txt"));
		
			
		/***  Reads peerInfo.cfg file and initializes peerList  ***/	
		proc.initializePeerList(proc, args[0]);
		
		
		/***  Reads common.cfg file and initializes peer process variables  ***/
		proc.initializePeerParams(proc);
		
		
		
		
		
		
		}
		catch(Exception e)
		{
			
		}
	}

}
