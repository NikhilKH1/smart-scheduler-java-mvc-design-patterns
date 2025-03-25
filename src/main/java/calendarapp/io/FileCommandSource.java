package calendarapp.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileCommandSource implements ICommandSource {
  private final BufferedReader reader;

  public FileCommandSource(String filePath) throws IOException {
    this.reader = new BufferedReader(new FileReader(filePath));
  }

  @Override
  public String getNextCommand() {
    try {
      return reader.readLine();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
    }
  }
}