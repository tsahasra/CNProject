import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
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
	Peer optimisticallyUnchokedNeighbor;
	PriorityQueue<DownloadingRate> unchokingIntervalWisePeerDownloadingRate;
	Logger logger;
	boolean[][] sentRequestMessageByPiece;
	boolean fileComplete;
	int lastPeerID;

	HashMap<Peer, Socket> peerSocketMap;
	HashMap<Peer, ObjectOutputStream> peerObjectOutputStream;
	HashMap<Peer, ObjectInputStream> peerObjectInputStream;

	PeerProcess() {
		sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		noOfPeers = getNoOfPeers();
		fileComplete = false;
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
			pireader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\peerInfo.cfg"));
			while ((line = pireader.readLine()) != null)
				count++;
			count--;
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
			int bfsize = (int) Math.ceil((double) (noOfPieces / 8.0));
			while ((line = pireader.readLine()) != null) {
				tokens = line.split(" ");
				lastPeerID = Integer.parseInt(tokens[0]);
				if (!tokens[0].equals(peerID)) {
					System.out.println("t:" + tokens[0] + " " + tokens[1] + " " + tokens[2]);
					Peer peer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					peer.bitfield = new byte[bfsize];
					Arrays.fill(peer.bitfield, (byte) 0);
					if (Integer.parseInt(tokens[3]) == 0)

						peer.isHandShakeDone = false;
					p.peerList.add(peer);
				} else {
					currentPeer = new Peer(Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]));
					currPeerNo = p.peerList.size();

					if (Integer.parseInt(tokens[3]) == 1)
						p.isFilePresent = true;
					if (p.isFilePresent) {
						p.copyFileUsingStream(new File(System.getProperty("user.dir") + "\\" + this.FileName),
								new File(System.getProperty("user.dir") + "\\peer_" + peerID + "\\" + this.FileName));
						FileName = System.getProperty("user.dir") + "\\peer_" + currentPeer.peerID + "\\"+ this.FileName;
						System.out.println(FileName);
						fileComplete = true;
						currentPeer.bitfield = new byte[bfsize];
						for(int i = 0; i<noOfPieces ;i++)
							setBit(currentPeer.bitfield,i);
					} else {
						FileName = System.getProperty("user.dir") + "\\peer_" + currentPeer.peerID + "\\"+ this.FileName;
						currentPeer.bitfield = new byte[bfsize];
						Arrays.fill(currentPeer.bitfield, (byte) 0);
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
			fh = new FileHandler(System.getProperty("user.dir") + "\\peer_" + peerId + "\\log_peer_" + peerId + ".log");
			fh.setFormatter(new LogFormatter());
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
			sentRequestMessageByPiece = new boolean[this.noOfPeers][this.noOfPieces];
			PeerProcess.this.chokedfrom = new HashSet<>();
			PeerProcess.this.peerSocketMap = new HashMap<>();
			PeerProcess.this.peerObjectInputStream = new HashMap<>();
			PeerProcess.this.peerObjectOutputStream = new HashMap<>();
			PeerProcess.this.unchokingIntervalWisePeerDownloadingRate = new PriorityQueue<>(
					new Comparator<DownloadingRate>() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * java.util.Comparator#compare(java.
						 * lang. Object, java.lang.Object)
						 */
						@Override
						public int compare(DownloadingRate o1, DownloadingRate o2) {
							return o1.downloadingRate > o2.downloadingRate ? 1 : -1;
						}
					});
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
			proc.initateLogFile(args[0]);
			/*
			 * proc.logfile = new File( System.getProperty("user.dir") +
			 * "\\peer_" + args[0] + "\\log_peer_" + args[0] + ".log");
			 * proc.logfile.createNewFile();
			 */

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

			// PeerProcess.this.chokedto = new HashSet<>();
			ExecutorService exec = Executors.newFixedThreadPool(2);
			exec.submit(new PrefferedNeighborsThread());
			exec.submit(new OptimisticallyUnchokedNeighborThread());

			int peerCompleteFileReceived = 0;
			serverSocket = new ServerSocket(portNo);

			while (true) {
				if (currentPeer.peerID != lastPeerID) {

					Socket socket;
					if (this.noOfPeerHS != this.noOfPeers) {
						socket = serverSocket.accept();
						Peer tempPeer = getPeerFromPeerList(socket.getInetAddress().getHostAddress(), socket.getPort());
						writeToLog(": Peer " + this.currentPeer.peerID + " is connected from Peer " + tempPeer.peerID);
						peerSocketMap.put(peerList.get(peerList.indexOf(tempPeer)), socket);
						ClientHandler clientHandler = new ClientHandler(tempPeer, false);
						clientHandler.start();
					}
				}
				// check for termination of this process
				if (peerCompleteFileReceived == peerList.size()) {
					for (Peer p : peerList) {
						if (checkIfFullFileRecieved(p)) {
							peerCompleteFileReceived++;
						}
					}

					// now terminate the process of executorService
					exec.shutdown();
					for (Socket s : peerSocketMap.values()) {
						s.close();
					}
					break;
				}
			}

		} catch (

		Exception e) {
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

	public static void setBit(byte[] b , int index)
	{
		byte b1 = 1;
		b[index/8] = (byte) (b[index/8] | b1 << ((index) % 8));
	}
	
	public static int getBit(byte[] b , int index)
	{
		byte b1 = b[index/8];
		byte be = 1;
		
		if((b1 & (be << ((index) % 8))) != 0)
			return 1;
		else
			return 0;
		
		
	}
	
	public static void clearBit(byte[] b , int index)
	{
		byte b1 = 1;
		b[index/8] = (byte) (b[index/8] & (~(b1 << ((index) % 8))));
		
	}
	public boolean checkIfFullFileRecieved(Peer p) {
		for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
			if (getBit(p.bitfield, i) == 0) {
				return false;
			}
		}
		return true;
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
		long starttime, endtime;

		ClientHandler(Peer p, boolean initiateHS) throws IOException {
			this.socket = PeerProcess.this.peerSocketMap.get(p);
			this.peer = p;

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			PeerProcess.this.peerObjectInputStream.put(p, inputStream);
			PeerProcess.this.peerObjectOutputStream.put(p, outputStream);
			this.initiateHandShake = initiateHS;

			this.peer.interestedFromBitfield = new boolean[PeerProcess.this.noOfPieces];

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
				outputStream.flush();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					starttime = System.currentTimeMillis();
					Object o = inputStream.readObject();
					endtime = System.currentTimeMillis();

					if (o instanceof HandShake) {
						HandShake h = (HandShake) o;
						if (h.peerID == this.peer.peerID) {
							this.peer.isHandShakeDone = true;
							if (!initiateHandShake)
								sendHandShake();
							else
								sendBitfield();
							writeToLog("HandShake completed");
						}
						PeerProcess.this.noOfPeerHS++;
					} else if (o instanceof Message) {

						Message message = (Message) o;

						switch (Byte.toUnsignedInt(message.type)) {

						case 0:
							choke(peer);
							break;
						case 1:
							unchoke(peer);
							break;

						case 2:
							this.peer.interestedInPieces = true;
							writeToLog("Peer " + PeerProcess.this.currentPeer.peerID
									+ " received the 'interested' message from " + peer.peerID);
							break;
						case 3:
							this.peer.interestedInPieces = false;
							writeToLog("Peer " + PeerProcess.this.currentPeer.peerID
									+ " received the 'not interested' message from " + peer.peerID);
							break;

						case 4: {

							processHaveMessage(message);

						}
							break;

						case 5: {
							peer.bitfield = message.payload;
							if (!initiateHandShake)
								sendBitfield();

							if (!PeerProcess.this.isFilePresent)
								sendInterestedifApplicable();

						}
							break;

						case 6: {

							processRequest(message);

						}
							break;

						case 7: {

							updatePeerDownloadingRate();

							writePieceToFile(message.payload);

							sendHaveMessageToAll(message.payload);

							sendNIToSomeNeighbours();

							if (!fileComplete) {
								List<Integer> pieceIndex = new ArrayList<Integer>();

								boolean indexRequestSentFlag = false;

								/*
								 * Get list of all pieces not yet received and
								 * for which request has not yet been sent
								 */
								for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
									if (getBit(PeerProcess.this.currentPeer.bitfield, i) != 1) {
										for (int j = 0; j < PeerProcess.this.noOfPeers; j++)
											if (PeerProcess.this.sentRequestMessageByPiece[j][i]) {
												indexRequestSentFlag = true;
												break;
											}

										if (!indexRequestSentFlag) {
											pieceIndex.add(i);
											indexRequestSentFlag = false;
										}

									}

								}

								if (pieceIndex.size() > 0) {
									Random rnd = new Random();
									int selectedIndex = rnd.nextInt(pieceIndex.size());
									sendRequest(peer, pieceIndex.get(selectedIndex));
								}

							}

						}
							break;

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
		 * @throws IOException
		 * 
		 * 
		 */
		private void sendInterestedifApplicable() throws IOException {
			// TODO Auto-generated method stub

			for (int i=0; i<noOfPieces; i++) {
				int bitAtIndexOfCurrPeer = getBit(currentPeer.bitfield, i);
				int bitAtIndexOfPeer = getBit(peer.bitfield, i);
				if (bitAtIndexOfCurrPeer == 0 && bitAtIndexOfPeer == 1) {
					Message interested = new Message((byte) 2, null);
					outputStream.writeObject((Object) interested);
					outputStream.flush();
					// update the interested from array
					this.peer.interestedFromBitfield[i] = true;
					break;
				}
			}

		}

		/**
		 * 
		 * 
		 */
		private void updatePeerDownloadingRate() {
			// TODO Auto-generated method stub

			DownloadingRate dr = new DownloadingRate(peer,
					(double) (PeerProcess.this.PieceSize / (this.endtime - this.starttime)));

			if (!unchokingIntervalWisePeerDownloadingRate.contains(dr))
				unchokingIntervalWisePeerDownloadingRate.add(dr);
			else {
				unchokingIntervalWisePeerDownloadingRate.remove(dr);
				unchokingIntervalWisePeerDownloadingRate.add(dr);
			}
		}

		/**
		 * @throws IOException
		 * 
		 * 
		 */
		private void sendHaveMessageToAll(byte[] payload) throws IOException {
			// TODO Auto-generated method stub

			byte[] i = new byte[4];
			System.arraycopy(payload, 0, i, 0, 4);
			int index = ByteBuffer.wrap(i).getInt();
			System.out.println(index);
			for (Peer p : PeerProcess.this.peerList) {
				Message have = new Message((byte) 4, i);
				this.socket = PeerProcess.this.peerSocketMap.get(p);
				outputStream.writeObject((Object) have);
				outputStream.flush();
			}
		}

		/**
		 * @throws IOException
		 * 
		 * 
		 */
		private void sendNIToSomeNeighbours() throws IOException {
			// TODO Auto-generated method stub

			List<Integer> NIIndices = new ArrayList<Integer>();

			boolean sendNIMessage = true;

			for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
				if (getBit(PeerProcess.this.currentPeer.bitfield, i) == 1)
					NIIndices.add(i);
			}

			for (int j = 0; j < PeerProcess.this.noOfPeers; j++) {
				for (int k = 0; k < PeerProcess.this.noOfPieces; k++)
					if (getBit(PeerProcess.this.currentPeer.bitfield, k) == 1 && !NIIndices.contains(k)) {
						for(int m = 0 ; m < PeerProcess.this.noOfPeers; m++)
							if(PeerProcess.this.sentRequestMessageByPiece[m][k])
								sendNIMessage = false;
						break;
					}

				if (sendNIMessage) {
					Message notinterested = new Message((byte) 3, null);
					outputStream = new ObjectOutputStream(
							PeerProcess.this.peerSocketMap.get(PeerProcess.this.peerList.get(j)).getOutputStream());
					outputStream.writeObject((Object) notinterested);
					outputStream.flush();
					break;
				}

			}

		}

		/**
		 * @param message
		 * @throws IOException
		 * 
		 * 
		 */
		private void processHaveMessage(Message message) throws IOException {
			// TODO Auto-generated method stub

			int index = ByteBuffer.wrap(message.payload).getInt();

			if (getBit(this.peer.bitfield, index) == 0)
				setBit(this.peer.bitfield, index);

			writeToLog("Peer " + PeerProcess.this.currentPeer.peerID + " received the 'have' message from " + peer.peerID
					+ " for the piece " + index + ".");

			if (getBit(PeerProcess.this.currentPeer.bitfield, index) == 0) {
				Message interested = new Message((byte) 2, null);
				outputStream.writeObject((Object) interested);
				outputStream.flush();
				// update the interested from array
				this.peer.interestedFromBitfield[index] = true;
			}
		}

		/**
		 * @param message
		 * @throws IOException
		 * 
		 * 
		 */
		private void processRequest(Message message) throws IOException {
			// TODO Auto-generated method stub
			if (PeerProcess.this.PreferedNeighbours.contains(peer)
					|| PeerProcess.this.optimisticallyUnchokedNeighbor.equals(peer)) {
				int index = ByteBuffer.wrap(message.payload).getInt();
				byte[] piece = new byte[PeerProcess.this.PieceSize + 4];
				System.arraycopy(message.payload, 0, piece, 0, 4);
				RandomAccessFile rafr = new RandomAccessFile(new File(FileName), "r");
				rafr.seek(PeerProcess.this.PieceSize * index);
				rafr.readFully(piece, 4, PeerProcess.this.PieceSize);
				rafr.close();
				Message mpiece = new Message((byte) 7, piece);
				outputStream.writeObject((Object) mpiece);
				// peer.interestedInPiece[index] = 0;
				setBit(peer.bitfield, index);

				outputStream.flush();
			}
		}

		/**
		 * @param piece
		 * @throws IOException
		 * 
		 */
		private void writePieceToFile(byte[] payload) throws IOException {
			// TODO Auto-generated method stub
			byte[] i = new byte[4];
			System.arraycopy(payload, 0, i, 0, 4);
			int index = ByteBuffer.wrap(i).getInt();
			byte[] piece = new byte[PeerProcess.this.PieceSize];
			System.arraycopy(payload, 4, piece, 0, PeerProcess.this.PieceSize);
			RandomAccessFile rafw = new RandomAccessFile(new File(FileName), "rw");
			rafw.seek(PeerProcess.this.PieceSize * index);
			rafw.write(piece, 0, PeerProcess.this.PieceSize);
			rafw.close();
			setBit(PeerProcess.this.currentPeer.bitfield, index);

			int nop = 0;

			for (int j = 0; j < PeerProcess.this.currentPeer.bitfield.length; j++)
				if (getBit(PeerProcess.this.currentPeer.bitfield, j) == 1)
					nop++;

			writeToLog("Peer " + PeerProcess.this.currentPeer.peerID + " has downloaded the piece " + index + " from "
					+ this.peer.peerID + ". Now the number of pieces it has is " + nop);

		}

		private void sendBitfield() throws IOException {
			Message bitfield = new Message(Byte.valueOf(Integer.toString(5)), PeerProcess.this.currentPeer.bitfield);
			outputStream.writeObject((Object) bitfield);
			outputStream.flush();
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
			writeToLog("Peer " + PeerProcess.this.currentPeer.peerID + " is choked by " + p.peerID + ".");
			chokedfrom.add(p);
			int indexOfPeer = peerList.indexOf(p);
			// reset the sentRequestMessageBy Piece array by comparing the
			// bitfield array and request array
			for (int i = 0; i < PeerProcess.this.sentRequestMessageByPiece[indexOfPeer].length; i++) {
				if (PeerProcess.this.sentRequestMessageByPiece[indexOfPeer][i]) {
					// check if piece received, if not reset the request message
					// field
					PeerProcess.this.sentRequestMessageByPiece[indexOfPeer][i] = false;
				}
			}
		}

		/**
		 * @param peer2
		 */
		private void unchoke(Peer peer2) {
			writeToLog("Peer " + PeerProcess.this.currentPeer.peerID + " is unchoked by " + peer2.peerID + ".");
			chokedfrom.remove(peer2);
			// after receiving unchoke, check if this peer is interested in any
			// of the pieces of the peerUnchokedFrom
			// if interested, check if that piece is not requested to any other
			// peer
			List<Integer> interestedPieces = new ArrayList<Integer>();
			int indexOfPeer = peerList.indexOf(peer2);
			for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
				if (getBit(currentPeer.bitfield,i) == 0
						&& !PeerProcess.this.sentRequestMessageByPiece[indexOfPeer][i]) {
					boolean alreadySentRequestToSomeOtherPeer = false;
					for (int j = 0; j < PeerProcess.this.sentRequestMessageByPiece.length; j++) {
						if (PeerProcess.this.sentRequestMessageByPiece[j][i] && j != indexOfPeer) {
							alreadySentRequestToSomeOtherPeer = true;
							break;
						}
					}
					if (!alreadySentRequestToSomeOtherPeer) {
						interestedPieces.add(i);
					}
				}
			}
			if (interestedPieces.size() > 0) {
				// select any one piece randomly
				Random ran = new Random();
				int index = ran.nextInt(interestedPieces.size());
				// PeerProcess.this.sentRequestMessageByPiece[indexOfPeer][index]
				// =
				// true;
				sendRequest(peer2, index);
			}
		}

		private void sendRequest(Peer p, int pieceIndex) {
			Message m = new Message(Byte.valueOf(Integer.toString(6)),
					ByteBuffer.allocate(4).putInt(pieceIndex).array());
			ObjectOutputStream o;
			try {
				o = this.outputStream;
				o.writeObject(m);
				o.flush();
				PeerProcess.this.sentRequestMessageByPiece[PeerProcess.this.peerList.indexOf(peer)][pieceIndex] = true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @author apurv
	 *
	 */
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

					Thread.sleep(UnchokingInterval * 1000);
					if (PeerProcess.this.peerList.size() > 0) {
						if (unchokingIntervalWisePeerDownloadingRate.size()==0) {
							

							// as it is a new arraylist, this thread is run for
							// the
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
							/*
							 * List<DownloadingRate>
							 * lastunchokingIntervalDownloadingValues =
							 * unchokingIntervalWisePeerDownloadingRate
							 * .get(unchokingIntervalWisePeerDownloadingRate.
							 * size() - 1);
							 */
							/*
							 * lastunchokingIntervalDownloadingValues.sort(new
							 * Comparator<DownloadingRate>() {
							 * 
							 * @Override public int compare(DownloadingRate
							 * arg0, DownloadingRate arg1) { return
							 * arg0.downloadingRate >= arg1.downloadingRate ? 1
							 * : -1; } });
							 */

							// select top NumberOfPrefferedNeighbors and update
							// the
							// preferred neoighbors list
							HashSet<Peer> NewPreferedNeighbours = new HashSet<Peer>();
							Random ran = new Random();
							for (int i = 0; i < NumberOfPreferredNeighbors; i++) {
								if (!unchokingIntervalWisePeerDownloadingRate.isEmpty()) {
									NewPreferedNeighbours.add(unchokingIntervalWisePeerDownloadingRate.poll().p);
								}
							}
							// if the previous downloading rates list is less
							// than
							// preffered neighbors size

							while (NewPreferedNeighbours.size() < NumberOfPreferredNeighbors) {
								NewPreferedNeighbours.add(peerList.get(ran.nextInt(peerList.size())));
							}

							// send choke messages to other who are not present
							// in
							// the
							// new list of preferred neighbors
							PreferedNeighbours.removeAll(NewPreferedNeighbours);
							sendChokeMessage(PreferedNeighbours);
							// change to new preferred neighbors
							PreferedNeighbours = NewPreferedNeighbours;
						}

						String peerIdList = "";
						for (Peer p : PreferedNeighbours) {
							peerIdList = p.peerID + ",";
						}
						writeToLog("Peer " + PeerProcess.this.currentPeer.peerID + " has the preferred neighbors "+ peerIdList.substring(0, peerIdList.length() - 1) + ".");
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
		List<Peer> interestedPeers;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (true) {
				try {

					Thread.sleep(OptimisticUnchokingInterval * 1000);

					interestedPeers = new ArrayList<>();
					for (Peer p : peerSocketMap.keySet()) {
						if (p.interestedInPieces) {
							interestedPeers.add(p);
						}
					}
					if (interestedPeers.size() > 0) {
						Random ran = new Random();
						if (optimisticallyUnchokedNeighbor != null) {
							// check if not a preferred neighbor then only
							// send
							// choke message
							if (!PeerProcess.this.PreferedNeighbours.contains(optimisticallyUnchokedNeighbor)) {
								// send a choke message to the previous
								// neighbor
								sendChokeMessage(new HashSet<>(Arrays.asList(optimisticallyUnchokedNeighbor)));
							}
						}
						optimisticallyUnchokedNeighbor = interestedPeers.get(ran.nextInt(interestedPeers.size()));
						sendUnChokeMessage(new HashSet<>(Arrays.asList(optimisticallyUnchokedNeighbor)));
						writeToLog("Peer " + PeerProcess.this.currentPeer.peerID
								+ " has the optimistically unchoked neighbor " + optimisticallyUnchokedNeighbor.peerID
								+ ".");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class DownloadingRate {
		Peer p;
		double downloadingRate;

		/**
		 * @param p
		 * @param downloadingRate
		 */
		public DownloadingRate(Peer p, double downloadingRate) {
			super();
			this.p = p;
			this.downloadingRate = downloadingRate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DownloadingRate other = (DownloadingRate) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (p == null) {
				if (other.p != null)
					return false;
			} else if (!p.equals(other.p))
				return false;
			return true;
		}

		private PeerProcess getOuterType() {
			return PeerProcess.this;
		}

	}

	private void sendChokeMessage(HashSet<Peer> peers) {
		Message m = new Message(Byte.valueOf(Integer.toString(0)), null);
		for (Peer p : peers) {

			ObjectOutputStream o;
			try {
				o = PeerProcess.this.peerObjectOutputStream.get(p);
				o.writeObject(m);
				o.flush();
				// o.close();
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
				o = PeerProcess.this.peerObjectOutputStream.get(p);
				o.writeObject(m);
				o.flush();
				// o.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
