
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("application/json, text/plain, */*")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.9")
		.contentTypeHeader("application/json;charset=UTF-8")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")

	val headers_0 = Map("Origin" -> "http://localhost:8080")

    val uri1 = "http://localhost:8080/sample-hystrix-aggregate/messageDirectCommand"

	val scn = scenario("RecordedSimulation")
		.exec(http("request_0")
			.post("/sample-hystrix-aggregate/messageDirectCommand")
			.headers(headers_0)
			.body(RawFileBody("RecordedSimulation_0000_request.txt")))

	setUp(scn.inject(rampUsers(1000) during (100 seconds))).protocols(httpProtocol)
}