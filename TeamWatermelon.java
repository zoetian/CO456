
public class TeamWatermelon extends Player {

	// for Monkey player
	boolean opponentCanCaptureKing = false;
	boolean opponentCanCaptureRook = false;

	boolean opponentCanWin;
	int trust;
	static boolean isTrustModeOn = false;

	public byte[] bestMoveBytesRealist;
	public byte[] scoreWhiteBytesRealist;
	public byte[] scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative;
	public byte[] scoreWhiteBytesCooperative;
	public byte[] scoreBlackBytesCooperative;
	public byte[] bestMoveBytesTruster;
	public byte[] scoreWhiteBytesTruster;
	public byte[] scoreBlackBytesTruster;

	public final int BETRAYAL_DELTA = 6;
	public final int COOPERATION_DELTA = 1;

	public TeamWatermelon(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);
		TeamRational teamRationalCooperative = TeamRational.createCooperative(maxNumMoves);
		TeamRational teamRationalTruster = TeamRational.createTruster(maxNumMoves);

		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;

		bestMoveBytesCooperative = teamRationalCooperative.bestMoveBytes;
		scoreWhiteBytesCooperative = teamRationalCooperative.scoreWhiteBytes;
		scoreBlackBytesCooperative = teamRationalCooperative.scoreBlackBytes;

		bestMoveBytesTruster = teamRationalTruster.bestMoveBytes;
		scoreWhiteBytesTruster = teamRationalTruster.scoreWhiteBytes;
		scoreBlackBytesTruster = teamRationalTruster.scoreBlackBytes;

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

	// for monkey player
	public void updateOpponentCanCaptureKingAndRook() {
		// their king captures our king
		if(theirKingIsAlive && myKingIsAlive){
			if ((theirKingRow == myKingRow && Math.abs(theirKingColumn - myKingColumn)==1) ||
			 		(theirKingColumn == myKingColumn && Math.abs(theirKingRow - myKingRow)==1) ||
					(Math.abs(theirKingColumn - myKingColumn)==1 && Math.abs(theirKingRow - myKingRow)==1)) {
						opponentCanCaptureKing = true;
			}
		}
		// their rook captures our king
		if(theirRookIsAlive && myKingIsAlive) {
			if((theirRookRow==myKingRow && myRookRow != myKingRow) ||
			   (theirRookColumn==myKingColumn && myRookColumn != myKingColumn)) {
					 opponentCanCaptureKing = true;
			}
		}
		// their king captures our rook
		if(theirKingIsAlive && myRookIsAlive) {
			if ((theirKingRow == myRookRow && Math.abs(theirKingColumn - myRookColumn)==1) ||
			 		(theirKingColumn == myRookColumn && Math.abs(theirKingRow - myRookRow)==1) ||
					(Math.abs(theirKingColumn - myRookColumn)==1 && Math.abs(theirKingRow - myRookRow)==1)) {
						opponentCanCaptureRook = true;
			}
		}
		// their rook captures our rook
		if(theirRookIsAlive && myRookIsAlive) {
			if((theirRookRow==myRookRow && theirRookRow != theirKingRow) ||
			   (theirRookColumn==myRookColumn && theirRookColumn != theirKingColumn)) {
					 opponentCanCaptureRook = true;
			}
		}
	}

	// the original chooseMove
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

		TeamRational.Node nodeCooperative = new TeamRational.Node(bestMoveBytesCooperative[boardPosition.toInt()],
																  scoreWhiteBytesCooperative[boardPosition.toInt()], scoreBlackBytesCooperative[boardPosition.toInt()]);
		int bestScoreCooperative = nodeCooperative.getScore(currentPlayerColour);

		TeamRational.Node nodeTruster = new TeamRational.Node(bestMoveBytesTruster[boardPosition.toInt()],
															  scoreWhiteBytesTruster[boardPosition.toInt()], scoreBlackBytesTruster[boardPosition.toInt()]);
		int bestScoreTruster = nodeTruster.getScore(currentPlayerColour);


		// If you cannot force a tie, and it is still possible to tie, and trust
		// remains, then play trustingly:
		//System.out.println(bestScoreRealist+" "+bestScoreCooperative+" "+bestScoreTruster);
		if (bestScoreRealist != 2 && bestScoreCooperative == 3 && trust > 0) {
			return nodeCooperative.bestMove;
		} else if (bestScoreRealist != 2 && bestScoreCooperative == 10 && isTrustModeOn && trust > 0) {
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
