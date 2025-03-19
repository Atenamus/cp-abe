import { Navigate } from "react-router-dom";
import { auth } from "@/lib/auth";
import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";

interface PrivateRouteProps {
  children: React.ReactNode;
}

export function PrivateRoute({ children }: PrivateRouteProps) {
  const [isValidating, setIsValidating] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const validateAuth = async () => {
      const authenticated = await auth.isAuthenticated();
      setIsAuthenticated(authenticated);
      setIsValidating(false);
    };
    validateAuth();
  }, []);

  if (isValidating) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/sign-in" replace />;
  }

  return <>{children}</>;
}
