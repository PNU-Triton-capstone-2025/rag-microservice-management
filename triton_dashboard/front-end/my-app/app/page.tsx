"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Eye, EyeOff, Lock, User, ArrowRight, Key, CheckCircle, XCircle, Loader2 } from "lucide-react"

export default function AuthPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [loginData, setLoginData] = useState({
    username: "",
    password: "",
  })
  const [signupData, setSignupData] = useState({
    username: "",
    password: "",
    confirmPassword: "",
  })
  const [isLoggingIn, setIsLoggingIn] = useState(false)
  const [isSigningUp, setIsSigningUp] = useState(false)

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

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoggingIn(true)

    try {
      const response = await fetch("http://192.168.0.126:8080/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: loginData.username,
          password: loginData.password,
        }),
      })

      if (response.ok) {
        const result = await response.json()
        console.log("Login successful:", result)
        // Handle successful login (e.g., redirect, store token, etc.)
      } else {
        console.error("Login failed:", response.statusText)
        // Handle login error
      }
    } catch (error) {
      console.error("Login error:", error)
    } finally {
      setIsLoggingIn(false)
    }
  }

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault()

    if (signupData.password !== signupData.confirmPassword) {
      alert("Passwords do not match")
      return
    }

    setIsSigningUp(true)

    try {
      const response = await fetch("http://192.168.0.126:8080/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: signupData.username,
          password: signupData.password,
          apiKeys: apiKeys,
        }),
      })

      if (response.ok) {
        const result = await response.json()
        console.log("Registration successful:", result)
        // Handle successful registration (e.g., redirect, show success message, etc.)
      } else {
        console.error("Registration failed:", response.statusText)
        // Handle registration error
      }
    } catch (error) {
      console.error("Registration error:", error)
    } finally {
      setIsSigningUp(false)
    }
  }

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
      <div className="w-full max-w-6xl">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-slate-800 rounded-xl mb-4 shadow-lg">
            <svg width="32" height="32" viewBox="0 0 32 32" className="text-white">
              {/* Central node representing main service */}
              <circle cx="16" cy="16" r="4" fill="currentColor" />

              {/* Surrounding nodes representing microservices */}
              <circle cx="8" cy="8" r="2.5" fill="currentColor" opacity="0.8" />
              <circle cx="24" cy="8" r="2.5" fill="currentColor" opacity="0.8" />
              <circle cx="8" cy="24" r="2.5" fill="currentColor" opacity="0.8" />
              <circle cx="24" cy="24" r="2.5" fill="currentColor" opacity="0.8" />

              {/* Connection lines */}
              <line x1="12" y1="12" x2="8" y2="8" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <line x1="20" y1="12" x2="24" y2="8" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <line x1="12" y1="20" x2="8" y2="24" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <line x1="20" y1="20" x2="24" y2="24" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />

              {/* Letter M overlay */}
              <text x="16" y="20" textAnchor="middle" className="text-xs font-bold" fill="currentColor">
                M
              </text>
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-slate-900 mb-2">Welcome To Triton</h1>
          <p className="text-slate-600">AI-powered microservices deployment with RAG intelligence</p>
        </div>

        {/* Auth Card */}
        <Card className="border-0 shadow-xl bg-white/80 backdrop-blur-sm max-w-md mx-auto">
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
                <form onSubmit={handleLogin} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="login-username" className="text-slate-700 font-medium">
                      Username
                    </Label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                      <Input
                        id="login-username"
                        type="text"
                        placeholder="Enter your username"
                        value={loginData.username}
                        onChange={(e) => setLoginData((prev) => ({ ...prev, username: e.target.value }))}
                        className="pl-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                        required
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
                        value={loginData.password}
                        onChange={(e) => setLoginData((prev) => ({ ...prev, password: e.target.value }))}
                        className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                        required
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
                    <button type="button" className="text-slate-900 hover:text-slate-700 font-medium">
                      Forgot password?
                    </button>
                  </div>

                  <Button
                    type="submit"
                    disabled={isLoggingIn}
                    className="w-full h-12 bg-slate-900 hover:bg-slate-800 text-white font-medium group disabled:opacity-50"
                  >
                    {isLoggingIn ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Logging In...
                      </>
                    ) : (
                      <>
                        Log In
                        <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                      </>
                    )}
                  </Button>
                </form>
              </TabsContent>

              {/* Sign Up Tab */}
              <TabsContent value="signup" className="space-y-4">
                <form onSubmit={handleSignup} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="signup-username" className="text-slate-700 font-medium">
                      Username
                    </Label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                      <Input
                        id="signup-username"
                        type="text"
                        placeholder="Choose a username"
                        value={signupData.username}
                        onChange={(e) => setSignupData((prev) => ({ ...prev, username: e.target.value }))}
                        className="pl-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                        required
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
                        value={signupData.password}
                        onChange={(e) => setSignupData((prev) => ({ ...prev, password: e.target.value }))}
                        className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                        required
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
                        value={signupData.confirmPassword}
                        onChange={(e) => setSignupData((prev) => ({ ...prev, confirmPassword: e.target.value }))}
                        className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400"
                        required
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

                  <div className="pt-4 border-t border-slate-200">
                    <div className="space-y-4">
                      <div className="text-left">
                        <Label className="text-slate-700 font-medium">LLM Provider API Keys</Label>
                        <p className="text-xs text-slate-500 mt-1">Configure your AI providers (optional)</p>
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
                  </div>

                  <div className="text-sm pt-4">
                    <label className="flex items-start space-x-2 text-slate-600">
                      <input type="checkbox" className="rounded border-slate-300 mt-0.5" required />
                      <span>
                        I agree to the{" "}
                        <button type="button" className="text-slate-900 hover:text-slate-700 font-medium underline">
                          Terms of Service
                        </button>{" "}
                        and{" "}
                        <button type="button" className="text-slate-900 hover:text-slate-700 font-medium underline">
                          Privacy Policy
                        </button>
                      </span>
                    </label>
                  </div>

                  <Button
                    type="submit"
                    disabled={isSigningUp}
                    className="w-full h-12 bg-slate-900 hover:bg-slate-800 text-white font-medium group disabled:opacity-50"
                  >
                    {isSigningUp ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Creating Account...
                      </>
                    ) : (
                      <>
                        Create Account
                        <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                      </>
                    )}
                  </Button>
                </form>
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
