

import java.util.*;

public abstract class Player {
	public int myColour;
	public int numMovesPlayed;
	public boolean myKingIsAlive;
	public int myKingColumn, myKingRow;
	public boolean myRookIsAlive;
	public int myRookColumn, myRookRow;
	public boolean theirKingIsAlive;
	public int theirKingColumn, theirKingRow;
	public boolean theirRookIsAlive;
	public int theirRookColumn, theirRookRow;
	
	private String name = getClass().getSimpleName();

	protected final static int WHITE = 0;
	protected final static int BLACK = 1;

	public Player() {
	}

	public void prepareForSeries() {
		throw new RuntimeException("You need to override the method prepareForSeries.");
	}

	public void prepareForMatch() {
		throw new RuntimeException("You need to override the method prepareForMatch.");
	}

	public MoveDescription chooseMove() {
		throw new RuntimeException("You need to override the method ChooseMove.");
	}

	public int outcomeToPayoff(int matchOutcome) {

		if (matchOutcome == 1) { // indicates a win
			return 3;
		} else if (matchOutcome == 2) { // indicates a loss
			return 0;
		} else if (matchOutcome == 3) { // indicates a tie
			return 2;
		} else if (matchOutcome == 4) { // indicates a draw
			return 1;
		}
		
		return -1;
	}
	
	public void receiveMatchOutcome(int matchOutcome) {
		// throw new RuntimeException("You need to override the method
		// receiveMatchOutcome.");
	}

	public final void update(Board board, Colour myColour, int numMovesPlayed) {
		this.myColour = myColour.toInt();
		this.numMovesPlayed = numMovesPlayed;

		Cell cell = null;

		cell = board.getPieceLocation(new Piece(PieceType.KING, myColour));
		if (cell == null) {
			myKingIsAlive = false;
			myKingColumn = -1;
			myKingRow = -1;
		} else {
			myKingIsAlive = true;
			myKingColumn = cell.getColumn();
			myKingRow = cell.getRow();
		}

		cell = board.getPieceLocation(new Piece(PieceType.ROOK, myColour));
		if (cell == null) {
			myRookIsAlive = false;
			myRookColumn = -1;
			myRookRow = -1;
		} else {
			myRookIsAlive = true;
			myRookColumn = cell.getColumn();
			myRookRow = cell.getRow();
		}

		cell = board.getPieceLocation(new Piece(PieceType.KING, myColour.reverse()));
		if (cell == null) {
			theirKingIsAlive = false;
			theirKingColumn = -1;
			theirKingRow = -1;
		} else {
			theirKingIsAlive = true;
			theirKingColumn = cell.getColumn();
			theirKingRow = cell.getRow();
		}

		cell = board.getPieceLocation(new Piece(PieceType.ROOK, myColour.reverse()));
		if (cell == null) {
			theirRookIsAlive = false;
			theirRookColumn = -1;
			theirRookRow = -1;
		} else {
			theirRookIsAlive = true;
			theirRookColumn = cell.getColumn();
			theirRookRow = cell.getRow();
		}
	}

	public final Colour getColour() {
		return myColour == WHITE ? Colour.WHITE : Colour.BLACK;
	}

	protected final BoardPosition toBoardPosition() {
		BoardPosition boardPosition = null;
		if (myColour == WHITE) {
			boardPosition = new BoardPosition(myKingIsAlive, myKingColumn, myKingRow, myRookIsAlive, myRookColumn,
					myRookRow, theirKingIsAlive, theirKingColumn, theirKingRow, theirRookIsAlive, theirRookColumn,
					theirRookRow, numMovesPlayed, myColour);
		} else {
			boardPosition = new BoardPosition(theirKingIsAlive, theirKingColumn, theirKingRow, theirRookIsAlive,
					theirRookColumn, theirRookRow, myKingIsAlive, myKingColumn, myKingRow, myRookIsAlive, myRookColumn,
					myRookRow, numMovesPlayed, myColour);
		}
		return boardPosition;
	}

	protected final ArrayList<MoveDescription> getAllPossibleMoves() {
		return this.toBoardPosition().getAllPossibleMoves();
	}

	@Deprecated //Use BoardPosition.getAllInitialBoardPositions()
	protected LinkedList<BoardPosition> getAllInitialBoardPositions() {
		return BoardPosition.getAllInitialBoardPositions();
	}

	public final String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
