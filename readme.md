Name : Amar Dhingra

This program runs the distributed Bellman-Ford algorithm at multiple nodes
in a network allowing each node to learn about and calculate the shortest
distance to every other node on the network

To compile type "make" in the directory containing the Makefile and the java
files.

To run invoke 
$ java bfclient localport timeout [ipaddress1 port1 weight1 ...]
with any triple of ipaddress, port, weight combinations
NOTE: ipaddress(n) cannot be 127.0.0.1 or localhost but should be the actual
ipaddress of the machine or the canonical name (eg: vienna, delhi, etc for clic machines)

After starting multiple instances of bfclient on different machines after some time
the clients distance vectors will converge to the shortest paths.

The program supports the following commands:
1) SHOWRT - this shows the current nodes distance vector
	usage: SHOWRT
NOTE: When there is no link available the distance will be Infinity
2) LINKUP - this reconnects the link to the given node
	usage: LINKUP ipaddress port
3) LINKDOWN - this disconnects the link to a given node
	usage: LINKDOWN ipaddress port
	NOTE: if the node receives a route update from the down node the link will
	be restored
4) CLOSE - this terminates the process on the current machine
	usage: CLOSE

5) SHOWDV - this command displays the distance vector of the specified node
	usage: SHOWDV ipaddress port
6) UPDATE - this command changes the distance to the specified node
	usage: UPDATE ipaddress port new_distance
