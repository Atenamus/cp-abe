"use client"

import type React from "react"

import { useState } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import {
  SidebarProvider,
  Sidebar,
  SidebarHeader,
  SidebarContent,
  SidebarFooter,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { Home, Key, Lock, FileText, Settings, LogOut, User, Shield } from "lucide-react"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { useToast } from "@/hooks/use-toast"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const pathname = usePathname()
  const { toast } = useToast()
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    toast({
      title: "Logged out",
      description: "You have been logged out successfully",
    })
    // In a real app, you would redirect to the login page
  }

  const navItems = [
    { href: "/dashboard", label: "Dashboard", icon: Home },
    { href: "/dashboard/keys", label: "Key Management", icon: Key },
    { href: "/dashboard/policies", label: "Policy Builder", icon: Lock },
    { href: "/dashboard/files", label: "File Encryption", icon: FileText },
    { href: "/dashboard/security", label: "Security Status", icon: Shield },
    { href: "/dashboard/settings", label: "Settings", icon: Settings },
  ]

  return (
    <SidebarProvider>
      <div className="flex min-h-screen flex-col bg-gradient-to-r from-blue-200 via-purple-200 to-pink-200 text-black">
        {/* Mobile header */}
        <header className="sticky top-0 z-40 border-b bg-gradient-to-r from-blue-300 to-purple-300 shadow-md md:hidden">
          <div className="container flex h-16 items-center justify-between px-4">
            <div className="flex items-center gap-2">
              <SidebarTrigger />
              <h1 className="text-lg font-bold text-black">CryptoGuard</h1>
            </div>
            <Avatar>
              <AvatarImage src="/placeholder.svg?height=32&width=32" alt="User" />
              <AvatarFallback>JD</AvatarFallback>
            </Avatar>
          </div>
        </header>

        <div className="flex flex-1">
          <Sidebar className="bg-gradient-to-b from-indigo-300 to-purple-400 text-black border-r border-indigo-400 shadow-lg">
            <SidebarHeader className="flex items-center justify-between p-4 border-b border-indigo-400">
              <div className="flex items-center gap-2">
                <div className="rounded-md bg-yellow-300 p-1">
                  <Lock className="h-5 w-5 text-black" />
                </div>
                <span className="text-lg font-semibold">CryptoGuard</span>
              </div>
            </SidebarHeader>
            <SidebarContent>
              <SidebarMenu>
                {navItems.map((item) => (
                  <SidebarMenuItem key={item.href}>
                    <SidebarMenuButton asChild isActive={pathname === item.href} tooltip={item.label}>
                      <Link href={item.href} className="flex items-center gap-3 p-2 rounded-lg hover:bg-purple-400 transition-all">
                        <item.icon className="h-5 w-5 text-yellow-600" />
                        <span>{item.label}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarContent>
            <SidebarFooter className="border-t border-indigo-400 p-4">
              <SidebarMenu>
                <SidebarMenuItem>
                  <SidebarMenuButton asChild tooltip="Profile">
                    <Link href="/dashboard/profile" className="flex items-center gap-3 p-2 rounded-lg hover:bg-purple-400 transition-all">
                      <User className="h-5 w-5 text-yellow-600" />
                      <span>Profile</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
                <SidebarMenuItem>
                  <SidebarMenuButton asChild tooltip="Logout" onClick={handleLogout}>
                    <button className="flex items-center gap-3 p-2 rounded-lg text-red-500 hover:text-red-700 hover:bg-red-300 transition-all">
                      <LogOut className="h-5 w-5" />
                      <span>Logout</span>
                    </button>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarFooter>
          </Sidebar>

          <main className="flex-1 bg-gradient-to-r from-blue-100 to-purple-100 p-6 shadow-inner rounded-lg">
            <div className="container px-4 py-6 md:px-6 md:py-8 text-black">{children}</div>
          </main>
        </div>
      </div>
    </SidebarProvider>
  )
}
