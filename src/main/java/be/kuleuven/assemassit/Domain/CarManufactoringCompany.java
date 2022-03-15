package be.kuleuven.assemassit.Domain;

import be.kuleuven.assemassit.Domain.Repositories.CarModelRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class CarManufactoringCompany {
  private List<CarModel> carModels;
  private AssemblyLine assemblyLine;
  private CarModelRepository carModelRepository;
  private LocalTime openingTime;
  private LocalTime closingTime;

  public CarManufactoringCompany() {
    this.carModelRepository = new CarModelRepository();
    this.carModels = carModelRepository.getCarModels();
    this.assemblyLine = new AssemblyLine();
  }

  public List<CarModel> getCarModels() {
    return List.copyOf(carModels);
  }

  public LocalTime getOpeningTime() {
    return LocalTime.of(openingTime.getHour(), openingTime.getMinute());
  }

  public LocalTime getClosingTime() {
    return LocalTime.of(closingTime.getHour(), closingTime.getMinute());
  }

  public CarModel giveCarModelWithId(int id) {
    Optional<CarModel> carModel = this.carModels
      .stream()
      .filter(cm -> cm.getId() == id)
      .findFirst();

    if (!carModel.isPresent())
      throw new IllegalArgumentException("CarModel not found");

    return carModel.get();
  }

  public void addCarAssemblyProcess(CarAssemblyProcess carAssemblyProcess) {
    assemblyLine.addCarAssemblyProcess(carAssemblyProcess);
  }

  public LocalDateTime giveEstimatedCompletionDateOfLatestProcess() {
    return assemblyLine.giveEstimatedCompletionDateOfLatestProcess(this.openingTime, this.closingTime);
  }
}
