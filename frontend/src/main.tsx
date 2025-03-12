import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { BrowserRouter, Routes, Route } from "react-router";
import CreatePolicyPage from './Pages/CreatePolicyPage.tsx';
import DashboardPage from './Pages/DashboardPage.tsx';
import FileManagementPage from './Pages/FileManagementPage.tsx';
import EncryptPage from './Pages/EncryptPage.tsx';
import KeyManagementPage from './Pages/KeyManagementPage.tsx';
import PoliciesPage from './Pages/PoliciesPage.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/dashboard/files" element={<FileManagementPage />} />
          <Route path="/dashboard/files/encrypt" element={<EncryptPage />} />
          <Route path="/dashboard/keys" element={<KeyManagementPage />} />
          <Route path="/dashboard/policies" element={<PoliciesPage />} />
          <Route path='/dashboard/policies/create' element={<CreatePolicyPage/>}/>
        </Routes>
    </BrowserRouter>
  </StrictMode>,
)
