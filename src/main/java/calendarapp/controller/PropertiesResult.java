package calendarapp.controller;

/**
 * Represents the properties of an event.
 * This class holds information about the description, location,
 * visibility (public or not), and an index value for identifying
 * the event in a list or sequence.
 */
public class PropertiesResult {

  protected String description;
  protected String location;
  protected boolean isPublic;
  protected int index;

  /**
   * Default constructor for initializing the PropertiesResult instance.
   * Initializes the description, location, isPublic, and index with default values.
   */
  public PropertiesResult() {
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.index = index;
  }

  /**
   * Gets the description of the event.
   *
   * @return the description of the event as a string.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the location of the event as a string.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks whether the event is public.
   *
   * @return true if the event is public, false if private.
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Gets the index of the event.
   *
   * @return the index of the event, used to identify the event in a list or sequence.
   */
  public int getIndex() {
    return index;
  }
}
