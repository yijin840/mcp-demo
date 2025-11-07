import {createReactAgent} from "@langchain/langgraph/prebuilt";
import {MultiServerMCPClient} from "@langchain/mcp-adapters";
import {ChatGoogleGenerativeAI} from "@langchain/google-genai";
import {HumanMessage} from "@langchain/core/messages";
import 'dotenv/config';

// 禁用 LangSmith 追踪
process.env.LANGCHAIN_TRACING_V2 = "false";
process.env.LANGCHAIN_CALLBACKS_BACKGROUND = "false";

const THIRDWEB_SECRET_KEY = process.env.THIRDWEB_SECRET_KEY;
const OTHER_API_KEY = process.env.OTHER_API_KEY!;

if (!THIRDWEB_SECRET_KEY || !OTHER_API_KEY) {
    throw new Error("请在 .env 文件中设置 THIRDWEB_SECRET_KEY 和 OTHER_API_KEY。");
}

/**
 * 创建可支付的 AI 代理执行器
 */
async function createPayableAgentExecutor() {
    // 1. 初始化 LLM 模型
    const model = new ChatGoogleGenerativeAI({
        model: "gemini-2.5-flash",
        apiKey: OTHER_API_KEY
    });

    // 2. 测试模型连通性
    console.log("模型连接测试中...");
    try {
        await model.invoke([new HumanMessage("测试")]);
        console.log("模型连接成功.");
    } catch (error) {
        console.error("模型连接失败，请检查 OTHER_API_KEY 和 baseURL。");
        throw error;
    }

    const mcpUrl = `http://localhost:4001/mcp`;
    const client = new MultiServerMCPClient({
        local: {url: mcpUrl},
    });

    console.log("初始化 MCP 连接...");
    await client.initializeConnections();

    const tools = await client.getTools();
    console.log(`可用工具: ${tools.map(t => t.name).join(", ")}`);
    // 4. 创建 React Agent
    const agentExecutor = createReactAgent({
        llm: model,
        tools: tools,
        prompt: `你是一个智能助手`,
    });

    return agentExecutor;
}

/**
 * 主函数：运行 AI 代理任务
 */
async function main() {
    try {
        console.log("初始化 X402 AI 代理...");
        const agentExecutor = await createPayableAgentExecutor();

        const createAgentInput = (content: string) => ({
            messages: [new HumanMessage(content)],
        });

        // 任务 1: 查询可用服务
        const input11 = "查询天气，把完整的返回信息都告诉我，要求格式要容易阅读，不要有markdown，并且全部用中文名称";
        console.log(`用户输入: ${input11}`);
        const response11 = await agentExecutor.invoke(createAgentInput(input11));
        const output11 = response11.messages[response11.messages.length - 1]?.content ?? "未能获取回复内容";
        console.log("回复:", output11);

    } catch (error: any) {
        console.error("\n执行过程中发生错误。");
        console.error("错误详情:", error.message);
    }
}

main().catch(console.error);
