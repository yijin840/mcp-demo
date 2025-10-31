package org.yijin.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * @author : yijin
 * @email : yijin840@gmail.com
 * @created : 10/31/25 11:40 AM
 **/
public class McpServerDemo {

    public static void runServer() {
        ObjectMapper objectMapper = new ObjectMapper();
        McpSchema.Implementation serverInfo = new McpSchema.Implementation(
                "mcp-server-demo",
                "1.0.0"
        );
        HttpServletSseServerTransportProvider transportProvider = new HttpServletSseServerTransportProvider(objectMapper, "/message");


        McpAsyncServer sever = McpServer.async(new HttpServletSseServerTransportProvider(objectMapper, "/message")).build();
    }

}
