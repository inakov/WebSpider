import web.spider.Spider.Check
import web.spider.{Spider, Getter}

import scala.util.{Success, Failure}
import akka.actor.{Props, ActorSystem}

object Main extends App {
  val system = ActorSystem("akka-web-spider-system")

  val spider = system.actorOf(Props(classOf[Spider]), "spider")
  spider ! Check("http://letitcrash.com/post/56322490862/22-spotlight-startup-when-cluster-size-reached", 2)

  def shutdown(): Unit = {
//    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }

}