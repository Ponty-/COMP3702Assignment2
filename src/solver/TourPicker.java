package solver;

import java.io.FileWriter;
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
		Cycle currentCycle = null;
		long timeFactor = findFactor(trackSet, cycleList);
		// iterates over tracks and checks if any of the cycles are too
		// expensive to make a profit on the track
		// then iterates over positions in the track using MTCS to try and find
		// the best cycle
		Iterator iterJ = trackSet.iterator();
		long numCycles = cycleList.size();
		long startPosCount = 0;
		long jobs = 0;
		//finds how big the array of track cycle simulations needs to be
		while (iterJ.hasNext()) {
			Track jTrack = (Track) iterJ.next();
			startPosCount += jTrack.getStartingPositions().size();
		}
		jobs = startPosCount * numCycles;
		EvaluatedTrack[] trackCycleArray;
		trackCycleArray = new EvaluatedTrack[(int) jobs];
		int counter = 0;
		// iterate over every track to find out how each cycle performs on it
		while (iterT.hasNext()) {
			currentTrack = (Track) iterT.next();
			Iterator iterC = cycleList.iterator();

			ArrayList<Player> players = new ArrayList<Player>();
			Map<String, GridCell> startingPositions = currentTrack
					.getStartingPositions();
			double[][] distractorMatrix = Consultant
					.buildDistractorMatrix(currentTrack);

			
			Cycle bestCycle = null;
			SearchNode bestNode = null;
			
			// iterate over every cycle to see how it performs
			while (iterC.hasNext()) {
				currentCycle = (Cycle) iterC.next();
				//iterate over every start position for the cycle
				for (GridCell pos : startingPositions.values()) {
					SearchNode node = new SearchNode(pos, currentCycle,
							currentTrack,
							Consultant.buildDistractorMatrix(currentTrack));

					// Search
					node.loopSearch(timeFactor);

					// store in an array
					trackCycleArray[counter] = new EvaluatedTrack(currentTrack, currentCycle, node.getValue(), node.getCell());
					counter++;
					
				}
			}
		}

		List<EvaluatedTrack> trackCombo = new ArrayList<EvaluatedTrack>();
		double maxPrize = 0;
		double maxCost = tour.getCurrentMoney();
		double currentPrize = 0;
		//check all combinations of tracks (1,2,3)		
		for (EvaluatedTrack t : trackCycleArray) {
			// if this track alone gives more than the current best set it to
			// best
			double currentCost = t.getCycle().getPrice() + t.getTrack().getRegistrationFee();
			if (currentCost > maxCost) {
				continue;
			}
			// if first iteration
			if(trackCombo.size() == 0) {
				trackCombo.add(t);
				currentPrize = t.getPrize() - currentCost;
			}
			
			// if the cost of the potential winnings (prize - cycle cost - reg cost) is greater than the current max prize
			if (t.getPrize() - currentCost > maxPrize) {
				trackCombo.clear();
				trackCombo.add(t);
				currentPrize = t.getPrize() - currentCost;
			}
			for (EvaluatedTrack x : trackCycleArray) {
				// if this track is different to the previous and gives more
				// when added set it and previous as best
				// check for same track
				if (x.getTrack().equals(t.getTrack())) {
					continue;
				}
				//check if we have already bought the cycle
				currentCost = currentCost + x.getTrack().getRegistrationFee();
				if(!x.getCycle().equals(t.getCycle())) {
					currentCost = currentCost + x.getCycle().getPrice();
				} 
				
				if(currentCost > maxCost) {
					continue;
				}
				
				
				if (t.getPrize() + x.getPrize() - currentCost > maxPrize) {
					trackCombo.clear();
					trackCombo.add(t);
					trackCombo.add(x);
					currentPrize = t.getPrize() + x.getPrize() - currentCost;
					
				}
				for (EvaluatedTrack y : trackCycleArray) {
					// if this track is different to the previous ones and gives
					// more when added with the previous set them as best
					// check for same track
					if (y.getTrack().equals(x.getTrack())) {
						continue;
					}
					if (y.getTrack().equals(t.getTrack())) {
						continue;
					}
					//check if we have already bought the cycle
					currentCost = currentCost + y.getTrack().getRegistrationFee();
					if(!y.getCycle().equals(t.getCycle())) {
						if(!y.getCycle().equals(x.getCycle())){
							currentCost = currentCost + y.getTrack().getRegistrationFee();
						}
					}
					
					if (t.getPrize() + x.getPrize() + y.getPrize() - currentCost> maxPrize) {
						trackCombo.clear();
						trackCombo.add(t);
						trackCombo.add(x);
						trackCombo.add(y);
						currentPrize = t.getPrize() + x.getPrize() + y.getPrize() - currentCost;
					}
					//if current prize for this set of 3 is best prize so far set max prize to it
					if(currentPrize > maxPrize) {
						maxPrize = currentPrize;
					}
				}
				//if current prize for this set of 2 is best prize so far set max prize to it
				if(currentPrize > maxPrize) {
					maxPrize = currentPrize;
				}
			}
			//if current prize for this set of 1 is best prize so far set max prize to it
			if(currentPrize > maxPrize) {
				maxPrize = currentPrize;
			}
		}
		// goes through the evaluated track list and selects the track and cycle
		// from it that corresponds to the selected track
		for (EvaluatedTrack z : trackCycleArray) {
			for (EvaluatedTrack t : trackCombo) {
				if (z.getTrack().equals(t.getTrack())) {
					if (z.getCycle().equals(t.getCycle())) {
						output.add(z);
					}
					
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
			//prize = prize * (1 - obsWeight);
			attributes[1] = obsWeight;

			// reduce expected prize based on distractors
			// distractors/cells
			double distractorWeight = (currentTrack.getDistractors().size() / (currentTrack
					.getNumCols() * currentTrack.getNumRows()));
			//prize = prize * (1 - distractorWeight);
			//prize = prize - currentTrack.getRegistrationFee();
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
