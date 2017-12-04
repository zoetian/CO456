public class TeamWa extends Player {

	// for DoubleAgent
	private Handshaker shaker;

	boolean opponentHadWinningPosition; //set to true if opponent can force a win at any point in the match.
	int trust;

	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative, scoreWhiteBytesCooperative, scoreBlackBytesCooperative;

	public int BETRAYAL_DELTA = 1;
	public int COOPERATION_DELTA = 1;
	public int IRRATIONALITY_DELTA = 2;
	public int SUBOPTIMALITY_DELTA = 1;

	int monkeyScore;
	boolean detectMonkey;

	boolean isOpponentMonkey;
	boolean isOpponentNihilist = false;
	boolean isOpponentOptimist = false;
	boolean isOpponentPessimist = false;
	boolean isOpponentQueller = false;
	boolean isOpponentRealist = false;
	boolean isOpponentScrapper = false;
	boolean isOpponentTruster = false;
	boolean isOpponentUtilitarian = false;

	boolean opponentCanCaptureKingThisRound;
	boolean opponentCanCaptureRookThisRound;
	boolean opponentCanCaptureKingLastRound;
	boolean opponentCanCaptureRookLastRound;

	int matchNum;

	public TeamWa(int maxNumMoves) {
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
		monkeyScore = 0;
		matchNum = 0;
		detectMonkey = true;
		isOpponentMonkey = false;
		opponentCanCaptureKingThisRound = false;
		opponentCanCaptureRookThisRound = false;
		opponentCanCaptureKingLastRound = false;
		opponentCanCaptureRookLastRound = false;
		shaker.handshakePrepareForSeries();
	}

	public void prepareForMatch() {
		BoardPosition boardPosition;

		opponentHadWinningPosition = false;

		matchNum += 1;
		// Take note if opponent starts in winning position:
		if (myColour == BLACK) {
			System.out.println("Match "+matchNum+" We are BLACK");
			boardPosition = toBoardPosition();
			if (scoreBlackBytesRealist[boardPosition.toInt()] == 0) {
				opponentHadWinningPosition = true;
			}

			opponentCanCaptureRookThisRound=updateOpponentCanCaptureRook(myRookRow,myRookColumn);
			opponentCanCaptureKingThisRound=updateOpponentCanCaptureKing(myKingRow,myRookColumn);
			System.out.println("Can opponent capture our rook at the beginning? "+opponentCanCaptureRookThisRound);
			System.out.println("Can opponent capture our king at the beginning? "+opponentCanCaptureKingThisRound);

		} else {
			System.out.println("Match "+matchNum+" We are WHITE");

		}
		shaker.handshakePrepareForMatch(toBoardPosition());

		//System.out.println("Match Num "+matchNum+" Can opponent capture rook last round? "+opponentCanCaptureRookLastRound
		//+" Can opponent capture king last round? "+opponentCanCaptureKingLastRound);
		/*
		System.out.println("My King position: ("+myKingRow+","+myKingColumn+")");
		System.out.println("Their King position: ("+theirKingRow+","+theirKingColumn+")");
		System.out.println("My Rook position: ("+myRookRow+","+myRookColumn+")");
		System.out.println("Their Rook position: ("+theirRookRow+","+theirRookColumn+")");
		*/
		//boardPosition = toBoardPosition();
		//int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		//System.out.println("Match Num is "+matchNum+" My color is "+myColour+" cuurent color is "+currentPlayerColour);
	}

	public void receiveMatchOutcome(int matchOutcome) {
		//Convert to a more reasonable format first:
		int myMatchPayoff = outcomeToPayoff(matchOutcome);
		trust = updateTrust(trust, myMatchPayoff);
		//System.out.println("Match "+matchNum+" This is a monkey: "+isOpponentMonkey+" Monkey score is "+monkeyScore);
		shaker.handshakeReceiveMatchOutcome(matchOutcome, toBoardPosition());
	}

	public int updateTrust(int trust, int myMatchPayoff) {
		if (trust>0) { // I trusted you! Let's see how you did:
			if (myMatchPayoff < 2) {
				// I didn't take your king? I trust you less now.
				return trust - BETRAYAL_DELTA;
			} else if (myMatchPayoff == 3) {
				// I tried to tie, but I won!!! I don't trust that you know what you're doing.
				//isOpponentMonkey += 2;
				return trust - IRRATIONALITY_DELTA;
			}
		} else if (opponentHadWinningPosition && myMatchPayoff == 2) {
			// I didn't trust you. I'm very picky about restoring trust!
			// You gave up a win for a tie? I trust you more now.
			return trust + COOPERATION_DELTA;

		} else if (opponentHadWinningPosition && myMatchPayoff != 2) {
			// I don't believe that you needed to let me win.
			//isOpponentMonkey += 4;
			return trust - SUBOPTIMALITY_DELTA;
		}
		return trust;
	}

	public boolean updateOpponentCanCaptureKing(int nextRow, int nextCol) {
		// their king captures our king
		if(theirKingIsAlive && myKingIsAlive){
			if (Math.abs(theirKingColumn - nextCol)<=1 && Math.abs(theirKingRow-nextRow)<=1) {
						return true;
			}
		}

		// their rook captures our king
		if(theirRookIsAlive && myKingIsAlive) {
			if((theirRookRow==nextRow && myRookRow != nextRow && theirKingRow != nextRow) ||
			   (theirRookColumn==nextCol && myRookColumn != nextCol && theirKingColumn != nextCol)) {
					 return true;
			}
		}
		 return false;
	 }

	 public boolean updateOpponentCanCaptureRook(int nextRow, int nextCol) {
		// their king captures our rook
		if(theirKingIsAlive && myRookIsAlive) {
			if (Math.abs(theirKingColumn - nextCol)<=1 && Math.abs(theirKingRow-nextRow)<=1) {
						return true;
			}
		}
		// their rook captures our rook
		if(theirRookIsAlive && myRookIsAlive) {
			if((theirRookRow==nextRow && theirRookRow != theirKingRow && myKingRow != theirRookRow) ||
			   (theirRookColumn==nextCol && theirRookColumn != theirKingColumn && myKingColumn != theirRookColumn)) {
					 return true;
			}
		}

		return false;
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
		return bestMoveFromTrust(boardPosition, currentPlayerColour);
	}

	// check if king/rook will be eaten in the next round
	public boolean kingOrRookCanBeCapturedNextRound(MoveDescription nextMove) {
		int nextRow = nextMove.getDestinationRow();
		int nextCol = nextMove.getDestinationColumn();
		String piece = nextMove.getPieceToMove();
		if((Math.abs(theirKingColumn - nextCol)<=1 && Math.abs(theirKingRow-nextRow)<=1)||
			 (theirRookRow==nextRow && theirRookRow != theirKingRow) ||
			 (theirRookColumn==nextCol && theirRookColumn != theirKingColumn)) {
				 return true;
		}
		return false;
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

		// detect monkey
		// logic when we start first, so we cannot detect monkey for now
		if(detectMonkey) {
			
			if (opponentCanCaptureRookThisRound) {
				if (!myRookIsAlive) {
					if (trust < 1) {
						System.out.println("He ate my rook! in Match "+matchNum+" Monkey score added! because trust is "+trust);
						monkeyScore += 4;
					}
				} else {
					// the enemy can take my rook but he did not. Definitely not a monkey
					System.out.println("the enemy can take my rook but he did not. Definitely not a monkey");
					isOpponentMonkey=false;
					detectMonkey=false;
				}
			}

			if (opponentCanCaptureKingThisRound) { // for next round?
				if (!myKingIsAlive) {
					// our king was captured in the last round!
					if (trust < 0 && (bestScoreCooperative == 3 || bestScoreRealist == 2)) {
						System.out.println("He ate my king while we can tie! in Match "+matchNum+" Monkey score added! because trust is "+trust);
						monkeyScore += 4;
					}
				} else {
					// the enemy can take my king but he did not. Definitely not a monkey
					System.out.println("the enemy can take my king but he did not. Definitely not a monkey");
					isOpponentMonkey=false;
					detectMonkey=false;
				}
			}

			if(monkeyScore>=20) {
				isOpponentMonkey = true;
				detectMonkey = false;
			}
		}

		if(isOpponentMonkey) {
			System.out.println("Activating Monkey Code...");
			int iteration = 20;
			while(iteration>0) {
				MoveDescription move = nodeRealist.bestMove;
				if(!kingOrRookCanBeCapturedNextRound(move)){
					return move;
				}
				iteration -= 1;
			}
		}

		MoveDescription move;
		//if (bestScoreRealist==2 || (isMonkey == true && matchNum <= 12)) {
		if (bestScoreRealist==2) {
			//System.out.println("Monkey score is "+isOpponentMonkey);
			// If the move forces a tie, play it in all cases (to be safe):
			move = nodeRealist.bestMove;
		} else if (trust > 0 && bestScoreCooperative == 3) {
			// If trust remains, play cooperatively for a tie:
			move = nodeCooperative.bestMove;
		} else {
			// In all other cases, play realistically:
			move = nodeRealist.bestMove;
		}


		int nextRow = move.getDestinationRow();
		int nextCol = move.getDestinationColumn();
		String pieceToMove = move.getPieceToMove();

		switch (pieceToMove){
		case "king":
			opponentCanCaptureKingThisRound = updateOpponentCanCaptureKing(nextRow,nextCol);
			if(opponentCanCaptureKingThisRound) {
				System.out.println("We decide to move king in Match "+matchNum+". Our king be captured next round");
			}
			System.out.println("Our king is at "+nextRow+" "+nextCol);
			System.out.println("Our rook is at "+myRookRow+" "+myRookColumn);
			System.out.println("Their king is at "+theirKingRow+" "+theirKingColumn);
			System.out.println("Their rook is at "+theirRookRow+" "+theirRookColumn);
			break;
		case "rook":
			opponentCanCaptureRookThisRound = updateOpponentCanCaptureRook(nextRow,nextCol);
			if(opponentCanCaptureRookThisRound) {
				System.out.println("We decide to move rook in Match "+matchNum+". Our rook be captured next round");
			}
			System.out.println("Our rook is at "+nextRow+" "+nextCol);
			System.out.println("Our king is at "+myKingRow+" "+myKingColumn);
			System.out.println("Their king is at "+theirKingRow+" "+theirKingColumn);
			System.out.println("Their rook is at "+theirRookRow+" "+theirRookColumn);
			break;
		}
		return move;
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
