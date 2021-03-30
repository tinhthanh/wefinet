package com.citi.winner21.ultils;


import org.apache.http.Header;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class HttpUtil {
    private static final Logger LOGGER = Logger.getLogger(HttpUtil.class.getName());

    private ClientHttpRequestFactory requestFactory;
    private RestTemplate restTemplate;

    private static final int REQUEST_TIMEOUT = 30 * 1000; // 30 sec

    private static final List<Header> DEFAULT_HEADERS = getDefaultHeaders();


    private synchronized RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            try {
                restTemplate = new RestTemplate(getRequestFactory());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, String.format("Can't initialize RestTemplate -> %s", e.getMessage()), e);
            }
        }
        return restTemplate;
    }

    private synchronized ClientHttpRequestFactory getRequestFactory() {
        if (requestFactory == null) {
            try {
                SSLConnectionSocketFactory sslFactory = createTrustAllFactory();
                CloseableHttpClient client = HttpClientBuilder.create().setDefaultHeaders(DEFAULT_HEADERS)
                        .setSSLSocketFactory(sslFactory)
                        .setConnectionManager(new PoolingHttpClientConnectionManager(createTrustAllFactoryRegistry(sslFactory))).build();
                HttpComponentsClientHttpRequestFactory defaultRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
                defaultRequestFactory.setReadTimeout(REQUEST_TIMEOUT);
                defaultRequestFactory.setConnectTimeout(REQUEST_TIMEOUT);
                requestFactory = defaultRequestFactory;
                return requestFactory;
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
                LOGGER.log(Level.WARNING, "Cannot init client", ex);
                throw new RuntimeException(ex);
            }

        }
        return null;
    }

    public <T> ResponseEntity<T> postRequestBody(String path, String json, Class<T> responseType, HttpHeaders headers) throws HttpClientErrorException, HttpServerErrorException {
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return getRestTemplate().postForEntity(path, entity, responseType);
    }

    public <T> ResponseEntity<T> sendRequest(String path, Class<T> responseType, HttpHeaders headers, HttpMethod method) {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return getRestTemplate().exchange(path, method, entity, responseType);
    }


    private static List<Header> getDefaultHeaders() {
        return Arrays.asList(new BasicHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                new BasicHeader(HttpHeaders.CACHE_CONTROL, "max-age=0"),
                new BasicHeader(HttpHeaders.CONNECTION, "keep-alive"),
                new BasicHeader("Keep-Alive", "timeout=5, max=100"),
                new BasicHeader(HttpHeaders.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.3"),
                new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "es-ES,es;q=0.8"),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
                new BasicHeader(HttpHeaders.ACCEPT, "application/json"),
                new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate"),
                new BasicHeader(HttpHeaders.CONTENT_ENCODING, "gzip, deflate"),
                new BasicHeader(HttpHeaders.PRAGMA, ""));
    }


    private static Registry<ConnectionSocketFactory> createTrustAllFactoryRegistry(SSLConnectionSocketFactory sslFactory) {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslFactory).build();
    }

    private static SSLConnectionSocketFactory createTrustAllFactory() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return new SSLConnectionSocketFactory(SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build(), (host, ssl) -> true);
    }
}
