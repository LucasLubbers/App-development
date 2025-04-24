import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Dumbbell } from "lucide-react"

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 border-b bg-background/95 backdrop-blur">
        <div className="container flex h-14 items-center px-4 md:px-6 lg:px-8">
          <div className="flex items-center gap-2 font-bold">
            <Dumbbell className="h-5 w-5" />
            <span>Workout Tracker</span>
          </div>
        </div>
      </header>
      <main className="flex-1">
        <section className="space-y-6 pb-8 pt-6 md:pb-12 md:pt-10 lg:py-32">
          <div className="container flex max-w-[64rem] flex-col items-center gap-4 text-center px-4 md:px-6 lg:px-8">
            <h1 className="text-3xl font-bold sm:text-4xl md:text-5xl lg:text-6xl">
              Houd je workouts bij, stel doelen en bekijk je voortgang
            </h1>
            <p className="max-w-[42rem] leading-normal text-muted-foreground sm:text-xl sm:leading-8">
              Een eenvoudige manier om je fitness reis te volgen en gemotiveerd te blijven
            </p>
            <div className="flex flex-col gap-4 sm:flex-row">
              <Link href="/login">
                <Button size="lg">Inloggen</Button>
              </Link>
              <Link href="/signup">
                <Button variant="outline" size="lg">
                  Account aanmaken
                </Button>
              </Link>
            </div>
          </div>
        </section>
        <section className="container space-y-6 py-8 md:py-12 lg:py-24 px-4 md:px-6 lg:px-8">
          <div className="mx-auto grid max-w-[58rem] gap-8 sm:grid-cols-2 md:grid-cols-3">
            <div className="flex flex-col items-center space-y-2 rounded-lg border p-6">
              <div className="rounded-full bg-primary/10 p-3">
                <Dumbbell className="h-6 w-6 text-primary" />
              </div>
              <h3 className="text-xl font-bold">Workouts bijhouden</h3>
              <p className="text-center text-muted-foreground">
                Houd al je workouts bij, inclusief type, duur en notities
              </p>
            </div>
            <div className="flex flex-col items-center space-y-2 rounded-lg border p-6">
              <div className="rounded-full bg-primary/10 p-3">
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
                  className="h-6 w-6 text-primary"
                >
                  <path d="M12 20v-6" />
                  <path d="M6 20v-6" />
                  <path d="M18 20v-6" />
                  <path d="M6 14v-4" />
                  <path d="M18 14v-4" />
                  <path d="M12 14v-4" />
                  <path d="M12 10V4" />
                  <path d="M6 4v4" />
                  <path d="M18 4v4" />
                </svg>
              </div>
              <h3 className="text-xl font-bold">Statistieken bekijken</h3>
              <p className="text-center text-muted-foreground">
                Bekijk je voortgang met duidelijke grafieken en statistieken
              </p>
            </div>
            <div className="flex flex-col items-center space-y-2 rounded-lg border p-6">
              <div className="rounded-full bg-primary/10 p-3">
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
                  className="h-6 w-6 text-primary"
                >
                  <path d="M8 2v4" />
                  <path d="M16 2v4" />
                  <rect width="18" height="18" x="3" y="4" rx="2" />
                  <path d="M3 10h18" />
                  <path d="m9 16 2 2 4-4" />
                </svg>
              </div>
              <h3 className="text-xl font-bold">Doelen stellen</h3>
              <p className="text-center text-muted-foreground">Stel persoonlijke doelen en volg je voortgang</p>
            </div>
          </div>
        </section>
      </main>
      <footer className="border-t py-6 md:py-0">
        <div className="container flex flex-col items-center justify-between gap-4 md:h-24 md:flex-row px-4 md:px-6 lg:px-8">
          <p className="text-center text-sm leading-loose text-muted-foreground md:text-left">
            &copy; {new Date().getFullYear()} Workout Tracker. Alle rechten voorbehouden.
          </p>
        </div>
      </footer>
    </div>
  )
}
