package org.cytoscape.CytoCopasiApp;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

public class NetworkComparison {

	CyNetworkFactory networkFactory;
			CyNetworkManager networkManager;
			CyRootNetworkManager rootNetworkManager;
			public void something() {
			// create the first network
			CyNetwork subNetwork1 = networkFactory.createNetwork();
			networkManager.addNetwork(subNetwork1);
			        
			// add more networks
			CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(subNetwork1);
			CyNetwork subNetwork2 = rootNetwork.addSubNetwork();
			rootNetwork.addSubNetwork();
			
			}
}
