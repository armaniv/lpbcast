package lpbcast;

import java.awt.Color;

import lpbcast.Node;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class NodeStyle2D extends DefaultStyleOGL2D {
	
	@Override
	public Color getColor(Object o) {
		Node node = (Node)o;
		
		if (node.getNodeState() == NodeState.CRASHED) {
			return Color.RED;
		}
		else if (node.getNodeState() == NodeState.UNSUB) {
			return Color.MAGENTA; // its like a purple
		}
		else if (node.getNewEventThisRound()) {
			return Color.GREEN;
		}
		else
			return Color.BLACK;
	}
	
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    return shapeFactory.createCircle(5, 5);
	 }
}
