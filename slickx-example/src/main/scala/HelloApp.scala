import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * HelloApp
  *
  * @author 01372461
  */
object HelloApp extends App {

  def exec[T](future: Future[T]): Unit = Await.result(future, Duration.Inf)

  exec(RepoSupport.userRepo.all().map(_.foreach(println)))

}
