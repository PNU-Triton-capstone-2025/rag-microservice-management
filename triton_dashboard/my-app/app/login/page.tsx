"use client"

import type React from "react"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Eye, EyeOff, Lock, User, ArrowRight, ArrowLeft, Loader2, AlertCircle } from "lucide-react"

export default function LoginPage() {
  const router = useRouter()
  const [showPassword, setShowPassword] = useState(false)
  const [loginData, setLoginData] = useState({
    username: "",
    password: "",
  })
  const [isLoggingIn, setIsLoggingIn] = useState(false)
  const [error, setError] = useState("")

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoggingIn(true)
    setError("")

    try {
      const response = await fetch("http://192.168.0.126:8080/api/users/login", {
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
        const data = await response.json()
        localStorage.setItem("authToken", data.token || "demo-token")
        localStorage.setItem("username", loginData.username)
        router.push("/dashboard")
      } else {
        const errorData = await response.json().catch(() => ({}))
        setError(errorData.message || "Invalid username or password. Please try again.")
      }
    } catch (error) {
      console.error("Login error:", error)
      setError("Unable to connect to server. Please check your connection and try again.")
    } finally {
      setIsLoggingIn(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-slate-800 rounded-xl mb-4 shadow-lg">
            <svg width="32" height="32" viewBox="0 0 32 32" className="text-white">
              <circle cx="16" cy="16" r="4" fill="currentColor" />
              <circle cx="8" cy="8" r="2.5" fill="currentColor" opacity="0.8" />
              <circle cx="24" cy="8" r="2.5" fill="currentColor" opacity="0.8" />
              <circle cx="8" cy="24" r="2.5" fill="currentColor" opacity="0.8" />
              <circle cx="24" cy="24" r="2.5" fill="currentColor" opacity="0.8" />
              <line x1="12" y1="12" x2="8" y2="8" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <line x1="20" y1="12" x2="24" y2="8" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <line x1="12" y1="20" x2="8" y2="24" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <line x1="20" y1="20" x2="24" y2="24" stroke="currentColor" strokeWidth="1.5" opacity="0.6" />
              <text x="16" y="20" textAnchor="middle" className="text-xs font-bold" fill="currentColor">
                M
              </text>
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-slate-900 mb-2">MSA Deploy</h1>
          <p className="text-slate-600">AI-powered microservices deployment with RAG intelligence</p>
        </div>

        <Card className="border-0 shadow-xl bg-white backdrop-blur-sm">
          <CardHeader className="pb-4">
            <CardTitle className="text-center text-slate-900">Log In</CardTitle>
            <CardDescription className="text-center text-slate-600">Access your deployment platform</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="space-y-4">
              {error && (
                <div className="flex items-center space-x-2 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700">
                  <AlertCircle className="h-4 w-4 flex-shrink-0" />
                  <span className="text-sm">{error}</span>
                </div>
              )}

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
                    className="pl-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400 bg-white"
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
                    className="pl-10 pr-10 h-12 border-slate-200 focus:border-slate-400 focus:ring-slate-400 bg-white"
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
                  <div className="relative">
                    <input type="checkbox" className="sr-only peer" />
                    <div className="w-4 h-4 border-2 border-slate-300 rounded peer-checked:bg-slate-800 peer-checked:border-slate-800 transition-colors cursor-pointer flex items-center justify-center">
                      <svg
                        className="w-3 h-3 text-white opacity-0 peer-checked:opacity-100 transition-opacity"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                  </div>
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

              <Button
                type="button"
                variant="ghost"
                onClick={() => router.push("/")}
                className="w-full h-12 text-slate-600 hover:text-slate-900 font-medium group"
              >
                <ArrowLeft className="mr-2 h-4 w-4 group-hover:-translate-x-1 transition-transform" />
                Back to Home
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
