import java.util.*;

public class TeamWatermelon extends Player {

	//A Node represents a board position, a best move and payoffs; this is according to Realist assumptions.
	private Node[] results; //Array of Nodes indexed by an integer representing a board position. Memory intensive!
	private int[] bestMoveInts; //A best move for each position, encoded with ``bit twiddling'' to save memory.
	int maxNumMoves; //Called GameLimit in the project specs
	public boolean isTrustModeOn = false;
	public final boolean DEBUG = false;

	// NOTE: only 60s
	public TeamWatermelon(int maxNumMoves) {
		this.maxNumMoves = maxNumMoves; // maximum # moves in a match
		// double check: due to the deprecated warining before
		LinkedList<BoardPosition> allInitialBoardPositions = BoardPosition.getAllInitialBoardPositions();

		//BoardPosition.toInt() will be used for indices in the following arrays:
		results = new Node[1<<23];
		bestMoveInts = new int[1<<23];

		for (BoardPosition boardPosition : allInitialBoardPositions) {
			computeBestMove(boardPosition);
		}

		results = null; //De-allocate results, so garbage collector will free up that memory.
	}

	public void prepareForSeries() {
		// try{
		// 	System.out.println("prepare for series: "+ boardPosition.toInt());
		// } catch(Exception e) {
		// 	System.out.println("could not fetch in series");
		// }

	}

	public void prepareForMatch() {
		// try{
		// 	System.out.println("prepare for series: "+ boardPosition.toInt());
		// } catch(Exception e) {
		// 	System.out.println("could not fetch in series");
		// }
	}

	public void receiveMatchOutcome(int matchOutcome) {
		/*
		This method will be called immediately after the end of each match.
		The parameter matchOutcome will tell you the outcome of the game, as follows:

		matchOutcome = 1 indicates that you won the match
		matchOutcome = 2 indicates that you lost the match
		matchOutcome = 3 indicates that the match ended in a tie
		matchOutcome = 4 indicates that the match ended in a draw

		The time limit for this method is .1 second.

		After you change this method, you need to remove the line below
		that throws an exception.
		*/

		// TODO: can pass out result to change the next move
		// switch(matchOutcome) {
		// 	case 1:
		// 		System.out.println("TeamWatermelon wins");
		// 		break;
		// 	case 2:
		// 		System.out.println("TeamWatermelon lost");
		// 		break;
		// 	case 3:
		// 		System.out.println("Tie");
		// 		break;
		// 	case 4:
		// 		System.out.println("Draw");
		// 		break;
		// 	default:
		// 		break;
		// }
	}

	public MoveDescription chooseMove() {
		/*
		This method will be called each time you need to make a move.

		At this point, the variables explained above (that describe the state of
		the game) have been correctly initialized, so you can use them.

		The time limit for this method is .1 second.

		Your method should return an object of type MoveDescription, which specifies
		which move you want to make. You can create such an object as in the following examples:

		MoveDescription move1 = new MoveDescription("king", 3, 4);
		// this indicates you want to move your king to column 3 and row 4

		MoveDescription move2 = new MoveDescription("rook", 2, 1);
		// this indicates you want to move your rook to column 2 and row 1

		You are allowed to use the other functions of the class MoveDescription as well,
		in order to access the fields of your object.

		For move1 defined above, for example:
		move.getPieceToMove() will return "king"
		move.getDestinationColumn() will return 3
		move.getDestinationRow() will return 4

		After you change this method, you need to remove the line below
		that throws an exception.
		*/
		BoardPosition boardPosition = toBoardPosition();
		// if the opponent's king already been captured
		// and we still have 2 pieces left on board
		isTrustModeOn = (theirRookIsAlive && !theirKingIsAlive && myKingIsAlive && myRookIsAlive);
		return new MoveDescription(bestMoveInts[boardPosition.toInt()]);
	}

	private Node computeBestMove(BoardPosition boardPosition) {
		int currentPositionInt = boardPosition.toInt();

		Node savedResult = results[currentPositionInt];
		if (savedResult != null) {
			return savedResult;
		}

		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		int nextPlayerColour = 1 - currentPlayerColour;

		Node ret = null;

		double[] payoffsWhiteWins = new double[] {3.0, 0.0};
		double[] payoffsBlackWins = new double[] {0.0, 3.0};
		double[] payoffsTie = new double[] {2.0, 2.0};
		double[] payoffsDraw = new double[] {1.0, 1.0};

		// if both kings have been captured, the game has ended
		if (boardPosition.whiteKingPosition == 0 && boardPosition.blackKingPosition == 0) {
			ret = new Node(null, payoffsTie);
		} else if (boardPosition.whiteKingPosition == 0 && currentPlayerColour == BLACK) {
			// if white king has been captured and it's black player's turn, the game has ended
			// black won
			ret = new Node(null, payoffsBlackWins);
		} else if (boardPosition.blackKingPosition == 0 && currentPlayerColour == WHITE) {
			// if black king has been captured and it's white player's turn, the game has ended
			// white won
			ret = new Node(null, payoffsWhiteWins);
		} else if (currentPlayerColour == WHITE && boardPosition.whiteKingPosition == 0 && boardPosition.whiteRookPosition == 0) {
			// if it's white player's turn but he has no pieces left, the game has ended
			// black won
			ret = new Node(null, payoffsBlackWins);
		} else if (currentPlayerColour == BLACK && boardPosition.blackKingPosition == 0 && boardPosition.blackRookPosition == 0) {
			// if it's black player's turn but he has no pieces left, the game has ended
			// white won
			ret = new Node(null, payoffsWhiteWins);
		} else if (boardPosition.numMovesPlayed == maxNumMoves) {
			// if we have reached the maximum number of moves, the game is over
			if (boardPosition.whiteKingPosition != 0 && boardPosition.blackKingPosition != 0) {
				// draw
				ret = new Node(null, payoffsDraw);
			} else if (boardPosition.blackKingPosition == 0) {
				// white won
				ret = new Node(null, payoffsWhiteWins);
			} else {
				// !whiteKingRemains
				// black won
				ret = new Node(null, payoffsBlackWins);
			}
		} else {

			// Game has not ended
			// Explore all possible moves
			ArrayList<MoveDescription> allPossibleMoves = boardPosition.getAllPossibleMoves();
			for (MoveDescription moveDescription : allPossibleMoves) {
				BoardPosition newBoardPosition = boardPosition.doMove(moveDescription);
				Node node = computeBestMove(newBoardPosition);
				if (isTrustModeOn) {
					if(DEBUG) System.out.println("PROUD TRUSTER!!!!!");
					double node_util = 3*node.getScore(currentPlayerColour)+2*node.getScore(nextPlayerColour);
					double ret_util = 3*ret.getScore(currentPlayerColour)+2*ret.getScore(nextPlayerColour);
					if (ret == null || node_util > ret_util) {
						ret = new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK));
					}
				} else {
					if (ret == null || node.getScore(currentPlayerColour) > ret.getScore(currentPlayerColour)) {
						ret = new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK));
					}
				}
			}
		}
		//Add result to array!
		results[currentPositionInt]=ret;
		if (ret.bestMove != null) {
			bestMoveInts[currentPositionInt]=ret.bestMove.toInt();
		}
		return ret;
	}


	private class Node {
		public MoveDescription bestMove;
		public double scoreWhite, scoreBlack;

		public Node(MoveDescription bestMove_, double scoreWhite_, double scoreBlack_) {
			bestMove = bestMove_;
			scoreWhite = scoreWhite_;
			scoreBlack = scoreBlack_;
		}

		public Node(MoveDescription bestMove_, double [] score_) {
			this(bestMove_, score_[WHITE], score_[BLACK]);
		}

		public double getScore(int colour) {
			return (colour == WHITE) ? scoreWhite : scoreBlack;
		}
	}


}
