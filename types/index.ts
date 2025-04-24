export interface Exercise {
  id: number
  name: string
  sets: number
  reps: number
  weight?: number
  duration?: number
  notes?: string
}

export interface Workout {
  id: number
  type: string
  date: Date
  duration: number
  distance: number | null
  notes: string
  exercises: Exercise[]
}
