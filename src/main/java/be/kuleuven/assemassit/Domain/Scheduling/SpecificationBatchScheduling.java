package be.kuleuven.assemassit.Domain.Scheduling;

import be.kuleuven.assemassit.Domain.AssemblyTask;
import be.kuleuven.assemassit.Domain.Car;
import be.kuleuven.assemassit.Domain.CarAssemblyProcess;
import be.kuleuven.assemassit.Domain.Enums.CarOption;
import be.kuleuven.assemassit.Domain.Helper.CustomTime;
import be.kuleuven.assemassit.Domain.Helper.EnhancedIterator;
import be.kuleuven.assemassit.Domain.Helper.MyEnhancedIterator;
import be.kuleuven.assemassit.Domain.WorkPost;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SpecificationBatchScheduling extends DefaultSchedulingAlgorithm {
  private final List<CarOption> batchCarOptions;

  public SpecificationBatchScheduling(List<CarOption> batchCarOptions) {
    super();
    this.batchCarOptions = batchCarOptions;
  }

  @Override
  public int moveAssemblyLine(
    int previousOvertimeInMinutes,
    LocalTime endTime,
    Queue<CarAssemblyProcess> carAssemblyProcessesQueue,
    List<CarAssemblyProcess> finishedCars,
    List<WorkPost> workPostsInOrder
  ) {

    int overtime = -1; // return -1 if the end of the day is not reached yet


    workPostsInOrder = new ArrayList<>(workPostsInOrder);
    Collections.reverse(workPostsInOrder);

    EnhancedIterator<WorkPost> iterator = new MyEnhancedIterator<>(workPostsInOrder);

    WorkPost workPost;

    do {
      workPost = iterator.next();

      if (workPost != null) {
        for (AssemblyTask assemblyTask : workPost.getWorkPostAssemblyTasks()) {
          //assemblyTask.setCompletionTime(minutes);
        }

        // last one
        if (!iterator.hasPrevious() && workPost.getCarAssemblyProcess() != null) {
          CarAssemblyProcess carAssemblyProcess = workPost.getCarAssemblyProcess();
          carAssemblyProcess.complete();
          finishedCars.add(workPost.getCarAssemblyProcess());
          workPost.removeCarAssemblyProcess();

          int overtimeInMinutes = differenceInMinutes(endTime.minusMinutes(previousOvertimeInMinutes), (new CustomTime().customLocalTimeNow()));
          if (overtimeInMinutes >= 0)
            overtime = overtimeInMinutes; // only set the overtime when it is greater than or equal to zero
        }
      }

      if (iterator.hasNext()) {
        WorkPost previousWorkPost = iterator.peek();
        workPost.addProcessToWorkPost(previousWorkPost.getCarAssemblyProcess());
        previousWorkPost.removeCarAssemblyProcess();
      }
    } while (iterator.hasNext());


    // check if the next process can still be produced today
    CarAssemblyProcess nextProcess = giveNextCarAssemblyProcess(carAssemblyProcessesQueue);
    if (
      !carAssemblyProcessesQueue.isEmpty() &&
        (new CustomTime().customLocalTimeNow())
          .plusMinutes(nextProcess.giveManufacturingDurationInMinutes())
          .plusMinutes(previousOvertimeInMinutes)
          .isBefore(endTime)
    ) {
      // add a new work process to the first work post
      carAssemblyProcessesQueue.remove(nextProcess);
      workPost.addProcessToWorkPost(nextProcess);
    }

    return overtime;
  }

  @Override
  public LocalDateTime giveEstimatedDeliveryTime(Queue<CarAssemblyProcess> carAssemblyProcessesQueue, int manufacturingTimeInMinutes, LocalTime endTime, LocalTime startTime, int maxTimeNeededForWorkPostOnLine) {

    List<CarAssemblyProcess> carAssemblyProcessesList = carAssemblyProcessesQueue.stream().toList();

    // this is not the most optimal way of doing this, but it is an option and it works
    Collections.sort(carAssemblyProcessesList, (p1, p2) -> {
      Car car1 = p1.getCarOrder().getCar();
      Car car2 = p2.getCarOrder().getCar();

      boolean car1Batch = car1.giveListOfCarOptions().containsAll(batchCarOptions) && batchCarOptions.containsAll(car1.giveListOfCarOptions());
      boolean car2Batch = car2.giveListOfCarOptions().containsAll(batchCarOptions) && batchCarOptions.containsAll(car1.giveListOfCarOptions());

      if (car1Batch && car2Batch) return 0;
      if (car1Batch) return 1;
      if (car2Batch) return -1;

      return 0;
    });

    carAssemblyProcessesQueue = new ArrayDeque<>(carAssemblyProcessesList);

    return super.giveEstimatedDeliveryTime(carAssemblyProcessesQueue, manufacturingTimeInMinutes, endTime, startTime, maxTimeNeededForWorkPostOnLine);
  }

  private CarAssemblyProcess giveNextCarAssemblyProcess(Queue<CarAssemblyProcess> carAssemblyProcesses) {
    return carAssemblyProcesses.stream().filter(p -> {
      Car car = p.getCarOrder().getCar();
      return car.giveListOfCarOptions().containsAll(batchCarOptions) && batchCarOptions.containsAll(car.giveListOfCarOptions());
    }).findFirst().orElse(carAssemblyProcesses.peek()); // if none found, peek the first one from queue
  }

  private int differenceInMinutes(LocalTime dateTime1, LocalTime dateTime2) {
    int timeInMinutes1 = dateTime1.getHour() * 60 + dateTime1.getMinute();
    int timeInMinutes2 = dateTime2.getHour() * 60 + dateTime2.getMinute();

    return timeInMinutes2 - timeInMinutes1;
  }

}
