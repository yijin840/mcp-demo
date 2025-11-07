package org.yijin.mcp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : yijin
 * @email : yijin840@gmail.com
 * @created : 10/31/25 3:25 PM
 **/
@RestController
public class McpController {

    @GetMapping("/")
    public String index() {
        return "MCP Server is running!";
    }

}
