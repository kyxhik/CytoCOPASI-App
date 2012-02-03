/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.session;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * A session is an immutable snapshot of the data contents of Cytoscape.
 * Sessions are only meant for saving and restoring the state of Cytoscape
 * and are not meant to be used interactively for anything besides 
 * writing, reading, and restoring from session files.
 * <br/>
 * Using the data returned from the various methods in a CySession object
 * should be sufficient to recreate all aspects of Cytoscape at the time
 * the session was created.
 * <br/>
 * Creating an instance of CySession is done following the builder pattern.
 * For example, the following code creates a session that only includes
 * a list of networkViews and Cytoscape properties, but nothing else.  
 * <br/>
 * <pre>
 * CySession session = new CySession.Builder().networkViews(viewList).cytoscapeProperties(cyProps).build();
 * </pre>
 * <br/>
 * @CyAPI.Final.Class
 */
public final class CySession {

	private final Set<CyNetwork> networks;
	private final Set<CyNetworkView> netViews;
	private final Set<CyTableMetadata> tables;
	private final Map<CyNetworkView,String> vsMap;
	private final Set<CyProperty<?>> properties;
	private final Set<VisualStyle> visualStyles;
	private final Map<String, List<File>> appFiles;

	private CySession(Builder b) {
		// Make defensive copies of objects
		networks = Collections.unmodifiableSet( b.networks == null ? new HashSet<CyNetwork>() : b.networks );
		netViews = Collections.unmodifiableSet( b.netViews == null ? new HashSet<CyNetworkView>() : b.netViews );
		tables = Collections.unmodifiableSet( b.tables == null ? new HashSet<CyTableMetadata>() : b.tables );
		vsMap = Collections.unmodifiableMap( b.vsMap == null ? new HashMap<CyNetworkView,String>() : b.vsMap );
		properties = Collections.unmodifiableSet( b.properties == null ? new HashSet<CyProperty<?>>() : b.properties );
		visualStyles = Collections.unmodifiableSet( b.visualStyles == null ? new HashSet<VisualStyle>() : b.visualStyles );
		appFiles = Collections.unmodifiableMap( b.appFiles == null ? new HashMap<String, List<File>>() : b.appFiles );
	}

	/**
	 * A implementation of the builder pattern used to construct immutable instances of CySession objects.
	 * @CyAPI.Static.Class
	 */
	public static class Builder {

		private Set<CyNetwork> networks; 
		private Set<CyNetworkView> netViews; 
		private Set<CyTableMetadata> tables;
		private Map<CyNetworkView,String> vsMap; 
		private Set<CyProperty<?>> properties;
		private Set<VisualStyle> visualStyles; 
		private Map<String, List<File>> appFiles; 

		/**
		 * Returns a complete instance of CySession based upon the methods called on this instance of Builder.
		 * @return A fully configured instanced of CySession.
		 */
		public CySession build() { return new CySession(this); }

		/**
		 * Returns an instance of Builder that has at least been configured with the specified networks.
		 * @param views A Set of CyNetwork objects, presumably all networks that exist in this instance of Cytoscape.
		 * @return An instance of Builder that has at least been configured with the specified networks.
		 */
		public Builder networks(final Set<CyNetwork> networks) { 
			this.networks = networks; 
			return this;
		}
		
		/**
		 * Returns an instance of Builder that has at least been configured with the specified network views.
		 * @param views A Set of CyNetworkView objects, presumably all network views that exist in this instance of
		 *            Cytoscape.
		 * @return An instance of Builder that has at least been configured with the specified network views.
		 */
		public Builder networkViews(final Set<CyNetworkView> views) { 
			netViews = views; 
			return this;
		}

		/**
		 * Returns an instance of Builder that has at least been configured with the specified tables.
		 * @param tables2 A Set of CyTable objects, presumably all tables that exist in this instance of Cytoscape.
		 * @return An instance of Builder that has at least been configured with the specified tables.
		 */
    	public Builder tables(final Set<CyTableMetadata> tables2) { 
			tables = tables2; 
			return this;
		}

		/**
		 * Returns an instance of Builder that has at least been configured with the specified network view visual style
		 * name map.
		 * @param vs A map of CyNetworkViews to the names of the VisualStyle currently applied to that network view, for
		 *            presumably all network views that exist in this instance of Cytoscape.
		 * @return An instance of Builder that has at least been configured with the specified network view visual style
		 *         name map.
		 */
    	public Builder viewVisualStyleMap(final  Map<CyNetworkView,String> vs) { 
			vsMap = vs; 
			return this;
		}

		/**
		 * Returns an instance of Builder that has at least been configured with the specified properties.
		 * @param p A set of session related {@link CyProperty} objects.
		 * @return An instance of Builder that has at least been configured with the specified properties.
		 */
		public Builder properties(final Set<CyProperty<?>> p) {
			properties = p;
			return this;
		}

		/**
		 * Returns an instance of Builder that has at least been configured with the specified properties.
		 * @param styles All VisualStyles in this instance of Cytoscape.
		 * @return An instance of Builder that has at least been configured with the specified properties.
		 */
    	public Builder visualStyles(final Set<VisualStyle> styles) { 
			visualStyles = styles; 
			return this;
		}

		/**
		 * Returns an instance of Builder that has at least been configured with the specified app file list map.<br/>
		 * The app name should follow the java class namespace convention (e.g. org.orgname.appname) in order to prevent
		 * name collisions. The "org.cytoscape" domain is reserved for core Cytoscape apps (e.g. org.cytoscape.filter).
		 * @param p A map of app names to a list of File objects that the given app wants stored in the session file.
		 *            The app name should follow the java class namespace convention.
		 * @return An instance of Builder that has at least been configured with the specified app file list map.
		 */
		public Builder appFileListMap(final Map<String, List<File>> p) { 
			this.appFiles = p; 
			return this;
		}
	}

	/**
	 * Returns a set of all CyNetwork objects contained in this Session. 
	 * @return A set of all CyNetwork objects contained in this Session. 
	 */
    public Set<CyNetwork> getNetworks() { return networks; }
	
	/**
	 * Returns a set of all CyNetworkView objects contained in this Session. 
	 * @return A set of all CyNetworkView objects contained in this Session. 
	 */
    public Set<CyNetworkView> getNetworkViews() { return netViews; }

	/**
	 * Returns a set of all CyTable objects contained in this Session. 
	 * @return A set of all CyTable objects contained in this Session. 
	 */
    public Set<CyTableMetadata> getTables() { return tables; }

	/**
	 * Returns a map of CyNetworkViews to the names of the VisualStyle applied to that network view in this session.
	 * @return A map of CyNetworkViews to the names of the VisualStyle applied to that network view in this session.
	 */
    public Map<CyNetworkView,String> getViewVisualStyleMap() { return vsMap; }

	/**
	 * Returns a set of {@link CyProperty} objects defined for this session.
	 * @return A set of session related {@link CyProperty} objects. defined for this session.
	 */
    public Set<CyProperty<?>> getProperties() { return properties; }

	/**
	 * Returns a set containing all VisualStyles defined for this session.
	 * @return A Set of {@link org.cytoscape.view.vizmap.VisualStyle} objects
	 */
    public Set<VisualStyle> getVisualStyles() { return visualStyles; }

	/**
	 * Returns a map of app names to lists of File objects that are stored as part of the session for the specified app.
	 * @return A map of app names to lists of File objects that are stored as part of the session for the specified app.
	 */
	public Map<String, List<File>> getAppFileListMap() { return appFiles; }
}
