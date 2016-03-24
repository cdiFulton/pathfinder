/*
 * Much credit here goes to Kirill Morozov. Most of the SVG parsing 
 * code is derived from code in his Mapper package.
 */

package ca.uwaterloo.pathfinder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.PointF;
import android.widget.TextView;
import ca.uwaterloo.pathfinder.NavigableGraph;
import ca.uwaterloo.pathfinder.GraphVertex;

/**
 * This is the first class to be instantiated in MainActivity and serves as a helper class 
 * to create and initialize the graph.
 * @author chris
 *
 */
public class SVGToGraph {
	// SVG is XML-based, so we use DocBuilder to do the initial parsing
	private static DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder DocBuilder = null;
	private Document doc;
	
	public NavigableGraph navGraph;
	private static float vertexSideLength;
	
	// only for testing
	TextView testView;
	public StringBuilder str = new StringBuilder();
	//////////////////
	
	public SVGToGraph(TextView outputView){
		// only exists for testing
		testView = outputView;
	}
	
	
	/**
	 * 
	 * @param dir The directory the SVG file is in.
	 * @param svgname The file name of the SVG, extension included.
	 * @return NavigableGraph, the graph that stores the information of the SVG and upon which 
	 * the pathfinding algorithm runs.
	 */
	public NavigableGraph createGraph(File dir, final String svgname) {
		// Initializes the DocBuilder, uses that to parse the SVG into a Document
		if(DocBuilder == null){
			try {
				DocBuilder = docBuildFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		File map = dir.listFiles(new FilenameFilter(){
		
			public boolean accept(File dir, String name) {
				return name.equals(svgname);
			}
		})[0];
		
		try {
			doc = DocBuilder.parse(map);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Element svg = (Element) doc.getElementsByTagName("svg").item(0);
		float navGraphWidth = Float.parseFloat(svg.getAttribute("width"));
		float navGraphHeight = Float.parseFloat(svg.getAttribute("height"));
		navGraph = new NavigableGraph(navGraphWidth, navGraphHeight);
		
		vertexSideLength = navGraph.vertexSideLength;
		
		NodeList filePaths = doc.getElementsByTagName("path");
		for(int i = 0; i < filePaths.getLength(); i++){
			parseSVG(filePaths.item(i), navGraph);
		}
		
		// Populate the graph
		int tagCounter = 0;
		for (int j = 0; j < navGraphHeight/vertexSideLength; j++) {
			for (int i = 0; i < navGraphWidth/vertexSideLength; i++) {	
				if (navGraph.vertices[i][j] == null){
					PointF mapLoc = toPoint(i, j);
					navGraph.addVertex(i, j, tagCounter, mapLoc);
					tagCounter++;
				}
			}
		}
		
		
		return navGraph;
	}
	
	
	
	public void parseSVG (Node node, NavigableGraph graph) {
		Element elem = (Element) node;
		
		String d = elem.getAttribute("d"); 
		
		String[] pathString = d.split("[ ,]");
		PointF startPoint = new PointF();
		PointF refPoint = new PointF();
		char defaultCommand = 'l';
		
		try {
			for (int i = 0; i < pathString.length; i++){
				PointF newPoint;
				
				if("cCsSqQtTaAmMlLzZ-1234567890".indexOf(pathString[i].charAt(0)) == -1){
					throw new InvalidParameterException("A character that was to be interpretated as a command charcater" +
							"is not known by the Map loader. Check your path Data. The unknown chatacter was: <" + pathString[i].charAt(0) +">" +
							"In the path {" + d + "}");
				}
				
				// All of these characters are used to draw Bezier curves, which I don't handle, so basically skip them
				switch (pathString[i].charAt(0)){
				case 'c':
					i += 4;
					pathString[i] = "l";
					break;
				case 'C':
					pathString[i] = "L";
					i+= 4;
					break;
				case 's':
					i += 2;
					pathString[i] = "l";
					break;
				case 'S':
					i += 2;
					pathString[i] = "L";
					break;
				case 'q':
					i += 2;
					pathString[i] = "l";
					break;
				case 'Q':
					i += 2;
					pathString[i] = "L";
					break;
				case 't':
					pathString[i] = "l";
					break;
				case 'T':
					pathString[i] = "L";
					break;
				case 'a':
					i += 5;
					pathString[i] = "l";
					break;
				case 'A':
					i += 5;
					pathString[i] = "L";
					break;
				}
			
			
				// Read control character
				switch (pathString[i].charAt(0)){
				case 'M':
					newPoint = makePoint(pathString[i+1], pathString[i+2]);
					// If this is the first point in a path, just initialize startPoint and refPoint
					if (startPoint.x == 0.0 && startPoint.y == 0.0) {
						startPoint.x = newPoint.x;
						startPoint.y = newPoint.y;
						refPoint.x = newPoint.x;
						refPoint.y = newPoint.y;
					}
					// Otherwise just move refPoint
					else {
						refPoint.x = newPoint.x;
						refPoint.y = newPoint.y;
					}
					i += 2;
					defaultCommand = 'L';
					break;
				case 'm':
					newPoint = makePointRelative(refPoint, pathString[i+1], pathString[i+2]);
					if (startPoint.x == 0.0 && startPoint.y == 0.0) {
						startPoint.x = newPoint.x;
						startPoint.y = newPoint.y;
						refPoint.x = newPoint.x;
						refPoint.y = newPoint.y;
					}
					else {
						refPoint.x = newPoint.x;
						refPoint.y = newPoint.y;
					}
					i += 2;
					defaultCommand = 'l';
					break;
				case 'l':
					newPoint = makePointRelative(refPoint, pathString[i+1], pathString[i+2]);
					traceLine(refPoint, findVector(refPoint, newPoint), graph);
					refPoint = newPoint;
					i += 2;
					defaultCommand = 'l';
					break;
				case 'L':
					newPoint = makePoint(pathString[i+1], pathString[i+2]);
					traceLine(refPoint, findVector(refPoint, newPoint), graph);
					refPoint = newPoint;
					i += 2;
					defaultCommand = 'L';
					break;
				case 'z':
				case 'Z':
					newPoint = startPoint;
					traceLine(refPoint, findVector(refPoint, newPoint), graph);
					refPoint = newPoint;
					break;
					// If there is no control character, we use the last one.
				default:
					switch(defaultCommand){
					case 'l':
					default:
						newPoint = makePointRelative(refPoint, pathString[i], pathString[i+1]);
						traceLine(refPoint, findVector(refPoint, newPoint), graph);
						refPoint = newPoint;
						i += 1;
						break;
					case 'L':
						newPoint = makePoint(pathString[i], pathString[i+1]);
						traceLine(refPoint, findVector(refPoint, newPoint), graph);
						refPoint = newPoint;
						i += 1;
						break;
					}	
				}						
			}
		}catch (IndexOutOfBoundsException e){
			throw new IndexOutOfBoundsException("There were not enough elements to process all the commands. "+
					"Either the path contains an unknown command or one of the commands has too few parameters" +
					"The path being processed was {" + d +"}");
		}catch (NumberFormatException e){
			throw new NumberFormatException("The map loader encountered a problem parsing path data. This likely means that you have an " +
					"unknown control character. Check your paths" +
					"The path being processed was {" + d +"}");
		}
	}
	
	
	
	/**
	 * Traces the line of a path and marks all potential vertices along the line as inaccessible. 
	 * @param location The starting point for the line to be traced.
	 * @param vector The distance and direction the line travels from location.
	 * @param NavigableGraph The graph that will be modified. 
	 */
	public void traceLine(PointF location, PointF vector, NavigableGraph graph) {
		float locx = location.x;
		float locy = location.y;
		float distx = vector.x;
		float disty = vector.y;
		float distLength = (float) Math.sqrt(Math.pow(distx, 2) + Math.pow(disty, 2));
		// The unit vector length is made proportional to the vertex side length
		float unitx = (vertexSideLength/2)*distx/distLength;
		float unity = (vertexSideLength/2)*disty/distLength;
		
		for (int i = 0; i < Math.ceil(distLength/(vertexSideLength/2)); i++) {
			int[] wallIndex = toIndex(locx + unitx*i, locy + unity*i);
			PointF non = null;
			graph.addVertex(wallIndex[0], wallIndex[1], -1, non);
		}
	}
	
	/**
	 * Takes in a pair of floats corresponding to a map position and converts it into its 
	 * graph index
	 * @param xpos
	 * @param ypos
	 * @return
	 */
	public static int[] toIndex(float xpos, float ypos) {
		int[] index = new int[2];
		index[0] = (int)(xpos/vertexSideLength);
		index[1] = (int)(ypos/vertexSideLength);
		return index;
	}
	
	public static int[] pointfToIndex(PointF pointf) {
		int[] index = new int[2];
		index[0] = (int)(pointf.x/vertexSideLength);
		index[1] = (int)(pointf.y/vertexSideLength);
		return index;
	}
	
	/**
	 * Takes in a pair of ints corresponding to a graph index and converts it into its 
	 * map position
	 * @param xIndex
	 * @param yIndex
	 * @return
	 */
	private PointF toPoint(int xIndex, int yIndex) {
		PointF location = new PointF(
				(float) xIndex*vertexSideLength,
				(float) yIndex*vertexSideLength);
		return location;
	}
	
	/**
	 * Finds a relative vector based on an absolute heading.
	 * @param start
	 * @param end
	 * @return PointF
	 */
	public PointF findVector(PointF start, PointF end) {
		PointF newVector = new PointF((end.x - start.x), (end.y - start.y));
		return newVector;
	}
	
	
	public PointF makePoint(String x, String y) {
		return new PointF(Float.parseFloat(x),
				Float.parseFloat(y));
	}
	
	private PointF makePointRelative(PointF p, String s1, String s2)
	{
		return new PointF(
				p.x + Float.parseFloat(s1),
				p.y + Float.parseFloat(s2)
				);
	}
	
	
	/**
	 * Returns the graph that was just created.
	 * @return NavigableGraph
	 */
	public NavigableGraph getLastGraph() {
		return navGraph;
	}
	
	
	
	
	// only exists for testing
	public void printTest(ArrayList<PointF> input){
		if (input.size() == 0){
			str.append("No path found");
		}
		else {
			for (int i = 0; i < input.size(); i++) {
				str.append(input.get(i));
				str.append(", ");
			}
		}
		testView.setText(str);
	}
	
	// testing etc you know the drill
	public void print2D(GraphVertex[][] array) {
		/*
		graphy.addVertex(0, 4, 0);
		graphy.addVertex(2, 3, 1);
		graphy.addVertex(2, 4, 2); 
		graphy.addVertex(0, 0, 3); 
		graphy.addVertex(0, 1, 4); 
		graphy.addVertex(0, 2, 5); 
		graphy.addVertex(0, 3, 6); 
		graphy.addVertex(1, 3, 7);
		graphy.addVertex(1, 2, 8); 
		graphy.addVertex(1, 4, 9); 
		graphy.addVertex(3, 4, 10); 
		graphy.addVertex(3, 0, 11); 
		*/
		
		navGraph.createNeighbourList();
		
		
		int w = array.length;
		int h = array[0].length;
		
		str.append("\n");
		str.append(w);
		str.append("*");
		str.append(h);
		str.append("\n");
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				if (array[i][j] != null){
					str.append(array[i][j].tag);
				}
				else {
					str.append(array[i][j]);
				}
			}
			str.append("\n");
		}
		
		
		
		testView.setText(str);
		
	}
}
