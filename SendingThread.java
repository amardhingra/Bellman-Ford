import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class SendingThread extends Thread {

	ConcurrentHashMap<String, Node> adjacentNodes;
	ConcurrentHashMap<String, Node> networkNodes;

	int port;

	public SendingThread(ConcurrentHashMap<String, Node> adjacentNodes,
			ConcurrentHashMap<String, Node> networkNodes, int port) {

		this.adjacentNodes = adjacentNodes;
		this.networkNodes = networkNodes;

		this.port = port;
	}

	public void run() {

		DatagramSocket sendingSocket = null;

		try {

			// create a socket for sending outgoing packets
			sendingSocket = new DatagramSocket();

			for (String key : adjacentNodes.keySet()) {

				// iterate through all connected nodes
				Node adjacentNode = adjacentNodes.get(key);

				// skip all nodes that are down
				if (adjacentNode.isDown)
					continue;

				// generate the first line of my dv protocol
				String sendingString = InetAddress.getLocalHost()
						.getHostAddress()
						+ ":" + port + "/"
						+ adjacentNode.distance + "\n";

				// iterate through all the nodes in nodes distance vector
				for (String netKey : networkNodes.keySet()) {

					Node networkNode = networkNodes.get(netKey);

					// if networkNode is being routed to through the adjacentNode
					// send a poissoned reverse value of MAX_FLOAT
					if (networkNode.prevNode == adjacentNode) {
						sendingString += networkNode.ipAddress + ":"
								+ networkNode.port + "/" + Float.MAX_VALUE
								+ "\n";
						continue;
					}

					// add the cost to the node
					sendingString += networkNode.ipAddress + ":"
							+ networkNode.port + "/" + networkNode.distance
							+ "\n";

				}

				// convert the string to a byte array and build a datagrampacket
				byte[] sendingArray = sendingString.getBytes();
				DatagramPacket packet = new DatagramPacket(sendingArray,
						sendingArray.length,
						InetAddress.getByName(adjacentNode.ipAddress),
						adjacentNode.port);

				// send the datagram packet
				sendingSocket.send(packet);
			}

		}

		catch (IOException e) {
		}

		// close the datagram socket
		sendingSocket.close();

	}

}
