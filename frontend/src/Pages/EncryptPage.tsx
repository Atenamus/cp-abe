import { useState, useRef } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Progress } from "@/components/ui/progress"
import { toast } from "sonner"
import { AlertCircle, File, Upload, X } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useNavigate } from "react-router"

export default function EncryptPage() {
  let navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [isEncrypting, setIsEncrypting] = useState(false)
  const [selectedPolicy, setSelectedPolicy] = useState("")
  const [uploadProgress, setUploadProgress] = useState(0)
  const [file, setFile] = useState<File | null>(null)
  const [dragActive, setDragActive] = useState(false)

  // Sample policies
  const policies = [
    { id: "pol-1", name: "Engineering Team Access" },
    { id: "pol-2", name: "Marketing Documents" },
    { id: "pol-3", name: "Financial Reports" },
  ]

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()

    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true)
    } else if (e.type === "dragleave") {
      setDragActive(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileChange(e.dataTransfer.files[0])
    }
  }

  const handleFileChange = (selectedFile: File) => {
    setFile(selectedFile)

    // Simulate upload progress
    setUploadProgress(0)
    const interval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval)
          return 100
        }
        return prev + 10
      })
    }, 200)
  }

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleFileChange(e.target.files[0])
    }
  }

  const handleRemoveFile = () => {
    setFile(null)
    setUploadProgress(0)
    if (fileInputRef.current) {
      fileInputRef.current.value = ""
    }
  }

  const handleEncryptFile = async () => {
    if (!file) {
      toast("No file selected", {
        description: "Please select a file to encrypt",
      })
      return
    }

    if (!selectedPolicy) {
      toast("No policy selected", {
        description: "Please select an access policy",
      })
      return
    }

    setIsEncrypting(true)
    try {
      // Simulate encryption process
      await new Promise((resolve) => setTimeout(resolve, 2000))

      toast("File encrypted", {
        description: "Your file has been encrypted successfully",
      })

      navigate("/dashboard/files")
    } catch (error) {
      toast("Encryption failed", {
        description: "There was an error encrypting your file",
      })
    } finally {
      setIsEncrypting(false)
    }
  }

  return (
    <div className="space-y-6 px-8 py-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight py-3">Encrypt File</h1>
        <p className="text-muted-foreground ">Encrypt files with attribute-based access policies</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>File Selection</CardTitle>
          <CardDescription>Select a file to encrypt with your chosen access policy</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div
            className={`border-2 border-dashed rounded-lg p-6 text-center ${
              dragActive ? "border-primary bg-primary/5" : "border-muted-foreground/25"
            }`}
            onDragEnter={handleDrag}
            onDragOver={handleDrag}
            onDragLeave={handleDrag}
            onDrop={handleDrop}
          >
            {!file ? (
              <div className="flex flex-col items-center justify-center space-y-4 py-4">
                <Upload className="h-10 w-10 text-muted-foreground" />
                <div className="space-y-2">
                  <p className="text-lg font-medium">Drag and drop your file here</p>
                  <p className="text-sm text-muted-foreground">or click to browse your files</p>
                </div>
                <Input
                  ref={fileInputRef}
                  type="file"
                  className="hidden"
                  onChange={handleFileInputChange}
                  accept="*/*"
                />
                <Button variant="outline" onClick={() => fileInputRef.current?.click()}>
                  Browse Files
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="rounded-md bg-primary/10 p-2">
                      <File className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium">{file.name}</p>
                      <p className="text-sm text-muted-foreground">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                    </div>
                  </div>
                  <Button variant="ghost" size="icon" onClick={handleRemoveFile}>
                    <X className="h-4 w-4" />
                    <span className="sr-only">Remove file</span>
                  </Button>
                </div>
                <Progress value={uploadProgress} className="h-2" />
                {uploadProgress === 100 && (
                  <p className="text-sm text-center text-muted-foreground">File ready for encryption</p>
                )}
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="policy">Access Policy</Label>
            <Select value={selectedPolicy} onValueChange={setSelectedPolicy}>
              <SelectTrigger id="policy">
                <SelectValue placeholder="Select an access policy" />
              </SelectTrigger>
              <SelectContent>
                {policies.map((policy) => (
                  <SelectItem key={policy.id} value={policy.id}>
                    {policy.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-sm text-muted-foreground">
              The selected policy will determine who can decrypt this file
            </p>
          </div>

          {selectedPolicy && (
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>Access Control Information</AlertTitle>
              <AlertDescription>
                <p className="mt-2">
                  This file will be encrypted with the{" "}
                  <strong>{policies.find((p) => p.id === selectedPolicy)?.name}</strong> policy. Only users with the
                  required attributes will be able to decrypt and access this file.
                </p>
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
        <CardFooter className="flex justify-between">
          <Button variant="outline" onClick={() => navigate("/dashboard/files")}>
            Cancel
          </Button>
          <Button
            onClick={handleEncryptFile}
            disabled={isEncrypting || !file || uploadProgress < 100 || !selectedPolicy}
          >
            {isEncrypting ? "Encrypting..." : "Encrypt File"}
          </Button>
        </CardFooter>
      </Card>
    </div>
  )
}

