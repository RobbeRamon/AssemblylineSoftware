package be.kuleuven.assemassit.UI;

public class OrderNewCarActionUITest {

//  @Test
//  public void OrderNewCarUseCaseTest() throws IOException {
//
//    LocalDateTime localDateTimeNow = (new CustomTime().customLocalDateTimeNow());
//    LocalDateTime actualDate = (new CustomTime().customLocalDateTimeNow());
//
//    if (localDateTimeNow.getHour() < 6) {
//      actualDate = actualDate.withHour(9);
//    }
//    if (localDateTimeNow.getHour() >= 6 && localDateTimeNow.getHour() <= 19) {
//      actualDate = actualDate.plusHours(3);
//    }
//    if (localDateTimeNow.getHour() > 19) {
//      actualDate = actualDate.plusDays(1).withHour(14).withMinute(0);
//    }
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' H:mm");
//    String str = Files.readString(Path.of("src/test/resources/OrderNewCarActionUITest.txt"));
//    Pattern p = Pattern.compile("%date%", Pattern.CASE_INSENSITIVE);
//    Matcher m = p.matcher(str);
//    String result = m.replaceAll(actualDate.format(formatter));
//
//    InputStream is = new ByteArrayInputStream(result.getBytes());
//
//    TextUITestScriptRunner.runTestScript(is);
//  }


}
