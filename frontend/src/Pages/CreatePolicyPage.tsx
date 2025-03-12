import { useState } from "react"
import { useNavigate } from "react-router"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { X, Plus, Check, AlertCircle } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { toast } from "sonner"

type AttributeGroup = {
  id: string
  attributes: string[]
  operator: "AND" | "OR"
}

export default function CreatePolicyPage() {
    let navigate = useNavigate();
  const [isCreating, setIsCreating] = useState(false)
  const [policyName, setPolicyName] = useState("")
  const [policyDescription, setPolicyDescription] = useState("")
  const [attributeGroups, setAttributeGroups] = useState<AttributeGroup[]>([
    { id: "group-1", attributes: [""], operator: "AND" },
  ])
  const [groupOperator, setGroupOperator] = useState<"AND" | "OR">("AND")

  // Available attributes for the demo
  const availableAttributes = [
    "department:engineering",
    "department:marketing",
    "department:finance",
    "department:hr",
    "role:admin",
    "role:manager",
    "role:developer",
    "role:designer",
    "role:accountant",
    "clearance:top-secret",
    "clearance:secret",
    "clearance:confidential",
    "location:hq",
    "location:remote",
  ]

  const handleAddAttributeGroup = () => {
    setAttributeGroups([
      ...attributeGroups,
      { id: `group-${attributeGroups.length + 1}`, attributes: [""], operator: "AND" },
    ])
  }

  const handleRemoveAttributeGroup = (groupId: string) => {
    if (attributeGroups.length > 1) {
      setAttributeGroups(attributeGroups.filter((group) => group.id !== groupId))
    }
  }

  const handleAddAttribute = (groupId: string) => {
    setAttributeGroups(
      attributeGroups.map((group) => {
        if (group.id === groupId) {
          return {
            ...group,
            attributes: [...group.attributes, ""],
          }
        }
        return group
      }),
    )
  }

  const handleRemoveAttribute = (groupId: string, index: number) => {
    setAttributeGroups(
      attributeGroups.map((group) => {
        if (group.id === groupId && group.attributes.length > 1) {
          const newAttributes = [...group.attributes]
          newAttributes.splice(index, 1)
          return {
            ...group,
            attributes: newAttributes,
          }
        }
        return group
      }),
    )
  }

  const handleAttributeChange = (groupId: string, index: number, value: string) => {
    setAttributeGroups(
      attributeGroups.map((group) => {
        if (group.id === groupId) {
          const newAttributes = [...group.attributes]
          newAttributes[index] = value
          return {
            ...group,
            attributes: newAttributes,
          }
        }
        return group
      }),
    )
  }

  const handleGroupOperatorChange = (groupId: string, operator: "AND" | "OR") => {
    setAttributeGroups(
      attributeGroups.map((group) => {
        if (group.id === groupId) {
          return {
            ...group,
            operator,
          }
        }
        return group
      }),
    )
  }

  const generatePolicyExpression = () => {
    const groupExpressions = attributeGroups
      .map((group) => {
        const validAttributes = group.attributes.filter((attr) => attr.trim() !== "")
        if (validAttributes.length === 0) return ""
        if (validAttributes.length === 1) return validAttributes[0]

        return `(${validAttributes.join(` ${group.operator} `)})`
      })
      .filter((expr) => expr !== "")

    if (groupExpressions.length === 0) return ""
    if (groupExpressions.length === 1) return groupExpressions[0]

    return groupExpressions.join(` ${groupOperator} `)
  }

  const handleCreatePolicy = async () => {
    if (!policyName.trim()) {
      toast("Policy name required", {
        description: "Please provide a name for your policy",
      })
      return
    }

    const policyExpression = generatePolicyExpression()
    if (!policyExpression) {
      toast("Invalid policy", {
        description: "Please add at least one attribute to your policy",
      })
      return
    }

    setIsCreating(true)
    try {
      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1500))

      toast("Policy created", {
        description: "Your access policy has been created successfully",
      })

      navigate("/dashboard/policies")
    } catch (error) {
      toast("Failed to create policy", {
        description: "There was an error creating your policy",
      })
    } finally {
      setIsCreating(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Create Access Policy</h1>
        <p className="text-muted-foreground">Define attribute-based access control policies for your encrypted files</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Policy Details</CardTitle>
          <CardDescription>Provide basic information about your access policy</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="policy-name">Policy Name</Label>
            <Input
              id="policy-name"
              placeholder="e.g., Engineering Team Access"
              value={policyName}
              onChange={(e) => setPolicyName(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="policy-description">Description (Optional)</Label>
            <Textarea
              id="policy-description"
              placeholder="Describe the purpose of this policy"
              value={policyDescription}
              onChange={(e) => setPolicyDescription(e.target.value)}
              className="min-h-[80px]"
            />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Policy Builder</CardTitle>
          <CardDescription>Build your access policy using attributes and logical operators</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {attributeGroups.length > 1 && (
            <div className="flex items-center space-x-2">
              <Label>Combine Groups With:</Label>
              <Select value={groupOperator} onValueChange={(value) => setGroupOperator(value as "AND" | "OR")}>
                <SelectTrigger className="w-24">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="AND">AND</SelectItem>
                  <SelectItem value="OR">OR</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}

          {attributeGroups.map((group, groupIndex) => (
            <div key={group.id} className="rounded-lg border p-4 space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <h3 className="font-medium">Attribute Group {groupIndex + 1}</h3>
                  {group.attributes.length > 1 && (
                    <Select
                      value={group.operator}
                      onValueChange={(value) => handleGroupOperatorChange(group.id, value as "AND" | "OR")}
                    >
                      <SelectTrigger className="w-24">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="AND">AND</SelectItem>
                        <SelectItem value="OR">OR</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                </div>
                {attributeGroups.length > 1 && (
                  <Button variant="ghost" size="icon" onClick={() => handleRemoveAttributeGroup(group.id)}>
                    <X className="h-4 w-4" />
                    <span className="sr-only">Remove Group</span>
                  </Button>
                )}
              </div>

              <div className="space-y-2">
                {group.attributes.map((attribute, attrIndex) => (
                  <div key={`${group.id}-attr-${attrIndex}`} className="flex items-center space-x-2">
                    <Select
                      value={attribute}
                      onValueChange={(value) => handleAttributeChange(group.id, attrIndex, value)}
                    >
                      <SelectTrigger className="flex-1">
                        <SelectValue placeholder="Select an attribute" />
                      </SelectTrigger>
                      <SelectContent>
                        {availableAttributes.map((attr) => (
                          <SelectItem key={attr} value={attr}>
                            {attr}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {group.attributes.length > 1 && (
                      <Button variant="ghost" size="icon" onClick={() => handleRemoveAttribute(group.id, attrIndex)}>
                        <X className="h-4 w-4" />
                        <span className="sr-only">Remove Attribute</span>
                      </Button>
                    )}
                  </div>
                ))}
              </div>

              <Button variant="outline" size="sm" onClick={() => handleAddAttribute(group.id)}>
                <Plus className="mr-2 h-3 w-3" />
                Add Attribute
              </Button>
            </div>
          ))}

          <Button variant="outline" onClick={handleAddAttributeGroup}>
            <Plus className="mr-2 h-4 w-4" />
            Add Attribute Group
          </Button>

          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Policy Expression Preview</AlertTitle>
            <AlertDescription>
              <code className="block p-2 mt-2 bg-muted rounded-md text-sm whitespace-pre-wrap">
                {generatePolicyExpression() || "(No attributes selected)"}
              </code>
            </AlertDescription>
          </Alert>
        </CardContent>
        <CardFooter className="flex justify-between">
          <Button variant="outline" onClick={() => navigate("/dashboard/policies")}>
            Cancel
          </Button>
          <Button onClick={handleCreatePolicy} disabled={isCreating}>
            {isCreating ? (
              <>Creating Policy...</>
            ) : (
              <>
                <Check className="mr-2 h-4 w-4" />
                Create Policy
              </>
            )}
          </Button>
        </CardFooter>
      </Card>
    </div>
  )
}

