"use client"

import type React from "react"

import { useState, useEffect, useRef } from "react"
import { cn } from "@/lib/utils"
import { Loader2 } from "lucide-react"

interface PullToRefreshProps {
  onRefresh: () => Promise<void>
  children: React.ReactNode
  className?: string
  pullDistance?: number
  loadingText?: string
}

export function PullToRefresh({
  onRefresh,
  children,
  className,
  pullDistance = 80,
  loadingText = "Refreshing...",
}: PullToRefreshProps) {
  const [isPulling, setIsPulling] = useState(false)
  const [pullY, setPullY] = useState(0)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const startY = useRef(0)

  useEffect(() => {
    const container = containerRef.current
    if (!container) return

    const handleTouchStart = (e: TouchEvent) => {
      // Only enable pull to refresh when at the top of the page
      if (window.scrollY === 0) {
        startY.current = e.touches[0].clientY
        setIsPulling(true)
      }
    }

    const handleTouchMove = (e: TouchEvent) => {
      if (!isPulling) return

      const currentY = e.touches[0].clientY
      const diff = currentY - startY.current

      // Only allow pulling down
      if (diff > 0) {
        // Apply resistance to make it harder to pull
        const newPullY = Math.min(pullDistance, diff * 0.5)
        setPullY(newPullY)

        // Prevent default when pulling
        if (newPullY > 0) {
          e.preventDefault()
        }
      }
    }

    const handleTouchEnd = async () => {
      if (!isPulling) return

      // If pulled enough, trigger refresh
      if (pullY >= pullDistance) {
        setIsRefreshing(true)
        setPullY(0)

        try {
          await onRefresh()
        } finally {
          setIsRefreshing(false)
        }
      } else {
        // Reset pull state
        setPullY(0)
      }

      setIsPulling(false)
    }

    container.addEventListener("touchstart", handleTouchStart)
    container.addEventListener("touchmove", handleTouchMove, { passive: false })
    container.addEventListener("touchend", handleTouchEnd)

    return () => {
      container.removeEventListener("touchstart", handleTouchStart)
      container.removeEventListener("touchmove", handleTouchMove)
      container.removeEventListener("touchend", handleTouchEnd)
    }
  }, [isPulling, pullY, onRefresh, pullDistance])

  return (
    <div ref={containerRef} className={cn("relative", className)}>
      {/* Pull indicator */}
      <div
        className={cn(
          "absolute left-0 right-0 flex items-center justify-center transition-transform",
          pullY > 0 || isRefreshing ? "opacity-100" : "opacity-0",
        )}
        style={{
          transform: `translateY(${pullY - 60}px)`,
          height: "60px",
        }}
      >
        {isRefreshing ? (
          <div className="flex items-center gap-2">
            <Loader2 className="h-5 w-5 animate-spin" />
            <span className="text-sm">{loadingText}</span>
          </div>
        ) : (
          <div className="text-sm text-muted-foreground">
            {pullY >= pullDistance ? "Release to refresh" : "Pull down to refresh"}
          </div>
        )}
      </div>

      {/* Content with pull transform */}
      <div
        style={{
          transform: isRefreshing ? "translateY(0)" : `translateY(${pullY}px)`,
          transition: isPulling ? "none" : "transform 0.3s ease",
        }}
      >
        {children}
      </div>
    </div>
  )
}
