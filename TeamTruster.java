import java.util.*;

public class TeamTruster extends Player {
	//A Node represents a board position, a best move and payoffs; this is according to Realist assumptions.
	private Node[] results; //Array of Nodes indexed by an integer representing a board position. Memory intensive!
	public int[] bestMoveInts; //A best move node for each position, encoded with ``bit twiddling'' to save memory.
	int maxNumMoves; //Called GameLimit in the project specs
	private Random rand;

	public TeamTruster(int maxNumMoves_) {
		maxNumMoves = maxNumMoves_;
		LinkedList<BoardPosition> allInitialBoardPositions = getAllInitialBoardPositions();

		rand = new Random(0); //For choosing random moves...

		//BoardPosition.toInt() will be used for indices in the following arrays:
		results = new Node[1<<23];
		bestMoveInts = new int[1<<23];
		//Those indices will be less than 1<<23; that is 1 shifted left by 23 bits, which gives 2^23.

		for (BoardPosition boardPosition : allInitialBoardPositions) {
			computeBestMove(boardPosition);
		}

		results=null; //De-allocate results, so garbage collector will free up that memory.

	}

	public void prepareForSeries() {
	}

	public void prepareForMatch() {
	}

	public void receiveMatchOutcome(int matchOutcome) {
	}

	public MoveDescription chooseMove() {
		BoardPosition boardPosition = toBoardPosition();
		return (new Node(bestMoveInts[boardPosition.toInt()])).bestMove;
	}

	private Node computeBestMove(BoardPosition boardPosition) {
		int currentPositionInt = boardPosition.toInt();

		Node savedResult = results[currentPositionInt];
		if (savedResult != null) {
			return savedResult;
		}

		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;

		Node ret = null;

		int[] payoffsWhiteWins = new int[] {9,6};
		int[] payoffsBlackWins = new int[] {6,9};
		int[] payoffsTie = new int[] {10, 10};
		int[] payoffsDraw = new int[] {5, 5};

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
			ArrayList<Node> allBestNodes = new ArrayList<Node> ();
			int bestScore=0;
			for (MoveDescription moveDescription : allPossibleMoves) {
				BoardPosition newBoardPosition = boardPosition.doMove(moveDescription);
				Node node = computeBestMove(newBoardPosition);
				if (allBestNodes.size()==0) {
					bestScore=node.getScore(currentPlayerColour);
					allBestNodes.add(new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK)));
				} else if (node.getScore(currentPlayerColour) == bestScore) {
					allBestNodes.add(new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK)));
				} else if (node.getScore(currentPlayerColour) > bestScore) { 
					bestScore=node.getScore(currentPlayerColour);
					allBestNodes = new ArrayList<Node> ();
					allBestNodes.add(new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK)));
				}
			}
			ret=allBestNodes.get(rand.nextInt(allBestNodes.size()));
		}

		//Add result to array!
		results[currentPositionInt]=ret;
		if (ret.bestMove != null) {
			bestMoveInts[currentPositionInt]=ret.toInt();
		}
		return ret;
	}


	private class Node {
		public MoveDescription bestMove;
		public int scoreWhite, scoreBlack;

		public Node(MoveDescription bestMove_, int scoreWhite_, int scoreBlack_) {
			bestMove = bestMove_;
			scoreWhite = scoreWhite_;
			scoreBlack = scoreBlack_;
		}

		public Node(MoveDescription bestMove_, int[] score_) {
			this(bestMove_, score_[WHITE], score_[BLACK]);
		}

		public int getScore(int colour) {
			return (colour == WHITE) ? scoreWhite : scoreBlack;
		}

		public int toInt() {//encode a node as a 14 bit integer.
			//bits 0-3 for scoreBlack, 4-7 for scoreWhite, 8-12 for bestMove, 13 indicates that bestMove is null.
			if (bestMove != null) {
				return (bestMove.toInt() << 8)// 5-bit integer
						+ (scoreWhite << 4)// 4-bit integer
						+ (scoreBlack);// 4-bit integer
			} else {
				return (1<<13) // 5-bit integer
						+ (scoreWhite << 4)// 4-bit integer
						+ (scoreBlack);// 4-bit integer
			}
		}

		public Node(int nodeInt) {// decode an integer into a node.
			if ((nodeInt>>13)==1) {
				bestMove = null;
			} else {
				bestMove = new MoveDescription(nodeInt >> 8);
			}
			scoreWhite = (nodeInt - ((nodeInt >> 8) << 8)) >> 4;
			scoreBlack = nodeInt - ((nodeInt >> 4) << 4);
		}
	}
}
