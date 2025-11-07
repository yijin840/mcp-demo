package org.yijin.mcp.client;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class McpClientDemo {

//    public static void main(String[] args) {
//        try {
//            System.out.println("=== MCP Client Demo ===\n");
//            System.out.println("1. 创建 Transport...");
//            HttpClient.Builder customClientBuilder = HttpClient.newBuilder()
//                    .version(HttpClient.Version.HTTP_1_1)
//                    .connectTimeout(Duration.ofSeconds(10));
//            HttpRequest.Builder sseRequestBuilder = HttpRequest.newBuilder();
//            HttpClientSseClientTransport transport = HttpClientSseClientTransport
//                    .builder("http://localhost:4001")
//                    .sseEndpoint("/mcp/sse")
//                    .clientBuilder(customClientBuilder)
//                    .requestBuilder(sseRequestBuilder)
//                    .customizeRequest(builder -> builder
//                            .header("Accept", "text/event-stream")
//                            .header("Cache-Control", "no-cache")
//                    )
//                    .build();
//
//            // 2. 构建 MCP Client
//            System.out.println("2. 构建 MCP Client...");
//            McpAsyncClient client = McpClient.async(transport)
//                    .requestTimeout(Duration.ofSeconds(30))
//                    .initializationTimeout(Duration.ofSeconds(20))
//                    .build();
//
//            System.out.println("3. 初始化连接...");
//            McpSchema.InitializeResult initResult = client.initialize().block(Duration.ofSeconds(20));
//
//            System.out.println("\n✓ 初始化成功!");
//            System.out.println("  服务器: " + initResult.serverInfo().name() +
//                    " v" + initResult.serverInfo().version());
//            System.out.println("  协议版本: " + initResult.protocolVersion());
//
//            // 4. 获取工具列表...
//            System.out.println("\n4. 获取工具列表...");
//            McpSchema.ListToolsResult toolsList = client.listTools().block(Duration.ofSeconds(5));
//            System.out.println("✓ 可用工具: " + toolsList.tools().size());
//            toolsList.tools().forEach(tool ->
//                    System.out.println("  - " + tool.name() + ": " + tool.description())
//            );
//
//            // 5. 调用 noop-tool...
//            System.out.println("\n5. 调用 noop-tool...");
//            McpSchema.CallToolResult result = client.callTool(
//                    new McpSchema.CallToolRequest("noop-tool", Map.of())
//            ).block(Duration.ofSeconds(10));
//
//            System.out.println("✓ 工具执行成功!");
//            result.content().forEach(content -> {
//                if (content instanceof McpSchema.TextContent text) {
//                    System.out.println("  返回: " + text.text());
//                }
//            });
//
//            // 6. 关闭连接...
//            System.out.println("\n6. 关闭连接...");
//            client.closeGracefully().block(Duration.ofSeconds(5));
//            System.out.println("✓ 完成!\n");
//
//        } catch (Exception e) {
//            System.err.println("\n❌ 错误: 连接或操作失败");
//            e.printStackTrace();
//        }
//    }


    public static void main(String[] args) throws Exception {
        McpClientDemo demo = new McpClientDemo();
//        demo.test();
//        demo.testMessage();
        demo.testCallTool();

    }

    McpAsyncClient client(McpClientTransport transport) {
        AtomicReference<McpAsyncClient> client = new AtomicReference<>();
        McpClient.AsyncSpec builder = McpClient.async(transport).requestTimeout(Duration.ofSeconds(14)).initializationTimeout(Duration.ofSeconds(2)).sampling(req -> Mono.just(new McpSchema.CreateMessageResult(McpSchema.Role.USER, new McpSchema.TextContent("Oh, hi!"), "modelId", McpSchema.CreateMessageResult.StopReason.END_TURN))).capabilities(McpSchema.ClientCapabilities.builder().roots(true).sampling().build());
        client.set(builder.build());

        return client.get();
    }

    public HttpClientSseClientTransport getClientTransport() {
        HttpClient.Builder customClientBuilder = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10));
        HttpRequest.Builder sseRequestBuilder = HttpRequest.newBuilder().header("Accept", "text/event-stream").header("Cache-Control", "no-cache");
        return HttpClientSseClientTransport.builder("http://localhost:4001/mcp").sseEndpoint("/sse").clientBuilder(customClientBuilder).requestBuilder(sseRequestBuilder).customizeRequest(builder -> builder.header("Accept", "text/event-stream").header("Cache-Control", "no-cache")).build();

    }

    public void testMessage() {
        var client = client(getClientTransport());

        // 1. 客户端初始化
        client.initialize().block(Duration.ofSeconds(5));

        // 2. 构造正确的请求类型：CompleteRequest，使用你之前在 McpSchema 里看到的 PromptReference
        McpSchema.CompleteRequest request = new McpSchema.CompleteRequest(
                new McpSchema.ResourceReference("model/llama3"),
                new McpSchema.CompleteRequest.CompleteArgument("message", "请给我讲一个关于 MCP 协议的笑话。")
        );
        Mono<McpSchema.CompleteResult> messageMono = client.completeCompletion(request);

        log.info("等待服务器返回消息...");
        McpSchema.CompleteResult result = messageMono.block();

        log.info("✓ 接收到消息结果: {}", result);
        if (result != null && result.completion() != null) {
            log.info("✓ 接收到文本内容: {}", result.completion().values());
        } else {
            log.error("服务器返回结果为空！");
        }
    }

    public void testCallTool() {
        var client = client(getClientTransport());

        // 1. 客户端初始化
        client.initialize().block(Duration.ofSeconds(5));

        // 2. 构造 CallToolRequest
        Mono<McpSchema.ListToolsResult> listToolsResultMono = client.listTools();
        System.out.println(listToolsResultMono.block().tools());
        // 假设服务器注册了一个名为 "noopTool" 的工具
        // 并假设这个工具接受一个名为 "input" 的参数，值为 "hello"
        Map<String, Object> arguments = Collections.emptyMap(); // 干净利落，TMD！
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                "getCurrentTime", // <-- 替换成你服务器实际注册的工具名称
                arguments
        );

        // 3. 调用 callTool 方法
        log.info("等待服务器返回工具调用结果...");
        Mono<McpSchema.CallToolResult> toolMono = client.callTool(request);

        McpSchema.CallToolResult result = toolMono.block();

        // 4. 打印结果
        log.info("✓ 接收到工具调用结果: {}", result);
        if (result != null) {
            log.info("✓ 工具执行结果: {}", result.content());
        } else {
            log.error("工具调用返回结果为空！");
        }
    }
}
