"use client"

import { useState, useEffect } from "react"
import { Dumbbell } from "lucide-react"
import { cn } from "@/lib/utils"

export function SplashScreen() {
  const [show, setShow] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => {
      setShow(false)
    }, 1500)

    return () => clearTimeout(timer)
  }, [])

  if (!show) return null

  return (
    <div
      className={cn(
        "fixed inset-0 z-50 flex flex-col items-center justify-center bg-background transition-opacity duration-500",
        show ? "opacity-100" : "opacity-0 pointer-events-none",
      )}
    >
      <div className="flex flex-col items-center">
        <Dumbbell className="h-16 w-16 text-primary animate-pulse" />
        <h1 className="mt-4 text-2xl font-bold">Workout Tracker</h1>
        <p className="mt-2 text-muted-foreground">Track your fitness journey</p>
      </div>
    </div>
  )
}
