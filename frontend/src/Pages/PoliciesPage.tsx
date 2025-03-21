import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { Plus, Trash, Edit, Info } from "lucide-react";
import { Link } from "react-router";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { auth } from "@/lib/auth";

type Policy = {
  id: number;
  policyName: string;
  policyDescription: string;
  policyExpression: string;
  userId: number;
};

export default function PoliciesPage() {
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedPolicy, setSelectedPolicy] = useState<string | null>(null);
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const handleDeletePolicy = async (policyId: number) => {
    setIsDeleting(true);
    try {
      const response = await fetch(
        `http://localhost:8080/api/user/delete-policy/${policyId}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${auth.getToken()}`,
          },
        }
      );
      // Fetch policies again after successful deletion
      setPolicies(policies.filter((policy) => policy.id !== policyId));
      if (response.ok) {
        toast("Policy deleted", {
          description: "The policy has been deleted successfully",
        });
      }
    } catch (error) {
      toast("Failed to delete policy", {
        description: "There was an error deleting the policy",
      });
    } finally {
      setIsDeleting(false);
      setSelectedPolicy(null);
    }
  };

  useEffect(() => {
    const fetchPolicies = async () => {
      setIsLoading(true);
      try {
        const response = await fetch(
          "http://localhost:8080/api/user/get-policy",
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${auth.getToken()}`,
            },
          }
        );
        const data = await response.json();

        if (response.ok) {
          setPolicies(Array.isArray(data.body) ? data.body : []);
        } else {
          toast.error("Failed to fetch policies", {
            description: data.message || "Something went wrong",
          });
          setPolicies([]);
        }
      } catch (error) {
        console.error("Error fetching policies:", error);
        toast.error("Failed to fetch policies", {
          description: "There was an error connecting to the server",
        });
        setPolicies([]);
      } finally {
        setIsLoading(false);
      }
    };
    fetchPolicies();
  }, []);

  return (
    <div className="container mx-auto py-8 px-4 min">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-semibold">Access Policies</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Manage attribute-based access control policies
          </p>
        </div>
        <Button asChild size="sm">
          <Link to="/dashboard/policies/create">
            <Plus className="mr-2 h-4 w-4" />
            Create Policy
          </Link>
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 min-h-[40px] min-w-[150px]">
        {isLoading ? (
          <Card className="border border-border/40 col-span-full flex flex-col items-center justify-center p-6">
            <CardHeader>
              <CardTitle>Loading...</CardTitle>
              <CardDescription>
                Please wait while we fetch your policies
              </CardDescription>
            </CardHeader>
          </Card>
        ) : policies.length > 0 ? (
          policies.map((policy) => (
            <Card key={policy.id} className="border border-border/40">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg font-medium">
                    {policy.policyName}
                  </CardTitle>
                  {/* <Badge variant="secondary" className="font-normal text-xs">
                  {policy.files} files
                </Badge> */}
                </div>
                <CardDescription className="mt-1 text-sm">
                  {policy.policyDescription}
                </CardDescription>
              </CardHeader>
              <CardContent className="pb-3">
                <div className="space-y-2">
                  <div className="flex items-center">
                    <div className="text-xs font-medium">Policy Expression</div>
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-6 w-6 ml-1 p-0"
                          >
                            <Info className="h-3.5 w-3.5" />
                            <span className="sr-only">Policy Info</span>
                          </Button>
                        </TooltipTrigger>
                        <TooltipContent side="top">
                          <p className="max-w-xs text-xs">
                            This policy uses AND operators to combine
                            attributes. Users must possess the required
                            attributes to access files.
                          </p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </div>
                  <div className="rounded-md bg-muted p-2 min-h-[40px] min-w-[150px] flex items-center">
                    <code className="text-xs">
                      {policy.policyExpression.replace("/_/g", ":")}
                    </code>
                  </div>
                  {/* <div className="text-xs text-muted-foreground">
                  Created on {policy.created}
                </div> */}
                </div>
              </CardContent>
              <CardFooter className="pt-0 flex justify-end gap-2">
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        asChild
                      >
                        <Link to={`/dashboard/policies/edit/${policy.id}`}>
                          <Edit className="h-4 w-4" />
                          <span className="sr-only">Edit policy</span>
                        </Link>
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>Edit policy</TooltipContent>
                  </Tooltip>
                </TooltipProvider>

                <Dialog>
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <DialogTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10"
                            onClick={() => setSelectedPolicy(policy.policyName)}
                          >
                            <Trash className="h-4 w-4" />
                            <span className="sr-only">Delete policy</span>
                          </Button>
                        </DialogTrigger>
                      </TooltipTrigger>
                      <TooltipContent>Delete policy</TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                  <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                      <DialogTitle>Delete Policy</DialogTitle>
                      <DialogDescription className="text-sm">
                        Are you sure you want to delete this policy? Files
                        encrypted with this policy may become inaccessible.
                      </DialogDescription>
                    </DialogHeader>
                    <DialogFooter className="mt-4 gap-2 sm:gap-0">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setSelectedPolicy(null)}
                      >
                        Cancel
                      </Button>
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleDeletePolicy(policy.id)}
                        disabled={isDeleting}
                      >
                        {isDeleting ? "Deleting..." : "Delete"}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </CardFooter>
            </Card>
          ))
        ) : (
          <div className="col-span-full flex justify-center items-center text-muted-foreground">
            No Policies Found
          </div>
        )}
      </div>
    </div>
  );
}
