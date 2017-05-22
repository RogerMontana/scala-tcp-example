import java.nio.charset.StandardCharsets.UTF_8

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BidiFlow, Flow, Sink, Source, Tcp}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object TestTaskTcpStreams extends App {
  implicit val system = ActorSystem("akka-stream-tcp-test-task", ConfigFactory.defaultReference())
  implicit val materializer = ActorMaterializer()

  type Message = String
//  type Message = MessageRequest
  val (host, port) = ("localhost", 46235)

  val deserialize:ByteString => Message = _.utf8String
  val serialize:Message => ByteString = message => ByteString(message getBytes UTF_8)

  val incoming:Flow[ByteString, Message, _] = Flow fromFunction deserialize
  val outgoing:Flow[Message, ByteString, _] = Flow fromFunction serialize

  val protocol = BidiFlow.fromFlows(incoming, outgoing)

  def prompt(s:String):Source[Message, _] = Source fromIterator {
    () => Iterator.continually(StdIn readLine s"[$s]> ")
  }

  val print:Sink[Message, _] = Sink foreach println

  args.headOption foreach {
    case "server" => server()
    case "client" => client()
  }

  def server(): Unit =
    Tcp()
      .bind(host, port)
      .runForeach {
        _
          .flow
          .join(protocol).async
          .runWith(prompt("S").async, print) // note .async here
      }

  def client(): Unit =
    Tcp()
      .outgoingConnection(host, port)
      .join(protocol).async
      .runWith(prompt("C").async, print) // note .async here
}