package co.com.minesweeper.util

/**  Mutable Time Tracker using system time.
  *
  *  Mutable failure avoid impl based on TimeTracker from Apache Spark v1.x.x implementation
  *  @author david93111
  *  @constructor create a new timer with given time on seconds.
  *  @param startAt number of second to add to elapsed time
  *
  *  Companion object can be used to create timer with default zero value
  */
sealed class Timer(private var startAt: Long) {

  /** seconds of the current run */
  private var startedAt: Long = 0L

  /** state of the timer, stopped or not */
  private var stopped : Boolean = true

  /** Start the timer if not paused */
  def start(): Unit = {
    if(stopped) {
      stopped = false
      startedAt = System.nanoTime()
    }
  }

  /**  Stop the timer .
    *
    *  @return final time in seconds of current lap
    *
    *  If timer is stopped, returns last lap time
    */
  def stop(): Long = {
    if(stopped){
      startAt
    }else{
      stopped = true
      val elapsed = System.nanoTime() - startedAt
      startedAt = 0L
      startAt = (elapsed / 1e9).round + startAt
      startAt
    }
  }

  /**  Gives current time .
    *
    *  @return current time of lap plus initial given seconds
    *
    *  If timer is stopped, returns last lap time
    */
  def elapsed(): Long ={
    if(stopped){
      startAt
    }else {
      val elapsed = System.nanoTime() - startedAt
      (elapsed / 1e9).round + startAt
    }
  }

}

/**  Provide a Timer instance.
  *
  *  Factory for class [[Timer]] instances.
  */
object Timer{
  def createTimer(startAt: Long = 0L): Timer = new Timer(startAt)
}
