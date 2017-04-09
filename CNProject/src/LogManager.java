import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class LogManager implements Runnable {
	BlockingQueue<String> bql;
	Logger logger;

	public LogManager(BlockingQueue<String> b, Logger logger) {
		this.bql = b;
		this.logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (true) {
				if (!bql.isEmpty())
					logger.log(Level.INFO, bql.take());
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

	}

}