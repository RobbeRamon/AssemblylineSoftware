package be.kuleuven.assemassit.Domain;

import be.kuleuven.assemassit.Domain.Enums.AssemblyTaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AssemblyTask {
	private boolean pending;
	private List<String> actions;
	private int id;
	private LocalDateTime completionTime;
	private String name;

	public AssemblyTask(String name) {
	  this.name = name;
  }

	public AssemblyTask(int id) {
		this.id = id;
	}

	public boolean getPending() {
		return this.pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public List<String> getActions() {
		return new ArrayList<String>(actions);
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
	  return this.name;
  }

  public abstract AssemblyTaskType getAssemblyTaskType();

	public LocalDateTime completionTime() {

		if (!pending)
			throw new IllegalStateException();

		return this.completionTime;
	}

	public void complete() {
		this.pending = false;
		this.completionTime = LocalDateTime.now();
	}

  public boolean equals(Object object){
	  if(object instanceof AssemblyTask task) task.actions.equals(this.actions);
	  return false;
  }


}
