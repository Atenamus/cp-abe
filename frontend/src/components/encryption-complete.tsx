import { Button } from "@/components/ui/button";
import { CheckCircle, Download } from "lucide-react";
import { Link } from "react-router";

interface EncryptionCompleteProps {
  fileName: string;
}

export function EncryptionComplete({ fileName }: EncryptionCompleteProps) {
  return (
    <div className="flex flex-col items-center justify-center py-6 space-y-6">
      <div className="rounded-full bg-primary/10 p-4">
        <CheckCircle className="h-8 w-8 text-primary" />
      </div>

      <div className="space-y-2 text-center">
        <h3 className="text-xl font-semibold">Encryption Complete!</h3>
        <p className="text-muted-foreground">
          Your file has been successfully encrypted with the access policy you
          defined.
        </p>
      </div>

      <div className="w-full max-w-sm p-4 border rounded-md bg-muted/50">
        <div className="text-sm">
          <div>
            <span className="font-medium">File:</span> {fileName}
          </div>
          <div>
            <span className="font-medium">Size:</span> 256 KB (encrypted)
          </div>
          <div>
            <span className="font-medium">Encrypted on:</span>{" "}
            {new Date().toLocaleString()}
          </div>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-4 w-full max-w-sm">
        <Button className="flex-1">
          <Download className="mr-2 h-4 w-4" />
          Download Encrypted File
        </Button>

        <Link to="/onboarding/decrypt" className="flex-1">
          <Button variant="outline" className="w-full">
            Try Decrypting
          </Button>
        </Link>
      </div>
    </div>
  );
}
