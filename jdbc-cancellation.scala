#!/usr/bin/env -S scala-cli shebang -q

//> using scala "3.3.1"
//> using jvm "zulu:21"
//> using dep "org.postgresql:postgresql:42.6.0"

def exec(waitSecs: Int) = {
  val conn = java.sql.DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "pass")
  try {
    val stm = conn.prepareStatement(s"SELECT pg_sleep($waitSecs)")
    try {
      println("Executing...")
      assert(stm.executeQuery().next())
      println("Done.")
    } finally stm.close()
  } catch {
    case e => e.printStackTrace()
  } finally {
    conn.close()
  }
}

@main def main() = {
  println("=======================================")
  println("JDK version: " + System.getProperty("java.version"))
  println("JDK vendor:  " + System.getProperty("java.vendor"))
  println("=======================================")
  println("Test 1")
  println("=======================================")
  val th = Thread.ofVirtual.start(() => {
    Thread.currentThread().interrupt()
    println("Starting...")
    exec(2)
    println("Finished.")
    println("Still interrupted? " + Thread.currentThread().isInterrupted())
  })

  th.join()

  println("=======================================")
  println("Test 2")
  println("=======================================")

  val th2 = Thread.ofVirtual.start(() => {
    println("Starting...")
    exec(5)
    println("Finished.")
    println("Still interrupted? " + Thread.currentThread().isInterrupted())
  })

  Thread.sleep(1000)
  th2.interrupt()
  th2.join()
}
