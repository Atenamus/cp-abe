import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { HelpCircle } from "lucide-react";

interface PolicySelectorProps {
  value?: string;
  onChange?: (value: string) => void;
  disabled?: boolean;
}

export function PolicySelector({
  value,
  onChange,
  disabled = false,
}: PolicySelectorProps) {
  const [policyType, setPolicyType] = useState("predefined");
  const [selectedPolicy, setSelectedPolicy] = useState("");
  const [customPolicy, setCustomPolicy] = useState("");

  const handlePolicyChange = (newValue: string) => {
    if (policyType === "predefined") {
      const policy =
        predefinedPolicies.find((p) => p.id === newValue)?.value || "";
      setSelectedPolicy(newValue);
      onChange?.(policy);
    } else {
      setCustomPolicy(newValue);
      onChange?.(newValue);
    }
  };

  const predefinedPolicies = [
    { id: "hr_only", label: "Only HR can access", value: "department_HR" },
    {
      id: "managers_admins",
      label: "Managers OR Admins can access",
      value: "role_manager OR role_admin",
    },
    {
      id: "engineering_finance",
      label: "Engineering AND Finance can access",
      value: "department_engineering AND department_finance",
    },
    {
      id: "senior_staff",
      label: "Senior staff (3+ years experience)",
      value: "experience:3+",
    },
  ];

  return (
    <div className="space-y-6">
      <div className="space-y-4">
        <div className="flex items-center">
          <Label>Access Policy Type</Label>
          <TooltipProvider>
            <Tooltip>
              <TooltipTrigger asChild>
                <Button variant="ghost" size="icon" className="h-8 w-8 ml-2">
                  <HelpCircle className="h-4 w-4" />
                  <span className="sr-only">What is an access policy?</span>
                </Button>
              </TooltipTrigger>
              <TooltipContent className="max-w-xs">
                <p>
                  An access policy defines which attributes a user must have to
                  decrypt this file. You can use logical operators like AND, OR
                  to create complex policies.
                </p>
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>

        <RadioGroup
          value={policyType}
          onValueChange={setPolicyType}
          className="flex flex-col space-y-1"
          disabled={disabled}
        >
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="predefined" id="predefined" />
            <Label htmlFor="predefined">Use predefined policy</Label>
          </div>
          <div className="flex items-center space-x-2">
            <RadioGroupItem value="custom" id="custom" />
            <Label htmlFor="custom">Create custom policy</Label>
          </div>
        </RadioGroup>
      </div>

      {policyType === "predefined" ? (
        <div className="space-y-2">
          <Label htmlFor="policy">Select Policy</Label>
          <Select
            value={selectedPolicy}
            onValueChange={handlePolicyChange}
            disabled={disabled}
          >
            <SelectTrigger id="policy">
              <SelectValue placeholder="Select a policy" />
            </SelectTrigger>
            <SelectContent>
              {predefinedPolicies.map((policy) => (
                <SelectItem key={policy.id} value={policy.id}>
                  {policy.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {selectedPolicy && (
            <div className="text-sm text-muted-foreground mt-2">
              Policy expression:{" "}
              <code className="bg-muted px-1 py-0.5 rounded text-xs">
                {predefinedPolicies.find((p) => p.id === selectedPolicy)?.value}
              </code>
            </div>
          )}
        </div>
      ) : (
        <div className="space-y-2">
          <Label htmlFor="custom-policy">Custom Policy Expression</Label>
          <Textarea
            id="custom-policy"
            placeholder="Example: (role:Admin OR department:HR) AND experience:2+"
            value={customPolicy}
            onChange={(e) => handlePolicyChange(e.target.value)}
            disabled={disabled}
          />
          <p className="text-xs text-muted-foreground">
            Use attribute:value format with AND, OR operators. Parentheses can
            be used for grouping.
          </p>
        </div>
      )}
    </div>
  );
}
