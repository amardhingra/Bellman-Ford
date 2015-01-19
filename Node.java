import java.util.concurrent.ConcurrentHashMap;

public class Node {

	public String ipAddress;
	public int port;
	public float distance;

	boolean isDown;
	private long lastReceivedTime;

	Node prevNode;
	
	ConcurrentHashMap<String, Node> distanceVector;

	public Node(String ipAddress, int port, float distance) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.distance = distance;
	}

	/** Method for adding nodes to the distance vector of the current node **/
	public boolean addNode(String identifier, Node node) {

		// instantiate the distance vector if necessary
		if (distanceVector == null) {
			distanceVector = new ConcurrentHashMap<String, Node>();
		}

		// get the node from the distance vector
		Node dvNode = distanceVector.get(identifier);

		// if the node is the same as the old node don't change it
		if (dvNode != null && dvNode.distance == node.distance)
			return false;

		
		// add the new node to the distance vector
		distanceVector.put(identifier, node);
		return true;
	}

	public Node getNode(String identifier) {
		
		// if the distance vector has not been created yet return null
		if (distanceVector == null)
			return null;

		// return the node from the distance vector
		return distanceVector.get(identifier);
	
	}
	
	/** update the last updated time **/
	public void setLastReceivedTime(long time) {
		lastReceivedTime = time;
	}

	/** method to get the last updated time **/
	public long getLastReceivedTime() {
		return lastReceivedTime;
	}

	public String toString() {
		
		if(prevNode == null){
			return "Destination = " + ipAddress + ":" + port + ", Cost = "
					+ distance + ", Link = (None)";
		}
		
		if(distance == Float.MAX_VALUE){
			return "Destination = " + ipAddress + ":" + port + ", Cost = "
					+ "Infinity" + ", Link = (None)";
		}
		
		return "Destination = " + ipAddress + ":" + port + ", Cost = "
				+ distance + ", Link = (" + prevNode.ipAddress + ":" + prevNode.port + ")";
	}
	
	public String getIdentifier(){
		return ipAddress + ":" + port;
	}
	
	public String getDV(){
		
		String returnString = ipAddress + ":" + port + "/0.0" + "\n";
		
		if(distanceVector != null)
			for(String key:distanceVector.keySet()){
				Node n = distanceVector.get(key);
				returnString += n.ipAddress + ":" + n.port + "/" + n.distance + "\n";
			}
		
		return returnString;
		
	}

}
