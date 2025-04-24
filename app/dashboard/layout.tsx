"use client"

import type React from "react"
import { useState, useEffect } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Dumbbell, LogOut, Menu } from "lucide-react"
import { cn } from "@/lib/utils"
import { MobileNav } from "@/components/mobile-nav"
import { Sidebar } from "@/components/sidebar"
import { ScrollArea } from "@/components/ui/scroll-area"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [isMobile, setIsMobile] = useState(false)

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768)
      if (window.innerWidth < 768) {
        setSidebarOpen(false)
      } else {
        setSidebarOpen(true)
      }
    }

    checkMobile()
    window.addEventListener("resize", checkMobile)
    return () => window.removeEventListener("resize", checkMobile)
  }, [])

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen)
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <header className="sticky top-0 z-10 border-b bg-background/95 backdrop-blur-md safe-area-top">
        <div className="flex h-14 items-center justify-between px-4">
          <div className="flex items-center gap-2">
            {!isMobile && (
              <Button variant="ghost" size="icon" onClick={toggleSidebar} className="hidden md:flex">
                <Menu className="h-5 w-5" />
                <span className="sr-only">Toggle sidebar</span>
              </Button>
            )}
            <div className="font-bold flex items-center">
              <Dumbbell className="h-5 w-5 mr-2" />
              <span>Workout Tracker</span>
            </div>
          </div>
          <Link href="/">
            <Button variant="ghost" size="icon">
              <LogOut className="h-5 w-5" />
              <span className="sr-only">Logout</span>
            </Button>
          </Link>
        </div>
      </header>

      <div className="flex flex-1">
        {/* Desktop Sidebar */}
        <div
          className={cn(
            "hidden md:block border-r bg-background transition-all duration-300 ease-in-out",
            sidebarOpen ? "w-64" : "w-0 overflow-hidden",
          )}
        >
          <ScrollArea className="h-[calc(100vh-3.5rem)]">
            <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
          </ScrollArea>
        </div>

        {/* Main Content */}
        <main className="flex-1 overflow-auto pb-16 md:pb-0">
          <div className="container max-w-lg py-4 px-4 md:py-6 md:px-6">{children}</div>
        </main>
      </div>

      {/* Mobile Bottom Navigation */}
      <MobileNav />
    </div>
  )
}
