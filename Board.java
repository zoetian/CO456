import java.io.*;
import java.util.*;

public class Board {

  private HashMap<Cell, Piece> pieceInCell;
  private HashMap<Piece, Cell> pieceLocation;

  public Board(Cell[] startingPositions) {
    pieceInCell = new HashMap<Cell, Piece> ();
    pieceLocation = new HashMap<Piece, Cell> ();
    addPiece(startingPositions[0], new Piece(PieceType.KING, Colour.WHITE));
    addPiece(startingPositions[1], new Piece(PieceType.ROOK, Colour.WHITE));
    addPiece(startingPositions[2], new Piece(PieceType.KING, Colour.BLACK));
    addPiece(startingPositions[3], new Piece(PieceType.ROOK, Colour.BLACK));
  }

  private Piece getPieceInCell(Cell cell) {
    return pieceInCell.get(cell);
  }

  public Cell getPieceLocation(Piece piece) {
    return pieceLocation.get(piece);
  }
  
  public boolean containsPiece(Piece piece) {
    return pieceLocation.containsKey(piece);
  }
  
  private boolean cellIsOccupied(Cell cell) {
    return pieceInCell.containsKey(cell);
  }

  private void addPiece(Cell cell, Piece piece) {
    if (!cell.isValid() || cellIsOccupied(cell) || containsPiece(piece)) {
      throw new RuntimeException("Invalid parameters.");
    }
    pieceInCell.put(cell, piece);
    pieceLocation.put(piece, cell);
  }

  // check for pieces strictly between source and destination

  private boolean moveIsBlocked(Cell source, Cell destination) {  
    for (Cell cell : pieceInCell.keySet()) {
      if (cell.blocks(source, destination)) {
        return true;
      }
    }
    return false;
  }

  private void clearCell(Cell cell) {
    Piece piece = getPieceInCell(cell);
    if (piece == null) {
      throw new RuntimeException("Attempted to clear empty cell.");
    }
    pieceInCell.remove(cell); 
    pieceLocation.remove(piece);
  }
  
  public MatchStatus performMove(Move move, Colour playerColour, MatchStatus matchStatus) {
    if (move == null) {
      throw new RuntimeException("Null move.");
    }
    
    Piece piece = new Piece(move.getPieceType(), playerColour);

    Cell source = getPieceLocation(piece);

    if (source == null) {
      throw new RuntimeException("Asked to move a nonexisting piece.");
    }

    Cell destination = move.getDestination();

    if (!piece.moveIsValid(source, destination)) {
      throw new RuntimeException("This move is invalid for this type of piece.");
    }

    if (moveIsBlocked(source, destination)) {
      throw new RuntimeException("Move is being blocked by another piece.");
    }

    // get piece in destination cell
    Piece pieceInDestination = getPieceInCell(destination);
    
    // update game status
    
    if (matchStatus == MatchStatus.LAST_MOVE) {
      matchStatus = MatchStatus.OVER;
    } else {
      if (pieceInDestination != null && pieceInDestination.getType() == PieceType.KING) {
        if (getPieceLocation(new Piece(PieceType.ROOK, pieceInDestination.getColour())) != null) {
          matchStatus = MatchStatus.LAST_MOVE;
        } else {
          matchStatus = MatchStatus.OVER;
        }
      }
    }
    
    // do capture
    
    if (pieceInDestination != null) {
      if (pieceInDestination.getColour() == playerColour) {
        throw new RuntimeException("Attempted to move piece to a cell occupied by a piece of the same player.");
      }      
      // clear destination cell
      clearCell(destination);
    }

    // clear source cell
    clearCell(source);
    
    addPiece(destination, new Piece(move.getPieceType(), playerColour));
    
    return matchStatus;
  }
  
  public void print(PrintWriter log) {
    log.println("\\begin{center}");
    log.println("\\setchessboard{");
    log.println("labelbottomformat=\\arabic{filelabel},");
    log.println("showmover=false,");
    log.println("maxfield=d4,");
    
    String s = "";
    boolean firstPiece = true;
    for (Map.Entry<Cell, Piece> entry : pieceInCell.entrySet()) {
      Cell cell = entry.getKey();
      Piece piece = entry.getValue();
      if (!firstPiece) {
        s += ", ";
      } else {
        firstPiece = false;
      }
      s += piece.toChar();
      s += (char) ('a' + cell.getColumn() - 1);
      s += cell.getRow();
    }
    log.println("setpieces={" + s + "}}");
    log.println("\\newchessgame");
    log.println("\\chessboard");
    log.println("\\end{center}");
    log.println("\\noindent\\rule{\\textwidth}{1pt}");
  }
}
