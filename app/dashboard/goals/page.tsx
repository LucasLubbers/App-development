"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Edit, Plus, Trash2 } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Switch } from "@/components/ui/switch"

// Mock data for goals
const initialGoals = [
  {
    id: 1,
    title: "3x per week sporten",
    type: "frequency",
    target: 3,
    period: "week",
    current: 2,
    active: true,
  },
  {
    id: 2,
    title: "10 km hardlopen per week",
    type: "distance",
    target: 10,
    period: "week",
    current: 8,
    active: true,
  },
  {
    id: 3,
    title: "5 krachtsessies per maand",
    type: "frequency",
    target: 5,
    period: "month",
    current: 2,
    active: true,
  },
  {
    id: 4,
    title: "30 minuten yoga per dag",
    type: "duration",
    target: 30,
    period: "day",
    current: 0,
    active: false,
  },
]

export default function GoalsPage() {
  const [goals, setGoals] = useState(initialGoals)
  const [newGoal, setNewGoal] = useState({
    title: "",
    type: "frequency",
    target: "",
    period: "week",
  })
  const [editingGoal, setEditingGoal] = useState(null)
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)

  const activeGoals = goals.filter((goal) => goal.active)
  const inactiveGoals = goals.filter((goal) => !goal.active)

  const handleAddGoal = () => {
    if (newGoal.title && newGoal.target) {
      setGoals([
        ...goals,
        {
          id: goals.length + 1,
          ...newGoal,
          target: Number(newGoal.target),
          current: 0,
          active: true,
        },
      ])
      setNewGoal({
        title: "",
        type: "frequency",
        target: "",
        period: "week",
      })
      setIsAddDialogOpen(false)
    }
  }

  const handleEditGoal = () => {
    if (editingGoal) {
      setGoals(goals.map((goal) => (goal.id === editingGoal.id ? { ...editingGoal } : goal)))
      setEditingGoal(null)
      setIsEditDialogOpen(false)
    }
  }

  const handleDeleteGoal = (id) => {
    setGoals(goals.filter((goal) => goal.id !== id))
  }

  const handleToggleGoalStatus = (id) => {
    setGoals(goals.map((goal) => (goal.id === id ? { ...goal, active: !goal.active } : goal)))
  }

  const getProgressPercentage = (current, target) => {
    return Math.min(Math.round((current / target) * 100), 100)
  }

  const getGoalStatus = (current, target) => {
    const percentage = getProgressPercentage(current, target)
    if (percentage >= 100) return "Behaald"
    if (percentage >= 70) return "Op schema"
    return "Achter op schema"
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Doelen</h2>
          <p className="text-muted-foreground">Stel doelen in en volg je voortgang</p>
        </div>
        <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
          <DialogTrigger asChild>
            <Button className="gap-1">
              <Plus className="h-4 w-4" />
              <span>Nieuw doel</span>
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Nieuw doel toevoegen</DialogTitle>
              <DialogDescription>Stel een nieuw doel in om je motivatie te verhogen</DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="title">Titel</Label>
                <Input
                  id="title"
                  value={newGoal.title}
                  onChange={(e) => setNewGoal({ ...newGoal, title: e.target.value })}
                  placeholder="Bijv. 3x per week sporten"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="type">Type doel</Label>
                  <Select value={newGoal.type} onValueChange={(value) => setNewGoal({ ...newGoal, type: value })}>
                    <SelectTrigger id="type">
                      <SelectValue placeholder="Selecteer type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="frequency">Frequentie</SelectItem>
                      <SelectItem value="duration">Duur</SelectItem>
                      <SelectItem value="distance">Afstand</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="period">Periode</Label>
                  <Select value={newGoal.period} onValueChange={(value) => setNewGoal({ ...newGoal, period: value })}>
                    <SelectTrigger id="period">
                      <SelectValue placeholder="Selecteer periode" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="day">Per dag</SelectItem>
                      <SelectItem value="week">Per week</SelectItem>
                      <SelectItem value="month">Per maand</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="target">Doelwaarde</Label>
                <Input
                  id="target"
                  type="number"
                  value={newGoal.target}
                  onChange={(e) => setNewGoal({ ...newGoal, target: e.target.value })}
                  placeholder="Bijv. 3, 10, 30"
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsAddDialogOpen(false)}>
                Annuleren
              </Button>
              <Button onClick={handleAddGoal}>Toevoegen</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <Tabs defaultValue="active" className="space-y-4">
        <TabsList>
          <TabsTrigger value="active">Actieve doelen</TabsTrigger>
          <TabsTrigger value="inactive">Inactieve doelen</TabsTrigger>
        </TabsList>

        <TabsContent value="active" className="space-y-4">
          {activeGoals.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2">
              {activeGoals.map((goal) => (
                <Card key={goal.id}>
                  <CardHeader className="pb-2">
                    <CardTitle>{goal.title}</CardTitle>
                    <CardDescription>
                      {getGoalStatus(goal.current, goal.target)} • {goal.current}/{goal.target}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="h-2 w-full rounded-full bg-secondary">
                      <div
                        className="h-full rounded-full bg-primary"
                        style={{ width: `${getProgressPercentage(goal.current, goal.target)}%` }}
                      ></div>
                    </div>
                  </CardContent>
                  <CardFooter className="flex justify-between">
                    <div className="flex items-center gap-2">
                      <Dialog
                        open={isEditDialogOpen && editingGoal?.id === goal.id}
                        onOpenChange={(open) => {
                          if (!open) setEditingGoal(null)
                          setIsEditDialogOpen(open)
                        }}
                      >
                        <DialogTrigger asChild>
                          <Button variant="ghost" size="icon" onClick={() => setEditingGoal(goal)}>
                            <Edit className="h-4 w-4" />
                            <span className="sr-only">Bewerken</span>
                          </Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>Doel bewerken</DialogTitle>
                            <DialogDescription>Pas je doel aan naar wens</DialogDescription>
                          </DialogHeader>
                          {editingGoal && (
                            <div className="grid gap-4 py-4">
                              <div className="space-y-2">
                                <Label htmlFor="edit-title">Titel</Label>
                                <Input
                                  id="edit-title"
                                  value={editingGoal.title}
                                  onChange={(e) => setEditingGoal({ ...editingGoal, title: e.target.value })}
                                />
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="edit-type">Type doel</Label>
                                  <Select
                                    value={editingGoal.type}
                                    onValueChange={(value) => setEditingGoal({ ...editingGoal, type: value })}
                                  >
                                    <SelectTrigger id="edit-type">
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="frequency">Frequentie</SelectItem>
                                      <SelectItem value="duration">Duur</SelectItem>
                                      <SelectItem value="distance">Afstand</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="edit-period">Periode</Label>
                                  <Select
                                    value={editingGoal.period}
                                    onValueChange={(value) => setEditingGoal({ ...editingGoal, period: value })}
                                  >
                                    <SelectTrigger id="edit-period">
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="day">Per dag</SelectItem>
                                      <SelectItem value="week">Per week</SelectItem>
                                      <SelectItem value="month">Per maand</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="edit-target">Doelwaarde</Label>
                                  <Input
                                    id="edit-target"
                                    type="number"
                                    value={editingGoal.target}
                                    onChange={(e) => setEditingGoal({ ...editingGoal, target: Number(e.target.value) })}
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="edit-current">Huidige waarde</Label>
                                  <Input
                                    id="edit-current"
                                    type="number"
                                    value={editingGoal.current}
                                    onChange={(e) =>
                                      setEditingGoal({ ...editingGoal, current: Number(e.target.value) })
                                    }
                                  />
                                </div>
                              </div>
                              <div className="flex items-center space-x-2">
                                <Switch
                                  id="edit-active"
                                  checked={editingGoal.active}
                                  onCheckedChange={(checked) => setEditingGoal({ ...editingGoal, active: checked })}
                                />
                                <Label htmlFor="edit-active">Actief</Label>
                              </div>
                            </div>
                          )}
                          <DialogFooter>
                            <Button
                              variant="outline"
                              onClick={() => {
                                setEditingGoal(null)
                                setIsEditDialogOpen(false)
                              }}
                            >
                              Annuleren
                            </Button>
                            <Button onClick={handleEditGoal}>Opslaan</Button>
                          </DialogFooter>
                        </DialogContent>
                      </Dialog>
                      <Button variant="ghost" size="icon" onClick={() => handleDeleteGoal(goal.id)}>
                        <Trash2 className="h-4 w-4" />
                        <span className="sr-only">Verwijderen</span>
                      </Button>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Label htmlFor={`active-${goal.id}`} className="text-sm">
                        Actief
                      </Label>
                      <Switch
                        id={`active-${goal.id}`}
                        checked={goal.active}
                        onCheckedChange={() => handleToggleGoalStatus(goal.id)}
                      />
                    </div>
                  </CardFooter>
                </Card>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="flex h-[200px] items-center justify-center">
                <div className="text-center">
                  <p className="text-sm text-muted-foreground">Je hebt nog geen actieve doelen</p>
                  <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
                    <DialogTrigger asChild>
                      <Button variant="link" className="mt-2">
                        Voeg een doel toe
                      </Button>
                    </DialogTrigger>
                  </Dialog>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="inactive" className="space-y-4">
          {inactiveGoals.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2">
              {inactiveGoals.map((goal) => (
                <Card key={goal.id} className="opacity-70">
                  <CardHeader className="pb-2">
                    <CardTitle>{goal.title}</CardTitle>
                    <CardDescription>
                      Inactief • {goal.current}/{goal.target}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="h-2 w-full rounded-full bg-secondary">
                      <div
                        className="h-full rounded-full bg-muted-foreground"
                        style={{ width: `${getProgressPercentage(goal.current, goal.target)}%` }}
                      ></div>
                    </div>
                  </CardContent>
                  <CardFooter className="flex justify-between">
                    <div className="flex items-center gap-2">
                      <Dialog
                        open={isEditDialogOpen && editingGoal?.id === goal.id}
                        onOpenChange={(open) => {
                          if (!open) setEditingGoal(null)
                          setIsEditDialogOpen(open)
                        }}
                      >
                        <DialogTrigger asChild>
                          <Button variant="ghost" size="icon" onClick={() => setEditingGoal(goal)}>
                            <Edit className="h-4 w-4" />
                            <span className="sr-only">Bewerken</span>
                          </Button>
                        </DialogTrigger>
                      </Dialog>
                      <Button variant="ghost" size="icon" onClick={() => handleDeleteGoal(goal.id)}>
                        <Trash2 className="h-4 w-4" />
                        <span className="sr-only">Verwijderen</span>
                      </Button>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Label htmlFor={`active-${goal.id}`} className="text-sm">
                        Actief
                      </Label>
                      <Switch
                        id={`active-${goal.id}`}
                        checked={goal.active}
                        onCheckedChange={() => handleToggleGoalStatus(goal.id)}
                      />
                    </div>
                  </CardFooter>
                </Card>
              ))}
            </div>
          ) : (
            <Card>
              <CardContent className="flex h-[200px] items-center justify-center">
                <p className="text-sm text-muted-foreground">Je hebt geen inactieve doelen</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
