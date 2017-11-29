

import java.util.*;

public class TeamMonkey extends Player {
	private Random rand;

	public TeamMonkey(int maxNumMoves) {
		rand = new Random(System.currentTimeMillis());
	}

	public void prepareForSeries() {
	}

	public void prepareForMatch() {
	}

	public void receiveMatchOutcome(int matchOutcome) {
	}

	public MoveDescription chooseMove() {

		ArrayList<MoveDescription> allPossibleMoves = getAllPossibleMoves();

		// Try to capture their king.
		if (theirKingIsAlive) {
			for (MoveDescription moveDescription : allPossibleMoves) {
				if (moveDescription.getDestinationColumn() == theirKingColumn
						&& moveDescription.getDestinationRow() == theirKingRow) {
					return moveDescription;
				}
			}
		}

		// Try to capture their rook.
		if (theirRookIsAlive) {
			for (MoveDescription moveDescription : allPossibleMoves) {
				if (moveDescription.getDestinationColumn() == theirRookColumn
						&& moveDescription.getDestinationRow() == theirRookRow) {
					return moveDescription;
				}
			}
		}

		// Did not manage to capture any piece. Will choose random move.
		return allPossibleMoves.get(rand.nextInt(allPossibleMoves.size()));
	}
}
