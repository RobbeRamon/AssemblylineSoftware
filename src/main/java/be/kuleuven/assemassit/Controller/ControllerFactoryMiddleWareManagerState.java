package be.kuleuven.assemassit.Controller;

import be.kuleuven.assemassit.Domain.AssemblyLine;

public class ControllerFactoryMiddleWareManagerState extends ControllerFactoryMiddleWareState {

  public AdaptSchedulingAlgorithmController createAdaptSchedulingAlgorithmController(AssemblyLine assemblyLine) {
    return factory.createAdaptSchedulingAlgorithmController(assemblyLine);
  }

  public CheckProductionStatisticsController createCheckProductionStatisticsController(AssemblyLine assemblyLine) {
    return factory.createCheckProductionStatisticsController(assemblyLine);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ControllerFactoryMiddleWareManagerState;
  }
}
