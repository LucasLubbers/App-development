"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Separator } from "@/components/ui/separator"
import { Switch } from "@/components/ui/switch"

export default function ProfilePage() {
  const [user, setUser] = useState({
    name: "Jan Jansen",
    email: "jan.jansen@voorbeeld.nl",
    avatar: "/placeholder.svg?height=100&width=100",
  })

  const [notifications, setNotifications] = useState({
    email: true,
    push: true,
    workout: true,
    goals: true,
    reminders: true,
  })

  const [preferences, setPreferences] = useState({
    darkMode: false,
    language: "nl",
    units: "metric",
  })

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Profiel</h2>
        <p className="text-muted-foreground">Beheer je account en voorkeuren</p>
      </div>

      <Tabs defaultValue="account" className="space-y-4">
        <TabsList>
          <TabsTrigger value="account">Account</TabsTrigger>
          <TabsTrigger value="notifications">Notificaties</TabsTrigger>
          <TabsTrigger value="preferences">Voorkeuren</TabsTrigger>
        </TabsList>

        <TabsContent value="account" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Profielinformatie</CardTitle>
              <CardDescription>Beheer je persoonlijke gegevens</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex flex-col items-center space-y-4 sm:flex-row sm:space-x-4 sm:space-y-0">
                <Avatar className="h-24 w-24">
                  <AvatarImage src={user.avatar} alt={user.name} />
                  <AvatarFallback>
                    {user.name
                      .split(" ")
                      .map((n) => n[0])
                      .join("")}
                  </AvatarFallback>
                </Avatar>
                <div className="space-y-2">
                  <h3 className="font-medium">{user.name}</h3>
                  <p className="text-sm text-muted-foreground">{user.email}</p>
                  <Button size="sm">Foto wijzigen</Button>
                </div>
              </div>

              <Separator />

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="name">Naam</Label>
                  <Input id="name" value={user.name} onChange={(e) => setUser({ ...user, name: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    value={user.email}
                    onChange={(e) => setUser({ ...user, email: e.target.value })}
                  />
                </div>
              </div>
            </CardContent>
            <CardFooter>
              <Button>Opslaan</Button>
            </CardFooter>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Wachtwoord wijzigen</CardTitle>
              <CardDescription>Wijzig je wachtwoord om je account te beveiligen</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="current-password">Huidig wachtwoord</Label>
                <Input id="current-password" type="password" />
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="new-password">Nieuw wachtwoord</Label>
                  <Input id="new-password" type="password" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="confirm-password">Bevestig wachtwoord</Label>
                  <Input id="confirm-password" type="password" />
                </div>
              </div>
            </CardContent>
            <CardFooter>
              <Button>Wachtwoord wijzigen</Button>
            </CardFooter>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Account verwijderen</CardTitle>
              <CardDescription>Verwijder je account en alle bijbehorende gegevens</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Als je je account verwijdert, worden al je gegevens permanent verwijderd. Deze actie kan niet ongedaan
                worden gemaakt.
              </p>
            </CardContent>
            <CardFooter>
              <Button variant="destructive">Account verwijderen</Button>
            </CardFooter>
          </Card>
        </TabsContent>

        <TabsContent value="notifications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Notificatie-instellingen</CardTitle>
              <CardDescription>Bepaal hoe en wanneer je notificaties ontvangt</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-4">
                <h3 className="text-sm font-medium">Notificatiekanalen</h3>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="email-notifications" className="flex flex-col space-y-1">
                      <span>Email notificaties</span>
                      <span className="font-normal text-xs text-muted-foreground">Ontvang notificaties via email</span>
                    </Label>
                    <Switch
                      id="email-notifications"
                      checked={notifications.email}
                      onCheckedChange={(checked) => setNotifications({ ...notifications, email: checked })}
                    />
                  </div>
                  <div className="flex items-center justify-between">
                    <Label htmlFor="push-notifications" className="flex flex-col space-y-1">
                      <span>Push notificaties</span>
                      <span className="font-normal text-xs text-muted-foreground">
                        Ontvang notificaties op je apparaat
                      </span>
                    </Label>
                    <Switch
                      id="push-notifications"
                      checked={notifications.push}
                      onCheckedChange={(checked) => setNotifications({ ...notifications, push: checked })}
                    />
                  </div>
                </div>
              </div>

              <Separator />

              <div className="space-y-4">
                <h3 className="text-sm font-medium">Notificatietypes</h3>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="workout-reminders" className="flex flex-col space-y-1">
                      <span>Workout herinneringen</span>
                      <span className="font-normal text-xs text-muted-foreground">
                        Ontvang herinneringen voor geplande workouts
                      </span>
                    </Label>
                    <Switch
                      id="workout-reminders"
                      checked={notifications.workout}
                      onCheckedChange={(checked) => setNotifications({ ...notifications, workout: checked })}
                    />
                  </div>
                  <div className="flex items-center justify-between">
                    <Label htmlFor="goal-updates" className="flex flex-col space-y-1">
                      <span>Doel updates</span>
                      <span className="font-normal text-xs text-muted-foreground">
                        Ontvang updates over je voortgang richting doelen
                      </span>
                    </Label>
                    <Switch
                      id="goal-updates"
                      checked={notifications.goals}
                      onCheckedChange={(checked) => setNotifications({ ...notifications, goals: checked })}
                    />
                  </div>
                  <div className="flex items-center justify-between">
                    <Label htmlFor="weekly-reminders" className="flex flex-col space-y-1">
                      <span>Wekelijkse herinneringen</span>
                      <span className="font-normal text-xs text-muted-foreground">
                        Ontvang wekelijkse herinneringen om je workouts bij te houden
                      </span>
                    </Label>
                    <Switch
                      id="weekly-reminders"
                      checked={notifications.reminders}
                      onCheckedChange={(checked) => setNotifications({ ...notifications, reminders: checked })}
                    />
                  </div>
                </div>
              </div>
            </CardContent>
            <CardFooter>
              <Button>Opslaan</Button>
            </CardFooter>
          </Card>
        </TabsContent>

        <TabsContent value="preferences" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>App-voorkeuren</CardTitle>
              <CardDescription>Pas de app aan naar jouw voorkeuren</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-4">
                <h3 className="text-sm font-medium">Weergave</h3>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="dark-mode" className="flex flex-col space-y-1">
                      <span>Donkere modus</span>
                      <span className="font-normal text-xs text-muted-foreground">Schakel donkere modus in</span>
                    </Label>
                    <Switch
                      id="dark-mode"
                      checked={preferences.darkMode}
                      onCheckedChange={(checked) => setPreferences({ ...preferences, darkMode: checked })}
                    />
                  </div>
                </div>
              </div>

              <Separator />

              <div className="space-y-4">
                <h3 className="text-sm font-medium">Taal en eenheden</h3>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="language">Taal</Label>
                    <select
                      id="language"
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      value={preferences.language}
                      onChange={(e) => setPreferences({ ...preferences, language: e.target.value })}
                    >
                      <option value="nl">Nederlands</option>
                      <option value="en">Engels</option>
                      <option value="de">Duits</option>
                      <option value="fr">Frans</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="units">Eenheden</Label>
                    <select
                      id="units"
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      value={preferences.units}
                      onChange={(e) => setPreferences({ ...preferences, units: e.target.value })}
                    >
                      <option value="metric">Metrisch (km, kg)</option>
                      <option value="imperial">Imperiaal (mijl, lbs)</option>
                    </select>
                  </div>
                </div>
              </div>
            </CardContent>
            <CardFooter>
              <Button>Opslaan</Button>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
