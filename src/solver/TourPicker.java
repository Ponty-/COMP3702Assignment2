package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import problem.Distractor;
import problem.GridCell;
import problem.Player;
import problem.Track;
import problem.Cycle;
import problem.Tour;
import problem.Track.CellType;
import search.SearchNode;
import solver.Consultant;

public class TourPicker {
	private static long SINGLE_SECOND = 1000000000;

	public static List<EvaluatedTrack> choose(Tour tour) {
		// Return the decision as as a map of tracks and cycles to use on them
		List<EvaluatedTrack> output = new ArrayList<EvaluatedTrack>(3);
		Map<Track, double[]> weightedTracks = rankTracks(tour);
		List<EvaluatedTrack> trackResults = new ArrayList<EvaluatedTrack>();
		List<Cycle> cycleList = tour.getPurchasableCycles();
		Set<Track> trackSet = weightedTracks.keySet();
		Iterator iterT = trackSet.iterator();
		Track currentTrack;
		Cycle currentCycle;
		long timeFactor = findFactor(trackSet, cycleList);
		// iterates over tracks and checks if any of the cycles are too
		// expensive to make a profit on the track
		// then iterates over positions in the track using MTCS to try and find
		// the best cycle
		while (iterT.hasNext()) {
			currentTrack = (Track) iterT.next();
			Iterator iterC = cycleList.iterator();

			ArrayList<Player> players = new ArrayList<Player>();
			Map<String, GridCell> startingPositions = currentTrack
					.getStartingPositions();
			double[][] distractorMatrix = Consultant
					.buildDistractorMatrix(currentTrack);

			// iterate over cycles
			Cycle bestCycle = null;
			SearchNode bestNode = null;

			while (iterC.hasNext()) {
				// if the track has a certain proportion or higher of obstacles
				// or distractors then discount certain cycles
				currentCycle = (Cycle) iterC.next();
				if (weightedTracks.get(currentTrack)[1] >= 0.2) {
					if (!currentCycle.isWild()) {
						continue;
					}
				}
				if (weightedTracks.get(currentTrack)[2] >= 0.2) {
					if (!currentCycle.isReliable()) {
						continue;
					}
				}
				// only continue if the cycle could make money on the track
				if (weightedTracks.get(currentTrack)[0]
						- currentCycle.getPrice() > 0) {
					// check the cycle from each potential starting position
					for (GridCell pos : startingPositions.values()) {
						SearchNode node = new SearchNode(pos, currentCycle,
								currentTrack,
								Consultant.buildDistractorMatrix(currentTrack));

						// Search
						node.loopSearch(timeFactor);

						// Compare to previous best cycle
						if (bestNode == null) {
							bestNode = node;
							bestCycle = currentCycle;
						} else if (node.getValue() - currentCycle.getPrice() > bestNode
								.getValue()) {
							bestNode = node;
							bestCycle = currentCycle;
						}
					}

				}
			}
			// store tracks and their best cycle and expected winnings with that
			// cycle
			trackResults.add(new EvaluatedTrack(currentTrack, bestCycle,
					bestNode.getValue(), bestNode.getCell()));
		}
		List<Track> trackCombo = new ArrayList<Track>();
		double maxPrize = 0;
		// check all combinations of tracks (1,2,3)
		for (EvaluatedTrack t : trackResults) {
			// if this track alone gives more than the current best set it to
			// best
			if (trackCombo.size() == 0) {
				trackCombo.add(t.getTrack());
			}
			if (t.getPrize() > maxPrize) {
				trackCombo.clear();
				trackCombo.add(t.getTrack());
				maxPrize = t.getPrize();
			}
			for (EvaluatedTrack x : trackResults) {
				// if this track is different to the previous and gives more
				// when added set it and previous as best
				// check for repeat cycle usage
				if (x.equals(t)) {
					continue;
				}
				if (t.getPrize() + x.getPrize() > maxPrize) {
					trackCombo.clear();
					trackCombo.add(t.getTrack());
					trackCombo.add(x.getTrack());
					if (x.getCycle().equals(t.getCycle())) {
						maxPrize = t.getPrize() + x.getPrize()
								+ x.getCycle().getPrice();
					} else {
						maxPrize = t.getPrize() + x.getPrize();
					}

				}
				for (EvaluatedTrack y : trackResults) {
					// if this track is different to the previous ones and gives
					// more when added with the previous set them as best
					// check for repeat cycle usage
					if (y.equals(x)) {
						continue;
					}
					if (y.equals(t)) {
						continue;
					}
					if (t.getPrize() + x.getPrize() + y.getPrize() > maxPrize) {
						trackCombo.clear();
						trackCombo.add(t.getTrack());
						trackCombo.add(x.getTrack());
						trackCombo.add(y.getTrack());
						if (x.getCycle().equals(y.getCycle())) {
							maxPrize = t.getPrize() + x.getPrize()
									+ y.getPrize() + y.getCycle().getPrice();
						} else if (t.getCycle().equals(y.getCycle())) {
							maxPrize = t.getPrize() + x.getPrize()
									+ y.getPrize() + y.getCycle().getPrice();
						} else {
							maxPrize = t.getPrize() + x.getPrize()
									+ y.getPrize();
						}

					}
				}
			}
		}
		// goes through the evaluated track list and selects the track and cycle
		// from it that corresponds to the selected track
		for (EvaluatedTrack z : trackResults) {
			for (Track t : trackCombo) {
				if (z.getTrack().equals(t)) {
					output.add(z);
				}
			}

		}

		return output;
	}

	// Methods
	/* ranks tracks based upon expected winnings */
	public static Map<Track, double[]> rankTracks(Tour tour) {
		// Map of tracks and their attributes (weighted prize, obstacle heavy,
		// distractor heavy, opponent heavy)
		Map<Track, double[]> trackInfo = new HashMap<Track, double[]>();
		List<Track> tracks = tour.getTracks();
		Track currentTrack;
		int noTracks = tracks.size();
		double prize = 0;

		// iterate over the tracks and find their attributes and weighted prize
		for (int i = 0; i < noTracks; i++) {
			currentTrack = tracks.get(i);
			double[] attributes = { 0, 0, 0, 0 };
			prize = currentTrack.getPrize();

			// reduce expected prize based on obstacles
			// obstacles/cells
			double obsWeight = percentObs(currentTrack);
			prize = prize * (1 - obsWeight);
			attributes[1] = obsWeight;

			// reduce expected prize based on distractors
			// distractors/cells
			double distractorWeight = (currentTrack.getDistractors().size() / (currentTrack
					.getNumCols() * currentTrack.getNumRows()));
			prize = prize * (1 - distractorWeight);
			prize = prize - currentTrack.getRegistrationFee();
			attributes[2] = distractorWeight;

			attributes[0] = prize;
			if (prize > 0.0) {
				trackInfo.put(currentTrack, attributes);
			}

		}

		return trackInfo;
	}

	/*
	 * iterates over each cell of the track and finds what the percentage is of
	 * obstacles to cells
	 */
	public static double percentObs(Track track) {
		double output = 0.0;
		int obsCount = 0;
		double cellCount = track.getNumRows() * track.getNumCols();
		for (int j = 0; j < track.getNumRows(); j++) {
			for (int k = 0; k < track.getNumCols(); k++) {
				GridCell cell = new GridCell(j, k);
				if (track.getCellType(cell).equals(CellType.OBSTACLE)) {
					obsCount++;
				}
			}
		}
		output = obsCount / cellCount;
		return output;
	}

	// finds how long we can allocate to do a MTCS on each start position
	public static long findFactor(Set<Track> tracks, List<Cycle> cycles) {
		Iterator iterT = tracks.iterator();
		long numCycles = cycles.size();
		long startPosCount = 0;
		long jobs = 0;
		long output = 0;
		while (iterT.hasNext()) {
			Track currentTrack = (Track) iterT.next();
			startPosCount += currentTrack.getStartingPositions().size();
		}
		// 4 minutes
		long timeLimit = 4 * 60 * SINGLE_SECOND;
		jobs = startPosCount * numCycles;
		output = timeLimit / jobs;

		return output;
	}
}
