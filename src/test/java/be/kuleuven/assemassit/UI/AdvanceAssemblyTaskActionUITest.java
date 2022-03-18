package be.kuleuven.assemassit.UI;

import org.junit.jupiter.api.Test;
import textuitester.TextUITestScriptRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvanceAssemblyTaskActionUITest {

  /**
   * The withHour has to be change every hour to work because of the error in the estimatedTime algorithm
   */
  @Test
  public void AdvanceAssemblyTasksUseCase() throws IOException {

    LocalDateTime localDateTimeNow = LocalDateTime.now();
    LocalDateTime actualDate = LocalDateTime.now();

    if (localDateTimeNow.getHour() < 6) {
      actualDate = actualDate.withHour(9);
    }
    if (localDateTimeNow.getHour() >= 6 && localDateTimeNow.getHour() <= 19) {
      actualDate = actualDate.plusHours(3);
    }
    if (localDateTimeNow.getHour() > 19) {
      //The withHour has to be change every hour to work because of the error in the estimatedTime algorithm
      actualDate = actualDate.plusDays(1).withHour(13).withMinute(0);
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' H:mm");
    String str = Files.readString(Path.of("src/test/resources/AdvanceAssemblyTaskActionUITest.txt"));
    Pattern p = Pattern.compile("%date%", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(str);
    String result = m.replaceAll(actualDate.format(formatter));

    InputStream is = new ByteArrayInputStream(result.getBytes());

    TextUITestScriptRunner.runTestScript(is);
  }

}

