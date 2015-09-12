package web.spider

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import web.spider.Getter.{Done, CheckResult}
import web.spider.Spider.{Result, Check}

/**
 * Created by inakov on 12.09.15.
 */
object Spider {

  case class Check(url: String, depth: Int)
  case class Result(web: Set[String])

}

class Spider extends Actor with ActorLogging{
  var web = Set.empty[String]
  var getters = Set.empty[ActorRef]

  override def receive: Receive = {
    case Check(url, depth) =>
      log.info("Building web for url: {} in depth: {}", url, depth)
      val getter = context.actorOf(Props(classOf[Getter], url, depth))
      getters += getter
    case CheckResult(link, depth) =>
      log.info("Link {} found in depth {}", link, depth)
      if(!web(link) && depth > 0) {
        val getter = context.actorOf(Props(classOf[Getter], link, depth - 1))
        getters += getter
      }
      web += link
    case Done =>
      log.info("Getter finished!")
      getters -= sender
      if(getters.isEmpty) {
        log.info("Spider finished a web: {}", web)
        context.parent ! Result(web)
      }
  }
}
