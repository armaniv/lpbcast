package lpbcast;

import java.awt.Color;

import lpbcast.Node;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class NodeStyle2D extends DefaultStyleOGL2D {
	
	@Override
	public Color getColor(Object o) {
		if (!(o instanceof Node)) {
			return Color.YELLOW; 
		}
		Node node = (Node)o;
		if (node.isCrashed())	{
			return Color.RED;
		}
		else
			return Color.GREEN;
	}
	
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    return shapeFactory.createCircle(5, 5);
	 }
}
