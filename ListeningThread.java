import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class ListeningThread extends Thread {

	// size of the receive buffer
	final static int RECV_BUFFER_SIZE = 4096;

	// own port
	int port;

	// two maps of the network
	ConcurrentHashMap<String, Node> adjacentNodes;
	ConcurrentHashMap<String, Node> networkNodes;

	boolean keepRunning = true;

	public ListeningThread(int port,
			ConcurrentHashMap<String, Node> adjacentNodes,
			ConcurrentHashMap<String, Node> networkNodes) {

		this.port = port;

		this.adjacentNodes = adjacentNodes;
		this.networkNodes = networkNodes;
	}

	public void run() {

		try {

			// create a datagram socket to receive incoming UDP connections
			DatagramSocket incomingSocket = new DatagramSocket(port);
			
			// set the socket to timeout to keep checking if the thread
			// should keep running
			incomingSocket.setSoTimeout(100);

			while (!isInterrupted()) {

				try {

					// create a datagram packet into which incoming data can be
					// read
					byte[] recvBuffer = new byte[RECV_BUFFER_SIZE];
					DatagramPacket incomingPacket = new DatagramPacket(
							recvBuffer, RECV_BUFFER_SIZE);

					incomingSocket.receive(incomingPacket);

					// convert the incoming bytes to a string
					String incomingData = new String(
							removeTrailingZeros(incomingPacket.getData()));

					// split the data on white space
					String[] nodeData = incomingData.split("\\s+");

					// update the distance vector for the sending node
					boolean updated = updateDistanceVector(nodeData);

					// if the nodes distance vector was updated recompute
					// distance vectors
					if (updated) {
						new DistanceCalculatorThread(adjacentNodes,
								networkNodes, port).start();
					}
				} catch (SocketTimeoutException e) {

				}

			}

			// close the socket
			incomingSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean updateDistanceVector(String[] nodeData) {
		
		// assume distance vector was not updated
		boolean updated = false;

		// break the sending nodes distance into ip:port and distance
		String[] sendingNodeData = nodeData[0].split("/");

		// get the identifier for the sending nodes and get the node from the
		// map
		String sendingNodeID = sendingNodeData[0];
		Node sendingNode = adjacentNodes.get(sendingNodeID);
		
		// change the distance between the nodes to the most recent value
		if (sendingNode != null)
			sendingNode.distance = Float.parseFloat(sendingNodeData[1]);

		// if the node is not already in the map add it
		if (sendingNode == null) {

			// create a new node
			sendingNode = new Node(sendingNodeID.split(":")[0],
					Integer.parseInt(sendingNodeID.split(":")[1]),
					Float.parseFloat(sendingNodeData[1]));

			// add the node to the map of adjacent nodes
			adjacentNodes.put(sendingNodeID, sendingNode);

			updated = true;

		}

		// update the last received time for the node
		sendingNode.setLastReceivedTime(System.currentTimeMillis());

		if (sendingNode.isDown) {
			updated = true;
		}

		sendingNode.isDown = false;

		// iterating through the sending nodes distance vector
		for (int i = 1; i < nodeData.length; i++) {

			// split the data into ip:port and distances
			String[] singleNodeData = nodeData[i].split("/");

			// create a new node
			Node singleNode = new Node(singleNodeData[0].split(":")[0],
					Integer.parseInt(singleNodeData[0].split(":")[1]),
					Float.parseFloat(singleNodeData[1]));

			// try to add the node to the sending nodes distance vector
			if (!updated) {
				updated = sendingNode.addNode(singleNodeData[0], singleNode);
			} else {
				sendingNode.addNode(singleNodeData[0], singleNode);
			}

		}

		return updated;
	}

	private byte[] removeTrailingZeros(byte[] originalArray) {
		/** Method for removing the trailing zeros from a byte array **/

		int count = 0;

		for (int i = originalArray.length - 1; i >= 0; i--) {
			if (originalArray[i] != (byte) 0)
				break;
			count++;
		}

		byte[] newArray = new byte[originalArray.length - count];
		System.arraycopy(originalArray, 0, newArray, 0, newArray.length);

		return newArray;

	}

}
