package ca.uwaterloo.pathfinder;

import android.graphics.PointF;

/**
 * This class represents the vertex of a graph.
 * @author chris
 *
 */
public class GraphVertex {
	public int tag;
	public Boolean visited;
	public int distance;
	public GraphVertex parent;
	public PointF mapLocation;
	
	public GraphVertex(int newTag, PointF mapLoc) {
		tag = newTag;
		visited = false; 
		distance = -1;
		parent = null;
		mapLocation = mapLoc;
	}
}
