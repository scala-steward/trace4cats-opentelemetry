package trace4cats.opentelemetry.otlp

import cats.Foldable
import cats.effect.kernel.Temporal
import cats.syntax.all._
import io.opentelemetry.proto.collector.trace.v1.service.TraceService
import org.http4s.client.Client
import org.http4s.{Header, Headers, Uri}
import trace4cats.kernel.SpanExporter
import trace4cats.model.Batch
import trace4cats.opentelemetry.otlp.proto.ExportSpansRequest

object OpenTelemetryOtlpHttp4sGrpcSpanExporter {

  /** Construct a OTLP/gRPC exporter derived using http4s-grpc
    *
    * @param client
    *   Must allow HTTP/2 (e.g. withHttp2 on EmberClientBuilder) for gRPC to function
    * @param uri
    * @param staticHeaders
    * @return
    *   OTLP/gRPC exporter
    */
  def fromUri[F[_]: Temporal, G[_]: Foldable](
    client: Client[F],
    uri: Uri,
    staticHeaders: List[Header.ToRaw] = List.empty
  ): SpanExporter[F, G] = new SpanExporter[F, G] {
    val service = TraceService.fromClient[F](client, uri)
    def exportBatch(batch: Batch[G]): F[Unit] = service
      .export(ExportSpansRequest.from(batch), Headers(staticHeaders))
      .void
  }

}
