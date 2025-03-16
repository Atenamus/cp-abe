import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertTriangle, Download, ShieldCheck } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

interface KeyDownloadProps {
  userData: {
    fullName: string;
    email: string;
    attributes: string[];
  };
  onContinue: () => void;
}

export function KeyDownload({ userData, onContinue }: KeyDownloadProps) {
  const [downloaded, setDownloaded] = useState(false);

  // In a real app, this would be a real key generated on the server
  const generateMockPrivateKey = () => {
    const header = `-----BEGIN PRIVATE KEY-----\n`;
    const footer = `\n-----END PRIVATE KEY-----`;
    const randomBytes = Array.from({ length: 10 }, () =>
      Math.random().toString(36).substring(2, 15)
    ).join("");

    const attributeSection = `\nAttributes: ${userData.attributes.join(
      ", "
    )}\n`;
    const userSection = `User: ${userData.fullName} (${userData.email})\n`;
    const dateSection = `Generated: ${new Date().toISOString()}\n`;

    return (
      header +
      randomBytes +
      attributeSection +
      userSection +
      dateSection +
      footer
    );
  };

  const handleDownload = () => {
    const privateKey = generateMockPrivateKey();
    const blob = new Blob([privateKey], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `private_key_${userData.email.split("@")[0]}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    setDownloaded(true);
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-2">
        <div className="flex justify-center">
          <div className="rounded-full bg-primary/10 p-3">
            <ShieldCheck className="h-6 w-6 text-primary" />
          </div>
        </div>
        <h3 className="text-lg font-medium">Your Private Key is Ready</h3>
        <p className="text-sm text-muted-foreground max-w-md mx-auto">
          We've generated a private key based on your attributes. You'll need
          this key to decrypt files that match your attributes.
        </p>
      </div>

      <Card>
        <CardContent className="p-4">
          <div className="space-y-2">
            <div className="text-sm font-medium">Key Details:</div>
            <div className="text-sm">
              <div>
                <span className="font-medium">User:</span> {userData.fullName}
              </div>
              <div>
                <span className="font-medium">Email:</span> {userData.email}
              </div>
              <div>
                <span className="font-medium">Attributes:</span>{" "}
                {userData.attributes.join(", ") || "None selected"}
              </div>
              <div>
                <span className="font-medium">Generated:</span>{" "}
                {new Date().toLocaleString()}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Alert variant="warning">
        <AlertTriangle className="h-4 w-4" />
        <AlertTitle>Important</AlertTitle>
        <AlertDescription>
          Store this key securely. If lost, you'll need to generate a new one.
          Never share your private key with others.
        </AlertDescription>
      </Alert>

      <div className="flex flex-col items-center gap-4">
        <Button onClick={handleDownload} className="w-full sm:w-auto">
          <Download className="mr-2 h-4 w-4" />
          Download Private Key
        </Button>

        <Button
          variant="outline"
          onClick={onContinue}
          disabled={!downloaded}
          className="w-full sm:w-auto"
        >
          Continue to Next Step
        </Button>

        {!downloaded && (
          <p className="text-sm text-muted-foreground">
            Please download your key before continuing
          </p>
        )}
      </div>
    </div>
  );
}
