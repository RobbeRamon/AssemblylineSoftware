package be.kuleuven.assemassit.Domain.TaskTypes;

import be.kuleuven.assemassit.Domain.AssemblyTask;
import be.kuleuven.assemassit.Domain.Enums.AssemblyTaskType;
import be.kuleuven.assemassit.Domain.Enums.Body;

import java.util.ArrayList;
import java.util.List;

/**
 * @mutable
 */
public class CarBodyAssemblyTask extends AssemblyTask {
  /**
   * @invar | body != null
   */
  private final Body body;
  private final AssemblyTaskType assemblyTaskType = AssemblyTaskType.ASSEMBLE_CAR_BODY;

  /**
   * @param body
   * @throws IllegalArgumentException body can not be null | body == null
   * @mutates | this
   * @post | this.body = body
   */
  public CarBodyAssemblyTask(Body body) {
    super("Assembly car body");
    if (body == null)
      throw new IllegalArgumentException("Body can not be null");
    this.body = body;
  }

  @Override
  public List<String> getActions() {
    List<String> actions = new ArrayList<>();
    actions.add("Installing the " + body + " body");
    return actions;
  }

  @Override
  public AssemblyTaskType getAssemblyTaskType() {
    return this.assemblyTaskType;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CarBodyAssemblyTask) {
      CarBodyAssemblyTask carBodyAssemblyTask = (CarBodyAssemblyTask) o;
      return carBodyAssemblyTask.getId() == this.getId();
    }
    return false;
  }
}
