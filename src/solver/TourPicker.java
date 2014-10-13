package solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import problem.GridCell;
import problem.Track;
import problem.Cycle;
import problem.Tour;
import problem.Track.CellType;

public class TourPicker {
	public static Map<Track, Cycle> choose(Tour tour) {
		// Pick the best 3 races from the tour and the cycles to use on them
		
		// Return the decision as as a map of tracks and cycles to use on them
		return null;
	}
	
	/* ranks tracks based upon expected winnings */
	public static List<Track> rankTracks(Tour tour) {
		List<Track> trackOrdering = new ArrayList<Track>();
		List<Track> tracks = tour.getTracks();
		Track currentTrack;
		int noTracks = tracks.size();
		double prize = 0;
		for(int i = 0; i < noTracks; i++) {
			currentTrack = tracks.get(i);
			prize = currentTrack.getPrize();
			//reduce expected prize based on obstacles
			prize = prize*(1 - percentObs(currentTrack));
			//reduce expected prize based on distractors
			prize = prize*(1 - currentTrack.getDistractors().size());
			//TODO:add elements and order them
		}
		
		return trackOrdering;
	}
	
	/* iterates over each cell of the track and finds what the percentage is of obstacles to cells */
	public static double percentObs(Track track) {
		double output = 0.0;
		int obsCount = 0;
		double cellCount = track.getNumRows() * track.getNumCols();
		for(int j = 0; j < track.getNumRows(); j++) {
			for(int k = 0; k < track.getNumCols(); k++) {
				GridCell cell = new GridCell(j,k);
				if(track.getCellType(cell).equals(CellType.OBSTACLE)) {
					obsCount++;
				}
			}
		}
		output = obsCount/cellCount;
		return output;
	}
}
