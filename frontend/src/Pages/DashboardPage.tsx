import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Key,
  FileText,
  Upload,
  Download,
  AlertTriangle,
  LockKeyhole,
} from "lucide-react";
import { ApiClient, UserActivity } from "@/lib/api-client";
import { EncryptedFilesTable } from "@/components/encrypted-files-table";
import { toast } from "sonner";

interface Policy {
  id: string;
  policyName: string;
  policyDescription: string;
  policyExpression: string;
}

interface PolicyResponse {
  body: Policy[];
}

export default function DashboardPage() {
  const [stats, setStats] = useState({
    totalFiles: 0,
    totalPolicies: 0,
  });
  const [activities, setActivities] = useState<UserActivity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchDashboardData() {
      try {
        const [filesResponse, policiesResponse, activitiesResponse] =
          await Promise.all([
            ApiClient.listEncryptedFiles(),
            ApiClient.listPolicies<PolicyResponse>(),
            ApiClient.getRecentActivities(),
          ]);

        const files = filesResponse.data || [];
        const policies = policiesResponse.data?.body || [];
        const recentActivities = activitiesResponse.data || [];

        setStats({
          totalFiles: files.length,
          totalPolicies: policies.length,
        });
        setActivities(recentActivities);
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
        toast.error("Failed to load dashboard data");
      } finally {
        setLoading(false);
      }
    }

    fetchDashboardData();
  }, []);

  const getActivityIcon = (type: UserActivity["type"]) => {
    switch (type) {
      case "file_encrypted":
        return <Upload className="h-4 w-4 text-green-500" />;
      case "file_decrypted":
        return <Download className="h-4 w-4 text-blue-500" />;
      case "policy_created":
      case "policy_updated":
        return <LockKeyhole className="h-4 w-4 text-purple-500" />;
      case "key_generated":
        return <Key className="h-4 w-4 text-orange-500" />;
      default:
        return <FileText className="h-4 w-4 text-gray-500" />;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 mx-auto w-full max-w-7xl py-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Manage your encrypted files securely with CP-ABE.
        </p>
      </div>

      <div className="grid gap-6 grid-cols-8 grid-rows-3">
        <Card className="col-span-2 row-span-1">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">
              Encrypted Files
            </CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalFiles}</div>
            <p className="text-xs text-muted-foreground">
              Total encrypted documents
            </p>
          </CardContent>
        </Card>
        <Card className="col-span-2 row-span-1 col-start-3">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">
              Active Policies
            </CardTitle>
            <LockKeyhole className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalPolicies}</div>
            <p className="text-xs text-muted-foreground">
              Access control policies created
            </p>
          </CardContent>
        </Card>

        <Card className="col-span-4 row-span-2 col-start-1 row-start-">
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common operations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <Button
                asChild
                className="w-full justify-start"
                variant="outline"
              >
                <Link to="/dashboard/files/encrypt">
                  <Upload className="mr-2 h-4 w-4" />
                  Upload & Encrypt File
                </Link>
              </Button>
              <Button
                asChild
                className="w-full justify-start"
                variant="outline"
              >
                <Link to="/dashboard/policies/create">
                  <LockKeyhole className="mr-2 h-4 w-4" />
                  Create New Policy
                </Link>
              </Button>
              <Button
                asChild
                className="w-full justify-start"
                variant="outline"
              >
                <Link to="/dashboard/keys/generate">
                  <Key className="mr-2 h-4 w-4" />
                  Generate New Key
                </Link>
              </Button>
            </div>

            <div className="mt-6 rounded-lg border p-4 bg-muted/50">
              <div className="flex items-center gap-2 text-sm">
                <AlertTriangle className="h-4 w-4 text-yellow-600" />
                <p className="text-muted-foreground">
                  Your private keys and decrypted files are never stored on our
                  servers.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card className="col-span-4 row-span-3 col-start-5 row-start-1">
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Latest actions in your workspace</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {activities.length === 0 ? (
                <div className="text-center py-6 text-muted-foreground">
                  No recent activity. Start by encrypting a file or creating a
                  policy.
                </div>
              ) : (
                activities.map((activity) => (
                  <div key={activity.id} className="flex items-center gap-4">
                    <div className="rounded-full bg-primary/10 p-2">
                      {getActivityIcon(activity.type)}
                    </div>
                    <div className="flex-1 space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {activity.resourceName}
                      </p>
                      <div className="flex items-center text-xs text-muted-foreground">
                        <span>
                          {new Date(activity.timestamp).toLocaleString()}
                        </span>
                        {activity.details && (
                          <>
                            <span className="mx-2">•</span>
                            <span>{activity.details}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Encrypted Files</CardTitle>
          <CardDescription>Manage your encrypted documents</CardDescription>
        </CardHeader>
        <CardContent>
          <EncryptedFilesTable />
        </CardContent>
      </Card>
    </div>
  );
}
