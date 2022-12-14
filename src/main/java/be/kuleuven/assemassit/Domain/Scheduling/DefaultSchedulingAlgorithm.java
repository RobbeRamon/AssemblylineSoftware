package be.kuleuven.assemassit.Domain.Scheduling;

import be.kuleuven.assemassit.Domain.CarAssemblyProcess;
import be.kuleuven.assemassit.Domain.Helper.CustomTime;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Queue;

public abstract class DefaultSchedulingAlgorithm implements SchedulingAlgorithm {

  // The constructor is package protected
  DefaultSchedulingAlgorithm() {
  }

  /**
   * Return the estimated delivery date of the last order in the queue
   *
   * @param carAssemblyProcessesQueue      the queue of planned cars that need to be manufactured
   * @param manufacturingTimeInMinutes     the duration of the build of exactly one car
   * @param endTime                        the closing time of the company
   * @param startTime                      the opening time of the company
   * @param maxTimeNeededForWorkPostOnLine the max time needed to finish one task on a work post
   * @return the estimated delivery time of the last order in the queue
   */
  @Override
  public LocalDateTime giveEstimatedDeliveryTime(
    Queue<CarAssemblyProcess> carAssemblyProcessesQueue,
    int manufacturingTimeInMinutes,
    LocalTime endTime,
    LocalTime startTime,
    int maxTimeNeededForWorkPostOnLine
  ) {
// calculate remaining cars for this day (1)
    double remaningCarsForTodayDouble = ((double) ((endTime.getHour() * 60 + endTime.getMinute()) - // end time
      manufacturingTimeInMinutes - // time needed to manufacture a car
      ((CustomTime.getInstance().customLocalTimeNow()).getHour() * 60 + (CustomTime.getInstance().customLocalTimeNow()).getMinute()) - // current time
      maxTimeNeededForWorkPostOnLine + // time needed for the slowest work post
      60) / (double) 60);
    int remainingCarsForToday =
      (int) Math.ceil(((double) ((endTime.getHour() * 60 + endTime.getMinute()) - // end time
        manufacturingTimeInMinutes - // time needed to manufacture a car
        ((CustomTime.getInstance().customLocalTimeNow()).getHour() * 60 + (CustomTime.getInstance().customLocalTimeNow()).getMinute()) - // current time
        maxTimeNeededForWorkPostOnLine + // time needed for the slowest work post
        60) / (double) 60));

    // calculate cars for a whole day (2)
    int amountOfCarsWholeDay =
      (int) ((double) ((endTime.getHour() * 60 + endTime.getMinute()) - // end time
        manufacturingTimeInMinutes - // time needed to manufacture a car
        (startTime.getHour() * 60 + startTime.getMinute()) - // opening time
        maxTimeNeededForWorkPostOnLine + // time needed for the slowest work post
        60) / (double) 60);

    // car can still be manufactured today
    if (carAssemblyProcessesQueue.size() <= remainingCarsForToday) {
      // total duration - max duration of work post + max duration * amount
      return (CustomTime.getInstance().customLocalDateTimeNow()).plusMinutes(manufacturingTimeInMinutes - maxTimeNeededForWorkPostOnLine).plusMinutes((long) maxTimeNeededForWorkPostOnLine * carAssemblyProcessesQueue.size());
    }

    // car can not be manufactured today
    int daysNeeded = Math.max(0, (carAssemblyProcessesQueue.size() - remainingCarsForToday) / amountOfCarsWholeDay - 1);


    // return date of tomorrow + days needed + minutes needed
    LocalDateTime today = (CustomTime.getInstance().customLocalDateTimeNow());
    int remainingMinutesForLastDay = (((carAssemblyProcessesQueue.size() - Math.abs(remainingCarsForToday)) % amountOfCarsWholeDay) + 1) * maxTimeNeededForWorkPostOnLine;
    return LocalDateTime.of(today.getYear(), today.getMonth(), today.getDayOfMonth(), startTime.getHour(), startTime.getMinute()).plusDays(1).plusDays(daysNeeded).plusMinutes(manufacturingTimeInMinutes - maxTimeNeededForWorkPostOnLine).plusMinutes(remainingMinutesForLastDay);
  }
}
