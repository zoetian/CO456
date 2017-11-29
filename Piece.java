

public class Piece {
	private PieceType type;
	private Colour colour;

	public Piece(PieceType type, Colour colour) {
		this.type = type;
		this.colour = colour;
	}

	public PieceType getType() {
		return type;
	}

	public Colour getColour() {
		return colour;
	}

	public boolean moveIsValid(Cell source, Cell destination) {
		if(!source.isValid() || !destination.isValid()) {
			return false;
		}
		int dc = Math.abs(destination.getColumn() - source.getColumn());
		int dr = Math.abs(destination.getRow() - source.getRow());
		if (type == PieceType.KING) {
			return (dc != 0 || dr != 0) && dc <= 1 && dr <= 1;
		} else {
			// type == PieceType.ROOK
			return (dc == 0 && dr > 0) || (dc > 0 && dr == 0);
		}
	}

	public char toChar() {
		if (type == PieceType.KING) {
			return (colour == Colour.WHITE) ? 'K' : 'k';
		} else {
			return (colour == Colour.WHITE) ? 'R' : 'r';
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}
		Piece piece_ = (Piece) obj;
		return type == piece_.getType() && colour == piece_.getColour();
	}

	@Override
	public int hashCode() {
		return 10 * (type == PieceType.KING ? 0 : 1) + (colour == Colour.WHITE ? 0 : 1);
	}
}
