package ca.uwaterloo.pathfinder;

import android.graphics.PointF;
import ca.uwaterloo.pathfinder.GraphVertex;

/**
 * Data structure representing the graph that will be formed from the SVG.
 * From this, the object that holds the actual data will be created. 
 * @author chris
 *
 */

public class NavigableGraph {
	public GraphVertex[][] vertices;		// 2d does not use more memory, and makes createNeighbourList much more efficient.
											// each vertex is assigned a simple integer as a tag. 
	public GraphVertex[][] neighbours;		// holds a list of the neighbours of each vertex. Each index in the first array corresponds to 
											// one vertex, and contains the secondary array of length 4, which contains each neighbour.
	public final float vertexSideLength = 10;
	private int verticesW;
	private int verticesH;
	

	
	/**
	 * The map will be split into 10x10 square vertices, so the size of vertices[][] will be maxWidth*maxHeight/100.
	 * Adjacent edges are considered to be those directly adjacent geometrically, so there are at most 4*size(vertices[][])
	 * edges. neighbours will be initialized with that in mind.
	 * @param maxWidth The maximum width of the graph, determined from the width declaration in the SVG.
	 * @param maxHeight The maximum height of the graph, determined from the height declaration in the SVG.
	 */
	public NavigableGraph(float maxWidth, float maxHeight) {
		verticesW = (int)Math.ceil(maxWidth/vertexSideLength);
		verticesH = (int)Math.ceil(maxHeight/vertexSideLength);
		vertices = new GraphVertex[verticesW][verticesH];
		neighbours = new GraphVertex[verticesW*verticesH][4];
	}
	
	
	/**
	 * addVertex takes in coordinates and a tag and adds a vertex to vertices[][] darauf.
	 * Does not check to see if the vertex already exists, so overwrites are possible!
	 * @param posX The x-coordinate of the top-left corner of the vertex.
	 * @param posY The y-coordinate of the top-left corner of the vertex.
	 * @param tag The label or tag that will be given to the vertex. -1 is interpreted as inaccessible. 
	 */
	public void addVertex(int posX, int posY, int tag, PointF mapLoc) {
		vertices[posX][posY] = new GraphVertex(tag, mapLoc);
	}
	
	
	/**
	 * This does not check to see if the neighbours of vertex have already been entered, so overwrites are possible!
	 * @param vertex The vertex that is having its neighbours updated.
	 * @param newNeighbour The neighbour that is being added.
	 * @param location The location-to-be of the neighbour in the array of neighbours. 
	 */
	private void addNeighbour(int vertex, GraphVertex newNeighbour, int location) {
		neighbours[vertex][location] = newNeighbour;
	}
	
	
	/**
	 * createNeighbourList is called when all vertices have been found and entered into vertices[][]. It goes through
	 * vertices[][] and creates neighbours based on geometrically adjacent vertices.
	 */
	public void createNeighbourList(){
		int locationCounter = 0;
		
		// loop through the 2-D array vertices[][] 
		for (int w = 0; w < verticesW; w++) {
			for (int h = 0; h < verticesH; h++) {
				// First see if the vertex is valid (exists, is accessable)
				if (validVertex(vertices[w][h])) {
					// Check to see if the vertex whose neighbours are being counted is on one of the edges of the map:
					// Left edge
					if (w != 0) {
						// Only add vertex to neighbour list if it's a valid 
						if (validVertex(vertices[w-1][h])) {
							addNeighbour(vertices[w][h].tag, vertices[w-1][h], locationCounter);
							locationCounter += 1;
						}
					}
				
					// Top edge
					if (h != 0) {
						if (validVertex(vertices[w][h-1])) {
							addNeighbour(vertices[w][h].tag, vertices[w][h-1], locationCounter);
							locationCounter += 1;
						}
					}
					
					// Right edge
					if (w+1 != verticesW){
						if (validVertex(vertices[w+1][h])) {
							addNeighbour(vertices[w][h].tag, vertices[w+1][h], locationCounter);
							locationCounter += 1;
						}
					}
					
					// Bottom edge
					if (h+1 != verticesH) {
						if (validVertex(vertices[w][h+1])) {
							addNeighbour(vertices[w][h].tag, vertices[w][h+1], locationCounter);
							locationCounter += 1;
						}
					}
					locationCounter = 0;
				}
			}
		}
	}
	
	/**
	 * Returns true if the vertex is not null and not -1 (inaccessible)
	 * @param neighbour
	 * @return
	 */
	private Boolean validVertex(GraphVertex neighbour) {
		if (neighbour != null) {
			if (neighbour.tag != -1) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * Resets the values of distance (-1) and parent (null) for every vertex on the graph. 
	 */
	public void clearValues() {
		for (int w = 0; w < verticesW; w++) {
			for (int h = 0; h < verticesH; h++) {	
				vertices[w][h].distance = -1;
				vertices[w][h].parent = null;
			}
		}
	}
	
	/**
	 * Finds the index of a vertex based on its tag. 
	 * @param tag
	 * @return
	 */
	public GraphVertex findVertex(int tag) {
		for (int w = 0; w < verticesW; w++) {
			for (int h = 0; h < verticesH; h++) {
				if (vertices[w][h].tag == tag) {
					return vertices[w][h];
				}
			}
		}
		// Could not find requested vertex
		return null;
	}
}
