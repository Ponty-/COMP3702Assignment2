package solver;

import problem.Cycle;
import problem.GridCell;
import problem.Track;

public class EvaluatedTrack {

	private Track track;
	private Cycle cycle;
	private double prize;
	private GridCell start;

	public EvaluatedTrack(Track track, Cycle cycle, Double prize, GridCell start) {
		this.track = track;
		this.cycle = cycle;
		this.prize = prize;
		this.start = start;
	}

	public Track getTrack() {
		return track;
	}

	public Cycle getCycle() {
		return cycle;
	}

	public Double getPrize() {
		return prize;
	}

	public GridCell getStart() {
		return start;
	}

}
