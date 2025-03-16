"use client";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { PolicySelector } from "@/components/policy-selector";
import { ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router";

export default function SetPolicyPage() {
  const navigate = useNavigate();

  const handlePolicySet = () => {
    navigate("/onboarding/encryption-complete");
  };

  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <Link
            to="/onboarding/get-key"
            className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Key Download
          </Link>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Set Access Policy</CardTitle>
            <CardDescription>
              Define who can access your encrypted file
            </CardDescription>
          </CardHeader>

          <CardContent>
            <PolicySelector onPolicySet={handlePolicySet} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
