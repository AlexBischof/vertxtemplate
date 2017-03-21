package de.bischinger.vertx;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.IOException;
import java.util.function.UnaryOperator;

import static io.vertx.core.Vertx.vertx;
import static io.vertx.core.logging.LoggerFactory.getLogger;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Objects.requireNonNull;

/**
 * Created by Alex Bischof on 07.02.2017.
 */
public class MainVerticle extends AbstractVerticle
{
	private UnaryOperator<HttpServerResponse> corsFunction = response ->
	{
		response.putHeader("Access-Control-Allow-Origin", "*");
		response.putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.putHeader("Access-Control-Max-Age", "-1");
		response.putHeader("Access-Control-Allow-Headers", "accept, authorization, content-type, location");
		response.putHeader("Access-Control-Expose-Headers", "accept, authorization, content-type, location");
		return response;
	};

	@Override
	public void start(Future<Void> startFuture) throws Exception
	{
		//Deploy other Verticles
		//vertx.deployVerticle(ImportVerticle.class.getName(),new DeploymentOptions().setWorker(true));

		// Metric logging
		//		MetricsService service = create(vertx);
		//		vertx.setPeriodic(config().getInteger("metric.logging.period", 30_000), event ->
		//				{
		//					getLogger(getClass()).info(service.getMetricsSnapshot("vertx.http.servers.localhost:8084.responses-2xx"));
		//					getLogger(getClass()).info(service.getMetricsSnapshot("vertx.http.servers.localhost:8084.responses-4xx"));
		//					getLogger(getClass()).info(service.getMetricsSnapshot("vertx.http.servers.localhost:8084.responses-5xx"));
		//				}
		//		);

		Integer httpPort = config().getInteger("http.port", 8080);
		Integer httpsPort = config().getInteger("https.port", 8081);
		String host = config().getString("host", "localhost");
		Router httpRouter = Router.router(vertx);

		httpRouter.route().handler(BodyHandler.create());
		httpRouter.get(path("/health")).handler(handleHealth());
		vertx.createHttpServer().requestHandler(httpRouter::accept).listen(httpPort);
		getLogger(getClass()).info(format("Started on http://%s:%s", host, httpPort));

		//Creates server (either with https or only http)
		//		Router httpsRouter = Router.router(vertx);
		//activate cors
		//TODO
		//		httpsRouter.route().handler(CorsHandler.create("*")
		//				.allowedMethod(GET)
		//				.allowedMethod(POST)
		//				.allowedMethod(OPTIONS)
		//				.allowedHeader("Access-Control-Request-Method")
		//				.allowedHeader("Access-Control-Allow-Origin")
		//				.allowedHeader("Access-Control-Allow-Headers")
		//				.allowedHeader("Content-Type"));
		//		vertx.createHttpServer(new HttpServerOptions()
		//				.setSsl(true)
		//				.setKeyStoreOptions(
		//						new JksOptions()
		//								.setPath(config().getString("keystore.path"))
		//								.setPassword(config().getString("keystore.password"))))
		//				.requestHandler(httpsRouter::accept).listen(httpPort, host);
		//		getLogger(getClass()).info(format("Started on https://%s:%s", host, httpsPort));

		startFuture.complete();
	}

	private Handler<RoutingContext> handleHealth()
	{
		return rc -> corsFunction.apply(rc.response()).end(new Version().toString());
	}

	/**
	 * Adds rootContext
	 *
	 * @param path
	 * @return
	 */
	private String path(String path)
	{
		String rootcontext = config().getString("rootcontext", "/");
		requireNonNull(path);

		return "/".equals(rootcontext)
				? path
				: rootcontext + path;
	}

	//TODO
	//	private void enableAuthentication(Router router)
	//	{
	//		router.route().handler(CookieHandler.create());
	//		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
	//		AuthHandler basicAuthHandler = BasicAuthHandler.create(ShiroAuth.create(vertx, PROPERTIES, new JsonObject()));
	//		router.route("/teilnehmer").handler(basicAuthHandler);
	//	}

	public static void main(String[] args) throws IOException
	{
		Vertx vertx = vertx(new VertxOptions()
				.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true).setJmxEnabled(true)));
		vertx.deployVerticle(MainVerticle.class.getName(),
				new DeploymentOptions().setConfig(
						new JsonObject(new String(readAllBytes(get("conf/app-conf.json"))))));
	}
}
