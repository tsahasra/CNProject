import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
	int FileName;
	int FileSize;
	int PieceSize;
	int noOfPieces;
	boolean isFilePresent;
	ServerSocket serverSocket;
	
	HashMap<Socket, Peer> peerSocketMap = new HashMap<>();

	PeerProcess() {

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

	private void initializePeerList(PeerProcess p, String peerID) throws IOException {
		BufferedReader pireader = new BufferedReader(new FileReader("peerInfo.cfg"));
		String line, tokens[];
		boolean ispeerIdFound = false;
		try {
			while ((line = pireader.readLine()) != null) {
				tokens = line.split(" ");
				if (!tokens[0].equals(peerID)){
					Peer peer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					p.peerList.add(peer);
					if(!ispeerIdFound)
						connectToPreviousPeer(peer);
				}
				else{
					currentPeer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					if(Integer.parseInt(tokens[3])==1)
						p.isFilePresent = true;
					ispeerIdFound = true;
				}
			}
		} finally {
			pireader.close();
		}

	}

	private void initializePeerParams(PeerProcess p) throws IOException {
		BufferedReader commonreader = new BufferedReader(new FileReader("common.cfg"));
		String line, tokens[];
		int i = 0;

		try {

			while ((line = commonreader.readLine()) != null) {
				tokens = line.split(" ");
				p.NumberOfPreferredNeighbors = Integer.parseInt(tokens[1]);
				p.UnchokingInterval = Integer.parseInt(tokens[1]);
				p.OptimisticUnchokingInterval = Integer.parseInt(tokens[1]);
				p.FileName = Integer.parseInt(tokens[1]);
				p.FileSize = Integer.parseInt(tokens[1]);
				p.PieceSize = Integer.parseInt(tokens[1]);
				p.noOfPieces = Integer.parseInt(tokens[1]);

			}
		} finally {
			commonreader.close();
		}

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
			File peerLogFile = new File("log_peer_" + args[0] + ".log");
			peerLogFile.createNewFile();
			/*** Reads peerInfo.cfg file and initializes peerList ***/
			proc.initializePeerList(proc, args[0]);

			/***
			 * Reads common.cfg file and initializes peer process variables
			 ***/
			proc.initializePeerParams(proc);
			if(proc.isFilePresent){
				proc.copyFileUsingStream(new File("File.txt"), new File("\\peer_" + args[0] + "\\File.txt"));
			}
			
			proc.createServerSocket(proc.currentPeer.peerPort);
			
		} catch (Exception e) {

		}
	}
	
	public void createServerSocket(int portNo){
		try{
			serverSocket = new ServerSocket(portNo);
		}catch(Exception e){
			return;
		}
		while(true){
			Socket socket;
			try {
				socket = serverSocket.accept();
				peerSocketMap.put(socket, peerList.get(peerList.indexOf(new Peer(socket.getInetAddress().getHostAddress(), socket.getPort()))));
				ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void connectToPreviousPeer(Peer p){
		Socket socket;
		try {
			socket = new Socket(p.peerIP, p.peerPort);
			peerSocketMap.put(socket, peerList.get(peerList.indexOf(new Peer(socket.getInetAddress().getHostAddress(), socket.getPort()))));
			ClientHandler clientHandler = new ClientHandler(socket);
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
        Peer peer;
        
        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.peer = PeerProcess.this.peerSocketMap.get(socket);
            inputStream = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Object o = inputStream.readObject();
                    if(o instanceof HandShake){
                    	HandShake h = (HandShake)o;
                    	if(h.peerID==this.peer.peerID){
                    		this.peer.isHandShakeDone = true;
                    	}
                    }else if(o instanceof Message){
                    	Message message = (Message)o;
                    	switch(message.type){
                    	
                    	}
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
