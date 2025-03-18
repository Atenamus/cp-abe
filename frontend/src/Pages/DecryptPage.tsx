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

export default function DecryptPage() {
  const [step, setStep] = useState(0);
  const [file, setFile] = useState<File | null>(null);
  const [keyUploaded, setKeyUploaded] = useState(false);
  const [decryptionSuccess, setDecryptionSuccess] = useState<boolean | null>(
    null
  );

  const handleFileUpload = (uploadedFile: File) => {
    setFile(uploadedFile);
    setStep(1);
  };

  const handleKeyUpload = async (uploadedKey: File) => {
    setKeyUploaded(true);

    if (!file) {
      console.error("No encrypted file selected.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("key", uploadedKey);

    try {
      const response = await fetch("http://localhost:8080/api/cpabe/decrypt", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);

        // Extract original filename from the content disposition header if available
        const contentDisposition = response.headers.get("content-disposition");
        let filename = "decrypted-file";

        if (contentDisposition) {
          const filenameMatch =
            contentDisposition.match(/filename="?([^"]+)"?/);
          if (filenameMatch && filenameMatch[1]) {
            filename = filenameMatch[1];
          }
        } else {
          if (file.name.endsWith(".cpabe")) {
            filename = file.name.slice(0, -6);
          } else {
            filename = file.name;
          }
        }

        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        setDecryptionSuccess(true);
      } else {
        const errorText = await response.text();
        console.error("Decryption failed:", errorText);
        setDecryptionSuccess(false);
      }
    } catch (error) {
      console.error("Decryption failed:", error);
      setDecryptionSuccess(false);
    }
  };

  const decryptAnotherFile = () => {
    setFile(null);
    setStep(0);
    setKeyUploaded(false);
    setDecryptionSuccess(null);
  };

  return (
    <div className="max-w-7xl space-y-6 w-full mx-auto p-6">
      <h1 className="text-3xl font-bold tracking-tight mb-8">Decrypt a File</h1>

      <Card>
        <CardHeader>
          <CardTitle>
            {step === 0
              ? "Upload Encrypted File"
              : keyUploaded && decryptionSuccess !== null
              ? "Decryption Result"
              : "Provide Your Private Key"}
          </CardTitle>
          <CardDescription>
            {step === 0
              ? "Select an encrypted file to decrypt"
              : keyUploaded && decryptionSuccess !== null
              ? "Here is the result of your decryption attempt"
              : "Upload your private key to attempt decryption"}
          </CardDescription>
        </CardHeader>

        <CardContent>
          {step === 0 ? (
            <FileUpload onFileUpload={handleFileUpload} forDecryption={true} />
          ) : (
            <>
              {!keyUploaded ? (
                <KeyUpload onKeyUpload={handleKeyUpload} />
              ) : (
                <DecryptionResult
                  success={decryptionSuccess}
                  fileName={file?.name || "file"}
                  // decryptedData={decryptedData}
                  onDecryptAnotherFile={decryptAnotherFile}
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
  );
}
