import {Server} from '@modelcontextprotocol/sdk/server/index.js';
import type {StreamableHTTPServerTransportOptions} from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import {StreamableHTTPServerTransport} from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import type {CallToolRequest, CallToolResult, Implementation} from '@modelcontextprotocol/sdk/types.js';
import {CallToolRequestSchema, ListToolsRequestSchema} from '@modelcontextprotocol/sdk/types.js';
import type {Request, Response} from 'express';
import express from 'express';
import {IncomingMessage, ServerResponse} from 'node:http';

const PORT = 3010;
const MCP_ENDPOINT = '/message';
const APP_INFO: Implementation = {name: 'mcp-server', version: '1.0.0'};

const app = express();
app.use(express.json());
app.use((req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, DELETE');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, mcp-session-id');
    res.setHeader('Access-Control-Expose-Headers', 'mcp-session-id');
    next();
});

const TRANSPORT_OPTIONS: StreamableHTTPServerTransportOptions = {
    sessionIdGenerator: undefined,
    enableJsonResponse: false,
};

const server = new Server(APP_INFO, {capabilities: {tools: {}}});

server.setRequestHandler(ListToolsRequestSchema, async () => {
    return {
        tools: [
            {
                name: 'get_weather',
                description: '获取天气',
                inputSchema: {type: 'object', properties: {city: {type: 'string'}}, required: ['city']}
            },
            {
                name: 'calculator',
                description: '执行计算',
                inputSchema: {type: 'object', properties: {expression: {type: 'string'}}, required: ['expression']}
            },
            {name: 'get_time', description: '获取时间', inputSchema: {type: 'object', properties: {}}},
        ],
    };
});

server.setRequestHandler(CallToolRequestSchema, async (request: CallToolRequest): Promise<CallToolResult> => {
    const {name, arguments: args} = request.params;
    try {
        let resultText: string;
        switch (name) {
            case 'get_weather':
                if (!args || typeof args.city !== 'string') throw new Error('缺少城市');
                const temp = Math.floor(Math.random() * 15 + 15);
                resultText = JSON.stringify({city: args.city, temperature: `${temp}°C`});
                break;
            case 'calculator':
                if (!args || typeof args.expression !== 'string') throw new Error('缺少表达式');
                const result = new Function('return ' + args.expression)();
                resultText = JSON.stringify({result});
                break;
            case 'get_time':
                resultText = JSON.stringify({time: new Date().toLocaleTimeString('zh-CN')});
                break;
            default:
                throw new Error(`未知工具: ${name}`);
        }

        return {content: [{type: 'text', text: resultText}]};

    } catch (error: any) {
        console.error(`Tool execution error for ${name}:`, error.message);
        return {content: [{type: 'text', text: JSON.stringify({error: error.message})}], isError: true};
    }
});

app.all(MCP_ENDPOINT, async (req: Request, res: Response) => {
    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    try {
        // 添加日志：检查客户端发送的实际内容
        console.log('Received request body:', req.body);

        const transport = new StreamableHTTPServerTransport(TRANSPORT_OPTIONS);

        server.connect(transport);

        await transport.handleRequest(
            req as IncomingMessage & { auth?: any },
            res as ServerResponse,
            req.body
        );

    } catch (error: any) {
        console.error('MCP Transport Error:', error);

        if (!res.headersSent) {
            res.status(500).end('Internal Server Error: MCP Transport Failed.');
        } else {
            res.end();
        }
    }
});

app.listen(PORT, () => {
    console.log(`MCP Server running at http://localhost:${PORT}${MCP_ENDPOINT}`);
});
