"use client"

import type React from "react"

import { useState, useEffect } from "react"

interface SwipeHandlers {
  onSwipeLeft?: () => void
  onSwipeRight?: () => void
  onSwipeUp?: () => void
  onSwipeDown?: () => void
}

interface SwipeOptions {
  threshold?: number
  preventDefault?: boolean
}

export function useSwipe(ref: React.RefObject<HTMLElement>, handlers: SwipeHandlers, options: SwipeOptions = {}) {
  const { threshold = 50, preventDefault = true } = options
  const [touchStart, setTouchStart] = useState<{ x: number; y: number } | null>(null)

  useEffect(() => {
    const element = ref.current
    if (!element) return

    const handleTouchStart = (e: TouchEvent) => {
      setTouchStart({
        x: e.touches[0].clientX,
        y: e.touches[0].clientY,
      })
    }

    const handleTouchMove = (e: TouchEvent) => {
      if (preventDefault) {
        // Prevent scrolling when swiping
        e.preventDefault()
      }
    }

    const handleTouchEnd = (e: TouchEvent) => {
      if (!touchStart) return

      const touchEnd = {
        x: e.changedTouches[0].clientX,
        y: e.changedTouches[0].clientY,
      }

      const deltaX = touchStart.x - touchEnd.x
      const deltaY = touchStart.y - touchEnd.y

      // Check if the swipe was horizontal or vertical
      if (Math.abs(deltaX) > Math.abs(deltaY)) {
        // Horizontal swipe
        if (deltaX > threshold && handlers.onSwipeLeft) {
          handlers.onSwipeLeft()
        } else if (deltaX < -threshold && handlers.onSwipeRight) {
          handlers.onSwipeRight()
        }
      } else {
        // Vertical swipe
        if (deltaY > threshold && handlers.onSwipeUp) {
          handlers.onSwipeUp()
        } else if (deltaY < -threshold && handlers.onSwipeDown) {
          handlers.onSwipeDown()
        }
      }

      setTouchStart(null)
    }

    element.addEventListener("touchstart", handleTouchStart)
    element.addEventListener("touchmove", handleTouchMove, { passive: !preventDefault })
    element.addEventListener("touchend", handleTouchEnd)

    return () => {
      element.removeEventListener("touchstart", handleTouchStart)
      element.removeEventListener("touchmove", handleTouchMove)
      element.removeEventListener("touchend", handleTouchEnd)
    }
  }, [ref, handlers, touchStart, threshold, preventDefault])
}
