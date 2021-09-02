package nextstep.jwp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Stream;
import nextstep.jwp.infrastructure.http.FileResolver;
import nextstep.jwp.infrastructure.http.HandlerMapping;
import nextstep.jwp.infrastructure.http.RequestHandler;
import nextstep.jwp.infrastructure.http.handler.FileHandler;
import nextstep.jwp.infrastructure.http.interceptor.InterceptorResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {

    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    private static final int DEFAULT_PORT = 8080;

    private static final FileHandler FILE_HANDLER = new FileHandler(new FileResolver("static"));
    private static final HandlerMapping HANDLER_MAPPING = new HandlerMapping("nextstep.jwp.controller", FILE_HANDLER);
    private static final InterceptorResolver INTERCEPTOR_RESOLVER = new InterceptorResolver("nextstep.jwp.interceptor");
    private static final WebApplicationContext CONTEXT = new WebApplicationContext(HANDLER_MAPPING, INTERCEPTOR_RESOLVER);

    private final int port;

    public WebServer(int port) {
        this.port = checkPort(port);
    }

    public static int defaultPortIfNull(String[] args) {
        return Stream.of(args)
            .findFirst()
            .map(Integer::parseInt)
            .orElse(WebServer.DEFAULT_PORT);
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Web Server started {} port.", serverSocket.getLocalPort());
            handle(serverSocket);
        } catch (IOException exception) {
            logger.error("Exception accepting connection", exception);
        } catch (RuntimeException exception) {
            logger.error("Unexpected error", exception);
        }
    }

    private void handle(ServerSocket serverSocket) throws IOException {
        Socket connection;
        while ((connection = serverSocket.accept()) != null) {
            new Thread(new RequestHandler(connection, CONTEXT)).start();
        }
    }

    private int checkPort(int port) {
        if (port < 1 || 65535 < port) {
            return DEFAULT_PORT;
        }
        return port;
    }
}
