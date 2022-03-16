package be.kuleuven.assemassit.Domain.TaskTypes;

import be.kuleuven.assemassit.Domain.AssemblyTask;
import be.kuleuven.assemassit.Domain.Enums.Airco;
import be.kuleuven.assemassit.Domain.Enums.AssemblyTaskType;

public class InstallAircoAssemblyTask extends AssemblyTask {
  private Airco airco;
  private AssemblyTaskType assemblyTaskType = AssemblyTaskType.INSTALL_AIRCO;

  public InstallAircoAssemblyTask(Airco airco) {
    super("Install airco");
    this.airco = airco;
  }

  @Override
  public AssemblyTaskType getAssemblyTaskType() {
    return this.assemblyTaskType;
  }

  //Pattern matching not working here for some reason
  @Override
  public boolean equals(Object o){
    if(o instanceof InstallAircoAssemblyTask){
      return this.airco == ((InstallAircoAssemblyTask) o).airco;
    }
    return false;
  }
}
