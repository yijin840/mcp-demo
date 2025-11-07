package org.yijin.mcp.server;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * @author : yijin
 * @email : yijin840@gmail.com
 * @created : 11/6/25 5:06 PM
 **/
@Configuration
public class McpToolConfig {
    @Bean
    public ToolCallback getCurrentTimeTool() {
        Supplier<String> timeSupplier = () -> {
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            String result = String.format(
                    "{\"current_time\": \"%s\", \"status\": \"成功获取\"}",
                    time
            );
            System.out.println("✅ 无参数工具 'getCurrentTime' 被调用！");
            return result;
        };
        return FunctionToolCallback.builder("getCurrentTime", timeSupplier)
                .description("获取当前的系统时间和日期，不需要任何输入参数。")
                .build();
    }


    @Bean
    public ToolCallback getWeather() {
        Supplier<String> weatherSupplier = () -> {
            try {
                String format = "%l: %C %t ↓%w";
                String encodedFormat = URLEncoder.encode(format, StandardCharsets.UTF_8);
                String url = "https://wttr.in/Shanghai?format=" + encodedFormat;
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url)) // 简洁输出：天气 + 温度
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (Exception e) {
                return "无法获取天气：" + e.getMessage();
            }
        };

        return FunctionToolCallback.builder("getWeather", weatherSupplier)
                .description("获取当前天气，不需要任何参数。")
                .build();
    }

}
