package org.yijin.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author : yijin
 * @email : yijin840@gmail.com
 * @created : 10/31/25 2:55 PM
 **/
@SpringBootApplication
@ComponentScan(basePackages = "org.yijin.mcp")
public class McpApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }

}
