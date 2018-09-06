package co.com.minesweeper.util

import co.com.minesweeper.BaseTest

class TimerTest extends BaseTest{

  "Timer Test Should " -{

    "Start a timer at zero, elapsed time should be greater tan zero" in{

      val timer: Timer = Timer.createTimer()

      timer.start()
      Thread.sleep(1000)
      val elapsed = timer.elapsed()
      elapsed shouldEqual 1L

    }

    "Start a timer in a previous time, elapsed time should be greater tan startAt" in{

      val timer: Timer = Timer.createTimer(5L)

      timer.start()
      Thread.sleep(1000)
      val stoppedAt = timer.stop()

      timer.elapsed() shouldEqual 6L

    }

    "Stop a timer, Elapsed should remain equal as stop time" in{

      val timer: Timer = Timer.createTimer()

      timer.start()
      Thread.sleep(1000)
      val stoppedAt = timer.stop()

      timer.elapsed() shouldEqual stoppedAt

    }

    "Re start an stopped timer, elapsed should be greater than stop time" in{

      val timer: Timer = Timer.createTimer()

      timer.start()
      Thread.sleep(1000)

      val stoppedAt = timer.stop()

      timer.start()

      Thread.sleep(1000)

      timer.elapsed() should be > stoppedAt

    }

    "Stop an stopped timer, should not have any impact on timer" in{

      val timer: Timer = Timer.createTimer()

      timer.start()
      Thread.sleep(1000)

      val stoppedAt = timer.stop()

      timer.stop() shouldEqual stoppedAt

    }



  }

}
