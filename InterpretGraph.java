package ca.uwaterloo.pathfinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import android.graphics.PointF;

/**
 * This class contains the methods that provide the direct functionality for this entire package: 
 * take location and destination, and findPath. 
 * @author chris
 *
 */

public class InterpretGraph {
	/**
	 * 
	 * @param graph The graph to be navigated.
	 * @param root The starting location, given as a PointF  
	 * @param destination The destination vertex, given as the int corresponding to the vertex's tag. 
	 * @return A char array corresponding to the direction of the steps that should be taken to reach
	 * the destination. Temporary until graphical solution has been worked out. 
	 */
	public static ArrayList<PointF> findPath(NavigableGraph graph, PointF root, PointF destination) {
		ArrayList<PointF> pathDirections;
		
		int[] rootIndex = SVGToGraph.pointfToIndex(root);
		GraphVertex rootVertex = graph.vertices[rootIndex[0]][rootIndex[1]];
		
		int[] destIndex = SVGToGraph.pointfToIndex(destination);
		int destTag = graph.vertices[destIndex[0]][destIndex[1]].tag;
		
		// If the requested root vertex is not valid, return null; this is a standardized return and is handled upstream
		if (rootVertex == null) {
			pathDirections = new ArrayList<PointF>(0);
			return pathDirections;
		}
		
		GraphVertex path = breadthFirstSearch(graph, rootVertex, destTag);
		
		if (path == null) {
			pathDirections = new ArrayList<PointF>(0);
			return pathDirections;
		}
		
		
		pathDirections = new ArrayList<PointF>(path.distance);
		//pathDirections = new PointF[path.distance];
		
		GraphVertex current = path; 
		
		for (int i = 0; i < path.distance; i++) {
			pathDirections.add(i, current.mapLocation);
			current = current.parent;
		}
		
		return pathDirections;
		
	}
	
	
	private static GraphVertex breadthFirstSearch(NavigableGraph graph, GraphVertex rootVertex, int destination) {
		// Reset all of the values to initial state before starting
		graph.clearValues(); 
		
		// Queue structure
		Queue<GraphVertex> queue = new ArrayDeque<GraphVertex>(100000);
		
		rootVertex.distance = 0;
		queue.add(rootVertex);
		
		while (!queue.isEmpty()) {
			GraphVertex current = queue.remove();
			if (current.tag == destination) {
				return current;
			}
			
			for (int i = 0; i < 4; i++) {
				if(graph.neighbours[current.tag][i] == null) {		
				}
				else {
					if (graph.neighbours[current.tag][i].distance == -1) {
						graph.neighbours[current.tag][i].distance = current.distance + 1;
						graph.neighbours[current.tag][i].parent = current;
						queue.add(graph.neighbours[current.tag][i]);
					}
				}
			}
			
			
		}
		
		return null;
	}
}
