package org.cytoscape.view.presentation;

import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;

public interface TableRenderingEngine extends RenderingEngine<CyTable> {
	
	Collection<View<CyRow>> getSelectedRows();

	View<CyColumn> getSelectedColumn();
	
}