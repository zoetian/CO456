
public enum Colour {
  BLACK, WHITE;
  
  Colour reverse() {
    return (this == WHITE) ? BLACK : WHITE;
  }
  
  @Override
  public String toString() {
    return (this == WHITE) ? "WHITE" : "BLACK";
  }
  
  public int toInt() {
    return (this == WHITE) ? 0 : 1; 
  }
}
