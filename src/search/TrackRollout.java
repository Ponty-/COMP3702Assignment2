package search;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import problem.Action;
import problem.Cycle;
import problem.Direction;
import problem.Distractor;
import problem.GridCell;
import problem.Track;
import problem.RaceSimTools;
import problem.Track.CellType;

//k boss.

public class TrackRollout {
	// Global Fields
	// Discount factor, a decreasing amount to multiply the reward by at each
	// step.
	static private Double DISCOUNT_FACTOR = 1.0;
	// Rate at which the discount factor reduces thereby lowering the reward.
	static final private Double DISCOUNT_RATE = 1.0;

	// Reward
	// Empty cell
	static final private Double EMPTY = -0.04;
	// Distractor Reliable cell
	static final private Double DISTRACTOR_RELIABLE = -10.0;
	// Distractor Unreliable cell
	static final private Double DISTRACTOR_UNRELIABLE = -75.0;
	// Obstacle Wild cell
	static final private Double OBSTACLE_WILD = -5.0;
	// Obstacle Domesticated cell
	static final private Double OBSTACLE_DOMESTICATED = -50.0;

	// Fields
	// Random Generator for calculating results of an action.
	private Random rGen = new Random();
	// The node the rollout begins from.
	private SearchNode startNode;
	// The cell the cycle is occupying from step to step.
	private GridCell currentCell;
	// Total reward accrued so far.
	private Double totalDiscountedReward;
	// Distractor Matrix for lookup, corresponds with cell row and column,
	// contains distractor probability.
	// double arrays are automatically filled with 0.0 on creation.
	double[][] distractorMatrix;

	// Constructor
	public TrackRollout(SearchNode startNode) {
		this.startNode = startNode;
		// Initialise the summation to the starting cell's reward.
		this.totalDiscountedReward = DISCOUNT_FACTOR
				* reward(startNode.getCell());
		this.distractorMatrix = startNode.getDistractorMatrix();
	}

	// Methods
	// This method will begin the simulation run given a starting node and
	// return the simulated value for this path.
	public double rollout() {
		return 0;
	}

	private void simulator() {
		// Roll the dice to see which probability occurred. This is the
		// simulation section.
	}

	// Returns the probability that the cycle will arrive in the state sPrime in
	// the next step given the current state (currentCell) and action a.
	private double transition(GridCell sPrime, Action a) {
		// get the track
		Track t = startNode.getTrack();
		// get the cycle
		Cycle c = startNode.getCycle();	
		// Check the action we're transitioning on.
		switch (a) {
		case ST:
			if (sPrime.equals(currentCell)) {
				// Move to same cell.
				return 1.0;
			} else {
				// Move to any other cell.
				return 0.0;
			}
		case FS:
			// Get the cell to the East.
			GridCell fsShiftedE = currentCell.shifted(Direction.E);
			// Check for obstacles.
			Boolean fsObstacleE = t.getCellType(fsShiftedE) == CellType.OBSTACLE;
			// If the cell isn't an obstacle cell or the cycle is Wild.
			if (sPrime.equals(fsShiftedE)) {
				if (c.isWild() || !fsObstacleE) {
					return 1.0;
				} else {
					return 0.0;
				} 
			} else {
				return 0.0;
			}
		case FM:
			// Get the cell to the East.
			GridCell fmShiftedE = currentCell.shifted(Direction.E);
			// Get the cell two to the East.
			GridCell fmShifted2E = fmShiftedE.shifted(Direction.E);
			// Check for obstacles.
			Boolean fmObstacleE = t.getCellType(fmShiftedE) == CellType.OBSTACLE;
			Boolean fmObstacle2E = t.getCellType(fmShifted2E) == CellType.OBSTACLE;
			// If the to Eastern cells aren't an obstacle cell or the cycle is Wild.
			if (sPrime.equals(fmShifted2E)) {
				if (c.isWild() || !fmObstacleE && !fmObstacle2E) {
					return 1.0;
				} else {
					return 0.0;
				}
			} else {
				return 0.0;
			}
		case FF:
			// Get the cell to the East.
			GridCell ffShiftedE = currentCell.shifted(Direction.E);
			// Get the cell two to the East.
			GridCell ffShifted2E = ffShiftedE.shifted(Direction.E);
			// Get the cell three to the East.
			GridCell ffShifted3E = ffShifted2E.shifted(Direction.E);
			// Check for obstacles.
			Boolean ffObstacleE = t.getCellType(ffShiftedE) == CellType.OBSTACLE;
			Boolean ffObstacle2E = t.getCellType(ffShifted2E) == CellType.OBSTACLE;
			Boolean ffObstacle3E = t.getCellType(ffShifted3E) == CellType.OBSTACLE;
			// If the three Eastern cells aren't an obstacle cell or the cycle is Wild.
			if (sPrime.equals(ffShifted3E)) {
				if (c.isWild() || !ffObstacleE && !ffObstacle2E && !ffObstacle3E) {
					return 1.0;
				}
				else {
					return 0.0;
				}
			} else {
				return 0.0;
			}
		case NE:
			// Get the cell to the East.
			GridCell neShiftedE = currentCell.shifted(Direction.E);
			// Get the cell to the North.
			GridCell shiftedN = currentCell.shifted(Direction.N);
			// Get the cell to the North East.
			GridCell shiftedNE = currentCell.shifted(Direction.NE);
			// Check for obstacles.
			Boolean neObstacleE = t.getCellType(neShiftedE) == CellType.OBSTACLE;
			Boolean obstacleN = t.getCellType(shiftedN) == CellType.OBSTACLE;
			Boolean obstacleNE = t.getCellType(shiftedNE) == CellType.OBSTACLE;
			if (sPrime.equals(shiftedNE)) {
				if (c.isWild() || !obstacleNE) {
					return 0.7;
				}
			} else if (sPrime.equals(shiftedN)) {
				if (c.isWild() || !obstacleN) {
					return 0.1;
				}
			} else if (sPrime.equals(neShiftedE)) {
				if (c.isWild() || !neObstacleE) {
					return 0.1;
				}
			} else if (sPrime.equals(currentCell)) {
				return 0.1;
			} else {
				return 0.0;
			}
		case SE:
			// Get the cell to the East.
			GridCell seShiftedE = currentCell.shifted(Direction.E);
			// Get the cell to the South.
			GridCell shiftedS = currentCell.shifted(Direction.S);
			// Get the cell to the South East.
			GridCell shiftedSE = currentCell.shifted(Direction.SE);
			// Check for obstacles.
			Boolean seObstacleE = t.getCellType(seShiftedE) == CellType.OBSTACLE;
			Boolean obstacleS = t.getCellType(shiftedS) == CellType.OBSTACLE;
			Boolean obstacleSE = t.getCellType(shiftedSE) == CellType.OBSTACLE;
			if (sPrime.equals(shiftedSE)) {
				if (c.isWild() || !obstacleSE) {
					return 0.7;
				}
			} else if (sPrime.equals(shiftedS)) {
				if (c.isWild() || !obstacleS) {
					return 0.1;
				}
			} else if (sPrime.equals(seShiftedE)) {
				if (c.isWild() || !seObstacleE) {
					return 0.1;
				}
			}
		default:
			return 0.0;
		}
	}

	// Moves the cycle given a valid action and returns the cell it arrived in.
	private void step(Action a, GridCell cell) {
		// Increase the amount rewards are discounted.
		DISCOUNT_FACTOR *= DISCOUNT_RATE;
		// Add the reward to the summation.
		totalDiscountedReward += DISCOUNT_FACTOR * reward(cell);
		// Take the step
		currentCell = cell;
	}

	private Double reward(GridCell cell) {
		// Get the track to determine factors.
		Track t = startNode.getTrack();
		// Get the cycle for the same.
		Cycle c = startNode.getCycle();
		// Cell Attributes
		int cellRow = cell.getRow();
		int cellCol = cell.getCol();
		// Determine reward of a given cell, take into account distractors and
		// obstacles.
		// GOAL
		if (t.getCellType(cell) == Track.CellType.GOAL) {
			return t.getPrize();
		}
		// OBSTACLE
		if (t.getCellType(cell) == Track.CellType.OBSTACLE) {
			if (c.isWild()) {
				return OBSTACLE_WILD;
			} else {
				return OBSTACLE_DOMESTICATED;
			}
		}
		// DISTRACTOR
		// Get the probability of a distractor occurring at the cell from the
		// distractor matrix.
		double dProbability = distractorMatrix[cellRow][cellCol];
		// If the probability is non zero
		if (dProbability != 0.0) {
			// and the cycle is reliable
			if (c.isReliable()) {
				return DISTRACTOR_RELIABLE * dProbability;
			} else {
				// and the cycle is unreliable
				return DISTRACTOR_UNRELIABLE * dProbability;
			}
		}
		// Cell isn't an obstacle and has zero distractor probability, return
		// empty cell reward.
		return EMPTY;
	}
}
