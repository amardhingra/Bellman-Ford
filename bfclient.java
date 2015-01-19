import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class bfclient {

	// class variables for command line arguments
	int port;
	float timeout;

	// hashmaps that contain the connected nodes
	ConcurrentHashMap<String, Node> adjacentNodes;
	ConcurrentHashMap<String, Node> networkNodes;

	public bfclient(String[] args) {

		// instantiate the hashmaps
		adjacentNodes = new ConcurrentHashMap<String, Node>();
		networkNodes = new ConcurrentHashMap<String, Node>();

		// parse the arguments
		parseArgs(args);

	}

	public void parseArgs(String[] args) {

		// get port and timeout
		this.port = Integer.parseInt(args[0]);
		this.timeout = Float.parseFloat(args[1]);

		for (int i = 2; i < args.length; i += 3) {

			try {

				// creating the link object for the adjacent node
				String identifier = InetAddress.getByName(args[i])
						.getHostAddress() + ":" + args[i + 1];

				Node adjacentNode = null;

				adjacentNode = new Node(InetAddress.getByName(args[i])
						.getHostAddress(), Integer.parseInt(args[i + 1]),
						Float.parseFloat(args[i + 2]));

				adjacentNode.prevNode = adjacentNode;
				adjacentNode.setLastReceivedTime(System.currentTimeMillis());

				// placing the node in both hashmaps
				this.adjacentNodes.put(identifier, adjacentNode);

			} catch (NumberFormatException e) {
			} catch (UnknownHostException e) {
			}

		}
	}

	public void start() {

		new DistanceCalculatorThread(adjacentNodes, networkNodes, port).start();

		// start a thread that listens for distance vector updates from
		// neighbors
		ListeningThread listener = new ListeningThread(port, adjacentNodes,
				networkNodes);
		listener.start();

		// start a thread that allows the user to interact with the algorithm
		UserInteractionThread uiThread = new UserInteractionThread(
				adjacentNodes, networkNodes, listener, port);
		uiThread.start();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				for (String key : adjacentNodes.keySet()) {

					Node n = adjacentNodes.get(key);

					if (!n.isDown && System.currentTimeMillis() - n.getLastReceivedTime() > 3 * 1000 * timeout) {
						n.isDown = true;
						new DistanceCalculatorThread(adjacentNodes, networkNodes, port).start();
					}

				}

				new SendingThread(adjacentNodes, networkNodes, port).start();
			}
		}, 0, (long) (timeout * 1000));

		try {
			uiThread.join();
			timer.cancel();
		} catch (InterruptedException e) {
		}

	}

	public static void main(String[] args) {

		// make sure port and timeout are given
		if (args.length < 2) {
			System.out
					.println("usage: ./bfclient localport timeout [ipaddress1 port1 weight1 ...]");
			System.exit(1);
		}

		// start running the Bellman Ford algorithm
		bfclient bfc = new bfclient(args);
		bfc.start();

	}

}
