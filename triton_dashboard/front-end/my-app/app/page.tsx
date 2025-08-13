"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Eye, EyeOff, Mail, Lock, User, ArrowRight, Key, CheckCircle, XCircle, Loader2 } from "lucide-react"

export default function AuthPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const [apiKeys, setApiKeys] = useState({
    openai: "",
    gemini: "",
    claude: "",
  })
  const [keyValidation, setKeyValidation] = useState({
    openai: null as boolean | null,
    gemini: null as boolean | null,
    claude: null as boolean | null,
  })
  const [validatingKeys, setValidatingKeys] = useState({
    openai: false,
    gemini: false,
    claude: false,
  })

  const validateApiKey = async (provider: "openai" | "gemini" | "claude", key: string) => {
    if (!key.trim()) {
      setKeyValidation((prev) => ({ ...prev, [provider]: null }))
      return
    }

    setValidatingKeys((prev) => ({ ...prev, [provider]: true }))

    try {
      // Simulate API key validation - replace with actual validation logic
      const response = await fetch("/api/validate-key", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ provider, key }),
      })

      const isValid = response.ok
      setKeyValidation((prev) => ({ ...prev, [provider]: isValid }))
    } catch (error) {
      setKeyValidation((prev) => ({ ...prev, [provider]: false }))
    } finally {
      setValidatingKeys((prev) => ({ ...prev, [provider]: false }))
    }
  }

  const handleApiKeyChange = (provider: "openai" | "gemini" | "claude", value: string) => {
    setApiKeys((prev) => ({ ...prev, [provider]: value }))
    // Debounce validation
    setTimeout(() => validateApiKey(provider, value), 500)
  }

  const getValidationIcon = (provider: "openai" | "gemini" | "claude") => {
    if (validatingKeys[provider]) {
      return <Loader2 className="h-4 w-4 animate-spin text-slate-400" />
    }
    if (keyValidation[provider] === true) {
      return <CheckCircle className="h-4 w-4 text-green-500" />
    }
    if (keyValidation[provider] === false) {
      return <XCircle className="h-4 w-4 text-red-500" />
    }
    return null
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-600 to-purple-700 rounded-2xl mb-4 shadow-lg">
            <div className="relative">
              {/* Microservices nodes */}
              <div className="flex items-center space-x-1">
                <div className="w-2 h-2 bg-white rounded-full"></div>
                <div className="w-2 h-2 bg-white/80 rounded-full"></div>
                <div className="w-2 h-2 bg-white rounded-full"></div>
              </div>
              {/* Connection lines */}
              <div className="absolute top-1 left-0 w-full h-px bg-white/60"></div>
              <div className="absolute -top-1 left-0 w-full h-px bg-white/40"></div>
              {/* AI brain symbol */}
              <div className="mt-2 flex justify-center">
                <div className="w-4 h-3 border-2 border-white rounded-t-full relative">
                  <div className="absolute inset-x-0 top-1 h-px bg-white"></div>
                  <div className="absolute left-1 top-0.5 w-px h-1 bg-white"></div>
                  <div className="absolute right-1 top-0.5 w-px h-1 bg-white"></div>
                </div>
              </div>
            </div>
          </div>
          <h1 className="text-3xl font-bold text-slate-900 mb-2">MSA Deploy</h1>
          <p className="text-slate-600">AI-powered microservices deployment with RAG intelligence</p>
        </div>

        {/* Auth Card */}
        <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-sm">
          <CardHeader className="pb-4">
            <CardTitle className="text-center text-slate-900">Get Started</CardTitle>
            <CardDescription className="text-center text-slate-600">Access your deployment platform</CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="login" className="w-full">
              <TabsList className="grid w-full grid-cols-2 mb-6">
                <TabsTrigger value="login" className="font-medium">
                  Log In
                </TabsTrigger>
                <TabsTrigger value="signup" className="font-medium">
                  Sign Up
                </TabsTrigger>
              </TabsList>

              {/* Login Tab */}
              <TabsContent value="login" className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="login-email" className="text-slate-700 font-medium">
                    Email
                  </Label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <Input
                      id="login-email"
                      type="email"
                      placeholder="Enter your email"
                      className="pl-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="login-password" className="text-slate-700 font-medium">
                    Password
                  </Label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <Input
                      id="login-password"
                      type={showPassword ? "text" : "password"}
                      placeholder="Enter your password"
                      className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between text-sm">
                  <label className="flex items-center space-x-2 text-slate-600">
                    <input type="checkbox" className="rounded border-slate-300" />
                    <span>Remember me</span>
                  </label>
                  <button className="text-slate-900 hover:text-slate-700 font-medium">Forgot password?</button>
                </div>

                <Button className="w-full h-12 bg-slate-900 hover:bg-slate-800 text-white font-medium group">
                  Log In
                  <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </Button>
              </TabsContent>

              {/* Sign Up Tab */}
              <TabsContent value="signup" className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="signup-name" className="text-slate-700 font-medium">
                    Full Name
                  </Label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <Input
                      id="signup-name"
                      type="text"
                      placeholder="Enter your full name"
                      className="pl-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="signup-email" className="text-slate-700 font-medium">
                    Email
                  </Label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <Input
                      id="signup-email"
                      type="email"
                      placeholder="Enter your email"
                      className="pl-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="signup-password" className="text-slate-700 font-medium">
                    Password
                  </Label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <Input
                      id="signup-password"
                      type={showPassword ? "text" : "password"}
                      placeholder="Create a password"
                      className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirm-password" className="text-slate-700 font-medium">
                    Confirm Password
                  </Label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <Input
                      id="confirm-password"
                      type={showConfirmPassword ? "text" : "password"}
                      placeholder="Confirm your password"
                      className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                <div className="space-y-4 pt-4 border-t border-slate-200">
                  <div className="text-center">
                    <Label className="text-slate-700 font-medium text-sm">LLM Provider API Keys</Label>
                    <p className="text-xs text-slate-500 mt-1">Configure your AI providers</p>
                  </div>

                  {/* OpenAI API Key */}
                  <div className="space-y-2">
                    <Label htmlFor="openai-key" className="text-slate-700 font-medium text-sm">
                      OpenAI API Key
                    </Label>
                    <div className="relative">
                      <Key className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                      <Input
                        id="openai-key"
                        type="password"
                        placeholder="sk-..."
                        value={apiKeys.openai}
                        onChange={(e) => handleApiKeyChange("openai", e.target.value)}
                        className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                      />
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">{getValidationIcon("openai")}</div>
                    </div>
                  </div>

                  {/* Gemini API Key */}
                  <div className="space-y-2">
                    <Label htmlFor="gemini-key" className="text-slate-700 font-medium text-sm">
                      Gemini API Key
                    </Label>
                    <div className="relative">
                      <Key className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                      <Input
                        id="gemini-key"
                        type="password"
                        placeholder="AIza..."
                        value={apiKeys.gemini}
                        onChange={(e) => handleApiKeyChange("gemini", e.target.value)}
                        className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                      />
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">{getValidationIcon("gemini")}</div>
                    </div>
                  </div>

                  {/* Claude API Key */}
                  <div className="space-y-2">
                    <Label htmlFor="claude-key" className="text-slate-700 font-medium text-sm">
                      Claude API Key
                    </Label>
                    <div className="relative">
                      <Key className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                      <Input
                        id="claude-key"
                        type="password"
                        placeholder="sk-ant-..."
                        value={apiKeys.claude}
                        onChange={(e) => handleApiKeyChange("claude", e.target.value)}
                        className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                      />
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">{getValidationIcon("claude")}</div>
                    </div>
                  </div>
                </div>

                <div className="text-sm">
                  <label className="flex items-start space-x-2 text-slate-600">
                    <input type="checkbox" className="rounded border-slate-300 mt-0.5" />
                    <span>
                      I agree to the{" "}
                      <button className="text-slate-900 hover:text-slate-700 font-medium underline">
                        Terms of Service
                      </button>{" "}
                      and{" "}
                      <button className="text-slate-900 hover:text-slate-700 font-medium underline">
                        Privacy Policy
                      </button>
                    </span>
                  </label>
                </div>

                <Button className="w-full h-12 bg-slate-900 hover:bg-slate-800 text-white font-medium group">
                  Create Account
                  <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </Button>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="text-center mt-6 text-sm text-slate-500">
          <p>
            By continuing, you agree to our{" "}
            <button className="text-slate-700 hover:text-slate-900 underline">Terms</button> and{" "}
            <button className="text-slate-700 hover:text-slate-900 underline">Privacy Policy</button>
          </p>
        </div>
      </div>
    </div>
  )
}
