
public class TeamTFTADC extends Player {

	boolean opponentCanWin;
	int trust;
	public boolean isTrustModeOn = false;

	public byte[] bestMoveBytesRealist;
	public byte[] scoreWhiteBytesRealist;
	public byte[] scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative;
	public byte[] scoreWhiteBytesCooperative;
	public byte[] scoreBlackBytesCooperative;

	public final int BETRAYAL_DELTA = 3;
	public final int COOPERATION_DELTA = 1;

	public TeamTFTADC(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);
		TeamRational teamRationalCooperative = TeamRational.createCooperative(maxNumMoves);

		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;

		bestMoveBytesCooperative = teamRationalCooperative.bestMoveBytes;
		scoreWhiteBytesCooperative = teamRationalCooperative.scoreWhiteBytes;
		scoreBlackBytesCooperative = teamRationalCooperative.scoreBlackBytes;

		opponentCanWin = false;
		trust = 1;
	}

	public void prepareForSeries() {
		trust = 1;
	}

	public void prepareForMatch() {
		BoardPosition boardPosition;

		// Initial belief about whether opponent can win; if they can but a tie
		// happens, we increase trust.
		opponentCanWin = false; // The only case we can determine right now is
								// if the opponent is white:
		if (myColour == BLACK) {
			boardPosition = toBoardPosition();
			if (scoreBlackBytesRealist[boardPosition.toInt()] == 0) {
				opponentCanWin = true;
			}
		}
	}

	public void receiveMatchOutcome(int matchOutcome) {
		int matchPayoff = outcomeToPayoff(matchOutcome);
		trust = updateTrust(trust, matchPayoff);
	}

	public int updateTrust(int trust, int matchPayoff) {
		if (matchPayoff < 2) {
			// I didn't take your king? I don't trust you anymore.
			return trust - BETRAYAL_DELTA;
		} else if (opponentCanWin && matchPayoff == 2) {
			// You gave up a win? I trust you more now.
			return trust + COOPERATION_DELTA;
		}
		return trust;
	}

	public MoveDescription chooseMove() {

		BoardPosition boardPosition = toBoardPosition();

		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;

		opponentCanWin = updateOpponentCanWin(boardPosition, currentPlayerColour);

		isTrustModeOn = (theirRookIsAlive && !theirKingIsAlive && myKingIsAlive && myRookIsAlive);
		return bestMoveFromTrust(boardPosition, currentPlayerColour);
	}

	public MoveDescription bestMoveFromTrust(BoardPosition boardPosition, int currentPlayerColour) {
		TeamRational.Node nodeRealist = new TeamRational.Node(bestMoveBytesRealist[boardPosition.toInt()],
				scoreWhiteBytesRealist[boardPosition.toInt()], scoreBlackBytesRealist[boardPosition.toInt()]);
		int bestScoreRealist = nodeRealist.getScore(currentPlayerColour);

		TeamRational.Node nodeTruster = new TeamRational.Node(bestMoveBytesCooperative[boardPosition.toInt()],
				scoreWhiteBytesCooperative[boardPosition.toInt()], scoreBlackBytesCooperative[boardPosition.toInt()]);
		int bestScoreTruster = nodeTruster.getScore(currentPlayerColour);

		// If you cannot force a tie, and it is still possible to tie, and trust
		// remains, then play trustingly:
		if (bestScoreRealist != 2 && bestScoreTruster == 3 && trust > 0) {
			return nodeTruster.bestMove;
		} else { // In all other cases, play realistically:
			return nodeRealist.bestMove;
		}
	}

	public boolean updateOpponentCanWin(BoardPosition boardPosition, int currentPlayerColour) {
		TeamRational.Node nodeRealist = new TeamRational.Node(bestMoveBytesRealist[boardPosition.toInt()],
				scoreWhiteBytesRealist[boardPosition.toInt()], scoreBlackBytesRealist[boardPosition.toInt()]);
		int bestScoreRealist = nodeRealist.getScore(currentPlayerColour);

		if (!opponentCanWin && bestScoreRealist == 0) {
			return true;
		}

		return false;
	}
}
