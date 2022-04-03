package be.kuleuven.assemassit.UI;

import be.kuleuven.assemassit.Controller.AssemblyLineController;
import be.kuleuven.assemassit.Controller.ControllerFactory;
import be.kuleuven.assemassit.Controller.OrderNewCarController;
import be.kuleuven.assemassit.UI.Actions.CarMechanicActions.CarMechanicActionsOverviewUI;
import be.kuleuven.assemassit.UI.Actions.GarageHolderActions.GarageHolderActionsOverviewUI;
import be.kuleuven.assemassit.UI.Actions.ManagerActions.ManagerActionsOverviewUI;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class LoginUI implements UI {
  private ControllerFactory controllerFactory;
  private AssemblyLineController assemblyLineController;
  private OrderNewCarController orderNewCarController;

  private ManagerActionsOverviewUI managerActionsOverviewUI;
  private CarMechanicActionsOverviewUI carMechanicActionsOverviewUI;
  private GarageHolderActionsOverviewUI garageHolderActionsOverviewUI;

  public LoginUI() {
    this.controllerFactory = new ControllerFactory();
    this.assemblyLineController = controllerFactory.createAssemblyLineController();
    this.orderNewCarController = controllerFactory.createOrderNewCarController();

    this.managerActionsOverviewUI = new ManagerActionsOverviewUI(this.assemblyLineController);
    this.carMechanicActionsOverviewUI = new CarMechanicActionsOverviewUI(this.assemblyLineController);
    this.garageHolderActionsOverviewUI = new GarageHolderActionsOverviewUI(this.orderNewCarController);
  }

  private Optional<Integer> displayGarageHolderForm(Map<Integer, String> garageHolders) {
    Scanner scanner = new Scanner(System.in);
    int garageHolderId;

    do {
      System.out.println();
      System.out.println("Please select your name:");
      garageHolders.forEach((id, name) -> System.out.println(String.format("%2d", id) + ": " + name));
      System.out.println("-1: Go back");

      garageHolderId = scanner.nextInt();

      if (garageHolderId == -1) return Optional.empty();

    } while (!garageHolders.containsKey(garageHolderId));

    return Optional.of(garageHolderId);
  }

  @Override
  public void run() {
    int choice;

    do {
      IOCall.waitForConfirmation();
      IOCall.out("Please authenticate yourself:");
      IOCall.out(" 1: I am a garage holder");
      IOCall.out(" 2: I am a manager");
      IOCall.out(" 3: I am a car mechanic");
      IOCall.out("-1: Exit");

      choice = IOCall.in();

      switch (choice) {
        case 1 -> {
          Optional<Integer> selectedGarageHolderIdOptional = displayGarageHolderForm(orderNewCarController.giveGarageHolders());
          if (selectedGarageHolderIdOptional.isEmpty()) {
            AuthenticateUI.run(orderNewCarController, assemblyLineController);
            return;
          }
          orderNewCarController.logInGarageHolder(selectedGarageHolderIdOptional.get());
          this.garageHolderActionsOverviewUI.run();
        }
        case 2 -> this.managerActionsOverviewUI.run();
        case 3 -> this.carMechanicActionsOverviewUI.run();
        case -1 -> {
          return;
        }
      }
    } while (choice != -1 && (choice < 1 || choice > 3));
  }
}
