"use client"

import { useState } from "react"
import type { Exercise, Workout } from "@/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { format } from "date-fns"
import { nl } from "date-fns/locale"
import { CalendarIcon, ChevronDown, ChevronUp, Edit, MoreHorizontal, Plus, Trash2 } from "lucide-react"
import { cn } from "@/lib/utils"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"

// Mock data for workouts with exercises
const initialWorkouts: Workout[] = [
  {
    id: 1,
    type: "Krachttraining",
    date: new Date(2023, 3, 15),
    duration: 45,
    distance: null,
    notes: "Focus op benen en core.",
    exercises: [
      {
        id: 1,
        name: "Squats",
        sets: 3,
        reps: 12,
        weight: 60,
        notes: "Verhoog gewicht volgende keer",
      },
      {
        id: 2,
        name: "Lunges",
        sets: 3,
        reps: 10,
        weight: 20,
        notes: "Per been",
      },
      {
        id: 3,
        name: "Planks",
        sets: 3,
        reps: 1,
        duration: 60,
        notes: "60 seconden per set",
      },
    ],
  },
  {
    id: 2,
    type: "Cardio",
    date: new Date(2023, 3, 14),
    duration: 30,
    distance: 5,
    notes: "5 km hardlopen in het park. Goed tempo, voelde me energiek.",
    exercises: [],
  },
  {
    id: 3,
    type: "Yoga",
    date: new Date(2023, 3, 12),
    duration: 60,
    distance: null,
    notes: "Ochtend routine, focus op flexibiliteit en ademhaling.",
    exercises: [
      {
        id: 1,
        name: "Downward Dog",
        sets: 1,
        reps: 1,
        duration: 300,
        notes: "5 minuten gehouden",
      },
      {
        id: 2,
        name: "Warrior Pose",
        sets: 2,
        reps: 1,
        duration: 120,
        notes: "Beide kanten",
      },
    ],
  },
  {
    id: 4,
    type: "HIIT",
    date: new Date(2023, 3, 10),
    duration: 25,
    distance: null,
    notes: "Intensieve intervallen, 30 sec werk / 15 sec rust. 10 oefeningen.",
    exercises: [
      {
        id: 1,
        name: "Burpees",
        sets: 5,
        reps: 10,
        notes: "Max effort",
      },
      {
        id: 2,
        name: "Mountain Climbers",
        sets: 5,
        reps: 20,
        notes: "Snel tempo",
      },
    ],
  },
  {
    id: 5,
    type: "Krachttraining",
    date: new Date(2023, 3, 8),
    duration: 50,
    distance: null,
    notes: "Focus op bovenlichaam.",
    exercises: [
      {
        id: 1,
        name: "Push-ups",
        sets: 3,
        reps: 10,
        notes: "Breed handplaatsing",
      },
      {
        id: 2,
        name: "Pull-ups",
        sets: 3,
        reps: 8,
        notes: "Assisted met band",
      },
      {
        id: 3,
        name: "Shoulder Press",
        sets: 3,
        reps: 12,
        weight: 15,
        notes: "Per arm",
      },
    ],
  },
]

export default function HistoryPage() {
  const [workouts, setWorkouts] = useState<Workout[]>(initialWorkouts)
  const [date, setDate] = useState<Date | undefined>(undefined)
  const [type, setType] = useState<string>("")
  const [expandedWorkouts, setExpandedWorkouts] = useState<number[]>([])

  // Edit workout state
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editingWorkout, setEditingWorkout] = useState<Workout | null>(null)
  const [editDate, setEditDate] = useState<Date | undefined>(undefined)
  const [editType, setEditType] = useState("")
  const [editDuration, setEditDuration] = useState("")
  const [editDistance, setEditDistance] = useState("")
  const [editNotes, setEditNotes] = useState("")
  const [editExercises, setEditExercises] = useState<Exercise[]>([])
  const [nextExerciseId, setNextExerciseId] = useState(100) // Start high to avoid conflicts

  // Filter workouts based on selected date and type
  const filteredWorkouts = workouts.filter((workout) => {
    let matchesDate = true
    let matchesType = true

    if (date) {
      matchesDate =
        workout.date.getDate() === date.getDate() &&
        workout.date.getMonth() === date.getMonth() &&
        workout.date.getFullYear() === date.getFullYear()
    }

    if (type && type !== "alle") {
      matchesType = workout.type.toLowerCase() === type.toLowerCase()
    }

    return matchesDate && matchesType
  })

  const resetFilters = () => {
    setDate(undefined)
    setType("")
  }

  const toggleWorkoutExpand = (id: number) => {
    if (expandedWorkouts.includes(id)) {
      setExpandedWorkouts(expandedWorkouts.filter((workoutId) => workoutId !== id))
    } else {
      setExpandedWorkouts([...expandedWorkouts, id])
    }
  }

  const handleEditWorkout = (workout: Workout) => {
    setEditingWorkout(workout)
    setEditDate(workout.date)
    setEditType(workout.type.toLowerCase())
    setEditDuration(workout.duration.toString())
    setEditDistance(workout.distance ? workout.distance.toString() : "")
    setEditNotes(workout.notes)
    setEditExercises([...workout.exercises])
    setIsEditDialogOpen(true)
  }

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
    setEditExercises([...editExercises, newExercise])
    setNextExerciseId(nextExerciseId + 1)
  }

  const handleUpdateExercise = (id: number, field: keyof Exercise, value: any) => {
    setEditExercises(editExercises.map((exercise) => (exercise.id === id ? { ...exercise, [field]: value } : exercise)))
  }

  const handleRemoveExercise = (id: number) => {
    setEditExercises(editExercises.filter((exercise) => exercise.id !== id))
  }

  const handleSaveEdit = () => {
    if (editingWorkout && editDate) {
      const updatedWorkout: Workout = {
        ...editingWorkout,
        type: getWorkoutTypeLabel(editType),
        date: editDate,
        duration: Number.parseInt(editDuration),
        distance: editDistance ? Number.parseFloat(editDistance) : null,
        notes: editNotes,
        exercises: editExercises,
      }

      setWorkouts(workouts.map((w) => (w.id === updatedWorkout.id ? updatedWorkout : w)))
      setIsEditDialogOpen(false)
      setEditingWorkout(null)
    }
  }

  const handleDeleteWorkout = (id: number) => {
    setWorkouts(workouts.filter((workout) => workout.id !== id))
  }

  // Helper function to get workout type label
  const getWorkoutTypeLabel = (value: string): string => {
    switch (value) {
      case "krachttraining":
      case "strength":
        return "Krachttraining"
      case "cardio":
        return "Cardio"
      case "yoga":
        return "Yoga"
      case "hiit":
        return "HIIT"
      case "other":
        return "Anders"
      default:
        return value.charAt(0).toUpperCase() + value.slice(1)
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Workout geschiedenis</h2>
        <p className="text-muted-foreground">Bekijk en beheer je eerdere workouts</p>
      </div>

      <Tabs defaultValue="list" className="space-y-4">
        <TabsList className="grid w-full grid-cols-2 h-auto">
          <TabsTrigger value="list" className="py-2">
            Lijst
          </TabsTrigger>
          <TabsTrigger value="calendar" className="py-2">
            Kalender
          </TabsTrigger>
        </TabsList>

        <TabsContent value="list" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Filters</CardTitle>
              <CardDescription>Filter je workouts op datum en type</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-3">
                <div className="space-y-2">
                  <span className="text-sm font-medium">Datum</span>
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
                <div className="space-y-2">
                  <span className="text-sm font-medium">Type workout</span>
                  <Select value={type} onValueChange={setType}>
                    <SelectTrigger>
                      <SelectValue placeholder="Alle types" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="alle">Alle types</SelectItem>
                      <SelectItem value="krachttraining">Krachttraining</SelectItem>
                      <SelectItem value="cardio">Cardio</SelectItem>
                      <SelectItem value="yoga">Yoga</SelectItem>
                      <SelectItem value="hiit">HIIT</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="flex items-end">
                  <Button variant="outline" onClick={resetFilters} className="w-full">
                    Filters wissen
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Workouts</CardTitle>
              <CardDescription>{filteredWorkouts.length} workouts gevonden</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {filteredWorkouts.length > 0 ? (
                  filteredWorkouts.map((workout) => (
                    <div key={workout.id} className="rounded-lg border card-mobile">
                      <div className="flex flex-col p-4" onClick={() => toggleWorkoutExpand(workout.id)}>
                        <div className="space-y-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <h3 className="font-medium">{workout.type}</h3>
                            <Badge variant="outline">{workout.duration} min</Badge>
                            {workout.distance && <Badge variant="outline">{workout.distance} km</Badge>}
                          </div>
                          <p className="text-sm text-muted-foreground">{format(workout.date, "PPP", { locale: nl })}</p>
                        </div>
                        <p className="text-sm mt-2">{workout.notes}</p>

                        <div className="flex items-center justify-between mt-3">
                          {workout.exercises.length > 0 && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation()
                                toggleWorkoutExpand(workout.id)
                              }}
                              className="p-0 h-auto"
                            >
                              <span className="flex items-center text-sm text-muted-foreground">
                                {expandedWorkouts.includes(workout.id) ? (
                                  <>
                                    <ChevronUp className="mr-1 h-4 w-4" />
                                    Verberg oefeningen
                                  </>
                                ) : (
                                  <>
                                    <ChevronDown className="mr-1 h-4 w-4" />
                                    Toon oefeningen ({workout.exercises.length})
                                  </>
                                )}
                              </span>
                            </Button>
                          )}
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon" onClick={(e) => e.stopPropagation()}>
                                <MoreHorizontal className="h-5 w-5" />
                                <span className="sr-only">Menu</span>
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem onClick={() => handleEditWorkout(workout)}>
                                <Edit className="mr-2 h-4 w-4" />
                                <span>Bewerken</span>
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                className="text-destructive"
                                onClick={() => handleDeleteWorkout(workout.id)}
                              >
                                <Trash2 className="mr-2 h-4 w-4" />
                                <span>Verwijderen</span>
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </div>
                      </div>
                      {expandedWorkouts.includes(workout.id) && workout.exercises.length > 0 && (
                        <div className="border-t p-4">
                          <h4 className="mb-2 font-medium">Oefeningen</h4>
                          <div className="space-y-3">
                            {workout.exercises.map((exercise) => (
                              <div key={exercise.id} className="rounded-md border p-3">
                                <div className="flex flex-wrap items-center justify-between gap-2">
                                  <h5 className="font-medium">{exercise.name}</h5>
                                  <div className="flex flex-wrap gap-2">
                                    <Badge variant="outline">
                                      {exercise.sets} x {exercise.reps}
                                      {exercise.weight ? ` x ${exercise.weight} kg` : ""}
                                    </Badge>
                                    {exercise.duration && <Badge variant="outline">{exercise.duration} sec</Badge>}
                                  </div>
                                </div>
                                {exercise.notes && (
                                  <p className="mt-1 text-sm text-muted-foreground">{exercise.notes}</p>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ))
                ) : (
                  <div className="flex h-[200px] items-center justify-center rounded-lg border border-dashed">
                    <div className="text-center">
                      <p className="text-sm text-muted-foreground">Geen workouts gevonden met de huidige filters</p>
                      <Button variant="link" onClick={resetFilters} className="mt-2">
                        Filters wissen
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="calendar" className="space-y-4 pb-4">
          <Card>
            <CardHeader>
              <CardTitle>Workout kalender</CardTitle>
              <CardDescription>Bekijk je workouts per maand</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col items-center space-y-6">
                {/* Making the calendar responsive */}
                <div className="w-full max-w-full overflow-x-auto">
                  <Calendar
                    mode="single"
                    selected={date}
                    onSelect={setDate}
                    className="rounded-md border mx-auto"
                    initialFocus
                  />
                </div>

                {date && (
                  <Card className="w-full">
                    <CardHeader className="py-3">
                      <CardTitle className="text-base sm:text-lg">
                        Workouts op {format(date, "d MMMM yyyy", { locale: nl })}
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="px-3 py-2 sm:px-6">
                      {filteredWorkouts.length > 0 ? (
                        <div className="space-y-3">
                          {filteredWorkouts.map((workout) => (
                            <div key={workout.id} className="rounded-lg border">
                              <div className="flex flex-col space-y-2 p-3">
                                <div className="flex items-center justify-between">
                                  <div className="flex flex-wrap items-center gap-1 sm:gap-2">
                                    <h3 className="font-medium text-sm sm:text-base">{workout.type}</h3>
                                    <Badge variant="outline" className="text-xs">
                                      {workout.duration} min
                                    </Badge>
                                    {workout.distance && (
                                      <Badge variant="outline" className="text-xs">
                                        {workout.distance} km
                                      </Badge>
                                    )}
                                  </div>
                                  <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                      <Button variant="ghost" size="icon">
                                        <MoreHorizontal className="h-4 w-4" />
                                        <span className="sr-only">Menu</span>
                                      </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent align="end">
                                      <DropdownMenuItem onClick={() => handleEditWorkout(workout)}>
                                        <Edit className="mr-2 h-4 w-4" />
                                        <span>Bewerken</span>
                                      </DropdownMenuItem>
                                      <DropdownMenuItem
                                        className="text-destructive"
                                        onClick={() => handleDeleteWorkout(workout.id)}
                                      >
                                        <Trash2 className="mr-2 h-4 w-4" />
                                        <span>Verwijderen</span>
                                      </DropdownMenuItem>
                                    </DropdownMenuContent>
                                  </DropdownMenu>
                                </div>
                                <p className="text-xs sm:text-sm">{workout.notes}</p>

                                {workout.exercises.length > 0 && (
                                  <div className="mt-1">
                                    <Button
                                      variant="ghost"
                                      size="sm"
                                      className="p-0 h-auto"
                                      onClick={() => toggleWorkoutExpand(workout.id)}
                                    >
                                      <span className="flex items-center text-xs text-muted-foreground">
                                        {expandedWorkouts.includes(workout.id) ? (
                                          <>
                                            <ChevronUp className="mr-1 h-3 w-3" />
                                            Verberg
                                          </>
                                        ) : (
                                          <>
                                            <ChevronDown className="mr-1 h-3 w-3" />
                                            Toon ({workout.exercises.length})
                                          </>
                                        )}
                                      </span>
                                    </Button>

                                    {expandedWorkouts.includes(workout.id) && (
                                      <div className="mt-2 space-y-2">
                                        {workout.exercises.map((exercise) => (
                                          <div key={exercise.id} className="rounded-md border p-2">
                                            <div className="flex flex-wrap items-center justify-between gap-1">
                                              <h5 className="font-medium text-xs sm:text-sm">{exercise.name}</h5>
                                              <div className="flex flex-wrap gap-1">
                                                <Badge variant="outline" className="text-xs">
                                                  {exercise.sets} x {exercise.reps}
                                                  {exercise.weight ? ` x ${exercise.weight} kg` : ""}
                                                </Badge>
                                                {exercise.duration && (
                                                  <Badge variant="outline" className="text-xs">
                                                    {exercise.duration} sec
                                                  </Badge>
                                                )}
                                              </div>
                                            </div>
                                            {exercise.notes && (
                                              <p className="mt-1 text-xs text-muted-foreground">{exercise.notes}</p>
                                            )}
                                          </div>
                                        ))}
                                      </div>
                                    )}
                                  </div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="flex h-[100px] items-center justify-center rounded-lg border border-dashed">
                          <p className="text-sm text-muted-foreground">Geen workouts gevonden op deze datum</p>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Edit Workout Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Workout bewerken</DialogTitle>
            <DialogDescription>
              Pas de details van je workout aan. Klik op opslaan wanneer je klaar bent.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="edit-workout-type">Type workout</Label>
                <Select value={editType} onValueChange={setEditType}>
                  <SelectTrigger id="edit-workout-type">
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
                <Label htmlFor="edit-date">Datum</Label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      id="edit-date"
                      variant="outline"
                      className={cn("w-full justify-start text-left font-normal", !editDate && "text-muted-foreground")}
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {editDate ? format(editDate, "PPP", { locale: nl }) : <span>Kies een datum</span>}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0">
                    <Calendar mode="single" selected={editDate} onSelect={setEditDate} initialFocus />
                  </PopoverContent>
                </Popover>
              </div>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="edit-duration">Duur (minuten)</Label>
                <Input
                  id="edit-duration"
                  type="number"
                  value={editDuration}
                  onChange={(e) => setEditDuration(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-distance">Afstand (km, optioneel)</Label>
                <Input
                  id="edit-distance"
                  type="number"
                  step="0.01"
                  value={editDistance}
                  onChange={(e) => setEditDistance(e.target.value)}
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="edit-notes">Notities</Label>
              <Textarea
                id="edit-notes"
                value={editNotes}
                onChange={(e) => setEditNotes(e.target.value)}
                className="min-h-[100px]"
              />
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label>Oefeningen</Label>
                <Button type="button" variant="outline" size="sm" onClick={handleAddExercise}>
                  <Plus className="mr-2 h-4 w-4" />
                  Oefening toevoegen
                </Button>
              </div>

              {editExercises.length > 0 ? (
                <Accordion type="multiple" className="w-full">
                  {editExercises.map((exercise, index) => (
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
                            <Label htmlFor={`edit-exercise-name-${exercise.id}`}>Naam oefening</Label>
                            <Input
                              id={`edit-exercise-name-${exercise.id}`}
                              value={exercise.name}
                              onChange={(e) => handleUpdateExercise(exercise.id, "name", e.target.value)}
                              placeholder="Bijv. Squats, Push-ups, Planks"
                            />
                          </div>
                          <div className="grid gap-4 sm:grid-cols-3">
                            <div className="space-y-2">
                              <Label htmlFor={`edit-exercise-sets-${exercise.id}`}>Sets</Label>
                              <Input
                                id={`edit-exercise-sets-${exercise.id}`}
                                type="number"
                                value={exercise.sets}
                                onChange={(e) => handleUpdateExercise(exercise.id, "sets", Number(e.target.value))}
                              />
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor={`edit-exercise-reps-${exercise.id}`}>Herhalingen</Label>
                              <Input
                                id={`edit-exercise-reps-${exercise.id}`}
                                type="number"
                                value={exercise.reps}
                                onChange={(e) => handleUpdateExercise(exercise.id, "reps", Number(e.target.value))}
                              />
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor={`edit-exercise-weight-${exercise.id}`}>Gewicht (kg, optioneel)</Label>
                              <Input
                                id={`edit-exercise-weight-${exercise.id}`}
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
                            <Label htmlFor={`edit-exercise-notes-${exercise.id}`}>Notities</Label>
                            <Textarea
                              id={`edit-exercise-notes-${exercise.id}`}
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
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Annuleren
            </Button>
            <Button onClick={handleSaveEdit}>Opslaan</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
