import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { CheckCircle, Download, ShieldAlert, FileIcon } from "lucide-react";

interface DecryptionResultProps {
  success?: boolean | null;
  decryptedFileUrl?: String;
  originalFileName?: string;
  onReset?: () => void;
  handleDownload?: () => void;
}

export function DecryptionResult({
  success,
  decryptedFileUrl,
  originalFileName,
  onReset,
  handleDownload,
}: DecryptionResultProps) {
  if (success === null) {
    console.log("Decrypted File", decryptedFileUrl);

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

          {decryptedFileUrl && (
            <>
              <div className="w-full max-w-sm p-4 border rounded-md bg-muted/50">
                <div className="flex items-center space-x-3 mb-3">
                  <FileIcon className="h-5 w-5 text-primary" />
                  <div className="text-sm">
                    <div className="font-medium">{originalFileName}</div>
                  </div>
                </div>
                <div className="text-sm">
                  <div>
                    <span className="font-medium">Decrypted on:</span>{" "}
                    {new Date().toLocaleString()}
                  </div>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 w-full max-w-sm">
                <Button className="flex-1" onClick={handleDownload}>
                  <Download className="mr-2 h-4 w-4" />
                  Download Decrypted File
                </Button>

                <Button variant="outline" className="flex-1" onClick={onReset}>
                  Decrypt Another File
                </Button>
              </div>
            </>
          )}
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

          <Button
            variant="outline"
            onClick={onReset}
            className="w-full max-w-sm"
          >
            Try Different Key
          </Button>
        </div>
      )}
    </div>
  );
}
