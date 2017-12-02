public class TeamTitForTat extends Player {

	boolean opponentHadWinningPosition; //set to true if opponent can force a win at any point in the match.
	int trust;

	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative, scoreWhiteBytesCooperative, scoreBlackBytesCooperative;

	public int BETRAYAL_DELTA = 1;
	public int COOPERATION_DELTA = 1;

	public TeamTitForTat(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);
		/*TeamRationalRealist has the following beliefs as P1: 
		 * P1\P2 |  W  |  L  | 
		 * -------------------
		 *   W   | 2,2 | 3,0 |
		 * -------------------
		 *   L   | 0,3 | 1,1 |
		 * -------------------*/
		
		// Take the data that we need from teamRationalRealist:
		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;
		// then teamRationalRealist will be deconstructed.
		
		TeamRational teamRationalCooperative = TeamRational.createCooperative(maxNumMoves);
		/*TeamCooperative has the following beliefs as P1: 
		 * P1\P2 |  W  |  L  | 
		 * -------------------
		 *   W   | 3,3 | 2,0 |
		 * -------------------
		 *   L   | 0,2 | 1,1 |
		 * -------------------*/

		// Take the data that we need from teamRationalCooperative:
		bestMoveBytesCooperative = teamRationalCooperative.bestMoveBytes;
		scoreWhiteBytesCooperative = teamRationalCooperative.scoreWhiteBytes;
		scoreBlackBytesCooperative = teamRationalCooperative.scoreBlackBytes;
		// then teamRationalCooperative will be deconstructed.
	}

	public void prepareForSeries() {
		trust = 1;
	}

	public void prepareForMatch() {
		BoardPosition boardPosition;

		opponentHadWinningPosition = false; 

		// Take note if opponent starts in winning position:
		if (myColour == BLACK) {
			boardPosition = toBoardPosition();
			if (scoreBlackBytesRealist[boardPosition.toInt()] == 0) {
				opponentHadWinningPosition = true;
			}
		}
	}

	public void receiveMatchOutcome(int matchOutcome) {
		//Convert to a more reasonable format first:
		int myMatchPayoff = outcomeToPayoff(matchOutcome);
		trust = updateTrust(trust, myMatchPayoff);
	}

	public int updateTrust(int trust, int myMatchPayoff) {
		if (myMatchPayoff < 2) {
			// I didn't take your king? I trust you less now.
			return trust - BETRAYAL_DELTA;
		} else if (opponentHadWinningPosition && myMatchPayoff >= 2) {
			// You gave up a win for a tie/loss? I trust you more now.
			return trust + COOPERATION_DELTA;
		}
		return trust;
	}

	public MoveDescription chooseMove() {
		BoardPosition boardPosition = toBoardPosition();
		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		opponentHadWinningPosition = updateOpponentHadWinningPosition(boardPosition, currentPlayerColour);
		return bestMoveFromTrust(boardPosition, currentPlayerColour);
	}

	public MoveDescription bestMoveFromTrust(BoardPosition boardPosition, int currentPlayerColour) {
		
		TeamRational.Node nodeRealist = new TeamRational.Node(
				bestMoveBytesRealist[boardPosition.toInt()],
				scoreWhiteBytesRealist[boardPosition.toInt()],
				scoreBlackBytesRealist[boardPosition.toInt()]);
		
		int bestScoreRealist = nodeRealist.getScore(currentPlayerColour);
		
		TeamRational.Node nodeCooperative = new TeamRational.Node(
				bestMoveBytesCooperative[boardPosition.toInt()],
				scoreWhiteBytesCooperative[boardPosition.toInt()],
				scoreBlackBytesCooperative[boardPosition.toInt()]);
		
		int bestScoreCooperative = nodeCooperative.getScore(currentPlayerColour);
		
		if (bestScoreRealist==2) {
			// If the move forces a tie, play it in all cases (to be safe):
			return nodeRealist.bestMove;
		} else if (trust > 0 && bestScoreCooperative == 3) {
			// If trust remains, play cooperatively for a tie:
			return nodeCooperative.bestMove;
		} else {
			// In all other cases, play realistically:
			return nodeRealist.bestMove;
		}
	}

	public boolean updateOpponentHadWinningPosition(BoardPosition boardPosition, int currentPlayerColour) {
		TeamRational.Node nodeRealist = new TeamRational.Node(
				bestMoveBytesRealist[boardPosition.toInt()],
				scoreWhiteBytesRealist[boardPosition.toInt()],
				scoreBlackBytesRealist[boardPosition.toInt()]);
		
		int bestScoreRealist = nodeRealist.getScore(currentPlayerColour);
		
		if (opponentHadWinningPosition || bestScoreRealist == 0) {
			return true;
		}
		
		return false;
	}
}
