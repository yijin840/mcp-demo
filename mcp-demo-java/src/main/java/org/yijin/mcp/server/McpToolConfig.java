package org.yijin.mcp.server;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

/**
 * @author : yijin
 * @email : yijin840@gmail.com
 * @created : 11/6/25 5:06 PM
 **/
@Configuration
public class McpToolConfig {
    @Bean
    public ToolCallback getCurrentTimeToolCallback() {
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

    @Tool(description = "get desc")
    public String getDesc() {
        return "hello world";
    }

}
