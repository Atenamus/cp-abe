import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { toast } from "sonner"
import { Plus, Trash, Edit, Copy, Info } from "lucide-react"
import { Link } from "react-router"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"

export default function PoliciesPage() {
  const [isDeleting, setIsDeleting] = useState(false)
  const [selectedPolicy, setSelectedPolicy] = useState<string | null>(null)

  const handleDeletePolicy = async (policyId: string) => {
    setIsDeleting(true)
    try {
      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000))
      toast("Policy deleted", {
        description: "The policy has been deleted successfully",
      })
    } catch (error) {
      toast("Failed to delete policy", {
        description: "There was an error deleting the policy",
      })
    } finally {
      setIsDeleting(false)
      setSelectedPolicy(null)
    }
  }

  const handleCopyPolicy = (policyId: string) => {
    toast("Policy copied", {
      description: "The policy has been copied to clipboard",
    })
  }

  // Sample policy data
  const policies = [
    {
      id: "pol-1",
      name: "Engineering Team Access",
      description: "Access policy for engineering team members",
      created: "2025-01-15",
      expression: "(department:engineering AND role:developer) OR (role:admin)",
      files: 8,
    },
    {
      id: "pol-2",
      name: "Marketing Documents",
      description: "Access policy for marketing materials",
      created: "2025-02-20",
      expression: "department:marketing AND (role:manager OR role:content-creator)",
      files: 12,
    },
    {
      id: "pol-3",
      name: "Financial Reports",
      description: "Access policy for financial documents",
      created: "2025-03-05",
      expression: "department:finance AND (role:accountant OR role:executive)",
      files: 5,
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Access Policies</h1>
          <p className="text-muted-foreground">Create and manage attribute-based access control policies</p>
        </div>
        <Button asChild>
          <Link to="/dashboard/policies/create">
            <Plus className="mr-2 h-4 w-4" />
            Create Policy
          </Link>
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {policies.map((policy) => (
          <Card key={policy.id}>
            <CardHeader className="pb-2">
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg">{policy.name}</CardTitle>
                <Badge>{policy.files} files</Badge>
              </div>
              <CardDescription>{policy.description}</CardDescription>
            </CardHeader>
            <CardContent className="pb-2">
              <div className="space-y-2">
                <div className="flex items-center">
                  <div className="text-sm font-medium">Policy Expression:</div>
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-6 w-6 ml-1">
                          <Info className="h-3 w-3" />
                          <span className="sr-only">Policy Info</span>
                        </Button>
                      </TooltipTrigger>
                      <TooltipContent>
                        <p className="max-w-xs text-xs">
                          This policy uses AND/OR operators to combine attributes. Users must possess the required
                          attributes to access files.
                        </p>
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                </div>
                <div className="rounded-md bg-muted p-2">
                  <code className="text-xs">{policy.expression}</code>
                </div>
                <div className="text-xs text-muted-foreground">Created on {policy.created}</div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-between">
              <div className="flex space-x-2">
                <Button variant="outline" size="sm" onClick={() => handleCopyPolicy(policy.id)}>
                  <Copy className="mr-2 h-3 w-3" />
                  Copy
                </Button>
                <Button variant="outline" size="sm" asChild>
                  <Link to={`/dashboard/policies/edit/${policy.id}`}>
                    <Edit className="mr-2 h-3 w-3" />
                    Edit
                  </Link>
                </Button>
              </div>
              <Dialog>
                <DialogTrigger asChild>
                  <Button variant="destructive" size="sm" onClick={() => setSelectedPolicy(policy.id)}>
                    <Trash className="mr-2 h-3 w-3" />
                    Delete
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Delete Policy</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to delete this policy? This will not affect files that have already been
                      encrypted with this policy, but they may become inaccessible.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setSelectedPolicy(null)}>
                      Cancel
                    </Button>
                    <Button variant="destructive" onClick={() => handleDeletePolicy(policy.id)} disabled={isDeleting}>
                      {isDeleting ? "Deleting..." : "Delete"}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  )
}

