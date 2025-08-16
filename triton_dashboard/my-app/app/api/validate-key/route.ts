import { type NextRequest, NextResponse } from "next/server"

export async function POST(request: NextRequest) {
  try {
    const { provider, key } = await request.json()

    if (!provider || !key) {
      return NextResponse.json({ error: "Provider and key are required" }, { status: 400 })
    }

    let isValid = false

    switch (provider) {
      case "openai":
        isValid = await validateOpenAIKey(key)
        break
      case "gemini":
        isValid = await validateGeminiKey(key)
        break
      case "claude":
        isValid = await validateClaudeKey(key)
        break
      default:
        return NextResponse.json({ error: "Invalid provider" }, { status: 400 })
    }

    return NextResponse.json({ valid: isValid })
  } catch (error) {
    console.error("Key validation error:", error)
    return NextResponse.json({ error: "Validation failed" }, { status: 500 })
  }
}

async function validateOpenAIKey(key: string): Promise<boolean> {
  try {
    const response = await fetch("https://api.openai.com/v1/models", {
      headers: {
        Authorization: `Bearer ${key}`,
        "Content-Type": "application/json",
      },
    })
    return response.ok
  } catch {
    return false
  }
}

async function validateGeminiKey(key: string): Promise<boolean> {
  try {
    const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models?key=${key}`)
    return response.ok
  } catch {
    return false
  }
}

async function validateClaudeKey(key: string): Promise<boolean> {
  try {
    const response = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "x-api-key": key,
        "Content-Type": "application/json",
        "anthropic-version": "2023-06-01",
      },
      body: JSON.stringify({
        model: "claude-3-haiku-20240307",
        max_tokens: 1,
        messages: [{ role: "user", content: "test" }],
      }),
    })
    return response.ok
  } catch {
    return false
  }
}
