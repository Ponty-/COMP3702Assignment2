package search;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import problem.Cycle;
import problem.Cycle.Speed;
import problem.Direction;
import problem.GridCell;
import problem.Action;
import problem.Track;

public class SearchNode {
	private GridCell cell; // The cell this node represents
	private Cycle cycle; // The cycle being used for the search
	private Map<Action, SearchNode> children; // Map of actions to child nodes
	private int visits; // Number of times this node is visited
	private double totReward; // Total reward from rollouts. Divide by visits
	private boolean isGoal; // If this node represents a goal state
	private Track track; // The track this cell is in, used to query the world

	public SearchNode(GridCell cell, Cycle cycle, Track track) {
		this.cell = cell;
		this.cycle = cycle;
		this.track = track;
		totReward = 0;
		visits = 0;
		isGoal = track.getCellType(cell) == Track.CellType.GOAL;

		// Use the track to work out child nodes for actions
		GridCell shiftedNE = cell.shifted(Direction.NE);
		if (!(track.getCellType(shiftedNE) == (Track.CellType.OBSTACLE))) {
			children.put(Action.NE, new SearchNode(shiftedNE, cycle, track));
		}

		GridCell shiftedSE = cell.shifted(Direction.SE);
		if (!(track.getCellType(shiftedSE) == (Track.CellType.OBSTACLE))) {
			children.put(Action.SE, new SearchNode(shiftedSE, cycle, track));
		}

		// Nested if for moving forwards - if obstacle in the way, no action
		GridCell shiftedE = cell.shifted(Direction.E);
		if (!(track.getCellType(shiftedE) == (Track.CellType.OBSTACLE))) {
			children.put(Action.FS, new SearchNode(shiftedE, cycle, track));

			// If cycle is medium or fast speed try shifting again
			shiftedE = shiftedE.shifted(Direction.E);
			if (!(track.getCellType(shiftedE) == (Track.CellType.OBSTACLE))
					&& (cycle.getSpeed() == Speed.MEDIUM || cycle.getSpeed() == Speed.FAST)) {
				children.put(Action.FM, new SearchNode(shiftedE, cycle, track));

				// If cycle is fast try shifting again
				shiftedE = shiftedE.shifted(Direction.E);
				if (!(track.getCellType(shiftedE) == (Track.CellType.OBSTACLE))
						&& (cycle.getSpeed() == Speed.MEDIUM || cycle
								.getSpeed() == Speed.FAST)) {
					children.put(Action.FF, new SearchNode(shiftedE, cycle,
							track));
				}
			}
		}
	}

	public void selectAction() {
		List<SearchNode> visited = new LinkedList<SearchNode>();
		SearchNode cur = this;
		visited.add(this);
		while (!cur.isLeaf()) {
			cur = cur.select();
			// System.out.println("Adding: " + cur);
			visited.add(cur);
		}

		SearchNode newNode = cur.select();
		visited.add(newNode);
		double value = rollOut(newNode);
		for (SearchNode node : visited) {
			// would need extra logic for n-player game
			// System.out.println(node);
			node.updateStats(value);
		}
	}

	private SearchNode select() {
		SearchNode selected = null;
		double bestValue = Double.MIN_VALUE;
		for (SearchNode c : children.values()) {
			double uctValue = c.totReward
					/ (c.visits)
					+ // avg total discounted value of runs starting from here
					/* natural log of times visited / times action taken */Math
							.sqrt(Math.log(visits + 1) / (c.visits));
			// small random number to break ties randomly in unexpanded nodes
			// System.out.println("UCT value = " + uctValue);
			if (uctValue > bestValue) {
				selected = c;
				bestValue = uctValue;
			}
		}
		// System.out.println("Returning: " + selected);
		return selected;
	}
	
	public void updateStats(double value) {
        visits++;
        totReward += value;
    }

	public boolean isLeaf() {
		return isGoal || children.size() == 0;
	}
}
