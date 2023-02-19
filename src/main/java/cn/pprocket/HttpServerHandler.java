package cn.pprocket;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class HttpServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            String uri = request.uri();
            if ("/favicon.ico".equals(uri)) {
                return;
            }
            log.info(new Date().toString());
            String s = uri;
            log.info(s);
            s = s.replace("https://","").replace("http://","");
            URL url = new URL("https://proxy.pprocket.cn/proxyPath/" + s);
            /*
            if (s != null && s.length() != 0) {
                try {
                    //URL url = new URL("https://proxy.pprocket.cn/" + uri);
                    URL url = new URL("https://proxy.pprocket.cn/proxyAuth/" + s.replace("http://",""));
                    log.info(url.toString());
                    URLConnection urlConnection = url.openConnection();
                    HttpURLConnection connection = (HttpURLConnection) urlConnection;
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    log.info(String.valueOf(responseCode));
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                                (connection.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder bs = new StringBuilder();
                        String l;
                        while ((l = bufferedReader.readLine()) != null) {
                            bs.append(l).append("\n");
                        }
                        //s = cn.hutool.http.HttpUtil.get(bs.toString());
                        s = bs.toString();
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    log.error("", e);
                    return;
                }
            } else {
                log.info("case");
            }

             */
            log.info("请求：" + url);

            s = cn.hutool.http.HttpUtil.get(String.valueOf(url));
            log.info("响应： " + s);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK, Unpooled.wrappedBuffer(s != null ? s
                    .getBytes() : new byte[0]));
            response.headers().set(CONTENT_TYPE, "text/html");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
            ctx.flush();
        } else {
            //这里必须加抛出异常，要不然ab测试的时候一直卡住不动，暂未解决
            throw new Exception();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}