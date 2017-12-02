import java.text.*;
import java.util.*;

public class BoardPosition {

	public int whiteKingPosition, whiteRookPosition, blackKingPosition, blackRookPosition;
	public int numMovesPlayed;
	public int myColour;

	private final static int WHITE = 0, BLACK = 1;

	/* ----- CONSTRUCTORS ----- */

	public BoardPosition(int whiteKingPosition, int whiteRookPosition, int blackKingPosition, int blackRookPosition,
			int numMovesPlayed, int myColour) {
		this.whiteKingPosition = whiteKingPosition;
		this.whiteRookPosition = whiteRookPosition;
		this.blackKingPosition = blackKingPosition;
		this.blackRookPosition = blackRookPosition;
		this.numMovesPlayed = numMovesPlayed;
		this.myColour = myColour;
	}

	public BoardPosition(boolean whiteKingIsAlive, int whiteKingColumn, int whiteKingRow, boolean whiteRookIsAlive,
			int whiteRookColumn, int whiteRookRow, boolean blackKingIsAlive, int blackKingColumn, int blackKingRow,
			boolean blackRookIsAlive, int blackRookColumn, int blackRookRow, int numMovesPlayed, int myColour) {
		this(getCellId(whiteKingIsAlive, whiteKingColumn, whiteKingRow),
				getCellId(whiteRookIsAlive, whiteRookColumn, whiteRookRow),
				getCellId(blackKingIsAlive, blackKingColumn, blackKingRow),
				getCellId(blackRookIsAlive, blackRookColumn, blackRookRow), numMovesPlayed, myColour);
	}

	public BoardPosition(BoardPosition other) {
		whiteKingPosition = other.whiteKingPosition;
		whiteRookPosition = other.whiteRookPosition;
		blackKingPosition = other.blackKingPosition;
		blackRookPosition = other.blackRookPosition;
		numMovesPlayed = other.numMovesPlayed;
		myColour = other.myColour;
	}

	public BoardPosition(int pos) { // This is recovers a BoardPosition from the
		// integer output to toInt().
		int wkp = pos - ((pos >> 4) << 4);
		int wrp = (pos >> 4) - ((pos >> 8) << 4);
		int bkp = (pos >> 8) - ((pos >> 12) << 4);
		int brp = (pos >> 12) - ((pos >> 16) << 4);
		int brdead = (pos >> 16) - ((pos >> 17) << 1);

		if (brdead == 1) {
			blackRookPosition = 0;
		} else {
			blackRookPosition = brp + 1;
		}
		if (bkp == brp) {
			blackKingPosition = 0;
		} else {
			blackKingPosition = bkp + 1;
		}
		if (wrp == bkp) {
			whiteRookPosition = 0;
		} else {
			whiteRookPosition = wrp + 1;
		}
		if (wkp == wrp) {
			whiteKingPosition = 0;
		} else {
			whiteKingPosition = wkp + 1;
		}
		numMovesPlayed = (pos >> 17) - ((pos >> 22) << 5);
		myColour = pos >> 22;
	}

	/* ----- REPRESENTATIONS ----- */

	public static final int MAX_INT = 1 << 23; // Max size of
	// BoardPosition.toInt()

	public int toInt() {
		// Intention: to save memory, use this as an array index to store data.
		// Format: bits 2^i for i=0,1,... in an integer represent a board
		// position.
		// First suppose all pieces are on the board. Then
		// bits 0-3: wkp=whiteKingPosition-1
		// bits 4-7: wrp=whiteRookPosition-1
		// bits 8-11: bkp=blackKingPosition-1
		// bits 12-15: brp=blackRookPosition-1
		// For captured pieces:
		// bit 16 indicates that the black rook is dead.
		// Then brp is set to to the smallest empty square 000i.
		// If black king is dead, set bkp=brp.
		// Similarly set wrp=bkp (wkp=wrp) if the white rook (white king) is
		// dead.
		// bits 17-21 are used to indicate the number of moves played.
		// bit 22 is for myColour
		// Example: 0 00100 0 0000 0001 0010 0011 means
		// myColour=0, numMovesPlayed=4, [black rook is alive,]
		// blackRookPosition=1, blackKingPosition=2, whiteRookPosition=3,
		// whiteKingPosition=4
		// Example: 1 10011 1 0000 0001 0001 0011 means
		// myColour=1, numMovesPlayed=19, [black rook is dead,]
		// blackRookPosition=0, blackKingPosition=2, whiteRookPosition=0,
		// whiteKingPosition=4

		int wkp, wrp, bkp, brp; // usually whiteKingPosition-1, etc.
		int brdead = 0; // 1 indicates black rook is dead

		if (blackRookPosition == 0) {
			brdead = 1;
			int i = 1;
			while (i == whiteKingPosition || i == whiteRookPosition || i == blackKingPosition) {
				i++;
			}
			brp = i - 1;
		} else {
			brp = blackRookPosition - 1;
		}
		if (blackKingPosition == 0) {
			bkp = brp;
		} else {
			bkp = blackKingPosition - 1;
		}
		if (whiteRookPosition == 0) {
			wrp = bkp;
		} else {
			wrp = whiteRookPosition - 1;
		}
		if (whiteKingPosition == 0) {
			wkp = wrp;
		} else {
			wkp = whiteKingPosition - 1;
		}
		return (wkp) + (wrp << 4) + (bkp << 8) + (brp << 12) + (brdead << 16) + (numMovesPlayed << 17)
				+ (myColour << 22);
	}

	public static boolean isValidInt(int bpInt) {
		BoardPosition bp = new BoardPosition(bpInt);
		if (bp.toInt() != bpInt || !(bp.isValidBoard())) {
			return false;
		}
		;
		return true;
	}

	public boolean isValidBoard() {
		if (whiteKingPosition != 0 && (whiteKingPosition == whiteRookPosition || whiteKingPosition == blackKingPosition
				|| whiteKingPosition == blackRookPosition)) {
			return false;
		}
		if (whiteRookPosition != 0
				&& (whiteRookPosition == blackKingPosition || whiteRookPosition == blackRookPosition)) {
			return false;
		}
		if (blackKingPosition != 0 && blackKingPosition == blackRookPosition) {
			return false;
		}
		return true;
	}

	@Deprecated
	public String toString() { // Representation as a string. I don't think this
		// is used anymore.
		DecimalFormat decimalFormat = new DecimalFormat("00");
		String S = decimalFormat.format(whiteKingPosition) + decimalFormat.format(whiteRookPosition)
		+ decimalFormat.format(blackKingPosition) + decimalFormat.format(blackRookPosition);
		S += decimalFormat.format(numMovesPlayed);
		S += myColour;
		return S;
	}

	public void print() {
		char[][] board = new char[5][5];
		for (int column = 1; column <= 4; ++column) {
			for (int row = 1; row <= 4; ++row) {
				board[column][row] = '.';
			}
		}
		if (whiteKingPosition != 0) {
			board[getColumn(whiteKingPosition)][getRow(whiteKingPosition)]='K';
		}
		if (whiteRookPosition != 0) {
			board[getColumn(whiteRookPosition)][getRow(whiteRookPosition)]='R';
		}
		if (blackKingPosition != 0) {
			board[getColumn(blackKingPosition)][getRow(blackKingPosition)]='k';
		}
		if (blackRookPosition != 0) {
			board[getColumn(blackRookPosition)][getRow(blackRookPosition)]='r';
		}
		System.out.println();
		
		System.out.println("myColour="+myColour+" numMovesPlayed="+numMovesPlayed+" maxNumMoves="+Parameters.MAX_NUM_MOVES);
		
		System.out.println();

		for (int row = 4; row >= 1; --row) {
			System.out.println("---------");

			for (int column = 1; column <= 4; ++column) {
				System.out.print("|");

				System.out.print(board[column][row]);

			}
			System.out.print("|");
			System.out.println();
		}
		System.out.println("---------");
		System.out.println();
	}

	/* ----- UTILITIES ----- */

	public static final int getCellId(boolean isAlive, int column, int row) {
		if (!isAlive) {
			return 0;
		} else {
			return 4 * (column - 1) + row;
		}
	}

	static final int getCellId(int column, int row) {
		return 4 * (column - 1) + row;
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

	private boolean distinctColours(char piece1, char piece2) {
		return (Character.isLowerCase(piece1) && Character.isUpperCase(piece2))
				|| (Character.isUpperCase(piece1) && Character.isLowerCase(piece2));
	}

	private void putPieceInBoard(char board[][], int cellId, char piece) {
		if (cellId != 0) {
			int column = getColumn(cellId);
			int row = getRow(cellId);
			board[column][row] = piece;
		}
	}

	/* ----- USEFUL METHODS ----- */

	public void clearCell(int cell) { // Should this be private? Not changing
		// this now.
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

	public BoardPosition doMove(MoveDescription moveDescription) { // used by TeamRational,
		// etc.
		BoardPosition newBoardPosition = new BoardPosition(this);
		++newBoardPosition.numMovesPlayed;
		int destination = getCellId(moveDescription.getDestinationColumn(), moveDescription.getDestinationRow());
		newBoardPosition.clearCell(destination);
		int currentPlayerColour = (numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		if (currentPlayerColour == WHITE) {
			if (moveDescription.getPieceToMove().equals("king")) {
				newBoardPosition.whiteKingPosition = destination;
			} else {
				newBoardPosition.whiteRookPosition = destination;
			}
		} else {
			if (moveDescription.getPieceToMove().equals("king")) {
				newBoardPosition.blackKingPosition = destination;
			} else {
				newBoardPosition.blackRookPosition = destination;
			}
		}
		return newBoardPosition;
	}

	public ArrayList<MoveDescription> getAllPossibleMoves() {
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

		ArrayList<MoveDescription> L = new ArrayList<MoveDescription>();
		int currentPlayerColour = (numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		for (int column = 1; column <= 4; ++column) {
			for (int row = 1; row <= 4; ++row) {
				if ((board[column][row] == 'K' && currentPlayerColour == WHITE)
						|| (board[column][row] == 'k' && currentPlayerColour == BLACK)) {
					// this cell has a king that can be moved
					int[][] possibleDirections = new int[][] { { -1, -1 }, { -1, 0 }, { -1, +1 }, { 0, -1 }, { 0, +1 },
						{ +1, -1 }, { +1, 0 }, { +1, +1 } };
						for (int[] direction : possibleDirections) {
							int column_ = column + direction[0];
							int row_ = row + direction[1];
							if (1 <= column_ && column_ <= 4 && 1 <= row_ && row_ <= 4 && (board[column_][row_] == '.'
									|| distinctColours(board[column][row], board[column_][row_]))) {
								L.add(new MoveDescription("king", column_, row_));
							}
						}
				} else if ((board[column][row] == 'R' && currentPlayerColour == WHITE)
						|| (board[column][row] == 'r' && currentPlayerColour == BLACK)) {
					// this cell has a rook that can be moved
					int[][] possibleDirections = new int[][] { { -1, 0 }, { +1, 0 }, { 0, -1 }, { 0, +1 } };
					for (int[] direction : possibleDirections) {
						int column_ = column + direction[0];
						int row_ = row + direction[1];
						while (1 <= column_ && column_ <= 4 && 1 <= row_ && row_ <= 4 && (board[column_][row_] == '.'
								|| distinctColours(board[column][row], board[column_][row_]))) {
							L.add(new MoveDescription("rook", column_, row_));
							if (board[column_][row_] != '.') {
								break;
							} else {
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

	public static LinkedList<BoardPosition> getAllInitialBoardPositions() {
		LinkedList<BoardPosition> allInitialBoardPositions = new LinkedList<BoardPosition>();
		for (int whiteKingPosition = 1; whiteKingPosition <= 16; ++whiteKingPosition) {
			for (int whiteRookPosition = 1; whiteRookPosition <= 16; ++whiteRookPosition) {
				for (int blackKingPosition = 1; blackKingPosition <= 16; ++blackKingPosition) {
					for (int blackRookPosition = 1; blackRookPosition <= 16; ++blackRookPosition) {
						HashSet<Integer> S = new HashSet<Integer>();
						S.add(whiteKingPosition);
						S.add(whiteRookPosition);
						S.add(blackKingPosition);
						S.add(blackRookPosition);
						if (S.size() == 4) {
							for (int myColour = 0; myColour <= 1; ++myColour) {
								allInitialBoardPositions.add(new BoardPosition(whiteKingPosition, whiteRookPosition,
										blackKingPosition, blackRookPosition, 0, myColour));
							}
						}
					}
				}
			}
		}
		return allInitialBoardPositions;
	}
}
