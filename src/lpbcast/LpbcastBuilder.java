package lpbcast;

import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class LpbcastBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		
		// --- Lightweight Probabilistic Broadcast (P. Th. Eugster et al)
		
		context.setId("lpbcast");

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		// get the value for the parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		//int grid_size = params.getInteger("grid_size");
		//int node_count = params.getInteger("node_count");

		int grid_size = 20;
		int node_count = 200;
		
		// create a grid
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), false, grid_size, grid_size));
		
		
		Network network = new Network();
		HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
		
		// populate the grid with at most one Node per cell
		for (int i = 0; i < node_count; i++) {
			Node node = new Node(grid, i, network);
			nodes.put(i, node);
			context.add(node);
			int x = RandomHelper.nextIntFromTo(0, grid_size - 1);
			int y = RandomHelper.nextIntFromTo(0, grid_size - 1);
			while (!grid.moveTo(node, x, y)) {
				x = RandomHelper.nextIntFromTo(0, grid_size - 1);
				y = RandomHelper.nextIntFromTo(0, grid_size - 1);
			}
		}
		
		network.setNodes(nodes);

		return context;
	}

}
