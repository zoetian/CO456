import java.text.DecimalFormat;
import java.util.*;

abstract class Player {
  protected int myColour;
  protected int numMovesPlayed; 
  protected boolean myKingIsAlive;
  protected int myKingColumn, myKingRow;
  protected boolean myRookIsAlive;
  protected int myRookColumn, myRookRow;
  protected boolean theirKingIsAlive;
  protected int theirKingColumn, theirKingRow;
  protected boolean theirRookIsAlive;
  protected int theirRookColumn, theirRookRow;

  protected final int WHITE = 0, BLACK = 1;

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
  
  public void receiveMatchOutcome(int matchOutcome) {
    throw new RuntimeException("You need to override the method receiveMatchOutcome.");
  }

  public final void update(Board board, Colour myColour_, int numMovesPlayed_) {
    myColour = myColour_.toInt();
    numMovesPlayed = numMovesPlayed_;

    Cell cell = null;

    cell = board.getPieceLocation(new Piece(PieceType.KING, myColour_));
    if (cell == null) {
      myKingIsAlive = false;
      myKingColumn = -1;
      myKingRow = -1;
    } else {
      myKingIsAlive = true;
      myKingColumn = cell.getColumn();
      myKingRow = cell.getRow();
    }

    cell = board.getPieceLocation(new Piece(PieceType.ROOK, myColour_));
    if (cell == null) {
      myRookIsAlive = false;
      myRookColumn = -1;
      myRookRow = -1;
    } else {
      myRookIsAlive = true;
      myRookColumn = cell.getColumn();
      myRookRow = cell.getRow();
    }

    cell = board.getPieceLocation(new Piece(PieceType.KING, myColour_.reverse()));
    if (cell == null) {
      theirKingIsAlive = false;
      theirKingColumn = -1;
      theirKingRow = -1;
    } else {
      theirKingIsAlive = true;
      theirKingColumn = cell.getColumn();
      theirKingRow = cell.getRow();
    }

    cell = board.getPieceLocation(new Piece(PieceType.ROOK, myColour_.reverse()));
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
      boardPosition = new BoardPosition(
          myKingIsAlive, myKingColumn, myKingRow,
          myRookIsAlive, myRookColumn, myRookRow,
          theirKingIsAlive, theirKingColumn, theirKingRow,
          theirRookIsAlive, theirRookColumn, theirRookRow,
          numMovesPlayed,
          myColour
          );
    } else {
      boardPosition = new BoardPosition(
          theirKingIsAlive, theirKingColumn, theirKingRow,
          theirRookIsAlive, theirRookColumn, theirRookRow,
          myKingIsAlive, myKingColumn, myKingRow,
          myRookIsAlive, myRookColumn, myRookRow,
          numMovesPlayed,
          myColour
          );
    }
    return boardPosition;
  }
  
  protected final ArrayList<MoveDescription> getAllPossibleMoves() {
    return this.toBoardPosition().getAllPossibleMoves();
  }

  protected LinkedList<BoardPosition> getAllInitialBoardPositions() {
    LinkedList<BoardPosition> allInitialBoardPositions = new LinkedList<BoardPosition> ();
    for (int whiteKingPosition = 1; whiteKingPosition <= 16; ++whiteKingPosition) {
      for (int whiteRookPosition = 1; whiteRookPosition <= 16; ++whiteRookPosition) {
        for (int blackKingPosition = 1; blackKingPosition <= 16; ++blackKingPosition) {
          for (int blackRookPosition = 1; blackRookPosition <= 16; ++blackRookPosition) {
            HashSet<Integer> S = new HashSet<Integer> ();
            S.add(whiteKingPosition);
            S.add(whiteRookPosition);
            S.add(blackKingPosition);
            S.add(blackRookPosition);
            if (S.size() == 4) {
              for (int myColour = WHITE; myColour <= BLACK; ++myColour) {
                allInitialBoardPositions.add(new BoardPosition(whiteKingPosition, whiteRookPosition, blackKingPosition, blackRookPosition, 0, myColour));
              }
            }
          }
        }
      }
    }
    return allInitialBoardPositions;
  }
  
  /*protected LinkedList<BoardPosition> getAllFinalBoardPositions() {
	    LinkedList<BoardPosition> allFinalBoardPositions = new LinkedList<BoardPosition> ();
	    for (int kingColour = WHITE; kingColour <= BLACK; ++kingColour) {
	    	for (int kingPosition = 0; kingPosition <= 16; ++kingPosition) {
	    		for (int whiteRookPosition = 0; whiteRookPosition <= 16; ++whiteRookPosition) {
	    			if ((whiteRookPosition !=0) && (whiteRookPosition == kingPosition)) {
	    				continue;
	    			}
	    			for (int blackRookPosition = 0; blackRookPosition <= 16; ++blackRookPosition) {
		    			if ((blackRookPosition !=0) && ((blackRookPosition == kingPosition) || (blackRookPosition == whiteRookPosition))) {
		    				continue;
		    			}
	    				HashSet<Integer> S = new HashSet<Integer> ();
	    				S.add(kingPosition);
	    				S.add(whiteRookPosition);
	    				S.add(blackRookPosition);
	    				if () {
	    					for (int myColour = WHITE; myColour <= BLACK; ++myColour) {
	    						allInitialBoardPositions.add(new BoardPosition(whiteKingPosition, whiteRookPosition, blackKingPosition, blackRookPosition, 0, myColour));
	    					}
	    				}
	    			}
	    		}
	    	}
	    }
	    return allFinalBoardPositions;
	  }*/

  public final String getName() {
    String result = getClass().getName();
    return result.substring(result.indexOf('$') + 1);
  }
}
