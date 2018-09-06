package co.com.minesweeper.util

sealed class Timer(var startAt: Long) {

  var startedAt: Long = 0L

  def start():Unit = {
    startedAt = System.nanoTime()
  }

  def stop(): Long = {
    if(startedAt==0){
      startAt
    }else{
      val elapsed = System.nanoTime() - startedAt
      startedAt = 0L
      startAt = (elapsed / 1e9).toLong + startAt
      startAt
    }
  }

  def elapsed(): Long ={
    if(startedAt==0){
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
