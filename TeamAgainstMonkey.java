public class TeamAgainstMonkey extends Player {

	// for DoubleAgent
	private Handshaker shaker;

	boolean opponentHadWinningPosition; //set to true if opponent can force a win at any point in the match.
	int trust;
	// TODO BY ZOE: REMEMBER TO TURN THIS OFF
	public final boolean DEBUG_MODE = true;

	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative, scoreWhiteBytesCooperative, scoreBlackBytesCooperative;

	public int BETRAYAL_DELTA = 1;
	public int COOPERATION_DELTA = 1;
	public int IRRATIONALITY_DELTA = 2;
	public int SUBOPTIMALITY_DELTA = 1;

	int monkeyScore;
	boolean isDetectMonkeyModeOn;

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

	int matchNum;

	public TeamAgainstMonkey(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);
		// Take the data that we need from teamRationalRealist:
		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;
		// then teamRationalRealist will be deconstructed.

		TeamRational teamRationalCooperative = TeamRational.createCooperative(maxNumMoves);
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
		isDetectMonkeyModeOn = true;
		isOpponentMonkey = false;
		opponentCanCaptureKingThisRound = false;
		opponentCanCaptureRookThisRound = false;
		shaker.handshakePrepareForSeries();
	}

	public String parseThreatenBy(String input) {
		try{
			return input.substring(input.lastIndexOf('/') + 1);
		} catch(Exception e) {
			if(DEBUG_MODE) {
				System.out.println("THERE IS A FUCKING STRING ERROR!!!!");
			}
			// let's just swing it
			return "EXCEPTION";
		}
	}

	public void prepareForMatch() {
		BoardPosition boardPosition;

		opponentHadWinningPosition = false;

		matchNum += 1;

		// Start in second : record the gameboard config?
		if (myColour == BLACK) {
			System.out.println("\n[prepareForMatch] Match "+matchNum+" We are BLACK");
			boardPosition = toBoardPosition();
			if (scoreBlackBytesRealist[boardPosition.toInt()] == 0) {
				opponentHadWinningPosition = true;
			}

			String kingRes = checkNextThreaten(myKingRow, myKingColumn, "king", "prepareForMatch");
			String rookRes = checkNextThreaten(myRookRow, myRookColumn, "rook", "prepareForMatch");

			String myKingThreatenBy = parseThreatenBy(kingRes);
			String myRookThreatenBy = parseThreatenBy(rookRes);

			opponentCanCaptureRookThisRound = !myKingThreatenBy.equals("safe");
			opponentCanCaptureKingThisRound = !myRookThreatenBy.equals("safe");

			if(DEBUG_MODE) {
				System.out.println("===");
				System.out.println("[prepareForMatch] kingRes: "+kingRes);
				System.out.println("[prepareForMatch] rookRes: "+rookRes);

				if(opponentCanCaptureRookThisRound) System.out.println("[prepareForMatch] Opponent can capture our rook ");
				if(opponentCanCaptureKingThisRound) System.out.println("[prepareForMatch] Opponent can capture our king");
				System.out.println("===");
			}

		} else {
			 if(DEBUG_MODE) System.out.println("[prepareForMatch] Match "+matchNum+" We are WHITE");

		}
		shaker.handshakePrepareForMatch(toBoardPosition());
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


	public boolean isBlocked(int src, int dest, int curr) {
		return (src <= dest && src <= curr && curr <= dest) ||
			   (dest <= src && dest <= curr && curr <= src);
	}

	// print out all the coordinates
	public void printCell(int myNextRow, int myNextCol, String myNextPieceType, String state) {
		if(DEBUG_MODE) {
			System.out.println("STATE = XXXX "+state);
			System.out.println("My next "+myNextPieceType+": [" + myNextRow + ", " + myNextCol + "]");
			System.out.println("My current king: ["+myKingRow+", "+myKingColumn+"]");
			System.out.println("My current rook: ["+myKingRow+", "+myKingColumn+"]");
			System.out.println("Their current king: ["+theirKingRow+", "+theirKingColumn+"]");
			System.out.println("Their current rook: ["+theirRookRow+", "+theirRookColumn+"]");
		}
	}

	// can be used to check current threaten as well
	public String checkNextThreaten(int myNextRow, int myNextCol, String myNextPieceType, String state) {
		// those two statements shouldn't be printed at any cases!
		if (myNextPieceType.equals("king") && !myKingIsAlive) return "delete non-existing king /error";
		if (myNextPieceType.equals("rook") && !myRookIsAlive) return "delete non-existing rook /error";

		boolean isThreatenByTheirKing = (theirKingIsAlive && Math.abs(theirKingRow - myNextRow) <= 1 && Math.abs(theirKingColumn - myNextCol) <= 1);

		// isThreatenByTheirRook: two directions
		boolean threatenHorizon = (theirRookRow == myNextRow) && !isBlocked(theirRookColumn, myNextCol, theirKingColumn) && !isBlocked(theirRookColumn, myNextCol, myRookColumn);
		boolean threatenVertical = (theirRookColumn == myNextCol) && !isBlocked(theirRookRow, myNextRow, theirKingRow) && !isBlocked(theirRookRow, myNextRow, myRookRow);

		boolean isThreatenByTheirRook = (theirRookIsAlive && threatenHorizon && threatenVertical);

		String res = myNextPieceType;

		if(isThreatenByTheirKing && isThreatenByTheirRook) res += " threaten by /both";
		else if(isThreatenByTheirKing) res += " threaten by /theirKing";
		else if(isThreatenByTheirRook) res += " threaten by /theirRook";
		else
			res = "/safe";

		printCell(myNextRow, myNextCol, myNextPieceType, state);

		return res;
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

	// the original chooseMove: now this is for all the non-doubleAgent player
	public MoveDescription internalChooseMove() {
		BoardPosition boardPosition = toBoardPosition();
		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		opponentHadWinningPosition = updateOpponentHadWinningPosition(boardPosition, currentPlayerColour);
		return bestMoveFromTrust(boardPosition, currentPlayerColour);
	}

	// check if king/rook will be eaten in the next round
	public boolean kingOrRookCanBeCapturedNextRound(MoveDescription nextMove) {
		int myNextRow = nextMove.getDestinationRow();
		int myNextCol = nextMove.getDestinationColumn();
		String piece = nextMove.getPieceToMove();

		return (checkNextThreaten(myNextRow, myNextCol, piece, "CHECKBOTH").equals("safe"));
	}

	// TODO: the opponentCanCaptureKingThisRound MUST BE CHANGED HERE!!
	// THE RUN TIME VALUE FOR THIS IS NO LONGER MEANINGFUL

	// also, the checkNextThreaten NEED TO BE CALLED AGAIN AND AGAIN DURING EVERY ROUND
	// MUST DOUBLE CHECK THE TIME AND MEMROY IN EVERY ROUND

	// Double check in isAgainstMonkey: when we determine the player type
	// Why is it useful to check if our king/ rook is already dead???

	// core function to detect monkey
	public boolean isAgainstMonkey(int bestScoreCooperative, int bestScoreRealist) {
		if(isDetectMonkeyModeOn)
		{
			if (opponentCanCaptureKingThisRound)
			{
				if (!myKingIsAlive)
				{	 // our king was captured in the last round!
					if (trust < 0 && (bestScoreCooperative == 3 || bestScoreRealist == 2))
					{

						if(DEBUG_MODE) System.out.println("Oppo took my king while we can tie! \nIn Match "+matchNum+" Monkey score added! because trust is "+trust);

						monkeyScore += 4;
					}
				}

				else {

					if(DEBUG_MODE) System.out.println("Oppo could take my king but he did not. Definitely not a monkey");

					isOpponentMonkey = false;
					isDetectMonkeyModeOn = false;
				}
			}

			else if (opponentCanCaptureRookThisRound)
			{
				if (!myRookIsAlive)
				{
					if (trust < 1)
					{
						if(DEBUG_MODE) System.out.println("Oppo took my rook! \nIn Match "+matchNum+" Monkey score added! because trust is "+trust);
						monkeyScore += 4;
					}
				}
				else
				{
					if(DEBUG_MODE) System.out.println("Oppo could take my rook but he did not. Definitely not a monkey");

					isOpponentMonkey = false;
					isDetectMonkeyModeOn = false;
				}
			}

			if(monkeyScore >= 20) {
				isOpponentMonkey = true;
				isDetectMonkeyModeOn = false;
			}
		}
		return isOpponentMonkey;
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

		isOpponentMonkey = isAgainstMonkey(bestScoreCooperative, bestScoreRealist);

		// TODO: change the logic for this one
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


		int myNextRow = move.getDestinationRow();
		int myNextCol = move.getDestinationColumn();
		String myNextPieceType = move.getPieceToMove();

		checkNextThreaten(myNextRow, myNextCol, myNextPieceType, "BESTMOVE");

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
