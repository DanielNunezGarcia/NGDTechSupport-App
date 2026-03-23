import fs from "fs";
import path from "path";

const AGENTS_PATH = path.resolve("./agents");

function loadAgent(agentId) {
  const filePath = path.join(AGENTS_PATH, ${agentId}.json);

  if (!fs.existsSync(filePath)) {
    throw new Error(❌ Agente no encontrado: );
  }

  const raw = fs.readFileSync(filePath, "utf-8");
  return JSON.parse(raw);
}

export async function runAgent(agentId, task) {
  try {
    const agent = loadAgent(agentId);

    return {
      status: "ok",
      agent: agent.id,
      role: agent.role,
      goal: agent.goal,
      task: task,
      instructions: agent.instructions
    };

  } catch (error) {
    return {
      status: "error",
      message: error.message
    };
  }
}
