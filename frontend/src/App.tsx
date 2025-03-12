import { Button } from "@/components/ui/button"
import { Link } from "react-router"

function App() {
  return (
    <div className="flex flex-col items-center justify-center min-h-svh">
      <Button asChild>
        <Link to="/dashboard">Click Me</Link>
      </Button>
    </div>
  )
}

export default App
