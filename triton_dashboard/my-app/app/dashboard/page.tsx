"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import {
  Server,
  Database,
  Activity,
  LogOut,
  CheckCircle,
  AlertCircle,
  XCircle,
  BarChart3,
  Cpu,
  HardDrive,
  Zap,
  MessageSquare,
  Download,
  History,
  Upload,
  Terminal,
  ChevronLeft,
  ChevronRight,
} from "lucide-react"
import { useRouter } from "next/navigation"

export default function Dashboard() {
  const router = useRouter()
  const [user, setUser] = useState<string>("Demo User")
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [showText, setShowText] = useState(true)
  const [activeSection, setActiveSection] = useState("dashboard")

  const handleLogout = () => {
    // Logout logic here
    router.push("/login")
  }

  useEffect(() => {
    if (sidebarOpen) {
      const timer = setTimeout(() => setShowText(true), 300)
      return () => clearTimeout(timer)
    } else {
      setShowText(false)
    }
  }, [sidebarOpen])

  const microservices = [
    { name: "User Service", status: "running", instances: 3, cpu: "45%", memory: "2.1GB" },
    { name: "Order Service", status: "running", instances: 2, cpu: "32%", memory: "1.8GB" },
    { name: "Payment Service", status: "warning", instances: 1, cpu: "78%", memory: "3.2GB" },
    { name: "Notification Service", status: "running", instances: 4, cpu: "23%", memory: "1.2GB" },
    { name: "Analytics Service", status: "error", instances: 0, cpu: "0%", memory: "0GB" },
  ]

  const databases = [
    { name: "PostgreSQL Main", status: "connected", connections: 45, size: "12.3GB" },
    { name: "Redis Cache", status: "connected", connections: 128, size: "2.1GB" },
    { name: "MongoDB Logs", status: "warning", connections: 12, size: "8.7GB" },
  ]

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "running":
      case "connected":
        return <CheckCircle className="h-4 w-4 text-green-500" />
      case "warning":
        return <AlertCircle className="h-4 w-4 text-yellow-500" />
      case "error":
        return <XCircle className="h-4 w-4 text-red-500" />
      default:
        return <AlertCircle className="h-4 w-4 text-slate-400" />
    }
  }

  const sidebarItems = [
    { icon: MessageSquare, label: "RAG Chat Bot for YML", key: "rag-chat" },
    { icon: Download, label: "Log Collector Download", key: "logs" },
    { icon: History, label: "YML Generation History", key: "history" },
    { icon: Upload, label: "Upload Internal Data", key: "upload" },
    { icon: Terminal, label: "SSH Connection", key: "ssh" },
  ]

  const renderContent = () => {
    switch (activeSection) {
      case "rag-chat":
        return (
          <div className="flex items-center justify-center h-96">
            <div className="text-center">
              <MessageSquare className="h-16 w-16 text-slate-400 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-slate-900 mb-2">RAG Chat Bot for YML</h2>
              <p className="text-slate-600">Content coming soon...</p>
            </div>
          </div>
        )
      case "logs":
        return (
          <div className="flex items-center justify-center h-96">
            <div className="text-center">
              <Download className="h-16 w-16 text-slate-400 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-slate-900 mb-2">Log Collector Download</h2>
              <p className="text-slate-600">Content coming soon...</p>
            </div>
          </div>
        )
      case "history":
        return (
          <div className="flex items-center justify-center h-96">
            <div className="text-center">
              <History className="h-16 w-16 text-slate-400 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-slate-900 mb-2">YML Generation History</h2>
              <p className="text-slate-600">Content coming soon...</p>
            </div>
          </div>
        )
      case "upload":
        return (
          <div className="flex items-center justify-center h-96">
            <div className="text-center">
              <Upload className="h-16 w-16 text-slate-400 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-slate-900 mb-2">Upload Internal Data to RAG</h2>
              <p className="text-slate-600">Content coming soon...</p>
            </div>
          </div>
        )
      case "ssh":
        return (
          <div className="flex items-center justify-center h-96">
            <div className="text-center">
              <Terminal className="h-16 w-16 text-slate-400 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-slate-900 mb-2">SSH Connection</h2>
              <p className="text-slate-600">Content coming soon...</p>
            </div>
          </div>
        )
      default:
        return (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-slate-600">Total Services</p>
                      <p className="text-3xl font-bold text-slate-900">12</p>
                    </div>
                    <Server className="h-8 w-8 text-slate-400" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-slate-600">Active Instances</p>
                      <p className="text-3xl font-bold text-slate-900">28</p>
                    </div>
                    <Activity className="h-8 w-8 text-slate-400" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-slate-600">CPU Usage</p>
                      <p className="text-3xl font-bold text-slate-900">42%</p>
                    </div>
                    <Cpu className="h-8 w-8 text-slate-400" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-slate-600">Memory Usage</p>
                      <p className="text-3xl font-bold text-slate-900">8.2GB</p>
                    </div>
                    <HardDrive className="h-8 w-8 text-slate-400" />
                  </div>
                </CardContent>
              </Card>
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Server className="h-5 w-5" />
                    <span>Microservices Status</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {microservices.map((service, index) => (
                      <div key={index} className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                        <div className="flex items-center space-x-3">
                          {getStatusIcon(service.status)}
                          <div>
                            <p className="font-medium text-slate-900">{service.name}</p>
                            <p className="text-sm text-slate-600">{service.instances} instances</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-medium text-slate-900">{service.cpu}</p>
                          <p className="text-sm text-slate-600">{service.memory}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Database className="h-5 w-5" />
                    <span>Database Connections</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {databases.map((db, index) => (
                      <div key={index} className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                        <div className="flex items-center space-x-3">
                          {getStatusIcon(db.status)}
                          <div>
                            <p className="font-medium text-slate-900">{db.name}</p>
                            <p className="text-sm text-slate-600">{db.connections} connections</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-medium text-slate-900">{db.size}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <Zap className="h-5 w-5" />
                    <span>LLM Provider Status</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        <div>
                          <p className="font-medium text-slate-900">OpenAI GPT-4</p>
                          <p className="text-sm text-slate-600">API Key Active</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-medium text-slate-900">98.5%</p>
                        <p className="text-sm text-slate-600">Uptime</p>
                      </div>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        <div>
                          <p className="font-medium text-slate-900">Google Gemini</p>
                          <p className="text-sm text-slate-600">API Key Active</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-medium text-slate-900">99.2%</p>
                        <p className="text-sm text-slate-600">Uptime</p>
                      </div>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                      <div className="flex items-center space-x-3">
                        <AlertCircle className="h-4 w-4 text-yellow-500" />
                        <div>
                          <p className="font-medium text-slate-900">Anthropic Claude</p>
                          <p className="text-sm text-slate-600">Rate Limited</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-medium text-slate-900">95.1%</p>
                        <p className="text-sm text-slate-600">Uptime</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center space-x-2">
                    <BarChart3 className="h-5 w-5" />
                    <span>Recent Activity</span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-start space-x-3">
                      <div className="w-2 h-2 bg-green-500 rounded-full mt-2"></div>
                      <div>
                        <p className="text-sm font-medium text-slate-900">User Service deployed</p>
                        <p className="text-xs text-slate-600">2 minutes ago</p>
                      </div>
                    </div>
                    <div className="flex items-start space-x-3">
                      <div className="w-2 h-2 bg-yellow-500 rounded-full mt-2"></div>
                      <div>
                        <p className="text-sm font-medium text-slate-900">Payment Service scaling up</p>
                        <p className="text-xs text-slate-600">5 minutes ago</p>
                      </div>
                    </div>
                    <div className="flex items-start space-x-3">
                      <div className="w-2 h-2 bg-red-500 rounded-full mt-2"></div>
                      <div>
                        <p className="text-sm font-medium text-slate-900">Analytics Service failed</p>
                        <p className="text-xs text-slate-600">12 minutes ago</p>
                      </div>
                    </div>
                    <div className="flex items-start space-x-3">
                      <div className="w-2 h-2 bg-blue-500 rounded-full mt-2"></div>
                      <div>
                        <p className="text-sm font-medium text-slate-900">Database backup completed</p>
                        <p className="text-xs text-slate-600">1 hour ago</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </>
        )
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 flex">
      <div
        className={`${sidebarOpen ? "w-64" : "w-18"} transition-all duration-300 bg-white shadow-lg border-r border-slate-200 flex flex-col`}
      >
        <div className="p-4 border-b border-slate-200">
          <div className="flex items-center justify-between">
            {sidebarOpen && showText && (
              <div className="flex items-center space-x-3">
                <div className="w-8 h-8 bg-slate-800 rounded-xl flex items-center justify-center">
                  <svg viewBox="0 0 24 24" className="w-5 h-5 text-white">
                    <circle cx="12" cy="12" r="2" fill="currentColor" />
                    <circle cx="6" cy="6" r="1.5" fill="currentColor" />
                    <circle cx="18" cy="6" r="1.5" fill="currentColor" />
                    <circle cx="6" cy="18" r="1.5" fill="currentColor" />
                    <circle cx="18" cy="18" r="1.5" fill="currentColor" />
                    <path
                      d="M12 10L6 6M12 10L18 6M12 14L6 18M12 14L18 18"
                      stroke="currentColor"
                      strokeWidth="1"
                      fill="none"
                    />
                  </svg>
                </div>
                <h2 className="text-lg font-bold text-slate-900">MSA Deploy</h2>
              </div>
            )}
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="text-slate-600 hover:text-slate-900"
            >
              {sidebarOpen ? <ChevronLeft className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
            </Button>
          </div>
        </div>
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            <li>
              <button
                onClick={() => setActiveSection("dashboard")}
                className={`w-full flex items-center justify-center ${sidebarOpen ? "justify-start" : ""} space-x-3 px-3 py-2 rounded-lg transition-colors ${
                  activeSection === "dashboard"
                    ? "text-slate-900 bg-slate-100"
                    : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
                }`}
              >
                <BarChart3 className="h-5 w-5 flex-shrink-0" />
                {sidebarOpen && showText && <span className="text-sm font-medium">Dashboard</span>}
              </button>
            </li>
            {sidebarItems.map((item, index) => (
              <li key={index}>
                <button
                  onClick={() => setActiveSection(item.key)}
                  className={`w-full flex items-center justify-center ${sidebarOpen ? "justify-start" : ""} space-x-3 px-3 py-2 rounded-lg transition-colors ${
                    activeSection === item.key
                      ? "text-slate-900 bg-slate-100"
                      : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
                  }`}
                >
                  <item.icon className="h-5 w-5 flex-shrink-0" />
                  {sidebarOpen && showText && <span className="text-sm font-medium">{item.label}</span>}
                </button>
              </li>
            ))}
          </ul>
        </nav>
      </div>
      <div className="flex-1 flex flex-col">
        <header className="bg-white shadow-sm border-b border-slate-200">
          <div className="px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center h-16">
              <div className="flex items-center space-x-3">
                <div>
                  <h1 className="text-xl font-bold text-slate-900">Dashboard</h1>
                </div>
              </div>
              <Button variant="ghost" onClick={handleLogout} className="text-slate-600 hover:text-slate-900">
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </Button>
            </div>
          </div>
        </header>
        <main className="flex-1 px-4 sm:px-6 lg:px-8 py-8 overflow-auto">{renderContent()}</main>
      </div>
    </div>
  )
}
