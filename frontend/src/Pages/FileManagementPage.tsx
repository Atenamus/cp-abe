import { useState } from "react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Download, Trash, Upload, Search, FileText } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Link } from "react-router"

export default function FileManagementPage() {
  const [searchQuery, setSearchQuery] = useState("")

  const encryptedFiles = [
    { id: "1", name: "Project_Proposal", size: "2.4 MB", date: "2025-01-15", type: "PDF" },
    { id: "2", name: "Marketing_Strategy", size: "1.8 MB", date: "2025-02-20", type: "DOCX" },
  ]
  
  const decryptedFiles = [
    { id: "3", name: "Financial_Report_Q1", size: "3.2 MB", date: "2025-03-05", type: "XLSX" },
  ]

  const handleDownload = (fileId : string) => {
    toast("Download Started", { description: `Downloading file ${fileId}` })
  }

  const handleDelete = (fileId : string) => {
    toast("File Deleted", { description: `File ${fileId} has been removed` })
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">File Management</h1>
        <Button asChild>
          <Link to="/dashboard/files/encrypt">
            <Upload className="mr-2 h-4 w-4" /> Encrypt File
          </Link>
        </Button>
      </div>

      <div className="flex items-center space-x-2">
        <Search className="h-5 w-5 text-gray-600" />
        <Input
          placeholder="Search files..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="max-w-md"
        />
      </div>

      <Tabs defaultValue="encrypted">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="encrypted">Encrypted</TabsTrigger>
          <TabsTrigger value="decrypted">Decrypted</TabsTrigger>
        </TabsList>
        
        {["encrypted", "decrypted"].map((type) => {
          const files = type === "encrypted" ? encryptedFiles : decryptedFiles
          return (
            <TabsContent key={type} value={type} className="space-y-4">
              {files.length === 0 ? (
                <Card className="w-full max-w-lg mx-auto">
                  <CardContent className="py-6 text-center text-gray-600">
                    <FileText className="h-10 w-10 mb-4 mx-auto" />
                    No {type} files found
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {files.map((file) => (
                    <Card key={file.id} className="w-full max-w-md mx-auto">
                      <CardHeader>
                        <CardTitle>{file.name}</CardTitle>
                        <CardDescription>{file.size} - {file.date}</CardDescription>
                      </CardHeader>
                      <CardFooter className="flex justify-between">
                        <Button variant="outline" size="sm" onClick={() => handleDownload(file.id)}>
                          <Download className="mr-2 h-4 w-4" /> Download
                        </Button>
                        <Button variant="destructive" size="sm" onClick={() => handleDelete(file.id)}>
                          <Trash className="mr-2 h-4 w-4" /> Delete
                        </Button>
                      </CardFooter>
                    </Card>
                  ))}
                </div>
              )}
            </TabsContent>
          )
        })}
      </Tabs>
    </div>
  )
}
