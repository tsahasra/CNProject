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
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
	File logfile;
	HashSet<Peer> chokedfrom;
	// HashSet<Peer> chokedto;
	HashSet<Peer> PreferedNeighbours;
	List<List<DownloadingRate>> unchokingIntervalWisePeerDownloadingRate;
	Logger logger;

	HashMap<Peer, Socket> peerSocketMap = new HashMap<>();

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
			while ((line = pireader.readLine()) != null)
				count++;
		} catch (IOException ie) {
			try {
				pireader.close();
			} catch (IOException ie1) {
				ie1.printStackTrace();
			}
		}

		return count;
	}

	private void copyFileUsingStream(File source, File dest) throws IOException {
		BufferedReader freader = new BufferedReader(new FileReader(source));
		BufferedWriter fwriter = new BufferedWriter(new FileWriter(dest));
		String line;
		try {
			while ((line = freader.readLine()) != null)
				fwriter.write(line + "\n");

		} finally {
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
				if (!tokens[0].equals(peerID)) {
					Peer peer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					int bfsize = (int) Math.ceil((double)(noOfPieces / 8.0));
					peer.bitfield = new byte[bfsize];
					if (Integer.parseInt(tokens[3]) == 0)

						peer.isHandShakeDone = false;
					p.peerList.add(peer);
				} else {
					currentPeer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					currPeerNo = p.peerList.size();

					if (Integer.parseInt(tokens[3]) == 1)
						p.isFilePresent = true;
					if (p.isFilePresent) {
						p.copyFileUsingStream(new File("TheFile.dat"),
								new File(System.getProperty("user.dir") + "\\peer_" + peerID + "\\File.txt"));
					}
					// ispeerIdFound = true;
				}

			}
			// Iterator itpeer = p.peerList.iterator();
			int i = 0;
			while (currPeerNo != 0 && i <= currPeerNo - 1) {

				p.connectToPreviousPeer(p.peerList.get(i));
				i++;

			}

		} finally {
			pireader.close();
		}

	}

	private void initateLogFile(String peerId) {
		logger = Logger.getLogger("LogFormatter");
		FileHandler fh;

		try {

			// This block configure the logger with handler and formatter
			fh = new FileHandler(System.getProperty("user.dir") + "\\log_peer_" + peerId + ".log");
			logger.addHandler(fh);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializePeerParams(PeerProcess p) throws IOException {
		BufferedReader commonreader = new BufferedReader(new FileReader("common.cfg"));
		String line, tokens[];
		int lineno = 1;

		try {

			while ((line = commonreader.readLine()) != null) {
				tokens = line.split(" ");
				switch (lineno) {
				case 1: {
					p.NumberOfPreferredNeighbors = Integer.parseInt(tokens[1]);
				}
					break;

				case 2: {
					p.UnchokingInterval = Integer.parseInt(tokens[1]);
				}
					break;

				case 3: {
					p.OptimisticUnchokingInterval = Integer.parseInt(tokens[1]);
				}
					break;

				case 4: {
					p.FileName = tokens[1];
				}
					break;

				case 5: {
					p.FileSize = Integer.parseInt(tokens[1]);
				}
					break;

				case 6: {
					p.PieceSize = Integer.parseInt(tokens[1]);
				}
					break;

				default:
				}

				lineno++;
			}
			p.noOfPieces = p.FileSize / p.PieceSize;
		} finally {
			commonreader.close();
		}

	}

	private boolean writeToLog(String message) {
		logger.log(Level.INFO, message);
		/*
		 * BufferedWriter br = null; try {
		 * 
		 * br = new BufferedWriter(new FileWriter(logfile)); StringBuilder sb =
		 * new StringBuilder(); sb.append("[" + sdf.format(new Date()) + "]");
		 * sb.append(message); br.append(sb.toString() + "\n"); br.close(); }
		 * catch (IOException ioe) { ioe.printStackTrace(); } finally { try { if
		 * (br != null) br.close(); } catch (IOException ioe2) {
		 * ioe2.printStackTrace(); } }
		 */

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
			proc.logfile = new File(
					System.getProperty("user.dir") + "\\peer_" + args[0] + "\\log_peer_" + args[0] + ".log");
			proc.logfile.createNewFile();

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

	public void createServerSocket(int portNo) {
		try {
			serverSocket = new ServerSocket(portNo);
			PeerProcess.this.chokedfrom = new HashSet<>();

			// PeerProcess.this.chokedto = new HashSet<>();
			ExecutorService exec = Executors.newFixedThreadPool(2);
			exec.submit(new PrefferedNeighborsThread());
			exec.submit(new OptimisticallyUnchokedNeighborThread());
			while (true) {
				Socket socket;
				socket = serverSocket.accept();
				Peer tempPeer = getPeerFromPeerList(socket.getInetAddress().getHostAddress(), socket.getPort());
				writeToLog(": Peer " + this.currentPeer.peerID + " is connected from Peer " + tempPeer.peerID);
				peerSocketMap.put(peerList.get(peerList.indexOf(tempPeer)), socket);
				ClientHandler clientHandler = new ClientHandler(tempPeer, false);
				clientHandler.start();
				if (this.noOfPeerHS == this.noOfPeers - 1)
					return;
			}
		} catch (Exception e) {
			return;
		} finally {
			try {
				serverSocket.close();
			} catch (Exception e) {
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
		while (it.hasNext()) {
			Peer tempPeer = (Peer) it.next();
			if (tempPeer.peerIP.equals(hostAddress))
				return tempPeer;
		}
		return null;
	}

	public void connectToPreviousPeer(Peer p) {
		Socket socket;
		try {
			socket = new Socket(p.peerIP, p.peerPort);
			writeToLog(": Peer " + this.currentPeer.peerID + " makes a connection to Peer " + p.peerID);
			// Peer tempPeer = new Peer(p.peerID,p.peerIP,p.peerPort);
			peerSocketMap.put(peerList.get(this.peerList.indexOf(p)), socket);
			ClientHandler clientHandler = new ClientHandler(p, true);
			clientHandler.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * static class ServerListener extends Thread {
	 * 
	 * private ServerSocket serverSocket;
	 * 
	 * ServerListener() throws IOException { serverSocket =
	 * ServerSocketFactory.getDefault().createServerSocket(15000); }
	 * 
	 * @Override public void run() { while (true) { try { final Socket
	 * socketToClient = serverSocket.accept(); ClientHandler clientHandler = new
	 * ClientHandler(socketToClient); clientHandler.start(); } catch
	 * (IOException e) { e.printStackTrace(); } } } }
	 */

	public class ClientHandler extends Thread {
		private Socket socket;
		ObjectInputStream inputStream;
		ObjectOutputStream outputStream;
		Peer peer;
		boolean initiateHandShake;

		ClientHandler(Peer p, boolean initiateHS) throws IOException {
			this.socket = PeerProcess.this.peerSocketMap.get(p);
			this.peer = p;

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.initiateHandShake = initiateHS;

			this.peer.interestedBitfield = new boolean[PeerProcess.this.noOfPieces];
			if (initiateHandShake)
				sendHandShake();

		}

		/**
		 * @throws IOException
		 * 
		 * 
		 */
		private void sendHandShake() throws IOException {
			// TODO Auto-generated method stub
						HandShake hs = new HandShake(PeerProcess.this.currentPeer.peerID);
						try {
							outputStream.writeObject((Object) hs);
							Message bitfield = new Message(Byte.valueOf(Integer.toString(5)), null);
							outputStream.writeObject((Object) bitfield);
							outputStream.flush();
						} catch (IOException ioException) {
							ioException.printStackTrace();
						}
		}

		@Override
		public void run() {
			while (true) {
				try {

					inputStream = new ObjectInputStream(socket.getInputStream());

					Object o = inputStream.readObject();
					if (o instanceof HandShake) {
						HandShake h = (HandShake) o;
						if (h.peerID == this.peer.peerID) {
							this.peer.isHandShakeDone = true;
							if (!initiateHandShake)
								sendHandShake();
							writeToLog("HandShake completed");
						}
						PeerProcess.this.noOfPeerHS++;
					} else if (o instanceof Message) {

						Message message = (Message) o;

						switch (Byte.toUnsignedInt(message.type)) {

						case 0:
							choke(peer);
						case 1:
							unchoke(peer);
						case 2:
						case 3:

						case 4:
							
						case 5:{
							sendBitfield();
						}break;
						
						case 6:
						case 7:

						}
					}
				} catch (IOException e) {
					e.printStackTrace();

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * @param piece
		 * 
		 */
		private void writePieceToFile(byte[] piece) {
			// TODO Auto-generated method stub

		}

		private void sendBitfield() throws IOException {
			// TODO Auto-generated method stub
			if (initiateHandShake) {
				Message bitfield = new Message(Byte.valueOf(Integer.toString(5)), PeerProcess.this.currentPeer.bitfield);
				outputStream.writeObject((Object) bitfield);
			}

		}

		/**
		 * @param payload
		 * @return
		 * 
		 */
		private int getMissingPiece(byte[] payload) {
			// TODO Auto-generated method stub
			int index = -1;

			for (int i = 0; i < payload.length; i++)
				if (peer.bitfield[i] != payload[i])
					return i;

			return index;
		}

		/**
		 * @param i
		 * @return
		 * 
		 */
		private Message createMessage(int type) {
			// TODO Auto-generated method stub
			Message m = null;

			switch (type) {

			case 2: {

			}
				break;

			case 7: {

			}
				break;

			}
			return m;
		}

		/**
		 * @param payload
		 * @return
		 * 
		 */
		private int convertToInt(byte[] payload) {
			// TODO Auto-generated method stub
			int result = 0;
			for (int i = 0; i < payload.length; i++) {
				result = (result << 8) - Byte.MIN_VALUE + (int) payload[i];
			}
			return result;

		}

		/**
		 * 
		 */
		private void choke(Peer p) {
			chokedfrom.add(p);
		}

		/**
		 * @param peer2
		 */
		private void unchoke(Peer peer2) {
			chokedfrom.remove(peer2);
		}
	}

	public class PrefferedNeighborsThread implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (true) {
				try {

					Thread.sleep(UnchokingInterval);
					if (unchokingIntervalWisePeerDownloadingRate == null) {
						unchokingIntervalWisePeerDownloadingRate = new ArrayList<List<DownloadingRate>>();
						// as it is a new arraylist, this thread is run for the
						// first time
						// so we do not have previous unchoking interval
						// available
						// thus select any random peers and add them to the
						// preffered neighbors list
						PreferedNeighbours = new HashSet<Peer>();
						Random ran = new Random();
						while (PreferedNeighbours.size() < NumberOfPreferredNeighbors) {
							PreferedNeighbours.add(peerList.get(ran.nextInt(peerList.size())));
						}
					} else {
						// send unchoke

						// only select top downloading rate neighbors
						List<DownloadingRate> lastunchokingIntervalDownloadingValues = unchokingIntervalWisePeerDownloadingRate
								.get(unchokingIntervalWisePeerDownloadingRate.size() - 1);
						lastunchokingIntervalDownloadingValues.sort(new Comparator<DownloadingRate>() {

							@Override
							public int compare(DownloadingRate arg0, DownloadingRate arg1) {
								return arg0.downloadingRate >= arg1.downloadingRate ? 1 : -1;
							}
						});

						// select top NumberOfPrefferedNeighbors and update the
						// preferred neoighbors list
						HashSet<Peer> NewPreferedNeighbours = new HashSet<Peer>();
						Random ran = new Random();
						for (int i = 0; i < NumberOfPreferredNeighbors; i++) {
							if (i < lastunchokingIntervalDownloadingValues.size()) {
								NewPreferedNeighbours.add(lastunchokingIntervalDownloadingValues.get(i).p);
							}
						}
						// if the previous downloading rates list is less than
						// preffered neighbors size
						while (NewPreferedNeighbours.size() < NumberOfPreferredNeighbors) {
							NewPreferedNeighbours.add(peerList.get(ran.nextInt(peerList.size())));
						}

						// send choke messages to other who are not present in
						// the
						// new list of preferred neighbors
						PreferedNeighbours.removeAll(NewPreferedNeighbours);
						sendChokeMessage(PreferedNeighbours);

						// change to new preferred neighbors
						PreferedNeighbours = NewPreferedNeighbours;
						// now send unchoke Messages to all the new preferred
						// neighbors
						sendUnChokeMessage(PreferedNeighbours);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public class OptimisticallyUnchokedNeighborThread implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (true) {
				try {
					while (true) {
						Thread.sleep(OptimisticUnchokingInterval);
						List<Peer> interestedPeers = new ArrayList<>();
						for (Peer p : peerSocketMap.keySet()) {
							for (boolean x : p.interestedBitfield) {
								if (x) {
									interestedPeers.add(p);
									break;
								}
							}
						}
						Random ran = new Random();
						Peer optimisticallyUnchokedPeer = interestedPeers.get(ran.nextInt(interestedPeers.size()));
						sendUnChokeMessage(new HashSet<>(Arrays.asList(optimisticallyUnchokedPeer)));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class DownloadingRate {
		Peer p;
		float downloadingRate;

		/**
		 * @param p
		 * @param downloadingRate
		 */
		public DownloadingRate(Peer p, float downloadingRate) {
			super();
			this.p = p;
			this.downloadingRate = downloadingRate;
		}

	}

	private void sendChokeMessage(HashSet<Peer> peers) {
		Message m = new Message(Byte.valueOf(Integer.toString(0)), null);
		for (Peer p : peers) {

			ObjectOutputStream o;
			try {
				o = new ObjectOutputStream(PeerProcess.this.peerSocketMap.get(p).getOutputStream());
				o.writeObject(m);
				o.flush();
				o.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	private void sendUnChokeMessage(HashSet<Peer> peers) {
		Message m = new Message(Byte.valueOf(Integer.toString(1)), null);
		for (Peer p : peers) {

			ObjectOutputStream o;
			try {
				o = new ObjectOutputStream(PeerProcess.this.peerSocketMap.get(p).getOutputStream());
				o.writeObject(m);
				o.flush();
				o.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
