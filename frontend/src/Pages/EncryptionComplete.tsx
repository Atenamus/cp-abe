import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { EncryptionComplete } from "@/components/encryption-complete";
import { ArrowLeft } from "lucide-react";
import { Link } from "react-router";

export default function EncryptionCompletePage() {
  // In a real app, this would come from state management or the server
  const fileName = "document.pdf";

  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <Link
            to="/onboarding/set-policy"
            className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Policy Selection
          </Link>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Encryption Complete</CardTitle>
            <CardDescription>
              Your file has been encrypted successfully
            </CardDescription>
          </CardHeader>

          <CardContent>
            <EncryptionComplete fileName={fileName} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
