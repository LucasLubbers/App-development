"use client"

import type React from "react"
import type { Exercise } from "@/types"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { format } from "date-fns"
import { nl } from "date-fns/locale"
import { CalendarIcon, Plus, Trash2 } from "lucide-react"
import { cn } from "@/lib/utils"
import { Badge } from "@/components/ui/badge"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"

export default function AddWorkoutPage() {
  const router = useRouter()
  const [date, setDate] = useState<Date | undefined>(new Date())
  const [workoutType, setWorkoutType] = useState("")
  const [duration, setDuration] = useState("")
  const [distance, setDistance] = useState("")
  const [notes, setNotes] = useState("")
  const [exercises, setExercises] = useState<Exercise[]>([])
  const [nextExerciseId, setNextExerciseId] = useState(1)

  const handleAddExercise = () => {
    const newExercise: Exercise = {
      id: nextExerciseId,
      name: "",
      sets: 3,
      reps: 10,
      weight: undefined,
      duration: undefined,
      notes: "",
    }
    setExercises([...exercises, newExercise])
    setNextExerciseId(nextExerciseId + 1)
  }

  const handleUpdateExercise = (id: number, field: keyof Exercise, value: any) => {
    setExercises(exercises.map((exercise) => (exercise.id === id ? { ...exercise, [field]: value } : exercise)))
  }

  const handleRemoveExercise = (id: number) => {
    setExercises(exercises.filter((exercise) => exercise.id !== id))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // In a real app, you would save the workout data here
    // For demo purposes, we'll just redirect to the dashboard
    router.push("/dashboard")
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Workout toevoegen</h2>
        <p className="text-muted-foreground">Voeg een nieuwe workout toe aan je logboek</p>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Workout details</CardTitle>
              <CardDescription>Vul de details van je workout in</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="workout-type">Type workout</Label>
                  <Select value={workoutType} onValueChange={setWorkoutType} required>
                    <SelectTrigger id="workout-type">
                      <SelectValue placeholder="Selecteer type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="strength">Krachttraining</SelectItem>
                      <SelectItem value="cardio">Cardio</SelectItem>
                      <SelectItem value="yoga">Yoga</SelectItem>
                      <SelectItem value="hiit">HIIT</SelectItem>
                      <SelectItem value="other">Anders</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="date">Datum</Label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant="outline"
                        className={cn("w-full justify-start text-left font-normal", !date && "text-muted-foreground")}
                      >
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        {date ? format(date, "PPP", { locale: nl }) : <span>Kies een datum</span>}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0">
                      <Calendar mode="single" selected={date} onSelect={setDate} initialFocus />
                    </PopoverContent>
                  </Popover>
                </div>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="duration">Duur (minuten)</Label>
                  <Input
                    id="duration"
                    type="number"
                    placeholder="45"
                    value={duration}
                    onChange={(e) => setDuration(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="distance">Afstand (km, optioneel)</Label>
                  <Input
                    id="distance"
                    type="number"
                    step="0.01"
                    placeholder="5.0"
                    value={distance}
                    onChange={(e) => setDistance(e.target.value)}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="notes">Notities</Label>
                <Textarea
                  id="notes"
                  placeholder="Hoe voelde je je tijdens deze workout? Wat ging er goed of minder goed?"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="min-h-[100px]"
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Oefeningen</CardTitle>
              <CardDescription>Voeg de oefeningen toe die je hebt gedaan tijdens deze workout</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {exercises.length > 0 ? (
                <Accordion type="multiple" className="w-full">
                  {exercises.map((exercise, index) => (
                    <AccordionItem key={exercise.id} value={`exercise-${exercise.id}`}>
                      <AccordionTrigger className="hover:no-underline">
                        <div className="flex items-center gap-2 text-left">
                          <span className="font-medium">{exercise.name || `Oefening ${index + 1}`}</span>
                          {exercise.sets && exercise.reps && (
                            <Badge variant="outline" className="ml-2">
                              {exercise.sets} x {exercise.reps}
                              {exercise.weight ? ` x ${exercise.weight} kg` : ""}
                            </Badge>
                          )}
                        </div>
                      </AccordionTrigger>
                      <AccordionContent>
                        <div className="space-y-4 pt-2">
                          <div className="space-y-2">
                            <Label htmlFor={`exercise-name-${exercise.id}`}>Naam oefening</Label>
                            <Input
                              id={`exercise-name-${exercise.id}`}
                              value={exercise.name}
                              onChange={(e) => handleUpdateExercise(exercise.id, "name", e.target.value)}
                              placeholder="Bijv. Squats, Push-ups, Planks"
                            />
                          </div>
                          <div className="grid gap-4 sm:grid-cols-3">
                            <div className="space-y-2">
                              <Label htmlFor={`exercise-sets-${exercise.id}`}>Sets</Label>
                              <Input
                                id={`exercise-sets-${exercise.id}`}
                                type="number"
                                value={exercise.sets}
                                onChange={(e) => handleUpdateExercise(exercise.id, "sets", Number(e.target.value))}
                              />
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor={`exercise-reps-${exercise.id}`}>Herhalingen</Label>
                              <Input
                                id={`exercise-reps-${exercise.id}`}
                                type="number"
                                value={exercise.reps}
                                onChange={(e) => handleUpdateExercise(exercise.id, "reps", Number(e.target.value))}
                              />
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor={`exercise-weight-${exercise.id}`}>Gewicht (kg, optioneel)</Label>
                              <Input
                                id={`exercise-weight-${exercise.id}`}
                                type="number"
                                step="0.5"
                                value={exercise.weight || ""}
                                onChange={(e) =>
                                  handleUpdateExercise(
                                    exercise.id,
                                    "weight",
                                    e.target.value ? Number(e.target.value) : undefined,
                                  )
                                }
                              />
                            </div>
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor={`exercise-notes-${exercise.id}`}>Notities</Label>
                            <Textarea
                              id={`exercise-notes-${exercise.id}`}
                              value={exercise.notes || ""}
                              onChange={(e) => handleUpdateExercise(exercise.id, "notes", e.target.value)}
                              placeholder="Bijv. Verhoog gewicht volgende keer, focus op vorm"
                            />
                          </div>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            className="mt-2 text-destructive"
                            onClick={() => handleRemoveExercise(exercise.id)}
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            Verwijderen
                          </Button>
                        </div>
                      </AccordionContent>
                    </AccordionItem>
                  ))}
                </Accordion>
              ) : (
                <div className="flex h-[100px] items-center justify-center rounded-lg border border-dashed">
                  <p className="text-sm text-muted-foreground">Nog geen oefeningen toegevoegd</p>
                </div>
              )}
              <Button type="button" variant="outline" className="w-full" onClick={handleAddExercise}>
                <Plus className="mr-2 h-4 w-4" />
                Oefening toevoegen
              </Button>
            </CardContent>
          </Card>
        </div>

        <div className="mt-6 grid grid-cols-2 gap-3">
          <Button variant="outline" type="button" onClick={() => router.back()}>
            Annuleren
          </Button>
          <Button type="submit">Opslaan</Button>
        </div>
      </form>
    </div>
  )
}
