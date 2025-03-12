import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Key, Lock, FileText, Shield, AlertTriangle } from "lucide-react"
import { Link } from "react-router"

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Welcome to your cryptographic access control dashboard.</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">Total Keys</CardTitle>
            <Key className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">12</div>
            <p className="text-xs text-muted-foreground">+2 from last week</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">Access Policies</CardTitle>
            <Lock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">8</div>
            <p className="text-xs text-muted-foreground">+1 from last week</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">Encrypted Files</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">24</div>
            <p className="text-xs text-muted-foreground">+5 from last week</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">Security Status</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">Good</div>
            <p className="text-xs text-muted-foreground">All systems operational</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Your recent cryptographic operations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center gap-4">
                <div className="rounded-full bg-primary/10 p-2">
                  <Key className="h-4 w-4 text-primary" />
                </div>
                <div className="flex-1 space-y-1">
                  <p className="text-sm font-medium">Generated new key pair</p>
                  <p className="text-xs text-muted-foreground">2 hours ago</p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="rounded-full bg-primary/10 p-2">
                  <Lock className="h-4 w-4 text-primary" />
                </div>
                <div className="flex-1 space-y-1">
                  <p className="text-sm font-medium">Created access policy</p>
                  <p className="text-xs text-muted-foreground">Yesterday</p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="rounded-full bg-primary/10 p-2">
                  <FileText className="h-4 w-4 text-primary" />
                </div>
                <div className="flex-1 space-y-1">
                  <p className="text-sm font-medium">Encrypted document</p>
                  <p className="text-xs text-muted-foreground">2 days ago</p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="rounded-full bg-primary/10 p-2">
                  <FileText className="h-4 w-4 text-primary" />
                </div>
                <div className="flex-1 space-y-1">
                  <p className="text-sm font-medium">Decrypted document</p>
                  <p className="text-xs text-muted-foreground">3 days ago</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className="col-span-3">
          <CardHeader>
            <CardTitle>Security Recommendations</CardTitle>
            <CardDescription>Improve your cryptographic security</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-start gap-4">
                <AlertTriangle className="mt-0.5 h-5 w-5 text-amber-500" />
                <div className="space-y-1">
                  <p className="text-sm font-medium">Update master key</p>
                  <p className="text-xs text-muted-foreground">
                    Your master key is 90 days old. Consider updating it for enhanced security.
                  </p>
                  <Button size="sm" variant="outline" className="mt-2">
                    Update Key
                  </Button>
                </div>
              </div>
              <div className="flex items-start gap-4">
                <AlertTriangle className="mt-0.5 h-5 w-5 text-amber-500" />
                <div className="space-y-1">
                  <p className="text-sm font-medium">Review access policies</p>
                  <p className="text-xs text-muted-foreground">
                    You have 2 policies that haven't been reviewed in the last 6 months.
                  </p>
                  <Button size="sm" variant="outline" className="mt-2">
                    Review Policies
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div>
        <Tabs defaultValue="quick-actions">
          <TabsList>
            <TabsTrigger value="quick-actions">Quick Actions</TabsTrigger>
            <TabsTrigger value="storage">Storage</TabsTrigger>
          </TabsList>
          <TabsContent value="quick-actions" className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              <Button asChild className="h-24 flex-col gap-2">
                <Link to="/dashboard/keys/generate">
                  <Key className="h-5 w-5" />
                  <span>Generate New Keys</span>
                </Link>
              </Button>
              <Button asChild className="h-24 flex-col gap-2">
                <Link to="/dashboard/policies/create">
                  <Lock className="h-5 w-5" />
                  <span>Create Access Policy</span>
                </Link>
              </Button>
              <Button asChild className="h-24 flex-col gap-2">
                <Link to="/dashboard/files/encrypt">
                  <FileText className="h-5 w-5" />
                  <span>Encrypt File</span>
                </Link>
              </Button>
              <Button asChild className="h-24 flex-col gap-2">
                <Link to="/dashboard/files/decrypt">
                  <FileText className="h-5 w-5" />
                  <span>Decrypt File</span>
                </Link>
              </Button>
            </div>
          </TabsContent>
          <TabsContent value="storage">
            <Card>
              <CardHeader>
                <CardTitle>Storage Usage</CardTitle>
                <CardDescription>Your encrypted file storage usage</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <div>Used Space</div>
                      <div className="font-medium">1.25 GB of 5 GB</div>
                    </div>
                    <Progress value={25} />
                  </div>
                  <div className="pt-4">
                    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                      <div className="space-y-2">
                        <div className="text-sm font-medium">Documents</div>
                        <div className="text-2xl font-bold">0.8 GB</div>
                        <Progress value={64} className="h-2" />
                      </div>
                      <div className="space-y-2">
                        <div className="text-sm font-medium">Images</div>
                        <div className="text-2xl font-bold">0.3 GB</div>
                        <Progress value={24} className="h-2" />
                      </div>
                      <div className="space-y-2">
                        <div className="text-sm font-medium">Other</div>
                        <div className="text-2xl font-bold">0.15 GB</div>
                        <Progress value={12} className="h-2" />
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}

