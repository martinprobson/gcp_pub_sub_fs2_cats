import Publish.log
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.permutive.gcp.auth.TokenProvider
import fs2.io.file.Path
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

trait HttpClient:
  
  val client: Resource[IO, Client[IO]] = EmberClientBuilder
    .default[IO]
    //    .withHttp2
    .build
    .mproduct(client =>
      TokenProvider
        .serviceAccount[IO](
          Path(
            "/home/martinr/Projects/github/gcp_pub_sub_cats_fs2/gcp-cloud-function-test-cf6f8304bb7f.json"
          ),
          scope = "https://www.googleapis.com/auth/pubsub" :: Nil,
          client
        )
        .toResource
    )
    .map { case (client, tokenProvider) =>
      tokenProvider.clientMiddleware(client)
    }
    .onFinalize(log.info("closing client"))
