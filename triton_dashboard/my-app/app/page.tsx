"use client"

import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { ArrowRight, LogIn, UserPlus } from "lucide-react"

export default function HomePage() {
  const router = useRouter()

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
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
          <h1 className="text-3xl font-bold text-slate-900 mb-2">MSA Deploy</h1>
          <p className="text-slate-600">AI-powered microservices deployment with RAG intelligence</p>
        </div>

        <Card className="border-0 shadow-xl bg-white backdrop-blur-sm">
          <CardHeader className="pb-4">
            <CardTitle className="text-center text-slate-900">Welcome</CardTitle>
            <CardDescription className="text-center text-slate-600">Choose an option to get started</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button
              onClick={() => router.push("/login")}
              className="w-full h-12 bg-slate-900 hover:bg-slate-800 text-white font-medium group"
            >
              <LogIn className="mr-2 h-4 w-4" />
              Log In
              <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
            </Button>

            <Button
              onClick={() => router.push("/signup")}
              variant="outline"
              className="w-full h-12 border-slate-200 hover:bg-slate-50 text-slate-900 font-medium group"
            >
              <UserPlus className="mr-2 h-4 w-4" />
              Sign Up
              <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
