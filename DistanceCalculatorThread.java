import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class DistanceCalculatorThread extends Thread {

	ConcurrentHashMap<String, Node> adjacentNodes;
	ConcurrentHashMap<String, Node> networkNodes;
	int port;

	public DistanceCalculatorThread(
			ConcurrentHashMap<String, Node> adjacentNodes,
			ConcurrentHashMap<String, Node> networkNodes, int port) {

		this.adjacentNodes = adjacentNodes;
		this.networkNodes = networkNodes;

		this.port = port;

	}

	public void run() {

		// calculate the nodes' own IP address
		String myIP = "";
		try {
			myIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		for (String key : adjacentNodes.keySet()) {

			// iterate through all adjacent nodes
			Node adjacentNode = adjacentNodes.get(key);

			// check if the node is down
			if (adjacentNode.isDown) {

				// if we weren't routing directly to the adjacent node remove it
				// from the distance vector
				if (networkNodes.get(key) != null
						&& networkNodes.get(key).prevNode == adjacentNode) {

					Node n = networkNodes.get(key);
					n.distance = Float.POSITIVE_INFINITY;
					n.prevNode = null;

				}

				// get the distance vector of the adjacent node
				ConcurrentHashMap<String, Node> distanceVector = adjacentNode.distanceVector;

				if (distanceVector == null)
					continue;

				// if we were routing to any of the nodes in the distance vector
				// through the
				// down node remove it from the distance vector
				for (String dvKey : distanceVector.keySet()) {
					if (networkNodes.get(dvKey) != null
							&& networkNodes.get(dvKey).prevNode == adjacentNode) {

						Node n = networkNodes.get(dvKey);
						n.distance = Float.POSITIVE_INFINITY;
						n.prevNode = null;

					}
				}

			}
		}

		for (String key : adjacentNodes.keySet()) {

			// iterate through all adjacent nodes that aren't down
			Node adjacentNode = adjacentNodes.get(key);
			if (!adjacentNode.isDown) {

				Node networkNode;

				// the the adjacentNode isn't already in the distance vector add
				// it
				if (!networkNodes.containsKey(key)) {

					networkNode = new Node(adjacentNode.ipAddress,
							adjacentNode.port, adjacentNode.distance);

					networkNode.prevNode = adjacentNode;
					networkNodes.put(key, networkNode);

				} else {

					// otherwise get the adjacent node from the distance vector
					networkNode = networkNodes.get(key);

					// update the distance if necassary
					if (networkNode.distance > adjacentNode.distance) {
						networkNode.distance = adjacentNode.distance;
						networkNode.prevNode = adjacentNode;
					}

				}

				// get the distance vector
				ConcurrentHashMap<String, Node> distanceVector = adjacentNode.distanceVector;

				// skip if the adjacent node doesn't have a distance vector yet
				if (distanceVector == null)
					continue;

				// iterate through the nodes in the distance vector
				for (String dvKey : distanceVector.keySet()) {

					// skip distances to node itself
					if (dvKey.equals(myIP + ":" + port))
						continue;

					Node dvNode = distanceVector.get(dvKey);

					Node n = networkNodes.get(dvKey);

					// if the ndoe didn't exist in the distance vector add it
					if (n == null) {
						Node newNode = new Node(dvNode.ipAddress, dvNode.port,
								dvNode.distance + networkNode.distance);
						newNode.prevNode = adjacentNode;
						networkNodes.put(dvKey, newNode);
						continue;
					}

					// if we have a lower distance than before update it
					if (n.distance > dvNode.distance + networkNode.distance) {

						n.distance = dvNode.distance + networkNode.distance;
						n.prevNode = networkNode.prevNode;

					}

					// if we are routing through the adjacent node change to the
					// latest distance we received
					if (n.prevNode == adjacentNode) {
						n.distance = adjacentNode.distance + dvNode.distance;
					}

				}

			}
		}

		// get rid of all nodes that can't be reached through any neighbours
		for (String key : networkNodes.keySet()) {

			Node n = networkNodes.get(key);
			Node prev = n.prevNode;

			if (prev == null || prev.distanceVector == null
					|| prev.distanceVector.get(n.getIdentifier()) == null
					|| adjacentNodes.get(n.getIdentifier()) == null)
				continue;

			if (prev.distanceVector.get(n.getIdentifier()).distance == Float.MAX_VALUE
					&& adjacentNodes.get(n.getIdentifier()).isDown) {
				n.distance = Float.POSITIVE_INFINITY;
				n.prevNode = null;
			}

		}

	}

}
