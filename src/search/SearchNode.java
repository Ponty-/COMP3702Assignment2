package search;

import java.util.HashMap;
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
	private GridCell cell; // The cell this node represents.
	private Cycle cycle; // The cycle being used for the search.
	private Map<Action, SearchNode> children; // Map of actions to child nodes.
	private Map<Action, Integer> actionVisits; // Times action taken.
	private int visits; // Number of times this node is visited.
	private double totReward; // Total reward from rollouts. Divide by visits.
	private boolean isGoal; // If this node represents a goal state.
	private Track track; // The track this cell is in, used to query the world.
	private double[][] distractorMatrix; // The distractorMatrix for this track.

	private final double BALANCING_FACTOR = 1.0;

	public SearchNode(GridCell cell, Cycle cycle, Track track,
			double[][] distractorMatrix) {
		this.cell = cell;
		this.cycle = cycle;
		this.track = track;
		this.distractorMatrix = distractorMatrix;
		totReward = 0;
		visits = 0;
		isGoal = track.getCellType(cell) == Track.CellType.GOAL;
		children = new HashMap<Action, SearchNode>();
		actionVisits = new HashMap<Action, Integer>();
	}

	public GridCell getCell() {
		return cell;
	}

	public Cycle getCycle() {
		return cycle;
	}

	public Track getTrack() {
		return track;
	}

	public double[][] getDistractorMatrix() {
		return distractorMatrix;
	}

	// Fill this node's children mapping with all possible actions from the
	// node.
	public void expand() {
		// Use the track to work out child nodes for actions.
		GridCell shiftedNE = cell.shifted(Direction.NE);
		// If there's not an obstacle or we're using a wild cycle.
		// Wild cycles may pass through obstacles for minor damage.
		if (!(track.getCellType(shiftedNE) == (Track.CellType.OBSTACLE))
				|| cycle.isWild()) {
			// Add it to the children.
			children.put(Action.NE, new SearchNode(shiftedNE, cycle, track,
					distractorMatrix));
		}

		GridCell shiftedSE = cell.shifted(Direction.SE);
		if (!(track.getCellType(shiftedSE) == (Track.CellType.OBSTACLE))
				|| cycle.isWild()) {
			children.put(Action.SE, new SearchNode(shiftedSE, cycle, track,
					distractorMatrix));
		}

		// Nested if for moving forwards - if obstacle in the way and not wild,
		// no action
		GridCell shiftedE = cell.shifted(Direction.E);
		if (!(track.getCellType(shiftedE) == (Track.CellType.OBSTACLE))
				|| cycle.isWild()) {
			children.put(Action.FS, new SearchNode(shiftedE, cycle, track,
					distractorMatrix));

			// If cycle is medium or fast speed try shifting again
			shiftedE = shiftedE.shifted(Direction.E);
			if (cycle.getSpeed() == Speed.MEDIUM
					|| cycle.getSpeed() == Speed.FAST) {
				if (!(track.getCellType(shiftedE) == (Track.CellType.OBSTACLE))
						|| cycle.isWild()) {
					children.put(Action.FM, new SearchNode(shiftedE, cycle,
							track, distractorMatrix));

					// If cycle is fast try shifting again
					shiftedE = shiftedE.shifted(Direction.E);
					if (cycle.getSpeed() == Speed.FAST) {
						if (!(track.getCellType(shiftedE) == (Track.CellType.OBSTACLE))
								|| cycle.isWild()) {
							children.put(Action.FF, new SearchNode(shiftedE,
									cycle, track, distractorMatrix));
						}
					}
				}
			}
		}
	}

	public void search() {
		// Track the visited nodes for backing up rollouts.
		List<SearchNode> visited = new LinkedList<SearchNode>();
		SearchNode cur = this; // Current node
		visited.add(this);
		// Search down to find a leaf node, always selecting the 'best'.
		// The 'best' is given by the exploration vs. exploitation algorithm
		// select().
		while (!cur.isLeaf()) {
			cur = cur.select();
			// System.out.println("Adding: " + cur);
			visited.add(cur);
		}

		double value;
		// Check if cur is the goal
		if (cur.isGoal) {
			// Set reward to the track prize
			value = track.getPrize();
		} else {
			// Otherwise expand the leaf node
			cur.expand();
			// Check if cur is a dead end
			if (cur.isLeaf() && !cur.isGoal) {
				// Set value to -prize as we can't win from here
				value = -track.getPrize();
			} else {
				// Select the best node
				SearchNode newNode = cur.select();
				// Add to visited and rollout from it.
				visited.add(newNode);
				// Returns the estimated through simulation value of this node.
				TrackRollout tr = new TrackRollout(newNode);
				value = TrackRollout.rollout();
			}
		}
		// back the value up the tree
		for (SearchNode node : visited) {
			node.updateStats(value);
		}
	}

	private SearchNode select() {
		Action selected = null;
		double bestValue = Double.MIN_VALUE;

		// Go over all the possible actions and pick the best one
		for (Action a : children.keySet()) {
			SearchNode c = children.get(a);
			double uctValue =
			// avg total discounted value of runs starting from here
			c.getValue() +
			/* natural log of times visited / times action taken */
			BALANCING_FACTOR
					* Math.sqrt(Math.log(c.visits) / (actionVisits.get(a)));
			// System.out.println("UCT value = " + uctValue);
			if (uctValue > bestValue) {
				selected = a;
				bestValue = uctValue;
			}
		}
		// Increment the action visits
		actionVisits.put(selected, actionVisits.get(selected) + 1);

		return children.get(selected);
	}

	public void updateStats(double value) {
		visits++;
		totReward += value;
	}

	public boolean isLeaf() {
		return isGoal || children.size() == 0;
	}

	public double getValue() {
		return totReward / visits;
	}
	
	public Action bestAction() {
		Action best = null;
		double bestUtility = Double.MIN_VALUE;
		
		//Loop over all the actions that can be performed
		for (Action a : children.keySet()) {
			//Find the action leading to the best child node
			if (children.get(a).getValue() > bestUtility) {
				best = a;
			}
		}
		
		return best;
	}
}
