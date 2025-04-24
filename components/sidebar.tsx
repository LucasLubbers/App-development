"use client"

import type * as React from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { BarChart3, CalendarCheck, ChevronRight, Dumbbell, Home, Menu, PlusCircle, Target, User, X } from "lucide-react"

interface SidebarProps extends React.HTMLAttributes<HTMLDivElement> {
  isOpen: boolean
  onToggle: () => void
}

export function Sidebar({ className, isOpen, onToggle }: SidebarProps) {
  const pathname = usePathname()

  const navigation = [
    { name: "Dashboard", href: "/dashboard", icon: Home },
    { name: "Workout toevoegen", href: "/dashboard/add-workout", icon: PlusCircle },
    { name: "Workout geschiedenis", href: "/dashboard/history", icon: CalendarCheck },
    { name: "Statistieken", href: "/dashboard/statistics", icon: BarChart3 },
    { name: "Doelen", href: "/dashboard/goals", icon: Target },
    { name: "Profiel", href: "/dashboard/profile", icon: User },
  ]

  return (
    <div className={cn("pb-12", className)}>
      <div className="space-y-4 py-4">
        <div className="px-6 py-2">
          <Button variant="outline" size="icon" onClick={onToggle} className="absolute right-4 top-4 md:hidden">
            <X className="h-4 w-4" />
            <span className="sr-only">Sluit menu</span>
          </Button>
          <div className="flex items-center gap-2">
            <Dumbbell className="h-6 w-6" />
            <h2 className="text-lg font-semibold tracking-tight">Workout Tracker</h2>
          </div>
          <Button variant="ghost" size="icon" onClick={onToggle} className="hidden md:flex absolute right-4 top-4">
            <ChevronRight className={cn("h-4 w-4 transition-transform", isOpen ? "" : "rotate-180")} />
            <span className="sr-only">Toggle sidebar</span>
          </Button>
        </div>
        <div className="px-5">
          <div className="space-y-1">
            {navigation.map((item) => (
              <Link
                key={item.name}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium hover:bg-accent hover:text-accent-foreground",
                  pathname === item.href ? "bg-accent text-accent-foreground" : "text-muted-foreground",
                )}
              >
                <item.icon className="h-4 w-4" />
                <span>{item.name}</span>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

interface MobileSidebarProps {
  isOpen: boolean
  onToggle: () => void
}

export function MobileSidebar({ isOpen, onToggle }: MobileSidebarProps) {
  return (
    <Sheet open={isOpen} onOpenChange={onToggle}>
      <SheetTrigger asChild>
        <Button variant="ghost" size="icon" className="md:hidden">
          <Menu className="h-5 w-5" />
          <span className="sr-only">Open menu</span>
        </Button>
      </SheetTrigger>
      <SheetContent side="left" className="p-0">
        <ScrollArea className="h-full">
          <Sidebar isOpen={isOpen} onToggle={onToggle} />
        </ScrollArea>
      </SheetContent>
    </Sheet>
  )
}
