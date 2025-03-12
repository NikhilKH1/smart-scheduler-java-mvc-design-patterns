package calendarapp.controller;

public class PropertiesResult {
  protected String description;
  protected String location;
  protected boolean isPublic;
  protected int index;

  public PropertiesResult() {
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.index = index;
  }

  public String getDescription() { return description; }
  public String getLocation() { return location; }
  public boolean isPublic() { return isPublic; }
  public int getIndex() { return index; }
}
