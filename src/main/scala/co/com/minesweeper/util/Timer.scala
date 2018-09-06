package co.com.minesweeper.util

// Resilent implementation, avoid exception at all cost to have a clean implementation
// As its use is designed only for internal purposes
sealed class Timer(private var startAt: Long) {

  private var startedAt: Long = 0L

  private var stopped : Boolean = true

  def start(): Unit = {
    if(stopped) {
      stopped = false
      startedAt = System.nanoTime()
    }
  }

  def stop(): Long = {
    if(stopped){
      startAt
    }else{
      stopped = true
      val elapsed = System.nanoTime() - startedAt
      startedAt = 0L
      startAt = (elapsed / 1e9).toLong + startAt
      startAt
    }
  }

  def elapsed(): Long ={
    if(stopped){
      startAt
    }else {
      val elapsed = System.nanoTime() - startedAt
      (elapsed / 1e9).round + startAt
    }
  }

}

object Timer{
  def createTimer(startAt: Long = 0L): Timer = new Timer(startAt)
}
