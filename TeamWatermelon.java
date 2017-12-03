public class TeamWatermelon extends Player {

	// for DoubleAgent
	private Handshaker shaker;

	boolean opponentHadWinningPosition; //set to true if opponent can force a win at any point in the match.
	int trust;

	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative, scoreWhiteBytesCooperative, scoreBlackBytesCooperative;

	public int BETRAYAL_DELTA = 2;
	public int COOPERATION_DELTA = 1;
	public int IRRATIONALITY_DELTA = 3;
	public int SUBOPTIMALITY_DELTA = 1;

	int isOpponentMonkey = 0;
	boolean isOpponentNihilist = false;
	boolean isOpponentOptimist = false;
	boolean isOpponentPessimist = false;
	boolean isOpponentQueller = false;
	boolean isOpponentRealist = false;
	boolean isOpponentScrapper = false;
	boolean isOpponentTruster = false;
	boolean isOpponentUtilitarian = false;

	boolean opponentCanCaptureKing = false;
	boolean opponentCanCaptureRook = false;

	public TeamWatermelon(int maxNumMoves) {
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
		shaker = Handshaker.createHandshakeAccepter();
	}

	public void prepareForSeries() {
		trust = 1;
		shaker.handshakePrepareForSeries();
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
		shaker.handshakePrepareForMatch(toBoardPosition());
	}

	public void receiveMatchOutcome(int matchOutcome) {
		//Convert to a more reasonable format first:
		int myMatchPayoff = outcomeToPayoff(matchOutcome);
		trust = updateTrust(trust, myMatchPayoff);

		shaker.handshakeReceiveMatchOutcome(matchOutcome, toBoardPosition());
	}

	public int updateTrust(int trust, int myMatchPayoff) {
		if (trust>0) { // I trusted you! Let's see how you did:
			if (myMatchPayoff < 2) {
				// I didn't take your king? I trust you less now.
				return trust - BETRAYAL_DELTA;
			} else if (myMatchPayoff == 3) {
				// I tried to tie, but I won!!! I don't trust that you know what you're doing.
				return trust - IRRATIONALITY_DELTA;
			}
		} else if (opponentHadWinningPosition && myMatchPayoff >= 2) {
			// You gave up a win for a tie/loss? I trust you more now.
			return trust + COOPERATION_DELTA;
		}
		return trust;
	}

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


	// Against DoubleAgent ONLY!!!
	public MoveDescription chooseMove() {
		BoardPosition currentBoardPosition = toBoardPosition();
		// update shaker with opponent move
		shaker.updateTheirMove(currentBoardPosition);

		MoveDescription myMove;
		if (shaker.shouldSendHandshakeMove()) {
			myMove=Handshaker.getHandshakeMove(currentBoardPosition);
		} else {
			myMove = internalChooseMove();
		}

		shaker.receiveMyMove(myMove);
		return myMove;
	}

	// double check if this is working against the other players
	// the original chooseMove: now this is for all the non-doubleAgent player
	public MoveDescription internalChooseMove() {
		BoardPosition boardPosition = toBoardPosition();
		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		opponentHadWinningPosition = updateOpponentHadWinningPosition(boardPosition, currentPlayerColour);
		updateOpponentCanCaptureKingAndRook();
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

		if (opponentCanCaptureKing && !myKingIsAlive) {
			isOpponentMonkey += 3;
		}

		if (opponentCanCaptureRook && !myRookIsAlive) {
			isOpponentMonkey += 5;
		}

		if (bestScoreRealist==2 || isOpponentMonkey >= 20) {
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
