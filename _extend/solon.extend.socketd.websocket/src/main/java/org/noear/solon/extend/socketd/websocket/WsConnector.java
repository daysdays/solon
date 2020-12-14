package org.noear.solon.extend.socketd.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.noear.solon.core.message.Session;
import org.noear.solon.extend.socketd.Connector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class WsConnector implements Connector<WebSocket> {
    private URI uri;
    private boolean autoReconnect;
    private SSLSocketFactory sslSocketFactory;

    public WsConnector(URI uri, boolean autoReconnect) {
        this.uri = uri;
        this.autoReconnect = autoReconnect;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean autoReconnect() {
        return autoReconnect;
    }

    @Override
    public Class<WebSocket> realType() {
        return WebSocket.class;
    }

    @Override
    public WebSocket open(Session session) {
        try {
            WebSocketClient socket = new WsSocketClientImp(uri, session);

            if ("wss".equals(uri.getScheme())) {
                enableTls(socket);
            }

            socket.connectBlocking();

            return socket;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void enableTls(WebSocketClient client) throws Exception {
        //
        // wss support
        //
        if (sslSocketFactory == null) {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain,
                                                       String authType) {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain,
                                                       String authType) {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());

            sslSocketFactory = sslContext.getSocketFactory();
        }

        client.setSocketFactory(sslSocketFactory);
    }
}
