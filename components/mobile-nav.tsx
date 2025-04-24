"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { BarChart3, CalendarCheck, Dumbbell, PlusCircle, User } from "lucide-react"
import { cn } from "@/lib/utils"

export function MobileNav() {
  const pathname = usePathname()

  const navItems = [
    { href: "/dashboard", label: "Home", icon: Dumbbell },
    { href: "/dashboard/add-workout", label: "Add", icon: PlusCircle },
    { href: "/dashboard/history", label: "History", icon: CalendarCheck },
    { href: "/dashboard/statistics", label: "Stats", icon: BarChart3 },
    { href: "/dashboard/profile", label: "Profile", icon: User },
  ]

  return (
    <div className="fixed bottom-0 left-0 z-50 w-full h-16 border-t bg-background/95 backdrop-blur-md md:hidden safe-area-bottom">
      <div className="grid h-full grid-cols-5 max-w-md mx-auto">
        {navItems.map((item) => {
          const isActive = pathname === item.href

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex flex-col items-center justify-center transition-colors",
                isActive ? "text-primary" : "text-muted-foreground hover:text-foreground active:text-primary/70",
              )}
            >
              <item.icon className={cn("h-6 w-6 mb-1", isActive ? "text-primary" : "text-muted-foreground")} />
              <span className="text-xs font-medium">{item.label}</span>
            </Link>
          )
        })}
      </div>
    </div>
  )
}
