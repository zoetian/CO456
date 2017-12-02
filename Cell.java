public class Cell {
	private int column, row;

	public Cell(int column, int row) {
		this.column = column;
		this.row = row;
	}
	
	/*public Cell(Cell other) {
		this.column = other.column;
		this.row = other.row;
	}*/

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}
		Cell cell = (Cell) obj;
		return column == cell.getColumn() && row == cell.getRow();
	}

	@Override
	public int hashCode() {
		return column * (Parameters.NUM_ROWS) + row;
	}

	public boolean isValid() {
		return 1 <= column && column <= Parameters.NUM_COLUMNS && 1 <= row && row <= Parameters.NUM_ROWS;
	}

	// checks if this cell blocks a horizontal or vertical move
	public boolean blocks(Cell source, Cell destination) {
		if (source.getColumn() == destination.getColumn() && column == source.getColumn()) {
			int minRow = Math.min(source.getRow(), destination.getRow());
			int maxRow = Math.max(source.getRow(), destination.getRow());
			return minRow < row && row < maxRow;
		} else if (source.getRow() == destination.getRow() && row == source.getRow()) {
			int minColumn = Math.min(source.getColumn(), destination.getColumn());
			int maxColumn = Math.max(source.getColumn(), destination.getColumn());
			return minColumn < column && column < maxColumn;
		}
		return false;
	}
}
