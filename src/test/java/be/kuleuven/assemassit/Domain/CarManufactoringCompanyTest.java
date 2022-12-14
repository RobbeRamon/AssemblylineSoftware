package be.kuleuven.assemassit.Domain;

import be.kuleuven.assemassit.Domain.Enums.*;
import be.kuleuven.assemassit.Domain.Helper.CustomTime;
import be.kuleuven.assemassit.Repositories.CarModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


public class CarManufactoringCompanyTest {
  private List<CarModel> carModels;
  private AssemblyLine assemblyLine;
  private CarModelRepository carModelRepository;
  private LocalTime openingTime;
  private LocalTime closingTime;
  private CarManufactoringCompany carManufactoringCompany;
  private CarAssemblyProcess carAssemblyProcess;


  @BeforeEach
  public void beforeEach() {
    carModelRepository = new CarModelRepository();
    this.carModels = carModelRepository.getCarModels();
    this.openingTime = LocalTime.of(LocalTime.of(6, 0).getHour(), LocalTime.of(6, 0).getMinute());
    this.closingTime = LocalTime.of(LocalTime.of(22, 0).getHour(), LocalTime.of(22, 0).getMinute());
    this.assemblyLine = new AssemblyLine(openingTime, closingTime);
    carManufactoringCompany = new CarManufactoringCompany(assemblyLine);
    carAssemblyProcess = new CarAssemblyProcess(new CarOrder(new Car(new CarModel(0, "Tolkswagen Rolo", Arrays.asList(Wheel.values()), Arrays.asList(Gearbox.values()), Arrays.asList(Seat.values()), Arrays.asList(Body.values()), Arrays.asList(Color.values()), Arrays.asList(Engine.values()), Arrays.asList(Airco.values()), Arrays.asList(Spoiler.values())), Body.SEDAN, Color.BLACK, Engine.PERFORMANCE, Gearbox.FIVE_SPEED_MANUAL, Seat.LEATHER_BLACK, Airco.MANUAL, Wheel.SPORT, Spoiler.LOW)));

    carManufactoringCompany.addCarAssemblyProcess(carAssemblyProcess);
  }


  @Test
  public void giveEstimatedCompletionDateOfLatestProcessTest() {

    LocalDateTime localDateTimeNow = (CustomTime.getInstance().customLocalDateTimeNow());
    LocalDateTime expectedDate = (CustomTime.getInstance().customLocalDateTimeNow());
    LocalDateTime actual = carManufactoringCompany.giveEstimatedCompletionDateOfLatestProcess();

    if (localDateTimeNow.getHour() < 6) {
      expectedDate = expectedDate.withHour(9).withMinute(0);
    }
    if (localDateTimeNow.getHour() >= 6 && localDateTimeNow.getHour() <= 19) {
      expectedDate = expectedDate.plusHours(3);
    }
    if (localDateTimeNow.getHour() > 19) {
      expectedDate = expectedDate.plusDays(1).withHour(9).withMinute(0);
    }

    assertEquals(expectedDate.getMinute(), actual.getMinute());
    assertEquals(expectedDate.getHour(), actual.getHour());
    assertEquals(expectedDate.toLocalDate(), actual.toLocalDate());
  }

  @Test
  public void constructorTest() {
    assertEquals(assemblyLine.getOpeningTime(), this.openingTime);
    assertEquals(assemblyLine.getClosingTime(), this.closingTime);
    for (CarAssemblyProcess carAssemblyProcess : carManufactoringCompany.getAssemblyLine().getCarAssemblyProcessesQueue()) {
      assertTrue(assemblyLine.getCarAssemblyProcessesQueue().contains(carAssemblyProcess));
    }
  }

  @Test
  public void constructorTest2() {
    CarManufactoringCompany company = new CarManufactoringCompany(carModelRepository, assemblyLine);

    assertEquals(assemblyLine.getOpeningTime(), this.openingTime);
    assertEquals(assemblyLine.getClosingTime(), this.closingTime);
    for (CarAssemblyProcess carAssemblyProcess : company.getAssemblyLine().getCarAssemblyProcessesQueue()) {
      assertTrue(assemblyLine.getCarAssemblyProcessesQueue().contains(carAssemblyProcess));
    }
  }

  @Test
  public void giveCarModelWithIdTest() {
    assertThrows(IllegalArgumentException.class, () -> carManufactoringCompany.giveCarModelWithId(-1));
    assertEquals(carManufactoringCompany.giveCarModelWithId(0).getName(), carModels.get(0).getName());
  }

  @Test
  public void addCarAssemblyProcessTest() {

    assertTrue(carManufactoringCompany.getAssemblyLine().getCarAssemblyProcessesQueue().contains(carAssemblyProcess));
  }


  @Test
  void getCarModels() {
    List<CarModel> models = carManufactoringCompany.getCarModels();
    assertEquals(models.size(), 3);
  }

  @Test
  void assemblyLineMove() {
    LocalTime time = (CustomTime.getInstance().customLocalTimeNow());
    AssemblyLine line = new AssemblyLine(LocalTime.of(0, 1), LocalTime.of(23, 59));
    CarManufactoringCompany company = new CarManufactoringCompany(line);
    assertTrue(company.isAssemblyLineAvailable());
    CarAssemblyProcess process = new CarAssemblyProcess(new CarOrder(new Car(new CarModel(0, "Tolkswagen Rolo", Arrays.asList(Wheel.values()), Arrays.asList(Gearbox.values()), Arrays.asList(Seat.values()), Arrays.asList(Body.values()), Arrays.asList(Color.values()), Arrays.asList(Engine.values()), Arrays.asList(Airco.values()), Arrays.asList(Spoiler.values())), Body.SEDAN, Color.BLACK, Engine.PERFORMANCE, Gearbox.FIVE_SPEED_MANUAL, Seat.LEATHER_BLACK, Airco.MANUAL, Wheel.SPORT, Spoiler.LOW)));
    company.addCarAssemblyProcess(process);
    assertTrue(company.isAssemblyLineAvailable());
    company.triggerAutomaticFirstMove(process.giveManufacturingDurationInMinutes());
    assertFalse(company.isAssemblyLineAvailable());
  }


  @Test
  public void designCarOrderOrderTest_succeeds() {
    GarageHolder mockedGarageHolder = mock(GarageHolder.class);
    CarOrder.resetIdRunner();
    int carOrderId = carManufactoringCompany.designCarOrder(mockedGarageHolder, 0, "BREAK", "BLACK", "PERFORMANCE", "FIVE_SPEED_MANUAL", "LEATHER_BLACK", "AUTOMATIC", "COMFORT", "NO_SPOILER");
    assertEquals(carOrderId, 0);
  }

  @Test
  public void designCarOrderTest_throws() {
    GarageHolder mockedGarageHolder = mock(GarageHolder.class);

    assertThrows(IllegalArgumentException.class, () -> carManufactoringCompany.designCarOrder(mockedGarageHolder, 0, "", "", "", "", "", "", "", ""));
  }
}
