import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class UserInteractionThread extends Thread {

	ConcurrentHashMap<String, Node> adjacentNodes;
	ConcurrentHashMap<String, Node> networkNodes;

	int port;

	// references to the sending and listening threads
	ListeningThread listener;

	public UserInteractionThread(ConcurrentHashMap<String, Node> adjacentNodes,
			ConcurrentHashMap<String, Node> networkNodes,
			ListeningThread listener, int port) {

		this.adjacentNodes = adjacentNodes;
		this.networkNodes = networkNodes;

		this.listener = listener;

		this.port = port;

	}

	public void run() {

		// Begin scanning for user input
		Scanner userInput = new Scanner(System.in);

		while (true) {

			// read the next line the user types
			String command = userInput.nextLine();

			if (command.contains("LINKDOWN")) {

				// split on space
				String[] nodeData = command.split(" ");

				// get the IP address and port of the node that is down and
				// mark that node as down in the adjacent Node table
				Node downNode = adjacentNodes.get(nodeData[1] + ":"
						+ nodeData[2]);

				if (downNode == null){
					System.out.println("Node does not exist");
					continue;
				}

				if (!downNode.isDown) {

					downNode.isDown = true;

					// recalculate all distances
					new DistanceCalculatorThread(adjacentNodes, networkNodes,
							port).start();
				}

			} else if (command.contains("LINKUP")) {

				// split on space
				String[] nodeData = command.split(" ");

				// get the IP address and port of the node that is down and
				// mark that node as up in the adjacent Node table
				Node upNode = adjacentNodes
						.get(nodeData[1] + ":" + nodeData[2]);

				if (upNode == null){
					System.out.println("Node does not exist");
					continue;
				}
				
				
				if (upNode.isDown) {

					upNode.isDown = false;

					// recalculate all distances
					new DistanceCalculatorThread(adjacentNodes, networkNodes,
							port).start();
				}

			} else if (command.contains("SHOWRT")) {

				// format and print the distances to all nodes
				System.out.println(new Date() + " Distance vector list is:");

				for (String key : networkNodes.keySet()) {
					System.out.println(networkNodes.get(key));
				}

			} else if (command.contains("CLOSE")) {

				// let the sender and listener know they should end
				listener.interrupt();
				break;
			} else if (command.contains("SHOWDV")) {

				// split on space
				String[] nodeData = command.split(" ");

				// get the IP address and port of the node that is down and
				// mark that node as up in the adjacent Node table
				Node node = adjacentNodes.get(nodeData[1] + ":" + nodeData[2]);
				
				if (node == null) {
					System.out.println("Node does not exist");
					continue;
				}

				System.out.println(node.getDV());

			} else if (command.contains("UPDATE")) {

				// split on space
				String[] nodeData = command.split(" ");

				// get the IP address and port of the node that is down and
				// mark that node as up in the adjacent Node table
				Node node = adjacentNodes.get(nodeData[1] + ":" + nodeData[2]);

				if (node == null) {
					System.out.println("Node does not exist");
					continue;
				}

				// updating the distanceg
				node.distance = Float.parseFloat(nodeData[3]);
				
				networkNodes.remove(nodeData[1] + ":" + nodeData[2]);
				
				new DistanceCalculatorThread(adjacentNodes, networkNodes, port).start();

			}

		}

		userInput.close();

	}

}
