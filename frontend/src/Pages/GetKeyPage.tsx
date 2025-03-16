import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { KeyDownload } from "@/components/key-download";
import { ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router";

export default function GetKeyPage() {
  const navigate = useNavigate();

  // In a real app, this would come from state management or the server
  const mockUserData = {
    fullName: "John Doe",
    email: "john@example.com",
    attributes: ["Admin", "HR"],
  };

  const handleContinue = () => {
    navigate("/onboarding/set-policy");
  };

  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <Link
            to="/onboarding/sign-up"
            className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Sign Up
          </Link>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Your Private Key</CardTitle>
            <CardDescription>
              Download your attribute-based private key
            </CardDescription>
          </CardHeader>

          <CardContent>
            <KeyDownload userData={mockUserData} onContinue={handleContinue} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
