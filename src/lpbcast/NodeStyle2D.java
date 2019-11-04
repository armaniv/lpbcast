package lpbcast;

import java.awt.Color;

import lpbcast.Node;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class NodeStyle2D extends DefaultStyleOGL2D {
	
	@Override
	public Color getColor(Object o) {
		if (!(o instanceof Node)) {
			return Color.BLACK; 
		}
		Node node = (Node)o;
		if (node.getEventIdsSize()>0)	{
			return Color.GREEN;
		}
		else
			return Color.BLACK;
	}
	
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    return shapeFactory.createCircle(5, 5);
	 }
}
