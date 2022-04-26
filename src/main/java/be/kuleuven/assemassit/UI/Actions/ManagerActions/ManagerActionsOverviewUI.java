package be.kuleuven.assemassit.UI.Actions.ManagerActions;

import be.kuleuven.assemassit.Controller.ControllerFactory;
import be.kuleuven.assemassit.UI.Actions.AdaptSchedulingAlgorithmActionUI;
import be.kuleuven.assemassit.UI.Actions.CheckProductionStatisticsActionUI;
import be.kuleuven.assemassit.UI.IOCall;
import be.kuleuven.assemassit.UI.UI;

public class ManagerActionsOverviewUI implements UI {

  private final ControllerFactory controllerFactory;

  public ManagerActionsOverviewUI(ControllerFactory controllerFactory) {
    this.controllerFactory = controllerFactory;
  }

  @Override
  public void run() {

    while (true) {
      int action;

      IOCall.out();
      IOCall.out("Welcome Manager");
      IOCall.out("Please choose an action:");
      IOCall.out(" 1: Car and delay statistics");
      IOCall.out(" 2: Change the scheduling algorithm");
      IOCall.out("-1: Logout and go back");

      action = IOCall.in();

      switch (action) {
        case 1 -> new CheckProductionStatisticsActionUI(controllerFactory).run();
        case 2 -> new AdaptSchedulingAlgorithmActionUI(controllerFactory).run();
        case -1 -> {
          controllerFactory.logoutManager();
          return;
        }
      }
    }
  }
}
