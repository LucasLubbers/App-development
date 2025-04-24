import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

export default function StatisticsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Statistieken</h2>
        <p className="text-muted-foreground">Bekijk je voortgang en prestaties</p>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="overview">Overzicht</TabsTrigger>
          <TabsTrigger value="weekly">Wekelijks</TabsTrigger>
          <TabsTrigger value="monthly">Maandelijks</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Totaal aantal workouts</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">12</div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Totale tijd gesport</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">8u 30m</div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Gemiddelde duur</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">42 min</div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Totale afstand</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">35 km</div>
              </CardContent>
            </Card>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <Card className="col-span-1">
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
                        clipPath: "polygon(50% 50%, 0% 0%, 50% 0%)",
                      }}
                    ></div>
                    <div
                      className="absolute bg-yellow-500"
                      style={{
                        width: "100%",
                        height: "100%",
                        clipPath: "polygon(50% 50%, 0% 0%, 0% 100%, 50% 100%)",
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

            <Card className="col-span-1">
              <CardHeader>
                <CardTitle>Workout duur</CardTitle>
                <CardDescription>Gemiddelde duur per workout type</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-[300px] flex items-end justify-around gap-2 pt-4">
                  {[45, 30, 60, 25].map((duration, i) => (
                    <div key={i} className="relative flex flex-col items-center">
                      <div className="w-16 bg-primary rounded-t-md" style={{ height: `${duration * 3}px` }}></div>
                      <span className="mt-2 text-xs">{["Kracht", "Cardio", "Yoga", "HIIT"][i]}</span>
                      <span className="text-xs text-muted-foreground">{duration} min</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="weekly" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Wekelijkse activiteit</CardTitle>
              <CardDescription>Aantal workouts per dag deze week</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex items-end justify-between gap-2 pt-4">
                {[2, 3, 1, 4, 2, 0, 0].map((count, i) => (
                  <div key={i} className="relative flex flex-col items-center">
                    <div className="w-16 bg-primary rounded-t-md" style={{ height: `${count * 60}px` }}></div>
                    <span className="mt-2 text-xs">{["Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"][i]}</span>
                    <span className="text-xs text-muted-foreground">{count} workouts</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Wekelijkse duur</CardTitle>
              <CardDescription>Aantal minuten per dag deze week</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex items-end justify-between gap-2 pt-4">
                {[90, 135, 45, 180, 90, 0, 0].map((minutes, i) => (
                  <div key={i} className="relative flex flex-col items-center">
                    <div className="w-16 bg-primary rounded-t-md" style={{ height: `${minutes}px` }}></div>
                    <span className="mt-2 text-xs">{["Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"][i]}</span>
                    <span className="text-xs text-muted-foreground">{minutes} min</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Wekelijkse voortgang</CardTitle>
                <CardDescription>Vergelijking met vorige week</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-medium">Aantal workouts</div>
                      <div className="text-sm font-medium text-green-500">+2</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-primary" style={{ width: "60%" }}></div>
                      </div>
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-green-500" style={{ width: "80%" }}></div>
                      </div>
                    </div>
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      <div>Vorige week: 10</div>
                      <div>Deze week: 12</div>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-medium">Totale tijd</div>
                      <div className="text-sm font-medium text-green-500">+1u 15m</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-primary" style={{ width: "70%" }}></div>
                      </div>
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-green-500" style={{ width: "85%" }}></div>
                      </div>
                    </div>
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      <div>Vorige week: 7u 15m</div>
                      <div>Deze week: 8u 30m</div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Wekelijkse intensiteit</CardTitle>
                <CardDescription>Gemiddelde intensiteit per dag</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-[200px] flex items-end justify-between gap-2 pt-4">
                  {[3, 4, 2, 5, 3, 0, 0].map((intensity, i) => (
                    <div key={i} className="relative flex flex-col items-center">
                      <div className="w-12 bg-primary rounded-t-md" style={{ height: `${intensity * 30}px` }}></div>
                      <span className="mt-2 text-xs">{["Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"][i]}</span>
                      <span className="text-xs text-muted-foreground">{intensity}/5</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="monthly" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Maandelijkse activiteit</CardTitle>
              <CardDescription>Aantal workouts per week deze maand</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex items-end justify-around gap-2 pt-4">
                {[8, 10, 12, 9].map((count, i) => (
                  <div key={i} className="relative flex flex-col items-center">
                    <div className="w-20 bg-primary rounded-t-md" style={{ height: `${count * 20}px` }}></div>
                    <span className="mt-2 text-xs">{`Week ${i + 1}`}</span>
                    <span className="text-xs text-muted-foreground">{count} workouts</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Maandelijkse duur</CardTitle>
              <CardDescription>Aantal minuten per week deze maand</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="h-[300px] flex items-end justify-around gap-2 pt-4">
                {[360, 450, 510, 390].map((minutes, i) => (
                  <div key={i} className="relative flex flex-col items-center">
                    <div className="w-20 bg-primary rounded-t-md" style={{ height: `${minutes / 2}px` }}></div>
                    <span className="mt-2 text-xs">{`Week ${i + 1}`}</span>
                    <span className="text-xs text-muted-foreground">
                      {Math.floor(minutes / 60)}u {minutes % 60}m
                    </span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Maandelijkse voortgang</CardTitle>
                <CardDescription>Vergelijking met vorige maand</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-medium">Aantal workouts</div>
                      <div className="text-sm font-medium text-green-500">+8</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-primary" style={{ width: "70%" }}></div>
                      </div>
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-green-500" style={{ width: "90%" }}></div>
                      </div>
                    </div>
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      <div>Vorige maand: 31</div>
                      <div>Deze maand: 39</div>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-medium">Totale tijd</div>
                      <div className="text-sm font-medium text-green-500">+5u 30m</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-primary" style={{ width: "65%" }}></div>
                      </div>
                      <div className="h-2 w-full rounded-full bg-secondary">
                        <div className="h-full rounded-full bg-green-500" style={{ width: "85%" }}></div>
                      </div>
                    </div>
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      <div>Vorige maand: 23u 45m</div>
                      <div>Deze maand: 29u 15m</div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Workout types per maand</CardTitle>
                <CardDescription>Verdeling van workout types</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="h-[200px] flex items-end justify-around gap-2 pt-4">
                  {[
                    { type: "Kracht", current: 20, previous: 15 },
                    { type: "Cardio", current: 12, previous: 10 },
                    { type: "Yoga", current: 7, previous: 6 },
                  ].map((data, i) => (
                    <div key={i} className="relative flex flex-col items-center">
                      <div className="flex items-end gap-1">
                        <div className="w-8 bg-primary rounded-t-md" style={{ height: `${data.previous * 5}px` }}></div>
                        <div
                          className="w-8 bg-green-500 rounded-t-md"
                          style={{ height: `${data.current * 5}px` }}
                        ></div>
                      </div>
                      <span className="mt-2 text-xs">{data.type}</span>
                      <div className="flex gap-1 text-xs text-muted-foreground">
                        <span>{data.previous}</span>
                        <span>→</span>
                        <span>{data.current}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
