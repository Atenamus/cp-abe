import type React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { HelpCircle } from "lucide-react";
import { auth } from "@/lib/auth";
import { toast } from "sonner";

interface SignupFormProps {
  onSubmit?: (data: {
    fullName: string;
    email: string;
    password: string;
    attributes: string[];
  }) => void;
}

export function SignUpForm({ onSubmit }: SignupFormProps) {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [attributes, setAttributes] = useState<
    { id: string; label: string; checked: boolean }[]
  >([
    { id: "admin", label: "Admin", checked: false },
    { id: "hr", label: "HR", checked: false },
    { id: "manager", label: "Manager", checked: false },
    { id: "finance", label: "Finance", checked: false },
    { id: "engineering", label: "Engineering", checked: false },
    { id: "marketing", label: "Marketing", checked: false },
  ]);

  const [passwordError, setPasswordError] = useState("");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    if (name === "confirmPassword" || name === "password") {
      if (name === "confirmPassword" && value !== formData.password) {
        setPasswordError("Passwords do not match");
      } else if (
        name === "password" &&
        formData.confirmPassword &&
        value !== formData.confirmPassword
      ) {
        setPasswordError("Passwords do not match");
      } else {
        setPasswordError("");
      }
    }
  };

  const toggleAttribute = (id: string) => {
    setAttributes((prev) =>
      prev.map((attr) =>
        attr.id === id ? { ...attr, checked: !attr.checked } : attr
      )
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      setPasswordError("Passwords do not match");
      return;
    }

    const selectedAttributes = attributes
      .filter((attr) => attr.checked)
      .map((attr) => attr.id);

    setIsLoading(true);

    try {
      const signupData = {
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        attributes: selectedAttributes,
      };

      await auth.signUp(signupData);
      toast.success("Successfully signed up!");

      if (onSubmit) {
        onSubmit(signupData);
      } else {
        navigate("/dashboard");
      }
    } catch (error) {
      toast.error("Registration failed. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="fullName">Full Name</Label>
          <Input
            id="fullName"
            name="fullName"
            placeholder="John Doe"
            value={formData.fullName}
            onChange={handleChange}
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input
            id="email"
            name="email"
            type="email"
            placeholder="john@example.com"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="password">Password</Label>
          <Input
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="confirmPassword">Confirm Password</Label>
          <Input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
          />
          {passwordError && (
            <p className="text-sm text-destructive">{passwordError}</p>
          )}
        </div>

        <div className="space-y-3">
          <div className="flex items-center">
            <Label>Select Attributes</Label>
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8 ml-2">
                    <HelpCircle className="h-4 w-4" />
                    <span className="sr-only">Why do I need attributes?</span>
                  </Button>
                </TooltipTrigger>
                <TooltipContent className="max-w-xs">
                  <p>
                    Attributes define your access rights in the system. Your
                    private key will be generated based on these attributes,
                    determining which encrypted files you can access.
                  </p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {attributes.map((attribute) => (
              <div key={attribute.id} className="flex items-center space-x-2">
                <Checkbox
                  id={attribute.id}
                  checked={attribute.checked}
                  onCheckedChange={() => toggleAttribute(attribute.id)}
                />
                <Label htmlFor={attribute.id} className="cursor-pointer">
                  {attribute.label}
                </Label>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="flex items-center justify-center">
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
              <Button variant="link" type="button" className="text-sm">
                Why do I need to sign up?
              </Button>
            </TooltipTrigger>
            <TooltipContent className="max-w-xs">
              <p>
                Signing up is required because CPABE (Ciphertext-Policy
                Attribute-Based Encryption) needs to generate a unique private
                key based on your attributes. This key will be used to decrypt
                files if your attributes match the encryption policy.
              </p>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </div>

      <Button type="submit" className="w-full" disabled={isLoading}>
        {isLoading ? "Signing up..." : "Sign Up & Continue"}
      </Button>
    </form>
  );
}
