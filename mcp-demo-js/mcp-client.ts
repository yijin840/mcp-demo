import {Client} from '@modelcontextprotocol/sdk/client/index.js';
import {StreamableHTTPClientTransport} from '@modelcontextprotocol/sdk/client/streamableHttp.js';

const SERVER_URL = 'http://localhost:3010/message';
const CLIENT_CONFIG = {
    name: 'local-mcp-client',
    version: '1.0.0',
};

interface TextContent {
    type: 'text';
    text: string;
}

interface ToolResponse {
    content: TextContent[];
}

async function runMcpClient() {
    console.log('--- MCP Client Demo Starting ---');

    let transport;
    try {
        const serverUrl = new URL(SERVER_URL);
        transport = new StreamableHTTPClientTransport(serverUrl);

        const client = new Client(CLIENT_CONFIG, {
            capabilities: {}
        });

        await client.connect(transport);
        console.log(`✅ Connected to ${serverUrl.origin}`);

        // --- 2. 演示核心功能 ---

        // 2.1. 列出可用工具
        console.log('\n--- List Tools ---');
        const listToolsResponse = await client.listTools();
        const toolNames = listToolsResponse.tools.map(t => t.name);
        console.log('Available Tools:', toolNames);

        // 2.2. 调用第一个工具 (get_time)
        console.log('\n--- Call Tool: get_time ---');
        const getTimeResult = await client.callTool({
            name: 'get_time',
            arguments: {}
        }) as ToolResponse;

        if (getTimeResult.content && getTimeResult.content.length > 0) {
            const timeContent = getTimeResult.content[0];
            if (timeContent.type === 'text') {
                console.log('Time Result:', timeContent.text);
            }
        }

        // 2.3. 调用第二个工具 (calculator)
        console.log('\n--- Call Tool: calculator ---');
        const expression = '5 + 4 * 10 / 2';
        const calcResult = await client.callTool({
            name: 'calculator',
            arguments: {expression}
        }) as ToolResponse;

        if (calcResult.content && calcResult.content.length > 0) {
            const calcContent = calcResult.content[0];
            if (calcContent.type === 'text') {
                console.log(`Calc (${expression}): ${calcContent.text}`);
            }
        }

    } catch (error) {
        console.error('\n❌ An error occurred during client run:');
        console.error(error);
    } finally {
        if (transport) {
            try {
                await transport.close();
                console.log('\n🛑 Disconnected.');
            } catch (closeError) {
                console.warn('Warning: Failed to close transport.', closeError);
            }
        }
        console.log('--- MCP Client Demo Finished ---');
    }
}

runMcpClient();