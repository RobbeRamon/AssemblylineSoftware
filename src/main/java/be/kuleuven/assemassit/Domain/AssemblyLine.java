package be.kuleuven.assemassit.Domain;

import be.kuleuven.assemassit.Domain.Enums.AssemblyTaskType;
import be.kuleuven.assemassit.Domain.Enums.WorkPostType;
import be.kuleuven.assemassit.Domain.Helper.Observer;
import be.kuleuven.assemassit.Domain.Helper.Subject;
import be.kuleuven.assemassit.Domain.Scheduling.FIFOScheduling;
import be.kuleuven.assemassit.Domain.Scheduling.SchedulingAlgorithm;
import be.kuleuven.assemassit.Domain.Scheduling.SpecificationBatchScheduling;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @mutable
 * @invar | getCarBodyPost() != null
 * @invar | getDriveTrainPost() != null
 * @invar | getAccessoriesPost() != null
 * @invar | getCarAssemblyProcess() != null
 * @invar | getFinishedCars() != null
 * @invar | getSchedulingAlgorithm() != null
 * @invar | giveSchedulingAlgorithmNames() != null
 */
public class AssemblyLine implements Subject {

  /**
   * @invar | carBodyPost != null
   * @invar | driveTrainPost != null
   * @invar | accessoriesPost != null
   * @invar | carAssemblyProcessesQueue != null
   * @invar | finishedCars != null
   * @invar | (startTime == null || endTime == null) || startTime.isBefore(endTime)
   * @invar | schedulingAlgorithm != null
   * @representationObject
   */
  private final WorkPost carBodyPost;

  /**
   * @representationObject
   */
  private final WorkPost drivetrainPost;

  /**
   * @representationObject
   */
  private final WorkPost accessoriesPost;

  /**
   * @representationObject
   * @representationObjects
   */
  private final Queue<CarAssemblyProcess> carAssemblyProcessesQueue;

  /**
   * @representationObject
   * @representationObjects
   */
  private final List<CarAssemblyProcess> finishedCars;
  private LocalTime startTime;
  private LocalTime endTime;

  /**
   * @representationObject
   */
  private SchedulingAlgorithm schedulingAlgorithm;
  private int previousOvertimeInMinutes;

  private List<Observer> observers;

  private int overtime;

  /**
   * @post | carBodyPost != null
   * @post | driveTrainPost != null
   * @post | accessoriesPost != null
   * @post | carAssemblyProcessesQueue != null
   * @post | finishedCars != null
   * @mutates | this
   */
  public AssemblyLine() {
    this.carBodyPost = new WorkPost(0, Arrays.asList(AssemblyTaskType.ASSEMBLE_CAR_BODY, AssemblyTaskType.PAINT_CAR), WorkPostType.CAR_BODY_POST, 60);
    this.drivetrainPost = new WorkPost(1, Arrays.asList(AssemblyTaskType.INSERT_ENGINE, AssemblyTaskType.INSERT_GEARBOX), WorkPostType.DRIVETRAIN_POST, 60);
    this.accessoriesPost = new WorkPost(2, Arrays.asList(AssemblyTaskType.INSTALL_AIRCO, AssemblyTaskType.INSTALL_SEATS, AssemblyTaskType.MOUNT_WHEELS), WorkPostType.ACCESSORIES_POST, 60);
    this.finishedCars = new ArrayList<>();
    this.carAssemblyProcessesQueue = new ArrayDeque<>();
    this.previousOvertimeInMinutes = 0;
    this.schedulingAlgorithm = new FIFOScheduling();
    this.observers = new ArrayList<>();
    this.overtime = 0;
  }

  /**
   * Sets the start time of the assembly line.
   *
   * @param startTime
   * @throws IllegalArgumentException startTime can not be null | startTime == null
   * @post | this.startTime == startTime
   */
  public void setStartTime(LocalTime startTime) {
    if (startTime == null) {
      throw new IllegalArgumentException("StartTime can not be null");
    }
    this.startTime = startTime;
  }

  /**
   * Sets the end time of the assembly line.
   * This is always set by the car manufacturing company and is not outside of the opening hours of the company.
   *
   * @param endTime
   * @throws IllegalArgumentException endTime can not be null | endTime == null
   * @post | this.endTime == endTime
   */
  public void setEndTime(LocalTime endTime) {
    if (endTime == null) {
      throw new IllegalArgumentException("EndTime can not be null");
    }
    this.endTime = endTime;
  }

  /**
   * Returns the current scheduling algorithm
   *
   * @return the current scheduling algorithm
   * @post | result != null
   */
  public SchedulingAlgorithm getSchedulingAlgorithm() {
    return this.schedulingAlgorithm;
  }

  /**
   * Sets the scheduling algorithm of the assembly line.
   *
   * @param schedulingAlgorithm
   * @throws IllegalArgumentException schedulingAlgorithm can not be null | schedulingAlgorithm == null
   * @post | this.schedulingAlgorithm == schedulingAlgorithm
   */
  public void setSchedulingAlgorithm(SchedulingAlgorithm schedulingAlgorithm) {
    if (schedulingAlgorithm == null)
      throw new IllegalArgumentException("SchedulingAlgorithm can not be null");
    this.schedulingAlgorithm = schedulingAlgorithm;
  }

  /**
   * Adds a car assembly process to the queue of pending car assembly processes
   *
   * @param carAssemblyProcess
   * @mutates | this
   */
  public void addCarAssemblyProcess(CarAssemblyProcess carAssemblyProcess) {
    if (carAssemblyProcess == null) {
      throw new IllegalArgumentException("CarAssemblyProcess not found");
    }
    carAssemblyProcessesQueue.add(carAssemblyProcess);
  }

  public WorkPost getCarBodyPost() {
    return this.carBodyPost;
  }

  public WorkPost getDrivetrainPost() {
    return this.drivetrainPost;
  }

  public WorkPost getAccessoriesPost() {
    return this.accessoriesPost;
  }

  public int getOvertime() {
    return this.overtime;
  }

  private void setOvertime(int overtime) {
    this.overtime = overtime;
    notifyObservers();
  }

  public List<CarAssemblyProcess> getFinishedCars() {
    return this.finishedCars;
  }

  /**
   * Return the list of assembly tasks from an associated work post on the assembly line that are pending
   *
   * @param workPostId
   * @return The list of pending assembly tasks
   * @inspects | this
   * @creates | result
   */
  public List<AssemblyTask> givePendingAssemblyTasksFromWorkPost(int workPostId) {
    WorkPost workPost = findWorkPost(workPostId);
    return workPost.givePendingAssemblyTasks();
  }

  /**
   * Complete the active assembly task of a work post
   *
   * @param workPostId the work post id
   * @throws IllegalArgumentException workPostId is below 0 | workPostId < 0
   * @mutates | this
   */
  public void completeAssemblyTask(int workPostId) {
    if (workPostId < 0)
      throw new IllegalArgumentException("WorkPostId can not be below 0");
    WorkPost workPost = findWorkPost(workPostId);
    workPost.completeAssemblyTask();
  }

  /**
   * Gives an overview of every work post with its corresponding active assembly task.
   * The corresponding active assembly task can be null if no task is active in the work post.
   *
   * @return an overview of the work posts with the corresponding active assembly task
   * @inspects | this
   * @creates | result
   */
  public HashMap<String, AssemblyTask> giveActiveTasksOverview() {
    HashMap<String, AssemblyTask> workPostStatuses = new LinkedHashMap<>();

    workPostStatuses.put("Car Body Post", carBodyPost.getActiveAssemblyTask());
    workPostStatuses.put("Drivetrain Post", drivetrainPost.getActiveAssemblyTask());
    workPostStatuses.put("Accessories Post", accessoriesPost.getActiveAssemblyTask());

    return workPostStatuses;
  }

  /**
   * Gives an overview of every work post with its corresponding pending and finished tasks.
   *
   * @return an overview of the work posts with all pending and finished assembly tasks
   * @inspects | this
   * @creates | result
   */
  public HashMap<String, List<AssemblyTask>> giveTasksOverview() {
    HashMap<String, List<AssemblyTask>> workPostPairs = new LinkedHashMap<>();

    workPostPairs.put("Car Body Post", carBodyPost.getWorkPostAssemblyTasks());
    workPostPairs.put("Drivetrain Post", drivetrainPost.getWorkPostAssemblyTasks());
    workPostPairs.put("Accessories Post", accessoriesPost.getWorkPostAssemblyTasks());

    return workPostPairs;
  }

  /**
   * Gives an overview of every work post with its corresponding pending and finished tasks in a future state
   * as if the assembly line would be moved by a responsible.
   *
   * @return an overview of the work posts with all pending and finished assembly tasks in a future state
   * @inspects | this
   * @creates | result
   */
  public HashMap<String, List<AssemblyTask>> giveFutureTasksOverview() {

    List<AssemblyTask> futureCarBodyPostAssemblyProcessAssemblyTasks = new ArrayList<>();
    if (!this.carAssemblyProcessesQueue.isEmpty()) {
      futureCarBodyPostAssemblyProcessAssemblyTasks =
        filterTasksOfSpecificTypeList(
          new ArrayList<>(this.carAssemblyProcessesQueue.peek().getAssemblyTasks()),
          this.carBodyPost.getAssemblyTaskTypes()
        );
    }

    List<AssemblyTask> futureDrivetrainPostAssemblyProcessAssemblyTasks = new ArrayList<>();
    if (this.carBodyPost.getCarAssemblyProcess() != null) {
      futureDrivetrainPostAssemblyProcessAssemblyTasks = filterTasksOfSpecificTypeList(
        new ArrayList<>(this.carBodyPost.getCarAssemblyProcess().getAssemblyTasks()),
        this.drivetrainPost.getAssemblyTaskTypes()
      );
    }

    List<AssemblyTask> futureAccessoriesPostAssemblyProcessAssemblyTasks = new ArrayList<>();
    if (this.drivetrainPost.getCarAssemblyProcess() != null) {
      futureAccessoriesPostAssemblyProcessAssemblyTasks = filterTasksOfSpecificTypeList(
        new ArrayList<>(this.drivetrainPost.getCarAssemblyProcess().getAssemblyTasks()),
        this.accessoriesPost.getAssemblyTaskTypes()
      );
    }

    HashMap<String, List<AssemblyTask>> workPostPairs = new LinkedHashMap<>();

    workPostPairs.put("Car Body Post", futureCarBodyPostAssemblyProcessAssemblyTasks);
    workPostPairs.put("Drivetrain Post", futureDrivetrainPostAssemblyProcessAssemblyTasks);
    workPostPairs.put("Accessories Post", futureAccessoriesPostAssemblyProcessAssemblyTasks);

    return workPostPairs;
  }

  /**
   * The assembly tasks from the given assembly task types are filtered from the given list of assembly tasks.
   *
   * @param allAssemblyTasks  the list of assembly tasks where the filter should be applied on
   * @param assemblyTaskTypes the list of assembly task types that has to be filtered on
   * @return
   * @throws IllegalArgumentException the list of assembly tasks is null or empty | (allAssemblyTasks == null || allAssemblyTasks.isEmpty())
   * @throws IllegalArgumentException the list of assembly tasks types is null or empty | (assemblyTaskTypes == null || assemblyTaskTypes.isEmpty())
   * @inspects | this
   * @creates | result
   */
  private List<AssemblyTask> filterTasksOfSpecificTypeList(List<AssemblyTask> allAssemblyTasks, List<AssemblyTaskType> assemblyTaskTypes) {
    if (allAssemblyTasks == null || allAssemblyTasks.isEmpty())
      throw new IllegalArgumentException("The list of assembly tasks can not be null or empty");
    if (assemblyTaskTypes == null || assemblyTaskTypes.isEmpty())
      throw new IllegalArgumentException("The list of assembly tasks types can not be null or empty");

    return allAssemblyTasks.stream().filter(task -> assemblyTaskTypes.contains(task.getAssemblyTaskType())).toList();
  }

  /**
   * Find a work post by ID
   *
   * @param workPostId the ID of the work post
   * @return the corresponding work post
   * @throws IllegalArgumentException | workPosts.stream().filter(workPost -> workPost.getId() == workPostId).findFirst().isEmpty
   * @throws IllegalArgumentException | workPostId < 0
   * @inspects | this
   */
  public WorkPost findWorkPost(int workPostId) {

    if (workPostId < 0)
      throw new IllegalArgumentException("The ID can not be lower than 0");


    List<WorkPost> workPosts = this.giveWorkPostsAsList();

    Optional<WorkPost> optionalWorkPost = workPosts.stream().filter(workPost -> workPost.getId() == workPostId).findFirst();

    if (optionalWorkPost.isEmpty()) {
      throw new IllegalArgumentException("Workpost not found");
    }
    return optionalWorkPost.get();
  }

  /**
   * Check if it is possible that the assembly line can be moved. This only possible if every work post is finished
   * with all its current tasks.
   *
   * @return true if the assembly line can be moved
   * @inspects | this
   */
  public boolean canMove() {
    List<WorkPost> workPosts = this.giveWorkPostsAsList();
    for (WorkPost workPost : workPosts) {
      if (!workPost.givePendingAssemblyTasks().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Moves the assembly and gives the duration of the current phase.
   * The assembly process is moved from one work post to another on the assembly line.
   *
   * @param minutes the amount of minutes spent during the current phase
   * @throws IllegalStateException    when the assembly line can not be moved | !canMove()
   * @throws IllegalArgumentException minutes is below 0 | minutes < 0
   * @mutates | this
   */
  public void move(int minutes) {

    if (!canMove())
      throw new IllegalArgumentException("AssemblyLine cannot be moved forward!");

    int overtime = schedulingAlgorithm.moveAssemblyLine
      (
        minutes,
        previousOvertimeInMinutes,
        endTime,
        carAssemblyProcessesQueue,
        finishedCars,
        giveWorkPostsAsList()
      );

    if (overtime > 0) {
      // overtime happened so we have to inform the assembly line
      setOvertime(overtime);
    }
  }

  /**
   * Calculates the estimated delivery time of the latest task in the process queue.
   * The algorithm first calculates if processes still can be performed on the current day.
   * After that it checks if followings days should be used.
   * Finally it ends with the estimated delivery time of the last assembly process in the queue.
   *
   * @return the estimated delivery time for the latest task in the process queue of this assembly line
   * @inspects | this
   * @creates | result
   */
  public LocalDateTime giveEstimatedCompletionDateOfLatestProcess() {
    return this.schedulingAlgorithm
      .giveEstimatedDeliveryTime(
        this.carAssemblyProcessesQueue,
        giveManufacturingDurationInMinutes(),
        this.endTime,
        this.startTime,
        maxTimeNeededForWorkPostOnLine()
      );
  }

  /**
   * Returns the duration of the slowest work post on the process.
   * It is possible that all work post have equal durations in the system.
   *
   * @return the duration of the slowest work post on the assembly line in minutes
   * @post | result >= 0
   * @inspects | this
   */
  private int maxTimeNeededForWorkPostOnLine() {
    return giveWorkPostsAsList()
      .stream()
      .mapToInt(WorkPost::getExpectedWorkPostDurationInMinutes)
      .max()
      .orElse(0);
  }

  /**
   * Returns the total manufacturing duration of a car assembly process in minutes.
   *
   * @return total manufacturing duration of a car assembly process in minutes
   * @post | result >= 0
   * @inspects | this
   */
  private int giveManufacturingDurationInMinutes() {
    return giveWorkPostsAsList()
      .stream()
      .mapToInt(WorkPost::getExpectedWorkPostDurationInMinutes)
      .reduce(0, Integer::sum);
  }

  /**
   * Collects the work posts of the assembly line in a list.
   *
   * @return the list of work posts on this assembly line
   * @post | result != null
   * @inspects | this
   * @creates | result
   */
  public List<WorkPost> giveWorkPostsAsList() {
    return Arrays.asList(carBodyPost, drivetrainPost, accessoriesPost);
  }

  /**
   * Returns the corresponding assembly task of the corresponding work post
   *
   * @param workPostId     the id of the work post
   * @param assemblyTaskId the id of the assembly task
   * @return the corresponding assembly task from the work post
   * @throws IllegalArgumentException | workPostId < 0 || assemblyTaskId < 0
   * @post | result != null
   * @inspects | this
   */
  public AssemblyTask giveCarAssemblyTask(int workPostId, int assemblyTaskId) {
    if (workPostId < 0 || assemblyTaskId < 0)
      throw new IllegalArgumentException("The IDs must be greater than or equal to zero");

    WorkPost workPost = findWorkPost(workPostId);
    return workPost.findAssemblyTask(assemblyTaskId);
  }

  /**
   * Set an assembly task as active in a work post
   *
   * @param workPost       the work post
   * @param assemblyTaskId the id of the assembly task
   * @throws IllegalArgumentException workPost is null | workPost == null
   * @throws IllegalArgumentException assemblyTaskId is below 0 | assemblyTaskId < 0
   */
  public void setActiveTask(WorkPost workPost, int assemblyTaskId) {
    if (workPost == null)
      throw new IllegalArgumentException("workPost can not be null");
    if (assemblyTaskId < 0)
      throw new IllegalArgumentException("assemblyTaskId can not be below 0");
    workPost.setActiveAssemblyTask(assemblyTaskId);
  }

  /**
   * @return a list of cas assembly processes
   * @creates | result
   * @inspects | this
   */
  public List<CarAssemblyProcess> getCarAssemblyProcessesQueue() {
    List<CarAssemblyProcess> carAssemblyProcessList = new ArrayList<>(carAssemblyProcessesQueue);
    carAssemblyProcessList.add(drivetrainPost.getCarAssemblyProcess());
    carAssemblyProcessList.add(accessoriesPost.getCarAssemblyProcess());
    carAssemblyProcessList.add(carBodyPost.getCarAssemblyProcess());
    if (!finishedCars.isEmpty()) {
      carAssemblyProcessList.addAll(finishedCars);
    }
    return carAssemblyProcessList;
  }

  /**
   * Returns a list of strings that represent the possible scheduling algorithms
   *
   * @return list of algorithm names
   * @post | result != null
   */
  public List<String> giveSchedulingAlgorithmNames() {
    return List.of
      (
        FIFOScheduling.class.getSimpleName(),
        SpecificationBatchScheduling.class.getSimpleName()
      );
  }

  public List<Car> givePossibleBatchCars() {
    List<Car> cars = this.carAssemblyProcessesQueue
      .stream()
      .map(p -> p.getCarOrder().getCar())
      .collect(Collectors.toList());

    Map<Car, Integer> frequencyMap = new HashMap<>();
    for (Car c : cars) {
      Integer count = frequencyMap.get(c);
      if (count == null) {
        count = 0;
      }

      frequencyMap.put(c, count++);
    }

    return cars.stream().filter(c -> frequencyMap.get(c) >= 3).collect(Collectors.toList());
  }

  @Override
  public void attach(Observer observer) {
    this.observers.add(observer);
  }

  @Override
  public void detach(Observer observer) {
    this.observers.remove(observer);
  }

  @Override
  public void notifyObservers() {
    for (Observer observer : observers) {
      observer.update();
    }
  }
}
