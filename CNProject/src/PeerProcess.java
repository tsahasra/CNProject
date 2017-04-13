import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

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
	HashSet<Peer> NewPrefNeighbors;
	HashSet<Peer> sendUnchokePrefNeig;
	Peer optimisticallyUnchokedNeighbor;
	PriorityQueue<DownloadingRate> unchokingIntervalWisePeerDownloadingRate;
	Logger logger;
	boolean[][] sentRequestMessageByPiece;
	boolean fileComplete;
	int lastPeerID;
	BlockingQueue<MessageWriter> bqm;
	BlockingQueue<String> bql;
	HashMap<Peer, Socket> peerSocketMap;
	int pieceMatrix[][];
	// HashMap<Peer, OutputStream> peerObjectOutputStream;
	public final Object inputSynchronize = new Object();
	Future<?> prefNeighborTask;
	Future<?> optimisticallyUnchokeNeighborTask;
	Future<?> logManagerTask;
	Future<?> messageQueueTask;
	public volatile boolean exit = false;

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
		BufferedReader pireader = null;

		int count = 0;
		try {
			pireader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\peerInfo.cfg"));
			while (pireader.readLine() != null)
				count++;
			count--;
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		return count;
	}

	private void copyFileUsingStream(String source, String dest) throws IOException {
		FileChannel sourceChannel = null;
		FileChannel destChannel = null;
		try {
			sourceChannel = new FileInputStream(new File(source)).getChannel();
			destChannel = new FileOutputStream(new File(dest), false).getChannel();
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		} finally {
			sourceChannel.close();
			destChannel.close();
		}
	}

	private void initializePeerList(PeerProcess p, String peerID) throws IOException {
		BufferedReader pireader = new BufferedReader(new FileReader(System.getProperty("user.dir") +"\\"+"peerInfo.cfg"));
		String line, tokens[];
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
						p.copyFileUsingStream(new String(System.getProperty("user.dir") + "\\" + this.FileName),
								new String(System.getProperty("user.dir") + "\\peer_" + peerID + "\\" + this.FileName));
						FileName = System.getProperty("user.dir") + "\\peer_" + currentPeer.peerID + "\\"
								+ this.FileName;
						System.out.println(FileName);
						fileComplete = true;
						currentPeer.bitfield = new byte[bfsize];
						for (int i = 0; i < noOfPieces; i++)
							setBit(currentPeer.bitfield, i);
					} else {
						FileName = System.getProperty("user.dir") + "\\peer_" + currentPeer.peerID + "\\"
								+ this.FileName;
						new File(FileName).delete();
						new File(FileName).createNewFile();
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
			p.noOfPieces = (p.FileSize / p.PieceSize) + 1;
			pieceMatrix = new int[noOfPieces][2];
			int startPos = 0;
			int psize = p.PieceSize;
			int cumpsize = p.PieceSize;
			for (int i = 0; i < noOfPieces; i++) {
				pieceMatrix[i][0] = startPos;
				pieceMatrix[i][1] = psize;

				startPos += psize;

				if (!(p.FileSize - cumpsize > p.PieceSize))
					psize = p.FileSize - cumpsize;

				cumpsize += psize;

			}
			sentRequestMessageByPiece = new boolean[this.noOfPeers][this.noOfPieces];
			PeerProcess.this.chokedfrom = new HashSet<>();
			PeerProcess.this.peerSocketMap = new HashMap<>();
			// PeerProcess.this.peerObjectOutputStream = new HashMap<>();
			PeerProcess.this.bqm = new LinkedBlockingQueue<MessageWriter>();
			PeerProcess.this.bql = new LinkedBlockingQueue<String>();
			PeerProcess.this.unchokingIntervalWisePeerDownloadingRate = new PriorityQueue<>(
					new Comparator<DownloadingRate>() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see java.util.Comparator#compare(java. lang. Object,
						 * java.lang.Object)
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
		ExecutorService exec = Executors.newFixedThreadPool(4);
		try {

			// PeerProcess.this.chokedto = new HashSet<>();

			prefNeighborTask = exec.submit(new PrefferedNeighborsThread(PeerProcess.this));
			optimisticallyUnchokeNeighborTask = exec.submit(new OptimisticallyUnchokedNeighborThread(PeerProcess.this));
			messageQueueTask = exec.submit(new MessageQueueProcess(PeerProcess.this));
			logManagerTask = exec.submit(new LogManager(PeerProcess.this.bql, logger, this));

			int peerCompleteFileReceived = 0;
			serverSocket = new ServerSocket(portNo);
			int totalConnectedPeers = 0;

			while (!PeerProcess.this.exit) {
				peerCompleteFileReceived = 0;
				if (currentPeer.peerID != lastPeerID && totalConnectedPeers < peerList.size()) {

					Socket socket;
					if (totalConnectedPeers != this.noOfPeers) {
						socket = serverSocket.accept();
						Peer tempPeer = getPeerFromPeerList(socket.getInetAddress().getHostAddress(), socket.getPort());
						PeerProcess.this.bql
								.put("Peer " + this.currentPeer.peerID + " is connected from Peer " + tempPeer.peerID);
						peerSocketMap.put(peerList.get(peerList.indexOf(tempPeer)), socket);
						ClientHandler clientHandler = new ClientHandler(tempPeer, false);
						clientHandler.start();
						totalConnectedPeers++;
					}
				}
				// check for termination of this process

				for (Peer p : peerList) {
					if (checkIfFullFileRecieved(p)) {
						peerCompleteFileReceived++;
					}
				}
				if (peerCompleteFileReceived == peerList.size()) {
					// check if you recievecd the whole file
					if (checkIfFullFileRecieved(currentPeer)) {
						// now terminate the process of executorService
						// exec.shutdown();
						PeerProcess.this.exit = true;
						
						break;
					}
				}
			}
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				
				while (!exec.isTerminated()) {
					prefNeighborTask.cancel(true);
					optimisticallyUnchokeNeighborTask.cancel(true);
					while(!bqm.isEmpty());
					while(!bql.isEmpty());
					messageQueueTask.cancel(true);
					logManagerTask.cancel(true);
					
					
					exec.shutdownNow();
				}

				for (Socket s : peerSocketMap.values()) {
					if (!s.isClosed())
						s.close();
				}

				

				
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

		Iterator<Peer> it = this.peerList.iterator();
		while (it.hasNext()) {
			
			Peer tempPeer = (Peer) it.next();
			System.out.println(port);
			if (tempPeer.peerIP.equals(hostAddress))
				return tempPeer;
		}
		return null;
	}

	public void connectToPreviousPeer(Peer p) {
		Socket socket;
		try {
			socket = new Socket(p.peerIP, p.peerPort);
			PeerProcess.this.bql.put("Peer " + this.currentPeer.peerID + " makes a connection to Peer " + p.peerID);
			// Peer tempPeer = new Peer(p.peerID,p.peerIP,p.peerPort);
			peerSocketMap.put(peerList.get(this.peerList.indexOf(p)), socket);
			ClientHandler clientHandler = new ClientHandler(p, true);
			clientHandler.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void setBit(byte[] b, int index) {
		byte b1 = 1;
		b[index / 8] = (byte) (b[index / 8] | b1 << ((index) % 8));
	}

	public static int getBit(byte[] b, int index) {
		byte b1 = b[index / 8];
		byte be = 1;

		if ((b1 & (be << ((index) % 8))) != 0)
			return 1;
		else
			return 0;

	}

	public static void clearBit(byte[] b, int index) {
		byte b1 = 1;
		b[index / 8] = (byte) (b[index / 8] & (~(b1 << ((index) % 8))));

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
		MessageReader mread;
		DataOutputStream outputStream;
		Peer peer;
		boolean initiateHandShake;
		long starttime, endtime;

		ClientHandler(Peer p, boolean initiateHS) throws IOException {
			this.socket = PeerProcess.this.peerSocketMap.get(p);
			this.peer = p;

			socket.setSoLinger(true, 70);
			mread = new MessageReader(socket, PeerProcess.this);
			// outputStream = new DataOutputStream(socket.getOutputStream());
			// PeerProcess.this.peerObjectOutputStream.put(p, new
			// DataOutputStream(socket.getOutputStream()));
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
			HandShake hs = new HandShake(PeerProcess.this.currentPeer.peerID);

			try {
				PeerProcess.this.bqm.put(new MessageWriter(hs, new DataOutputStream(socket.getOutputStream())));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {

			while (!PeerProcess.this.exit) {
				try {

					Object o;
					starttime = System.currentTimeMillis();
					o = mread.readObject();
					endtime = System.currentTimeMillis();

					/*
					 * if (o == null) { continue; }
					 */
					if (o instanceof HandShake) {
						HandShake h = (HandShake) o;
						if (ByteBuffer.wrap(h.peerID).getInt() == this.peer.peerID) {

							if (!initiateHandShake)
								sendHandShake();
							else {
								sendBitfield();
							}
						}
						PeerProcess.this.noOfPeerHS++;
					} else if (o instanceof Message) {

						Message message = (Message) o;
						System.out.println(message);
						switch (Byte.toUnsignedInt(message.type)) {

						case 0:
							choke(peer);
							break;
						case 1:
							unchoke(peer);
							break;

						case 2:
							this.peer.interestedInPieces = true;
							try {
								PeerProcess.this.bql.put("Peer " + PeerProcess.this.currentPeer.peerID
										+ " received the 'interested' message from " + peer.peerID);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							break;
						case 3:
							this.peer.interestedInPieces = false;
							try {
								PeerProcess.this.bql.put("Peer " + PeerProcess.this.currentPeer.peerID
										+ " received the 'not interested' message from " + peer.peerID);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							break;

						case 4:
							processHaveMessage(message);
							break;

						case 5:
							this.peer.isHandShakeDone = true;
							peer.bitfield = message.payload;
							if (!initiateHandShake)
								sendBitfield();

							if (!PeerProcess.this.isFilePresent) {

								sendInterestedifApplicable();
							}

							break;

						case 6:
							processRequest(message);
							break;

						case 7:
							processPieceMessage(message);

							break;

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					PeerProcess.this.exit = true;
					break;
				}

			}
		}

		/**
		 * @throws IOException
		 * 
		 * 
		 */
		private void sendInterestedifApplicable() throws IOException {

			for (int i = 0; i < noOfPieces; i++) {
				int bitAtIndexOfCurrPeer = getBit(currentPeer.bitfield, i);
				int bitAtIndexOfPeer = getBit(peer.bitfield, i);
				if (bitAtIndexOfCurrPeer == 0 && bitAtIndexOfPeer == 1) {
					
					Message interested = new Message(1, Byte.valueOf(Integer.toString(2)), null);
					// update the interested from array
					this.peer.interestedFromBitfield[i] = true;
					try {
						PeerProcess.this.bqm
								.put(new MessageWriter(interested, new DataOutputStream(socket.getOutputStream())));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					break;
				}
			}

		}

		private void processPieceMessage(Message message) throws IOException {

			updatePeerDownloadingRate();

			writePieceToFile(message.payload);

			

			sendHaveMessageToAll(message.payload);

			if (!fileComplete) {
				List<Integer> pieceIndex = new ArrayList<Integer>();

				/*
				 * Get list of all pieces not yet received and for which request
				 * has not yet been sent
				 */
				for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
					if (getBit(PeerProcess.this.currentPeer.bitfield, i) == 0 && getBit(peer.bitfield, i) == 1) {
						boolean indexRequestSentFlag = false;
						pieceIndex.add(i);
						/*for (int j = 0; j < PeerProcess.this.noOfPeers; j++)
							if (PeerProcess.this.sentRequestMessageByPiece[j][i]) {
								indexRequestSentFlag = true;
								break;
							}*/

					}
					
				}
				if (pieceIndex.size() > 0) {
					Random rnd = new Random();
					int selectedIndex = rnd.nextInt(pieceIndex.size());
					sendRequest(peer, pieceIndex.get(selectedIndex));
				}
			}
			
			
			sendNIToSomeNeighbours();
		}

		/**
		 * 
		 * 
		 */
		private void updatePeerDownloadingRate() {
			// TODO Auto-generated method stub

			DownloadingRate dr = new DownloadingRate(peer,
					(double) (PeerProcess.this.PieceSize / ((this.endtime - this.starttime) + 1)));

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
			
			if (getBit(PeerProcess.this.currentPeer.bitfield, index) == 0) {
				setBit(PeerProcess.this.currentPeer.bitfield, index);
				//if file complete set the bit
				
				for (Peer p : PeerProcess.this.peerList) {
					if (p.isHandShakeDone) {
						Message have = new Message(5, Byte.valueOf(Integer.toString(4)), i);
						this.socket = PeerProcess.this.peerSocketMap.get(p);
						try {
							PeerProcess.this.bqm
									.put(new MessageWriter(have, new DataOutputStream(socket.getOutputStream())));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if(checkIfFullFileRecieved(PeerProcess.this.currentPeer)){
					try {
						PeerProcess.this.bql.put("Peer "+PeerProcess.this.currentPeer.peerID+" has downloaded the complete file.");
						PeerProcess.this.exit=true;
					} catch (InterruptedException e) {
						e.printStackTrace();
						
					}
				}
				
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

			for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
				if (getBit(PeerProcess.this.currentPeer.bitfield, i) == 1)
					NIIndices.add(i);
			}

			for (Peer p : PeerProcess.this.peerList) {
				if (p.isHandShakeDone) {
					boolean amIInterestedInAnyPiecesOfThisPeer = false;
					for (int j = 0; j < PeerProcess.this.noOfPieces; j++) {
						if (getBit(p.bitfield, j) == 1 && !NIIndices.contains(j)
								&& !PeerProcess.this.sentRequestMessageByPiece[peerList.indexOf(p)][j]) {
							{
								amIInterestedInAnyPiecesOfThisPeer = true;
								break;
							}
						}

					}
					if (!amIInterestedInAnyPiecesOfThisPeer) {
						Message notinterested = new Message(1, Byte.valueOf(Integer.toString(3)), null);
						try {
							PeerProcess.this.bqm.put(new MessageWriter(notinterested,
									new DataOutputStream(peerSocketMap.get(p).getOutputStream())));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * for (int j = 0; j < PeerProcess.this.noOfPeers; j++) { for (int k
			 * = 0; k < PeerProcess.this.noOfPieces; k++){ if
			 * (getBit(PeerProcess.this.peerList.get(j).bitfield, k) == 1 &&
			 * !NIIndices.contains(k)) { for (int m = 0; m <
			 * PeerProcess.this.noOfPeers; m++) if
			 * (!PeerProcess.this.sentRequestMessageByPiece[m][k]) {
			 * sendNIMessage = false; break; }
			 * 
			 * }
			 * 
			 * 
			 * }
			 * 
			 * if (sendNIMessage) { Message notinterested = new Message(1,
			 * Byte.valueOf(Integer.toString(3)), null); try {
			 * PeerProcess.this.bqm .put(new MessageWriter(notinterested, new
			 * DataOutputStream(socket.getOutputStream()))); } catch
			 * (InterruptedException e) { e.printStackTrace(); } break; }
			 * 
			 * }
			 */

		}

		/**
		 * @param j
		 * @return
		 * 
		 */
		private boolean sentRequestForIndex(int j) {
			// TODO Auto-generated method stub

			for (int i = 0; i < PeerProcess.this.noOfPeers; i++)
				if (sentRequestMessageByPiece[i][j])
					return true;

			return false;
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

			try {
				PeerProcess.this.bql.put("Peer " + PeerProcess.this.currentPeer.peerID
						+ " received the 'have' message from " + peer.peerID + " for the piece " + index + ".");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			sendInterestedifApplicable();
			/*
			 * if (getBit(PeerProcess.this.currentPeer.bitfield, index) == 0) {
			 * Message interested = new Message(1,
			 * Byte.valueOf(Integer.toString(2)), null); try {
			 * PeerProcess.this.bqm .put(new MessageWriter(interested, new
			 * DataOutputStream(socket.getOutputStream()))); } catch
			 * (InterruptedException e) { e.printStackTrace(); } // update the
			 * interested from array this.peer.interestedFromBitfield[index] =
			 * true; }
			 */
		}

		/**
		 * @param message
		 * @throws IOException
		 * 
		 * 
		 */
		private void processRequest(Message message) throws IOException {
			// TODO Auto-generated method stub
			if ((PeerProcess.this.PreferedNeighbours != null && PeerProcess.this.PreferedNeighbours.contains(peer))
					|| (PeerProcess.this.optimisticallyUnchokedNeighbor != null
							&& PeerProcess.this.optimisticallyUnchokedNeighbor.equals(peer))) {
				int index = ByteBuffer.wrap(message.payload).getInt();
				if (getBit(PeerProcess.this.currentPeer.bitfield, index) == 1) {
					byte[] piece = new byte[PeerProcess.this.PieceSize + 4];
					System.arraycopy(message.payload, 0, piece, 0, 4);
					RandomAccessFile rafr = new RandomAccessFile(new File(FileName), "r");
					rafr.seek(PeerProcess.this.pieceMatrix[index][0]);
					rafr.readFully(piece, 4, PeerProcess.this.pieceMatrix[index][1]);
					rafr.close();
					Message mpiece = new Message(PeerProcess.this.PieceSize + 5, (byte) 7, piece);
					try {
						PeerProcess.this.bqm
								.put(new MessageWriter(mpiece, new DataOutputStream(socket.getOutputStream())));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

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
			byte[] piece = new byte[PeerProcess.this.pieceMatrix[index][1]];
			System.arraycopy(payload, 4, piece, 0, PeerProcess.this.pieceMatrix[index][1]);
			RandomAccessFile rafw = new RandomAccessFile(new File(FileName), "rw");
			rafw.seek(PeerProcess.this.pieceMatrix[index][0]);
			rafw.write(piece, 0, PeerProcess.this.pieceMatrix[index][1]);
			rafw.close();

			int nop = 0;

			for (int j = 0; j < PeerProcess.this.noOfPieces; j++)
				if (getBit(PeerProcess.this.currentPeer.bitfield, j) == 1)
					nop++;

			try {
				PeerProcess.this.bql.put("Peer " + PeerProcess.this.currentPeer.peerID + " has downloaded the piece "
						+ index + " from " + this.peer.peerID + ". Now the number of pieces it has is " + (nop + 1));
				// setBit(PeerProcess.this.currentPeer.bitfield, index);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		private void sendBitfield() throws IOException {
			Message m = new Message(PeerProcess.this.currentPeer.bitfield.length + 1, Byte.valueOf(Integer.toString(5)),
					PeerProcess.this.currentPeer.bitfield);
			try {
				PeerProcess.this.bqm.put(new MessageWriter(m, new DataOutputStream(socket.getOutputStream())));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 
		 */
		private void choke(Peer p) {
			try {
				PeerProcess.this.bql
						.put("Peer " + PeerProcess.this.currentPeer.peerID + " is choked by " + p.peerID + ".");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			chokedfrom.add(p);
			int indexOfPeer = peerList.indexOf(p);
			// reset the sentRequestMessageBy Piece array by comparing the
			// bitfield array and request array
			for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
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
			try {
				PeerProcess.this.bql
						.put("Peer " + PeerProcess.this.currentPeer.peerID + " is unchoked by " + peer2.peerID + ".");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			chokedfrom.remove(peer2);

			if (!isFilePresent) {
				// after receiving unchoke, check if this peer is interested in
				// any
				// of the pieces of the peerUnchokedFrom
				// if interested, check if that piece is not requested to any
				// other
				// peer
				List<Integer> interestedPieces = new ArrayList<Integer>();
				int indexOfPeer = peerList.indexOf(peer2);
				for (int i = 0; i < PeerProcess.this.noOfPieces; i++) {
					int bitPresent = getBit(currentPeer.bitfield, i);
					int bitPresentAtPeerWeRequesting = getBit(peer2.bitfield, i);
					if (bitPresent == 0 && bitPresentAtPeerWeRequesting == 1) {
						boolean alreadySentRequestToSomeOtherPeer = false;
						interestedPieces.add(i);
						/*for (int j = 0; j < PeerProcess.this.sentRequestMessageByPiece.length; j++) {
							if (PeerProcess.this.sentRequestMessageByPiece[j][i]) {
								alreadySentRequestToSomeOtherPeer = true;
								break;
							}
						}
						if (!alreadySentRequestToSomeOtherPeer) {
							interestedPieces.add(i);
						}*/
					}
				}
				if (interestedPieces.size() > 0) {
					// select any one piece randomly
					Random ran = new Random();
					int index = ran.nextInt(interestedPieces.size());
					// PeerProcess.this.sentRequestMessageByPiece[indexOfPeer][index]
					// =
					// true;
					sendRequest(peer2, interestedPieces.get(index));
				}
			}
		}

		private void sendRequest(Peer p, int pieceIndex) {
			if (getBit(PeerProcess.this.currentPeer.bitfield, pieceIndex) == 0 && getBit(p.bitfield, pieceIndex) == 1) {
				Message m = new Message(5, Byte.valueOf(Integer.toString(6)),
						ByteBuffer.allocate(4).putInt(pieceIndex).array());

				try {
					PeerProcess.this.bqm.put(new MessageWriter(m, new DataOutputStream(socket.getOutputStream())));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PeerProcess.this.sentRequestMessageByPiece[PeerProcess.this.peerList.indexOf(p)][pieceIndex] = true;
			}
		}
	}

	/**
	 * @author apurv
	 *
	 */

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

	public void sendChokeMessage(HashSet<Peer> peers) {
		Message m = new Message(1, Byte.valueOf(Integer.toString(0)), null);
		for (Peer p : peers) {
			if (p.isHandShakeDone) {
				try {
					Socket socket = PeerProcess.this.peerSocketMap.get(p);
					PeerProcess.this.bqm.put(new MessageWriter(m, new DataOutputStream(socket.getOutputStream())));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void sendUnChokeMessage(HashSet<Peer> peers) {
		Message m = new Message(1, Byte.valueOf(Integer.toString(1)), null);
		for (Peer p : peers) {
			if (p.isHandShakeDone) {
				try {
					Socket socket = PeerProcess.this.peerSocketMap.get(p);
					PeerProcess.this.bqm.put(new MessageWriter(m, new DataOutputStream(socket.getOutputStream())));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}