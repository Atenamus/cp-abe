import { SignUpForm } from "@/components/sign-up-form";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router";

export default function LoginPage() {
  const navigate = useNavigate();

  const handleSignup = (data: {
    fullName: string;
    email: string;
    password: string;
    attributes: string[];
  }) => {
    // In a real app, we would store the user data in state management or send it to the server
    // For now, we'll just navigate to the key generation page
    navigate("/onboarding/get-key");
  };

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-muted p-6 md:p-10">
      <div className="flex w-full max-w-sm flex-col gap-6">
        <div className="">
          <Link
            to="/"
            className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Home
          </Link>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Create an Account</CardTitle>
            <CardDescription>
              Sign up to generate your private key and encrypt your file
            </CardDescription>
          </CardHeader>

          <CardContent>
            <SignUpForm onSubmit={handleSignup} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
