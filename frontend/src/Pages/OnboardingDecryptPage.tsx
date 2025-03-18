import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { FileUpload } from "@/components/file-upload";
import { KeyUpload } from "@/components/key-upload";
import { DecryptionResult } from "@/components/decryption-result";
import { ArrowLeft } from "lucide-react";
import { Link } from "react-router";

export default function OnboardingDecryptPage() {
  const [step, setStep] = useState(0);
  const [file, setFile] = useState<File | null>(null);
  const [keyUploaded, setKeyUploaded] = useState(false);
  const [decryptionSuccess, setDecryptionSuccess] = useState<boolean | null>(
    null
  );
  const [currentUser, setCurrentUser] = useState({
    name: "",
    attributes: [] as string[],
  });

  const handleFileUpload = (uploadedFile: File) => {
    setFile(uploadedFile);
    setStep(1);
  };

  const handleKeyUpload = (userData: {
    name: string;
    attributes: string[];
  }) => {
    setCurrentUser(userData);
    setKeyUploaded(true);

    // Simulate decryption attempt
    // In a real app, this would check if the user's attributes satisfy the policy
    const simulateDecryption = () => {
      // For demo purposes, we'll randomly succeed or fail
      const success = Math.random() > 0.5;
      setDecryptionSuccess(success);
    };

    setTimeout(simulateDecryption, 1500);
  };

  const tryAsAnotherUser = () => {
    setKeyUploaded(false);
    setDecryptionSuccess(null);
  };

  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <Link
            to="/dashboard"
            className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Home
          </Link>
        </div>

        <h1 className="text-3xl font-bold tracking-tight mb-8 text-center">
          Decrypt a File
        </h1>

        <Card>
          <CardHeader>
            <CardTitle>
              {step === 0
                ? "Upload Encrypted File"
                : "Provide Your Private Key"}
            </CardTitle>
            <CardDescription>
              {step === 0
                ? "Select an encrypted file to decrypt"
                : "Upload your private key to attempt decryption"}
            </CardDescription>
          </CardHeader>

          <CardContent>
            {step === 0 ? (
              <FileUpload onFileUpload={handleFileUpload} />
            ) : (
              <>
                {!keyUploaded ? (
                  <KeyUpload onKeyUpload={handleKeyUpload} />
                ) : (
                  <DecryptionResult
                    success={decryptionSuccess}
                    fileName={file?.name || "file"}
                    userData={currentUser}
                    onTryAnother={tryAsAnotherUser}
                  />
                )}
              </>
            )}
          </CardContent>

          <CardFooter className="flex justify-between">
            {step > 0 && !keyUploaded && (
              <Button variant="outline" onClick={() => setStep(0)}>
                Back
              </Button>
            )}
            {step === 0 && <div />}
          </CardFooter>
        </Card>
      </div>
    </div>
  );
}
