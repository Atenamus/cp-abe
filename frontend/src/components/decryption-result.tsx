import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { CheckCircle, Download, ShieldAlert, UserCircle2 } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

interface DecryptionResultProps {
  success: boolean | null;
  fileName: string;
  userData: {
    name: string;
    attributes: string[];
  };
  onTryAnother: () => void;
}

export function DecryptionResult({
  success,
  fileName,
  userData,
  onTryAnother,
}: DecryptionResultProps) {
  if (success === null) {
    return (
      <div className="flex flex-col items-center justify-center py-6 space-y-4">
        <div className="h-12 w-12 rounded-full border-4 border-primary border-t-transparent animate-spin" />
        <p>Attempting to decrypt file...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {success ? (
        <div className="flex flex-col items-center justify-center py-4 space-y-4">
          <div className="rounded-full bg-green-100 p-3">
            <CheckCircle className="h-6 w-6 text-green-600" />
          </div>

          <div className="space-y-2 text-center">
            <h3 className="text-xl font-semibold text-green-600">
              Decryption Successful!
            </h3>
            <p className="text-muted-foreground">
              Your attributes match the file's access policy. You can now access
              the file.
            </p>
          </div>

          <Button className="mt-4">
            <Download className="mr-2 h-4 w-4" />
            Download Decrypted File
          </Button>
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-4 space-y-4">
          <div className="rounded-full bg-destructive/10 p-3">
            <ShieldAlert className="h-6 w-6 text-destructive" />
          </div>

          <div className="space-y-2 text-center">
            <h3 className="text-xl font-semibold text-destructive">
              Access Denied
            </h3>
            <p className="text-muted-foreground">
              Your attributes do not satisfy the file's access policy. You
              cannot decrypt this file.
            </p>
          </div>

          <Alert variant="destructive" className="mt-4">
            <AlertTitle>Permission Error</AlertTitle>
            <AlertDescription>
              The file requires attributes that are not present in your private
              key.
            </AlertDescription>
          </Alert>
        </div>
      )}

      <Card>
        <CardContent className="p-4">
          <div className="space-y-4">
            <div className="flex items-center">
              <UserCircle2 className="h-5 w-5 mr-2" />
              <span className="font-medium">Current User</span>
            </div>

            <div className="text-sm space-y-1">
              <div>
                <span className="font-medium">Name:</span> {userData.name}
              </div>
              <div>
                <span className="font-medium">Attributes:</span>{" "}
                {userData.attributes.map((attr, i) => (
                  <span
                    key={i}
                    className="inline-block bg-muted px-2 py-1 rounded text-xs mr-1 mb-1"
                  >
                    {attr}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Button variant="outline" onClick={onTryAnother} className="w-full">
        Try as Another User
      </Button>
    </div>
  );
}
