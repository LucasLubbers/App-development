import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { BarChart3, CalendarCheck, Dumbbell, Target } from "lucide-react"
import Link from "next/link"

export default function DashboardPage() {
  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-2">
        <h2 className="text-2xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">Welkom terug! Hier is een overzicht van je workouts.</p>
      </div>
      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3 h-auto">
          <TabsTrigger value="overview" className="py-2">
            Overzicht
          </TabsTrigger>
          <TabsTrigger value="analytics" className="py-2">
            Stats
          </TabsTrigger>
          <TabsTrigger value="goals" className="py-2">
            Doelen
          </TabsTrigger>
        </TabsList>
        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-3 grid-cols-2 md:grid-cols-3">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Totaal aantal workouts</CardTitle>
                <Dumbbell className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">12</div>
                <p className="text-xs text-muted-foreground">+2 sinds vorige week</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Totale tijd gesport</CardTitle>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="h-4 w-4 text-muted-foreground"
                >
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">8u 30m</div>
                <p className="text-xs text-muted-foreground">+1u 15m sinds vorige week</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Doelen behaald</CardTitle>
                <Target className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">2/3</div>
                <p className="text-xs text-muted-foreground">Je bent goed op weg!</p>
              </CardContent>
            </Card>
          </div>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
            <Card className="col-span-full md:col-span-4">
              <CardHeader>
                <CardTitle>Recente workouts</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {[
                    {
                      type: "Krachttraining",
                      date: "Vandaag, 10:30",
                      duration: "45 min",
                      notes: "Benen en core",
                    },
                    {
                      type: "Cardio",
                      date: "Gisteren, 18:00",
                      duration: "30 min",
                      notes: "5 km hardlopen",
                    },
                    {
                      type: "Yoga",
                      date: "2 dagen geleden, 08:15",
                      duration: "60 min",
                      notes: "Ochtend routine",
                    },
                  ].map((workout, i) => (
                    <div
                      key={i}
                      className="flex flex-col rounded-lg border p-3 md:flex-row md:items-center md:justify-between"
                    >
                      <div className="space-y-1">
                        <p className="font-medium leading-none">{workout.type}</p>
                        <p className="text-sm text-muted-foreground">{workout.date}</p>
                      </div>
                      <div className="flex items-center justify-between mt-2 md:mt-0 md:gap-4">
                        <div className="text-sm text-muted-foreground">{workout.duration}</div>
                        <div className="text-sm">{workout.notes}</div>
                      </div>
                    </div>
                  ))}
                </div>
                <div className="mt-4 flex justify-center">
                  <Link href="/dashboard/history">
                    <Button variant="outline" className="gap-1">
                      <CalendarCheck className="h-4 w-4" />
                      <span>Alle workouts bekijken</span>
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
            <Card className="col-span-3">
              <CardHeader>
                <CardTitle>Wekelijkse voortgang</CardTitle>
                <CardDescription>Aantal workouts per week</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-[200px] flex items-end justify-between gap-2 pt-4">
                  {[2, 3, 1, 4, 2, 0, 0].map((count, i) => (
                    <div key={i} className="relative flex flex-col items-center">
                      <div className="w-8 bg-primary rounded-t-md" style={{ height: `${count * 40}px` }}></div>
                      <span className="mt-2 text-xs">{["Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"][i]}</span>
                    </div>
                  ))}
                </div>
                <div className="mt-4 flex justify-center">
                  <Link href="/dashboard/statistics">
                    <Button variant="outline" className="gap-1">
                      <BarChart3 className="h-4 w-4" />
                      <span>Meer statistieken</span>
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
        <TabsContent value="analytics" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Workout verdeling</CardTitle>
              <CardDescription>Verdeling van je workouts per type</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex items-center justify-center">
                <div className="w-[300px] h-[300px] relative rounded-full overflow-hidden">
                  <div
                    className="absolute bg-primary"
                    style={{
                      width: "100%",
                      height: "100%",
                      clipPath: "polygon(50% 50%, 50% 0%, 100% 0%, 100% 100%, 50% 100%)",
                    }}
                  ></div>
                  <div
                    className="absolute bg-green-500"
                    style={{
                      width: "100%",
                      height: "100%",
                      clipPath: "polygon(50% 50%, 50% 0%, 0% 0%, 0% 50%)",
                    }}
                  ></div>
                  <div
                    className="absolute bg-yellow-500"
                    style={{
                      width: "100%",
                      height: "100%",
                      clipPath: "polygon(50% 50%, 0% 50%, 0% 100%, 50% 100%)",
                    }}
                  ></div>
                </div>
              </div>
              <div className="mt-4 grid grid-cols-3 gap-4 text-center">
                <div>
                  <div className="text-xl font-bold">50%</div>
                  <div className="text-xs text-muted-foreground">Krachttraining</div>
                </div>
                <div>
                  <div className="text-xl font-bold">30%</div>
                  <div className="text-xs text-muted-foreground">Cardio</div>
                </div>
                <div>
                  <div className="text-xl font-bold">20%</div>
                  <div className="text-xs text-muted-foreground">Yoga</div>
                </div>
              </div>
            </CardContent>
          </Card>
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Workout duur</CardTitle>
                <CardDescription>Gemiddelde duur per workout type</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-[200px] flex items-end justify-around gap-2 pt-4">
                  {[45, 30, 60].map((duration, i) => (
                    <div key={i} className="relative flex flex-col items-center">
                      <div className="w-16 bg-primary rounded-t-md" style={{ height: `${duration * 2}px` }}></div>
                      <span className="mt-2 text-xs">{["Kracht", "Cardio", "Yoga"][i]}</span>
                      <span className="text-xs text-muted-foreground">{duration} min</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Wekelijkse activiteit</CardTitle>
                <CardDescription>Aantal minuten per week</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-[200px] flex items-end justify-between gap-2 pt-4">
                  {[180, 210, 150, 240, 300].map((minutes, i) => (
                    <div key={i} className="relative flex flex-col items-center">
                      <div className="w-12 bg-primary rounded-t-md" style={{ height: `${minutes / 2}px` }}></div>
                      <span className="mt-2 text-xs">{["Week 1", "Week 2", "Week 3", "Week 4", "Week 5"][i]}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
        <TabsContent value="goals" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Actieve doelen</CardTitle>
              <CardDescription>Je huidige doelen en voortgang</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {[
                  {
                    title: "3x per week sporten",
                    progress: 66,
                    status: "Op schema",
                    current: "2/3",
                  },
                  {
                    title: "10 km hardlopen per week",
                    progress: 80,
                    status: "Op schema",
                    current: "8/10 km",
                  },
                  {
                    title: "5 krachtsessies per maand",
                    progress: 40,
                    status: "Achter op schema",
                    current: "2/5",
                  },
                ].map((goal, i) => (
                  <div key={i} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="font-medium">{goal.title}</div>
                        <div className="text-xs text-muted-foreground">
                          {goal.status} • {goal.current}
                        </div>
                      </div>
                      <div className="text-sm font-medium">{goal.progress}%</div>
                    </div>
                    <div className="h-2 w-full rounded-full bg-secondary">
                      <div className="h-full rounded-full bg-primary" style={{ width: `${goal.progress}%` }}></div>
                    </div>
                  </div>
                ))}
              </div>
              <div className="mt-6 flex justify-center">
                <Link href="/dashboard/goals">
                  <Button variant="outline" className="gap-1">
                    <Target className="h-4 w-4" />
                    <span>Doelen beheren</span>
                  </Button>
                </Link>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
