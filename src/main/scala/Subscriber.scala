import cats.effect.{IO, IOApp}
import com.permutive.common.types.gcp.ProjectId
import fs2.Stream
import fs2.pubsub.{PubSubSubscriber, Subscription}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.*

object Subscriber extends IOApp.Simple with HttpClient:

  def log: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = client.use { c =>

    val subscriber: Stream[IO, Option[String]] = PubSubSubscriber
      .http[IO]
      .projectId(ProjectId("gcp-cloud-function-test"))
      .subscription(Subscription("MyTestSub"))
      .defaultUri
      .httpClient(c)
      .noRetry
      .noErrorHandling
      .batchSize(100)
      .maxLatency(1.second)
      .readMaxMessages(1000)
      .readConcurrency(10)
//      .withDefaults
      .decodeTo[String]
      .subscribeAndAck

    subscriber
      .evalTap(s => log.info(s"s = $s"))
      .timeout(30.seconds)
      .compile
      .drain
  }
