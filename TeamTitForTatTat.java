public class TeamTitForTatTat extends TeamTitForTat {
	
	public TeamTitForTatTat(int maxNumMoves) {
		super(maxNumMoves);
		this.BETRAYAL_DELTA=2;
	}
	
	public void prepareForSeries() {
		trust = 0;
	}
}
