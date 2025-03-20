import { useState } from "react";
import { Button } from "@/components/ui/button";
import { FileUpload } from "@/components/file-upload";
import { KeyUpload } from "@/components/key-upload";
import { DecryptionResult } from "@/components/decryption-result";
import { toast } from "sonner";

export default function DecryptPage() {
  const [file, setFile] = useState<File | null>(null);
  const [key, setKey] = useState<File | null>(null);
  const [isDecrypting, setIsDecrypting] = useState(false);
  const [decryptionSuccess, setDecryptionSuccess] = useState<boolean | null>(
    null
  );
  const [decryptedFileUrl, setDecryptedFileUrl] = useState<string | null>(null);
  const [decryptedFileName, setDecryptedFileName] = useState<string>("");

  const handleDecrypt = async () => {
    if (!file || !key) {
      toast("Missing files", {
        description:
          "Please select both an encrypted file and your private key",
      });
      return;
    }

    setIsDecrypting(true);
    setDecryptionSuccess(null);

    const formData = new FormData();
    formData.append("file", file);
    formData.append("key", key);

    try {
      const response = await fetch("http://localhost:8080/api/cpabe/decrypt", {
        method: "POST",
        body: formData,
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
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
        setDecryptedFileUrl(url);
        setDecryptedFileName(filename);
        setDecryptionSuccess(true);
      } else {
        const errorText = await response.text();
        console.error("Decryption failed:", errorText);
        setDecryptionSuccess(false);
      }
    } catch (error) {
      console.error("Error decrypting file:", error);
      toast("Failed to decrypt", {
        description:
          error instanceof Error && error.message === "Policy not satisfied"
            ? "Your private key does not satisfy the access policy"
            : "There was an error decrypting your file",
      });
    } finally {
      setIsDecrypting(false);
    }
  };

  const handleDownload = () => {
    if (decryptedFileUrl && decryptedFileName) {
      const a = document.createElement("a");
      a.href = decryptedFileUrl;
      a.download = decryptedFileName;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(decryptedFileUrl);
    }
  };

  const handleReset = () => {
    setFile(null);
    setKey(null);
    setDecryptionSuccess(null);
  };

  if (decryptionSuccess !== null) {
    return (
      <DecryptionResult
        success={decryptionSuccess}
        decryptedFileUrl={decryptedFileUrl || undefined}
        originalFileName={file?.name.replace(/\.cpabe$/, "") || ""}
        handleDownload={handleDownload}
        onReset={handleReset}
      />
    );
  }

  return (
    <div className="space-y-4 max-w-2xl w-full mx-auto p-6">
      <div>
        <h1 className="text-3xl font-bold">Decrypt File</h1>
        <p className="text-muted-foreground py-1.5">
          Upload an encrypted file and your private key to decrypt it
        </p>
      </div>

      <FileUpload
        file={file}
        onFileSelect={setFile}
        onFileUpload={setFile}
        disabled={isDecrypting}
        forDecryption={true}
      />

      <KeyUpload
        file={key}
        onFileSelect={setKey}
        onKeyUpload={setKey}
        disabled={isDecrypting}
      />

      <Button
        onClick={handleDecrypt}
        disabled={!file || !key || isDecrypting}
        className="w-full"
      >
        {isDecrypting ? "Decrypting..." : "Decrypt File"}
      </Button>
    </div>
  );
}
