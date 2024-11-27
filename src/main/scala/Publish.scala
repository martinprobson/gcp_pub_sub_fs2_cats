import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.syntax.all.*
import fs2.pubsub.*

object Publish extends IOApp.Simple with HttpClient:

  def log: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def publish(n: Int, publisher: PubSubPublisher[IO, String]): IO[MessageId] =
    for {
      _ <- log.info(s"About to publish: $n")
      msgId <- publisher.publishOne(s"$n - Message")
      _ <- log.info(s"Published: $n - msgId: $msgId")
    } yield msgId

  def publishList(publisher: PubSubPublisher[IO, String]): List[IO[MessageId]] =
    Range(1, 5).inclusive.toList.map { i => publish(i, publisher) }

  override def run: IO[Unit] = client.use { c =>
    val publisher: PubSubPublisher[IO, String] = PubSubPublisher
      .http[IO, String]
      .projectId(ProjectId("gcp-cloud-function-test"))
      .topic(Topic("MyTestTopic"))
      .defaultUri
      .httpClient(c)
      .defaultRetry
    for {
      _ <- log.info("About to publish")
      msgIds <- publishList(publisher).parSequence
      _ <- log.info(s"Published $msgIds")
    } yield ()
  }
