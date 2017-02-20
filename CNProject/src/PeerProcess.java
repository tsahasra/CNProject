import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 
 */

/**
 * @author Tejas
 *
 */
public class PeerProcess {

	/**
	 * @param args
	 * 
	 */

	List<Peer> peerList;
	Peer currentPeer;
	int NumberOfPreferredNeighbors;
	int UnchokingInterval;
	int OptimisticUnchokingInterval;
	String FileName;
	int FileSize;
	int PieceSize;
	int noOfPieces;
	int noOfPeerHS;
	int noOfPeers;
	boolean isFilePresent;
	ServerSocket serverSocket;
	DateFormat sdf;
	
	HashMap<Socket, Peer> peerSocketMap = new HashMap<>();

	PeerProcess() {
			sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			noOfPeers = getNoOfPeers();
	}

	/**
	 * @param peerProcess
	 * @return
	 * 
	 */
	private int getNoOfPeers() {
		// TODO Auto-generated method stub
		BufferedReader pireader = null;
		
		String line;
		int count = 0;
		try {		
			pireader = new BufferedReader(new FileReader("peerInfo.cfg"));
			while ((line = pireader.readLine())!= null)
				count++;
		}
		catch(IOException ie)
		{
			
		}
		{
			try{
			pireader.close();
			}
			catch(IOException ie)
			{
				ie.printStackTrace();
			}
		}
		return count;
	}

	private void copyFileUsingStream(File source, File dest) throws IOException {
		BufferedReader freader = new BufferedReader(new FileReader(source));
		BufferedWriter fwriter = new BufferedWriter(new FileWriter(dest));
		String line;
		try{
			while((line = freader.readLine()) != null)
				fwriter.write(line + "\n");
			
		}
		finally
		{
			freader.close();
			fwriter.close();
		}
	}

	private void initializePeerList(PeerProcess p, String peerID) throws IOException {
		BufferedReader pireader = new BufferedReader(new FileReader("peerInfo.cfg"));
		String line, tokens[];
		boolean ispeerIdFound = false;
		int currPeerNo = 0;
		try {
			
			while ((line = pireader.readLine()) != null) {
				tokens = line.split(" ");
				if (!tokens[0].equals(peerID)){
					Peer peer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					peer.isHandShakeDone = false;
					p.peerList.add(peer);
				}
				else{
					currentPeer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					currPeerNo = p.peerList.size();
					
					if(Integer.parseInt(tokens[3])==1)
						p.isFilePresent = true;
					if(p.isFilePresent){
						p.copyFileUsingStream(new File("File.txt"), new File(System.getProperty("user.dir") + "\\peer_" + peerID + "\\File.txt"));
					}
					//ispeerIdFound = true;
				}
				
			}
			//Iterator itpeer = p.peerList.iterator();
			int i = 0; 
			while(currPeerNo != 0 && i <= currPeerNo - 1)
			{
				
					p.connectToPreviousPeer(p.peerList.get(i));
					i++;
				
			}
			
		} finally {
			pireader.close();
		}

	}

	private void initializePeerParams(PeerProcess p) throws IOException {
		BufferedReader commonreader = new BufferedReader(new FileReader("common.cfg"));
		String line, tokens[];
		int lineno = 1;

		try {

			while ((line = commonreader.readLine()) != null) {
				tokens = line.split(" ");
				switch(lineno)
				{
				case 1:{
				p.NumberOfPreferredNeighbors = Integer.parseInt(tokens[1]);
				}break;
				
				case 2:{
				p.UnchokingInterval = Integer.parseInt(tokens[1]);
				}break;
				
				case 3:{
				p.OptimisticUnchokingInterval = Integer.parseInt(tokens[1]);
				}break;
				
				case 4:{
				p.FileName = tokens[1];
				}break;
				
				case 5:{
				p.FileSize = Integer.parseInt(tokens[1]);
				}break;
				
				case 6:{
				p.PieceSize = Integer.parseInt(tokens[1]);
				}break;
				
				default:
				}
				
				lineno++;
			}
			p.noOfPieces = p.FileSize / p.PieceSize;
		} finally {
			commonreader.close();
		}

	}
	
	private boolean writeToLog(String message) 
	{
		BufferedWriter br = null;
		try
		{
			File tempFile = new File(System.getProperty("user.dir") + "\\peer_" + currentPeer.peerID + "\\" + "log_peer_"+ this.currentPeer.peerID+".log");
		br = new BufferedWriter(new FileWriter(tempFile));
		StringBuilder sb = new StringBuilder();
		sb.append("["+ sdf.format(new Date()) +"]"); 
		sb.append(message);
		br.append(sb.toString() + "\n");
		br.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally{
			try
			{
			  if(br!=null)
				  br.close();
			}
			catch(IOException ioe2)
			{
				ioe2.printStackTrace();
			}
		}
		
		
		
		return true;
	}

	public static void main(String[] args) {

		/***
		 * Creates a new process instance with the supplied peerID and
		 * initializes the peer list
		 ***/
		PeerProcess proc = new PeerProcess();
		proc.peerList = new ArrayList<Peer>();

		try {

			new File("peer_" + args[0]).mkdir();
			File peerLogFile = new File(System.getProperty("user.dir")+"\\" + "log_peer_" + args[0] + ".log");
			peerLogFile.createNewFile();
			
			/***
			 * Reads common.cfg file and initializes peer process variables
			 ***/
			proc.initializePeerParams(proc);
			
			/*** Reads peerInfo.cfg file and initializes peerList ***/
			proc.initializePeerList(proc, args[0]);
			
			proc.createServerSocket(proc.currentPeer.peerPort);
			
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
	public void createServerSocket(int portNo){
		try{
			serverSocket = new ServerSocket(portNo);
				while(true){
						Socket socket;
						
								socket = serverSocket.accept();								
								Peer tempPeer = getPeerFromPeerList(socket.getInetAddress().getHostAddress(), socket.getPort());
								writeToLog(": Peer " + this.currentPeer.peerID + " is connected from Peer " + tempPeer.peerID);
								peerSocketMap.put(socket, peerList.get(peerList.indexOf(tempPeer)));
								ClientHandler clientHandler = new ClientHandler(socket , false);
								clientHandler.start();
								if(this.noOfPeerHS == this.noOfPeers - 1)
									return;
                			
							}
		}catch(Exception e){
			return;
		}
		finally
		{
			try{
			serverSocket.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
		
	}
	
	/**
	 * @param hostAddress
	 * @param port
	 * @return
	 * 
	 */
	private Peer getPeerFromPeerList(String hostAddress, int port) {
		// TODO Auto-generated method stub
		
		Iterator it = this.peerList.iterator();
		while(it.hasNext())
		{
			Peer tempPeer = (Peer) it.next();
			if(tempPeer.peerIP.equals(hostAddress))
				return tempPeer;
		}
		return null;
	}

	public void connectToPreviousPeer(Peer p){
		Socket socket;
		try {
			socket = new Socket(p.peerIP, p.peerPort);
			writeToLog(": Peer " + this.currentPeer.peerID + " makes a connection to Peer " + p.peerID);
			//Peer tempPeer = new Peer(p.peerID,p.peerIP,p.peerPort);
			peerSocketMap.put(socket, peerList.get(this.peerList.indexOf(p)));
			ClientHandler clientHandler = new ClientHandler(socket , true);
            clientHandler.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
/*	 static class ServerListener extends Thread {

        private ServerSocket serverSocket;

        ServerListener() throws IOException {
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(15000);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final Socket socketToClient = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socketToClient);
                    clientHandler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    class ClientHandler extends Thread{
        private Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        Peer peer;
        boolean initiateHandShake;
        
        ClientHandler(Socket socket , boolean initiateHS) throws IOException {
            this.socket = socket;
            this.peer = PeerProcess.this.peerSocketMap.get(socket);
            
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.initiateHandShake = initiateHS;
            
            if(initiateHandShake)
            	sendHandShake();            	
            	
        }
        /**
		 * 
		 * 
		 */
		private void sendHandShake() {
			// TODO Auto-generated method stub
			HandShake hs = new HandShake(PeerProcess.this.currentPeer.peerID);
			try{
				outputStream.writeObject((Object)hs); 
				outputStream.flush(); 
				} 
				catch(IOException ioException){ 
				ioException.printStackTrace(); 
				}
		}
		
		
		@Override
        public void run() {
            while (true) {
                try {
                	inputStream = new ObjectInputStream(socket.getInputStream());	
                    Object o = inputStream.readObject();
                    if(o instanceof HandShake){
                    	HandShake h = (HandShake)o;
                    	if(h.peerID==this.peer.peerID){
                    		this.peer.isHandShakeDone = true;
                    		if(!initiateHandShake)
                    			sendHandShake();
                    		writeToLog("HandShake completed");
                    	}
                    	PeerProcess.this.noOfPeerHS++;
                    }
                    else if(o instanceof Message){
                    	Message message = (Message)o;
                    	switch(message.type){
                    		
                    	}
                    
                    }
                    break;
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
