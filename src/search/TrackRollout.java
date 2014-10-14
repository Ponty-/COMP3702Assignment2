package search;

import java.util.ArrayList;
import java.util.Random;

import problem.Action;
import problem.Cycle;
import problem.Cycle.Speed;
import problem.Direction;
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
	// The multiplier on the cycles maximum range per step to look ahead for
	// obstacles, distractors.
	static final private int LOOK_FACTOR = 3;

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
	// The cell the cycle is occupying from step to step.
	private GridCell currentCell;
	// Total reward accrued so far.
	private Double totalDiscountedReward;
	// Distractor Matrix for lookup, corresponds with cell row and column,
	// contains distractor probability.
	// double arrays are automatically filled with 0.0 on creation.
	double[][] distractorMatrix;
	// Track
	Track t;
	// Cycle
	Cycle c;
	// CycleType
	CycleType cType;

	// Constructor
	public TrackRollout(SearchNode startNode) {
		// Get the track.
		t = startNode.getTrack();
		// Get the cycle.
		c = startNode.getCycle();
		// Set the cycle type.
		if (c.isWild() && c.isReliable()) {
			cType = CycleType.WILD_RELIABLE;
		} else if (c.isWild()) {
			cType = CycleType.WILD;
		} else if (c.isReliable()) {
			cType = CycleType.RELIABLE;
		}
		// Initialise the summation to the starting cell's reward.
		this.totalDiscountedReward = DISCOUNT_FACTOR
				* reward(startNode.getCell());
		this.distractorMatrix = startNode.getDistractorMatrix();
	}

	// Methods
	// This method will begin the simulation run given a starting node and
	// return the simulated value for this path.
	public double rollout() {
		for (int n = 0; n < t.getNumCols() * 2; n++) {
			// Random generator.
			Random rgen = new Random();
			// get the cycle's speed.
			Speed cSpeed = c.getSpeed();
			// Initialise
			int lookRange;
			// Set the look range to lookFactor * max range of the cycle per
			// step.
			switch (cSpeed) {
			case SLOW:
				lookRange = LOOK_FACTOR * 1;
			case MEDIUM:
				lookRange = LOOK_FACTOR * 2;
			case FAST:
				lookRange = LOOK_FACTOR * 3;
			default:
				lookRange = LOOK_FACTOR * 1;
			}
			// Perform the policy based on the type of the cycle.
			// Create a list to store the cells in front of the cycle.
			ArrayList<GridCell> aheadList = new ArrayList<GridCell>();
			switch (cType) {
			// Ignore Obstacles, avoid Distractors
			case WILD:
				GridCell wCell = currentCell;
				for (int i = 0; i < lookRange; i++) {
					aheadList.add(wCell);
					// Always looking ahead.
					wCell = wCell.shifted(Direction.E);
				}
				Boolean wEvasiveAction = false;
				for (int i = 0; i < aheadList.size(); i++) {
					GridCell aheadCell = aheadList.get(i);
					int aheadCellRow = aheadCell.getRow();
					int aheadCellCol = aheadCell.getCol();
					if (distractorMatrix[aheadCellRow][aheadCellCol] > 0.0) {
						wEvasiveAction = true;
					}
				}
				if (wEvasiveAction) {
					// Obstacle or distractor ahead, evasive action.
					Double randomValue = rgen.nextDouble();
					if (randomValue <= 0.5) {
						// Step NE.
						step(Action.NE);
					} else {
						// Step SE.
						step(Action.SE);
					}
				} else {
					switch (cSpeed) {
					case SLOW:
						step(Action.FS);
					case MEDIUM:
						step(Action.FM);
					case FAST:
						step(Action.FF);
					default:
						step(Action.FS);
					}
				}
				// Ignore Distractors, avoid Obstacles
			case RELIABLE:
				GridCell rCell = currentCell;
				for (int i = 0; i < lookRange; i++) {
					aheadList.add(rCell);
					// Always looking ahead.
					rCell = rCell.shifted(Direction.E);
				}
				Boolean rEvasiveAction = false;
				for (int i = 0; i < aheadList.size(); i++) {
					GridCell aheadCell = aheadList.get(i);
					if (t.getCellType(aheadCell) == CellType.OBSTACLE) {
						rEvasiveAction = true;
					}
				}
				if (rEvasiveAction) {
					// Obstacle or distractor ahead, evasive action.
					Double randomValue = rgen.nextDouble();
					if (randomValue <= 0.5) {
						// Step NE.
						step(Action.NE);
					} else {
						// Step SE.
						step(Action.SE);
					}
				} else {
					switch (cSpeed) {
					case SLOW:
						step(Action.FS);
					case MEDIUM:
						step(Action.FM);
					case FAST:
						step(Action.FF);
					default:
						step(Action.FS);
					}
				}
				// Ignore Obstacles and Distractors
			case WILD_RELIABLE:
				switch (cSpeed) {
				case SLOW:
					step(Action.FS);
				case MEDIUM:
					step(Action.FM);
				case FAST:
					step(Action.FF);
				default:
					step(Action.FS);
				}
				// Avoid Obstacles and Distractors
			default:
				GridCell cell = currentCell;
				for (int i = 0; i < lookRange; i++) {
					aheadList.add(cell);
					// Always looking ahead.
					cell = cell.shifted(Direction.E);
				}
				Boolean evasiveAction = false;
				for (int i = 0; i < aheadList.size(); i++) {
					GridCell aheadCell = aheadList.get(i);
					int aheadCellRow = aheadCell.getRow();
					int aheadCellCol = aheadCell.getCol();
					if (t.getCellType(aheadCell) == CellType.OBSTACLE
							|| distractorMatrix[aheadCellRow][aheadCellCol] > 0.0) {
						evasiveAction = true;
					}
				}
				if (evasiveAction) {
					// Obstacle or distractor ahead, evasive action.
					Double randomValue = rgen.nextDouble();
					if (randomValue <= 0.5) {
						// Step NE.
						step(Action.NE);
					} else {
						// Step SE.
						step(Action.SE);
					}
				} else {
					switch (cSpeed) {
					case SLOW:
						step(Action.FS);
					case MEDIUM:
						step(Action.FM);
					case FAST:
						step(Action.FF);
					default:
						step(Action.FS);
					}
				}

			}
		}
		System.out.println(totalDiscountedReward);
		return totalDiscountedReward;
	}

	// Returns the probability that the cycle will arrive in the state sPrime in
	// the next step given the current state (currentCell) and action a.
	private double transition(GridCell sPrime, Action a) {
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
			// If the to Eastern cells aren't an obstacle cell or the cycle is
			// Wild.
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
			// If the three Eastern cells aren't an obstacle cell or the cycle
			// is Wild.
			if (sPrime.equals(ffShifted3E)) {
				if (c.isWild() || !ffObstacleE && !ffObstacle2E
						&& !ffObstacle3E) {
					return 1.0;
				} else {
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

	// If given a valid action that would move the cycle within the bounds of
	// the track moves the cycle and returns true else returns false.
	private boolean step(Action a) {
		Random rgen = new Random();
		double randomValue = rgen.nextDouble();
		Boolean validAction;
		GridCell cell = currentCell;
		switch (a) {
		case ST:
			validAction = RaceSimTools.withinBounds(currentCell, t);
			// No movement necessary.
		case FS:
			validAction = RaceSimTools.withinBounds(
					currentCell.shifted(Direction.E), t);
			// Set new cell East.
			cell = currentCell.shifted(Direction.E);
		case FM:
			validAction = RaceSimTools.withinBounds(
					(currentCell.shifted(Direction.E)).shifted(Direction.E), t);
			// Set new cell two East.
			cell = (currentCell.shifted(Direction.E)).shifted(Direction.E);
		case FF:
			validAction = RaceSimTools.withinBounds(((currentCell
					.shifted(Direction.E)).shifted(Direction.E))
					.shifted(Direction.E), t);
			cell = ((currentCell.shifted(Direction.E)).shifted(Direction.E))
					.shifted(Direction.E);
		case NE:
			validAction = RaceSimTools.withinBounds(
					(currentCell.shifted(Direction.NE)), t);
			// NE action has chance for number of probabilities to occur.
			double neProbabilityN = transition(
					currentCell.shifted(Direction.N), Action.NE);
			double neProbabilityE = transition(
					currentCell.shifted(Direction.E), Action.NE);
			double neProbabilityST = transition(currentCell, Action.NE);
			double neProbabilityNE = transition(
					currentCell.shifted(Direction.NE), Action.NE);
			if (randomValue >= 0.0 && randomValue <= neProbabilityN) {
				cell = currentCell.shifted(Direction.N);
			} else if (randomValue > neProbabilityN
					&& randomValue <= (neProbabilityN + neProbabilityE)) {
				cell = currentCell.shifted(Direction.E);
			} else if (randomValue > (neProbabilityN + neProbabilityE)
					&& randomValue <= (neProbabilityN + neProbabilityE + neProbabilityST)) {
				cell = currentCell;
			} else if (randomValue > (neProbabilityN + neProbabilityE + neProbabilityST)
					&& randomValue <= (neProbabilityN + neProbabilityE
							+ neProbabilityST + neProbabilityNE)) {
				cell = currentCell.shifted(Direction.NE);
			}
		case SE:
			validAction = RaceSimTools.withinBounds(
					(currentCell.shifted(Direction.SE)), t);
			// NE action has chance for number of probabilities to occur.
			double seProbabilityS = transition(
					currentCell.shifted(Direction.S), Action.SE);
			double seProbabilityE = transition(
					currentCell.shifted(Direction.E), Action.SE);
			double seProbabilityST = transition(currentCell, Action.SE);
			double seProbabilitySE = transition(
					currentCell.shifted(Direction.SE), Action.SE);
			if (randomValue >= 0.0 && randomValue <= seProbabilityS) {
				cell = currentCell.shifted(Direction.N);
			} else if (randomValue > seProbabilityS
					&& randomValue <= (seProbabilityS + seProbabilityE)) {
				cell = currentCell.shifted(Direction.E);
			} else if (randomValue > (seProbabilityS + seProbabilityE)
					&& randomValue <= (seProbabilityS + seProbabilityE + seProbabilityST)) {
				cell = currentCell;
			} else if (randomValue > (seProbabilityS + seProbabilityE + seProbabilityST)
					&& randomValue <= (seProbabilityS + seProbabilityE
							+ seProbabilityST + seProbabilitySE)) {
				cell = currentCell.shifted(Direction.NE);
			}
		case TO:
			// How about no.
			validAction = false;
		case TC:
			// How about noooooo.
			validAction = false;
		default:
			validAction = false;
		}
		if (validAction) {
			// Increase the amount rewards are discounted.
			DISCOUNT_FACTOR *= DISCOUNT_RATE;
			// Add the reward to the summation.
			totalDiscountedReward += DISCOUNT_FACTOR * reward(cell);
			// Take the step
			if (!currentCell.equals(cell)) {
				currentCell = cell;
			}
			return true;
		}
		return false;
	}

	private Double reward(GridCell cell) {
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
