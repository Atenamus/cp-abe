import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { BrowserRouter, Routes, Route, Outlet } from "react-router-dom";
import { PrivateRoute } from "./components/private-route";
import CreatePolicyPage from "./Pages/CreatePolicyPage.tsx";
import DashboardPage from "./Pages/DashboardPage.tsx";
import FileManagementPage from "./Pages/FileManagementPage.tsx";
import EncryptPage from "./Pages/EncryptPage.tsx";
import KeyManagementPage from "./Pages/KeyManagementPage.tsx";
import PoliciesPage from "./Pages/PoliciesPage.tsx";
import DashboardLayout from "./Pages/DashboardLayout.tsx";
import { ThemeProvider } from "./components/theme-provider.tsx";
import SignInPage from "./Pages/SignInPage.tsx";
import GetKeyPage from "./Pages/GetKeyPage.tsx";
import EncryptionCompletePage from "./Pages/EncryptionComplete.tsx";
import SetPolicyPage from "./Pages/SetPolicyPage.tsx";
import SignUpPage from "./Pages/SignUpPage.tsx";
import { Toaster } from "sonner";
import OnboardingDecryptPage from "./Pages/OnboardingDecryptPage.tsx";
import DecryptPage from "./Pages/DecryptPage.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider defaultTheme="light" storageKey="vite-ui-theme">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path="/sign-in" element={<SignInPage />} />
          <Route path="/onboarding" element={<Outlet />}>
            <Route path="sign-up" element={<SignUpPage />} />
            <Route path="get-key" element={<GetKeyPage />} />
            <Route path="set-policy" element={<SetPolicyPage />} />
            <Route
              path="encryption-complete"
              element={<EncryptionCompletePage />}
            />
            <Route path="decrypt" element={<OnboardingDecryptPage />} />
          </Route>
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <DashboardLayout />
              </PrivateRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="files" element={<FileManagementPage />} />
            <Route path="files/encrypt" element={<EncryptPage />} />
            <Route path="files/decrypt" element={<DecryptPage />} />
            <Route path="keys/generate" element={<KeyManagementPage />} />
            <Route path="policies" element={<PoliciesPage />} />
            <Route path="policies/create" element={<CreatePolicyPage />} />
            <Route
              path="policies/edit/:policyId"
              element={<CreatePolicyPage />}
            />
          </Route>
        </Routes>
      </BrowserRouter>
      <Toaster />
    </ThemeProvider>
  </StrictMode>
);
