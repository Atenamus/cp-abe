import { useState } from "react";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { RefreshCw, ShieldCheck, Download, Plus } from "lucide-react";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";

const attributeOptions = [
  { id: "department_HR", label: "HR Department" },
  { id: "department_Engineering", label: "Engineering Department" },
  { id: "department_Finance", label: "Finance Department" },
  { id: "role_Admin", label: "Admin Role" },
  { id: "role_Manager", label: "Manager Role" },
  { id: "experience_gt_2", label: "2+ Years Experience" },
  { id: "experience_gt_5", label: "5+ Years Experience" },
];

export default function KeyManagementPage() {
  const [isGenerating, setIsGenerating] = useState(false);
  const [selectedAttributes, setSelectedAttributes] = useState<string[]>([]);
  const [customAttribute, setCustomAttribute] = useState("");
  const [customAttributes, setCustomAttributes] = useState<string[]>([]);

  const handleAttributeSelect = (attributeId: string) => {
    setSelectedAttributes((current) =>
      current.includes(attributeId)
        ? current.filter((id) => id !== attributeId)
        : [...current, attributeId]
    );
  };

  const validateAttributeFormat = (attribute: string): boolean => {
    // Check for category_value format using regex - case insensitive check
    return /^[a-zA-Z0-9]+_[a-zA-Z0-9]+(?:_[a-zA-Z0-9]+)*$/.test(attribute);
  };

  /**
   * Parses an attribute with comparison operators into the format expected by the backend
   * Examples:
   * - "department = HR" becomes "department_HR"
   * - "age >= 30" becomes "age_ge_30"
   * - "salary > 50000" becomes "salary_gt_50000"
   * - "level <= 5" becomes "level_le_5"
   * - "experience < 2" becomes "experience_lt_2"
   */
  const parseAttribute = (input: string): string | null => {
    input = input.trim();

    // Handle equality (=)
    const eqRegex = /^(\w+)\s*=\s*(\w+)$/;
    const eqMatch = input.match(eqRegex);
    if (eqMatch) {
      return `${eqMatch[1]}_${eqMatch[2]}`;
    }

    // Handle greater than or equal (>=)
    const geRegex = /^(\w+)\s*>=\s*(\w+)$/;
    const geMatch = input.match(geRegex);
    if (geMatch) {
      return `${geMatch[1]}_ge_${geMatch[2]}`;
    }

    // Handle greater than (>)
    const gtRegex = /^(\w+)\s*>\s*(\w+)$/;
    const gtMatch = input.match(gtRegex);
    if (gtMatch) {
      return `${gtMatch[1]}_gt_${gtMatch[2]}`;
    }

    // Handle less than or equal (<=)
    const leRegex = /^(\w+)\s*<=\s*(\w+)$/;
    const leMatch = input.match(leRegex);
    if (leMatch) {
      return `${leMatch[1]}_le_${leMatch[2]}`;
    }

    // Handle less than (<)
    const ltRegex = /^(\w+)\s*<\s*(\w+)$/;
    const ltMatch = input.match(ltRegex);
    if (ltMatch) {
      return `${ltMatch[1]}_lt_${ltMatch[2]}`;
    }

    // If the input already follows the correct format, return as is
    if (validateAttributeFormat(input)) {
      return input;
    }

    // Return null if no matches found and it doesn't match the format
    return null;
  };

  const handleAddCustomAttribute = () => {
    if (customAttribute.trim() === "") {
      return;
    }

    // Parse the custom attribute using the new parseAttribute function
    const parsedAttribute = parseAttribute(customAttribute);

    if (!parsedAttribute) {
      toast("Invalid attribute format", {
        description:
          "Attributes must be in format 'category = value', 'category > value', etc. or directly as 'category_value'",
        duration: 3000,
      });
      return;
    }

    if (!customAttributes.includes(parsedAttribute)) {
      setCustomAttributes([...customAttributes, parsedAttribute]);
      setCustomAttribute("");
    } else {
      toast("Duplicate attribute", {
        description: "This attribute has already been added",
        duration: 2000,
      });
    }
  };

  const handleRemoveCustomAttribute = (attribute: string) => {
    setCustomAttributes(customAttributes.filter((attr) => attr !== attribute));
  };

  const handleGenerateKey = async () => {
    // Combine predefined and custom attributes
    const allAttributes = [...selectedAttributes, ...customAttributes];

    if (allAttributes.length === 0) {
      toast("Select attributes", {
        description: "Please select at least one attribute for the key",
      });
      return;
    }

    setIsGenerating(true);
    try {
      const response = await fetch("http://localhost:8080/api/cpabe/keygen", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ attributes: allAttributes }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `private_key_${new Date().getTime()}.dat`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);

      toast("Key generated successfully", {
        description: "Your new private key has been created and downloaded",
      });
    } catch (error) {
      console.error("Error generating key:", error);
      toast("Failed to generate key", {
        description: "There was an error generating your private key",
      });
    } finally {
      setIsGenerating(false);
    }
  };

  // Get combined attributes for display
  const allAttributes = [...selectedAttributes, ...customAttributes];
  const getAttributeLabel = (attributeId: string) => {
    const predefinedAttr = attributeOptions.find(
      (attr) => attr.id === attributeId
    );
    return predefinedAttr ? predefinedAttr.label : attributeId;
  };

  // Helper function to display human-readable attribute descriptions
  const getHumanReadableAttribute = (attributeId: string) => {
    // Check if it's a predefined attribute first
    const predefined = attributeOptions.find((attr) => attr.id === attributeId);
    if (predefined) {
      return predefined.label;
    }

    // Try to make custom attributes more readable
    const parts = attributeId.split("_");

    if (parts.length === 2) {
      // Simple category_value format
      return `${parts[0]}: ${parts[1]}`;
    } else if (parts.length === 3) {
      // Format with comparison operator
      const [category, operator, value] = parts;

      switch (operator) {
        case "gt":
          return `${category} > ${value}`;
        case "ge":
          return `${category} >= ${value}`;
        case "lt":
          return `${category} < ${value}`;
        case "le":
          return `${category} <= ${value}`;
        default:
          return attributeId; // Keep original if unknown format
      }
    }

    return attributeId; // Keep original if unknown format
  };

  return (
    <div className="space-y-8 max-w-5xl w-full mx-auto p-6">
      <div>
        <h1 className="text-3xl font-bold">Private Key Generation</h1>
        <p className="text-muted-foreground py-1.5">
          Generate an attribute-based private key for secure access control
        </p>
      </div>

      <Alert variant="default" className="bg-blue-50 border-blue-200">
        <ShieldCheck className="h-4 w-4" />
        <AlertTitle>How Key Generation Works</AlertTitle>
        <AlertDescription>
          The private key will be generated based on the attributes you select.
          These attributes determine which encrypted files you can access. You
          will need to store the key securely as it will not be saved on the
          server.
        </AlertDescription>
      </Alert>

      <Card className="shadow-md">
        <CardHeader>
          <CardTitle>Generate Attribute-Based Private Key</CardTitle>
          <CardDescription>
            Choose the attributes to embed in your private key that will
            determine your access permissions
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Predefined attributes section */}
          <div className="space-y-4">
            <div className="font-medium">Select Predefined Attributes:</div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {attributeOptions.map((attr) => (
                <div
                  key={attr.id}
                  className="flex items-center space-x-2 bg-gray-50 p-2 rounded-md"
                >
                  <Checkbox
                    id={attr.id}
                    checked={selectedAttributes.includes(attr.id)}
                    onCheckedChange={() => handleAttributeSelect(attr.id)}
                  />
                  <Label htmlFor={attr.id}>{attr.label}</Label>
                </div>
              ))}
            </div>
          </div>

          {/* Custom attributes section */}
          <div className="space-y-4 pt-4 border-t border-gray-200">
            <div className="font-medium">Add Custom Attributes:</div>
            <div className="flex flex-wrap gap-2">
              {customAttributes.map((attr, index) => (
                <div
                  key={index}
                  className="bg-blue-50 px-3 py-1 rounded-full flex items-center gap-1"
                >
                  <span className="text-sm">
                    {getHumanReadableAttribute(attr)}
                  </span>
                  <button
                    onClick={() => handleRemoveCustomAttribute(attr)}
                    className="ml-1 text-gray-500 hover:text-red-500 focus:outline-none"
                  >
                    &times;
                  </button>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <div className="flex-grow">
                <Input
                  placeholder="Enter attribute (e.g., department = HR, age > 25)"
                  value={customAttribute}
                  onChange={(e) => setCustomAttribute(e.target.value)}
                  onKeyDown={(e) =>
                    e.key === "Enter" && handleAddCustomAttribute()
                  }
                />
              </div>
              <Button variant="outline" onClick={handleAddCustomAttribute}>
                <Plus className="h-4 w-4 mr-1" /> Add
              </Button>
            </div>
            <p className="text-xs text-gray-500">
              You can enter attributes in multiple formats:
              <br />- Direct format: category_value (e.g., location_NY)
              <br />- With comparison: category = value, category {">"} value,
              category {">"}= value, etc.
            </p>
          </div>

          {/* Final attributes summary */}
          <div className="pt-4 border-t border-gray-200">
            <div className="font-medium mb-2">
              Attributes for Key Generation:
            </div>
            {allAttributes.length > 0 ? (
              <div className="flex flex-wrap gap-2 mb-4">
                {allAttributes.map((attr, index) => (
                  <Badge key={index} variant="secondary" className="px-3 py-1">
                    {getHumanReadableAttribute(attr)}
                  </Badge>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground mb-4">
                No attributes selected yet. Please select at least one
                attribute.
              </p>
            )}

            {allAttributes.length > 0 && (
              <div className="bg-gray-50 p-3 rounded-md text-sm">
                <div className="font-medium mb-1">
                  Raw attribute values to be sent:
                </div>
                <code className="text-xs bg-gray-100 p-2 block rounded overflow-x-auto whitespace-pre">
                  {JSON.stringify(allAttributes, null, 2)}
                </code>
              </div>
            )}
          </div>

          <div className="pt-4">
            <Button
              onClick={handleGenerateKey}
              disabled={
                (selectedAttributes.length === 0 &&
                  customAttributes.length === 0) ||
                isGenerating
              }
              size="lg"
              className="w-full md:w-auto"
            >
              {isGenerating ? (
                <>
                  <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                  Generating...
                </>
              ) : (
                <>
                  <Download className="mr-2 h-4 w-4" />
                  Generate and Download Key
                </>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
