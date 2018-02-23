package com.taifeng;

import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/**
 * @author 良仔
 */
public class HttpUtil implements Serializable {

    private static final long serialVersionUID = -5704424153620457369L;
    public String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36";
    public int connectionTimeOut = 5000;
    public int socketTimeOut = 10000;
    public int maxTotal = 20;
    public int maxPerRoute = 3;
    public boolean ignoreCookies = true;
    public boolean redirectsEnabled = true;
    public List<Header> headers = null;
    public HttpClientContext context = null;
    private CloseableHttpClient closeableHttpClient = null;
    
    public HttpUtil() {
        headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Cache-Control", "max-age=0"));
        headers.add(new BasicHeader("DNT", "1"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));

        initContext(new BasicCookieStore());
    }
    
    public void init() throws Exception {
        try {
            closeableHttpClient = newHttpClient();
        } catch (Exception e) {
            throw new Exception("HttpClient初始化失败");
        }
    }

    private CloseableHttpClient newHttpClient() throws Exception {
        RequestConfig config = RequestConfig.custom().setCookieSpec(
                ignoreCookies ? CookieSpecs.IGNORE_COOKIES : CookieSpecs.DEFAULT).setRedirectsEnabled(redirectsEnabled)
                .setSocketTimeout(socketTimeOut).setCircularRedirectsAllowed(true).setMaxRedirects(10)
                .setConnectTimeout(connectionTimeOut).build();
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { truseAllManager }, null);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory> create().register("http",
                            PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();

            PoolingHttpClientConnectionManager poolManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            poolManager.setMaxTotal(maxTotal);
            poolManager.setDefaultMaxPerRoute(maxPerRoute);
            poolManager.setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(Charset.defaultCharset())
                    .build());
            poolManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(socketTimeOut).build());

            return HttpClients.custom().setDefaultRequestConfig(config).setDefaultHeaders(headers).setUserAgent(
                    userAgent).setServiceUnavailableRetryStrategy(
                    new DefaultServiceUnavailableRetryStrategy(2, 2000)).setConnectionManager(poolManager).build();
        } catch (Exception e) {
            throw e;
        }
    }
    public CloseableHttpResponse get(String url, Map<String, String> additionalHeaders, RequestConfig requestConfig)
            throws Exception {
        HttpGet get = new HttpGet(url);

        if (requestConfig != null) {
            get.setConfig(requestConfig);
        }
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> head : additionalHeaders.entrySet()) {
                get.addHeader(head.getKey(), head.getValue());
            }
        }

        return getOrPost(get);
    }
    public CloseableHttpResponse post(String url, List<NameValuePair> postParamList,
            Map<String, String> additionalHeaders, RequestConfig requestConfig) throws Exception {
        
        HttpPost post = new HttpPost(url);
        
        if(requestConfig != null){
            post.setConfig(requestConfig);
        }
        if (postParamList != null) {
            HttpEntity httpEntity = new UrlEncodedFormEntity(postParamList, "UTF-8");
            post.setEntity(httpEntity);
        }
        if (additionalHeaders != null) {
            for (Map.Entry<String, String> head : additionalHeaders.entrySet()) {
                post.addHeader(head.getKey(), head.getValue());
            }
        }
        return getOrPost(post);
    }
    public String getStringFromHttpResponse(CloseableHttpResponse response) throws Exception {
        try {
            byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            int returnCode = response.getStatusLine().getStatusCode();
            if (returnCode == HttpStatus.SC_OK) {
                return new String(bytes, "UTF-8");
            } else {
                throw new Exception("返回状态码异常:" + returnCode);
            }
        }catch (Exception e) {
            throw e;
        } finally {
            close(response);
        }
    }
    public byte[] getByteFromHttpResponse(CloseableHttpResponse response) throws Exception {
        try {
            byte[] bytes = EntityUtils.toByteArray(response.getEntity());
            int returnCode = response.getStatusLine().getStatusCode();
            if (returnCode == HttpStatus.SC_OK) {
                return bytes;
            } else {
                throw new Exception("返回状态码异常:" + returnCode);
            }
        }catch (Exception e) {
            throw e;
        } finally {
            close(response);
        }
    }
    public static void close(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private CloseableHttpResponse getOrPost(HttpRequestBase httpRequestBase) throws Exception {
        if (closeableHttpClient == null) {
            throw new Exception("Httpclient初始化失败或是未调用init方法进行初始化；");
        }
        try {
            return closeableHttpClient.execute(httpRequestBase, context);
        } catch (ConnectTimeoutException e) {
            throw new Exception("连接超时:" + httpRequestBase.getURI());
        } catch (SocketTimeoutException e) {
            throw new Exception("读取超时:" + httpRequestBase.getURI());
        } catch (HttpHostConnectException e) {
            throw new Exception("无法连接服务器:" + httpRequestBase.getURI());
        } catch (Exception e) {
            throw new Exception("HTTP请求发生未知错误:" + e.toString());
        }
    }
    private void initContext(CookieStore cs) {
        context = HttpClientContext.create();
        context.setCookieStore(cs);
        context.setCredentialsProvider(new BasicCredentialsProvider());
    }
    private static TrustManager truseAllManager = new X509TrustManager() {
        public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }
        public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };
}
