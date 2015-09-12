package web.spider

import akka.actor.{ActorLogging, Actor}
import org.jsoup.Jsoup
import spray.client.pipelining._
import web.spider.Getter.{CheckResult, Done, GetResponse}
import scala.collection.JavaConversions._
import scala.util.{Success, Failure}

/**
 * Created by inakov on 12.09.15.
 */
object  Getter{
  case class GetResponse(body: String)
  case class CheckResult(link: String, depth: Int)
  case class Done()
}

class Getter(url: String, depth: Int) extends  Actor with ActorLogging{

  implicit val executor = context.dispatcher
  implicit val system = context.system

  val pipeline = sendReceive ~> unmarshal[String]

  val responseFuture = pipeline {
    Get(url)
  }

  responseFuture onComplete{
    case Success(responseContent) => self ! GetResponse(responseContent)
    case Failure(error) => self ! Failure(error)
  }

  override def receive: Receive = {
    case GetResponse(body) =>
      for (link <- findLinks(body)) {
        log.info("CheckResult with link {}", link)
        context.parent ! CheckResult(link, depth)
      }
      stop()
    case Failure =>
      log.info("Failure")
      stop()

  }

  def findLinks(body: String): List[String] = {
    val document = Jsoup.parse(body)
    val links = document.select("a[href]")
    for (link <- links.toList) yield {
      link.absUrl("href")
    }
  }

  def stop() = {
    context.parent ! Done
    context.stop(self)
  }

}
