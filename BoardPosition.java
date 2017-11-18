import java.text.*;
import java.util.*;

public class BoardPosition {
	public int whiteKingPosition, whiteRookPosition, blackKingPosition, blackRookPosition;
	public int numMovesPlayed;
	public int myColour;

	private final int WHITE = 0, BLACK = 1;

	public BoardPosition(int whiteKingPosition_, int whiteRookPosition_, int blackKingPosition_,
			int blackRookPosition_, int numMovesPlayed_, int myColour_) {
		whiteKingPosition = whiteKingPosition_;
		whiteRookPosition = whiteRookPosition_;
		blackKingPosition = blackKingPosition_;
		blackRookPosition = blackRookPosition_;
		numMovesPlayed = numMovesPlayed_;
		myColour = myColour_;
	}

	public BoardPosition(
			boolean whiteKingIsAlive, int whiteKingColumn, int whiteKingRow,
			boolean whiteRookIsAlive, int whiteRookColumn, int whiteRookRow,
			boolean blackKingIsAlive, int blackKingColumn, int blackKingRow,
			boolean blackRookIsAlive, int blackRookColumn, int blackRookRow,
			int numMovesPlayed_, int myColour_) {
		this(getCellId(whiteKingIsAlive, whiteKingColumn, whiteKingRow),
				getCellId(whiteRookIsAlive, whiteRookColumn, whiteRookRow),
				getCellId(blackKingIsAlive, blackKingColumn, blackKingRow),
				getCellId(blackRookIsAlive, blackRookColumn, blackRookRow),
				numMovesPlayed_, myColour_);
	}

	static final int getCellId(boolean isAlive, int column, int row) {
		if (!isAlive) {
			return 0;
		} else {
			return 4 * (column - 1) + row;
		}
	}

	static final int getCellId(int column, int row) {
		return 4 * (column - 1) + row;
	}

	private boolean distinctColours(char piece1, char piece2) {
		return (Character.isLowerCase(piece1) && Character.isUpperCase(piece2)) ||
				(Character.isUpperCase(piece1) && Character.isLowerCase(piece2));
	}

	public BoardPosition(BoardPosition other) {
		whiteKingPosition = other.whiteKingPosition;
		whiteRookPosition = other.whiteRookPosition;
		blackKingPosition = other.blackKingPosition;
		blackRookPosition = other.blackRookPosition;
		numMovesPlayed = other.numMovesPlayed;
		myColour = other.myColour;
	}

	public void clearCell(int cell) {
		if (whiteKingPosition == cell) {
			whiteKingPosition = 0;
		}
		if (whiteRookPosition == cell) {
			whiteRookPosition = 0;
		}
		if (blackKingPosition == cell) {
			blackKingPosition = 0;
		}
		if (blackRookPosition == cell) {
			blackRookPosition = 0;
		}
	}

	public BoardPosition doMove(MoveDescription moveDescription) {
		BoardPosition newBoardPosition = new BoardPosition(this);
		++newBoardPosition.numMovesPlayed;
		int destination = getCellId(moveDescription.getDestinationColumn(), moveDescription.getDestinationRow());
		newBoardPosition.clearCell(destination);
		int currentPlayerColour = (numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		if (currentPlayerColour == WHITE) {
			if (moveDescription.getPieceToMove() == "king") {
				newBoardPosition.whiteKingPosition = destination;
			} else {
				newBoardPosition.whiteRookPosition = destination;
			}
		} else {
			if (moveDescription.getPieceToMove() == "king") {
				newBoardPosition.blackKingPosition = destination;
			} else {
				newBoardPosition.blackRookPosition = destination;
			}
		}
		return newBoardPosition;
	}

	private int getColumn(int cellId) {
		return 1 + (cellId - 1) / 4;
	}

	private int getRow(int cellId) {
		int row = cellId % 4;
		if (row == 0) {
			row = 4;
		}
		return row;
	}

	private void putPieceInBoard(char board[][], int cellId, char piece) {
		if (cellId != 0) {
			int column = getColumn(cellId);
			int row = getRow(cellId);
			board[column][row] = piece;
		}
	}

	ArrayList<MoveDescription> getAllPossibleMoves() {
		char board[][] = new char[5][5];
		for (int column = 1; column <= 4; ++column) {
			for (int row = 1; row <= 4; ++row) {
				board[column][row] = '.';
			}
		}

		putPieceInBoard(board, whiteKingPosition, 'K');
		putPieceInBoard(board, whiteRookPosition, 'R');
		putPieceInBoard(board, blackKingPosition, 'k');
		putPieceInBoard(board, blackRookPosition, 'r');

		ArrayList<MoveDescription> L = new ArrayList<MoveDescription> ();
		int currentPlayerColour = (numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		for (int column = 1; column <= 4; ++column) {
			for (int row = 1; row <= 4; ++row) {
				if ((board[column][row] == 'K' && currentPlayerColour == WHITE) ||
						(board[column][row] == 'k' && currentPlayerColour == BLACK)) {
					// this cell has a king that can be moved
					int[][] possibleDirections = new int[][] {{-1, -1}, {-1, 0}, {-1, +1}, {0, -1}, {0, +1}, {+1, -1}, {+1, 0}, {+1, +1}};
					for (int[] direction : possibleDirections) {
						int column_ = column + direction[0];
						int row_ = row + direction[1];
						if (1 <= column_ && column_ <= 4 && 1 <= row_ && row_ <= 4 && 
								(board[column_][row_] == '.' || distinctColours(board[column][row], board[column_][row_]))) {
							L.add(new MoveDescription("king", column_, row_));
						}
					}
				}
				else if ((board[column][row] == 'R' && currentPlayerColour == WHITE) ||
						(board[column][row] == 'r' && currentPlayerColour == BLACK)) {
					// this cell has a rook that can be moved
					int[][] possibleDirections = new int[][] {{-1, 0}, {+1, 0}, {0, -1}, {0, +1}};
					for (int[] direction : possibleDirections) {
						int column_ = column + direction[0];
						int row_ = row + direction[1];
						while (1 <= column_ && column_ <= 4 && 1 <= row_ && row_ <= 4 && 
								(board[column_][row_] == '.' || distinctColours(board[column][row], board[column_][row_]))) {
							L.add(new MoveDescription("rook", column_, row_));
							if (board[column_][row_] != '.') {
								break;
							} else  {
								column_ += direction[0];
								row_ += direction[1];
							}
						}
					}
				}
			}
		}
		return L;
	}

	public String toString() {
		DecimalFormat decimalFormat = new DecimalFormat("00");
		String S = decimalFormat.format(whiteKingPosition) + decimalFormat.format(whiteRookPosition) + 
				decimalFormat.format(blackKingPosition) + decimalFormat.format(blackRookPosition);
		S += decimalFormat.format(numMovesPlayed);
		S += myColour;
		return S;
	}

	public int toInt() {
		//Intention: to save memory, use this as an array index to store data.
		//Format: We write an integer bit by bit to represent a board position. Bit i means bit representing 2^i.
		//  With all pieces on the board, the following bits correspond to a piece position, 0,...,15:
		//  0-3 for white king, 4-7 for white rook, 8-11 for black king, 12-15 for black rook.
		//	Bit 16 indicates whether black rook is dead;
		//  if so bits 12-15 are set to the smallest value not corresponding to another piece.
		//	if black king is dead, bits 8-11 are set to match bits 12-15,
		//  if white rook is dead, bits 4-7 are set to match bits 8-11,
		//  if white king is dead, bits 0-3 are set to match bits 4-7.
		//  Bits 17-21 are used to indicate the number of moves left
		// 	Other bits are left 0; note that toInt() is meant to be used as an array index,
		//so we want an efficient encoding which does not skip too many indices.

		int wkp, wrp, bkp, brp; //repurposed versions of whiteKingPosition, etc.
		int brdead=0; //1 indicates black rook is dead
		
		if (blackRookPosition==0) {
			brdead=1;
			int i=1;
			while (i==whiteKingPosition || i==whiteRookPosition || i==blackKingPosition) {
				i++;
			}
			brp=i-1;
		} else {
			brp=blackRookPosition-1;
		}
		if (blackKingPosition==0) {
			bkp=brp;
		} else {
			bkp=blackKingPosition-1;
		}
		if (whiteRookPosition==0) {
			wrp=bkp;
		} else {
			wrp=whiteRookPosition-1;
		}
		if (whiteKingPosition==0) {
			wkp=wrp;
		} else {
			wkp=whiteKingPosition-1;
		}
		return (wkp)+(wrp<<4)+(bkp<<8)+(brp<<12)+(brdead<<16)+(numMovesPlayed<<17)+(myColour<<22);
	}

	public BoardPosition(int pos) {//, int myColour_) {
		//This is a constructor that is "inverse" of toInt().
		int wkp=pos-((pos>>4)<<4);
		int wrp=(pos>>4)-((pos>>8)<<4);
		int bkp=(pos>>8)-((pos>>12)<<4);
		int brp=(pos>>12)-((pos>>16)<<4);
		int brdead=(pos>>16)-((pos>>17)<<1);

		if (brdead==1) {
			blackRookPosition = 0;
		} else {
			blackRookPosition=brp+1;
		}
		if (bkp==brp) {
			blackKingPosition = 0;
		} else {
			blackKingPosition=bkp+1;
		}
		if (wrp==bkp) {
			whiteRookPosition = 0;
		} else {
			whiteRookPosition=wrp+1;
		}
		if (wkp==wrp) {
			whiteKingPosition = 0;
		} else {
			whiteKingPosition=wkp+1;
		}
		numMovesPlayed = pos>>17-((pos>>22)<<5);
		myColour=pos>>22;
	}

}