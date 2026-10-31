import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout } from './components/Layout';
import { Login } from './pages/Login';
import { InstitutionOverview } from './pages/institution/Overview';
import { UploadStudents } from './pages/institution/UploadStudents';
import { ValidationIssues } from './pages/institution/ValidationIssues';
import { StudentsDirectory } from './pages/institution/StudentsDirectory';
import { StudentForm } from './pages/institution/StudentForm';
import { RegulatorDashboard } from './pages/regulator/Dashboard';
import { AnomalyCenter } from './pages/regulator/AnomalyCenter';
import { AlertsFeed } from './pages/regulator/AlertsFeed';
import { Reports } from './pages/regulator/Reports';
import { AuditLogs } from './pages/regulator/AuditLogs';
import { VerifierPage } from './pages/verifier/VerifierPage';
import { VerifyDetail } from './pages/verifier/VerifyDetail';
import { HelpGuide } from './pages/HelpGuide';
import { DemoPlayer } from './pages/DemoPlayer';
import { VerificationQueue } from './pages/VerificationQueue';
import { NotificationCenter } from './pages/NotificationCenter';

const ProtectedRoute: React.FC<{ children: React.ReactNode; allowedRoles?: string[] }> = ({
  children,
  allowedRoles,
}) => {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // If institution admin tries regulator page or vice versa, redirect gracefully to their home
    if (user.role === 'ROLE_AICTE_REGULATOR') {
      return <Navigate to="/regulator/dashboard" replace />;
    }
    if (user.role === 'ROLE_INSTITUTION_ADMIN') {
      return <Navigate to="/institution/overview" replace />;
    }
    return <Navigate to="/verify" replace />;
  }

  return <>{children}</>;
};

const RootRedirect: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }
  if (user.role === 'ROLE_AICTE_REGULATOR') {
    return <Navigate to="/regulator/dashboard" replace />;
  }
  if (user.role === 'ROLE_INSTITUTION_ADMIN') {
    return <Navigate to="/institution/overview" replace />;
  }
  return <Navigate to="/verify" replace />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/demo" element={<DemoPlayer />} />
          <Route path="/verify" element={<VerifierPage />} />
          <Route path="/verify/:token" element={<VerifyDetail />} />

          {/* Protected Application Routes inside Master Layout */}
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<RootRedirect />} />

            {/* Institution Routes */}
            <Route
              path="/institution/overview"
              element={
                <ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN', 'ROLE_AICTE_REGULATOR']}>
                  <InstitutionOverview />
                </ProtectedRoute>
              }
            />
            <Route
              path="/institution/upload"
              element={
                <ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN', 'ROLE_AICTE_REGULATOR']}>
                  <UploadStudents />
                </ProtectedRoute>
              }
            />
            <Route
              path="/institution/issues"
              element={
                <ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN', 'ROLE_AICTE_REGULATOR']}>
                  <ValidationIssues />
                </ProtectedRoute>
              }
            />
            <Route
              path="/institution/students"
              element={
                <ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN', 'ROLE_AICTE_REGULATOR']}>
                  <StudentsDirectory />
                </ProtectedRoute>
              }
            />
            <Route
              path="/institution/students/new"
              element={
                <ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN', 'ROLE_AICTE_REGULATOR']}>
                  <StudentForm />
                </ProtectedRoute>
              }
            />

            {/* AICTE Regulator Routes */}
            <Route
              path="/regulator/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ROLE_AICTE_REGULATOR']}>
                  <RegulatorDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/regulator/anomalies"
              element={
                <ProtectedRoute allowedRoles={['ROLE_AICTE_REGULATOR']}>
                  <AnomalyCenter />
                </ProtectedRoute>
              }
            />
            <Route
              path="/regulator/alerts"
              element={
                <ProtectedRoute allowedRoles={['ROLE_AICTE_REGULATOR']}>
                  <AlertsFeed />
                </ProtectedRoute>
              }
            />
            <Route
              path="/regulator/reports"
              element={
                <ProtectedRoute allowedRoles={['ROLE_AICTE_REGULATOR']}>
                  <Reports />
                </ProtectedRoute>
              }
            />
            <Route
              path="/regulator/audit"
              element={
                <ProtectedRoute allowedRoles={['ROLE_AICTE_REGULATOR']}>
                  <AuditLogs />
                </ProtectedRoute>
              }
            />

            <Route path="/verification/requests" element={<ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN','ROLE_AUTHORIZED_VERIFIER','ROLE_AICTE_REGULATOR']}><VerificationQueue /></ProtectedRoute>} />
            <Route path="/notifications" element={<ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN','ROLE_AUTHORIZED_VERIFIER','ROLE_AICTE_REGULATOR']}><NotificationCenter /></ProtectedRoute>} />

            {/* General Help Guide */}
            <Route path="/help" element={<HelpGuide />} />
          </Route>

          {/* Catch-all redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
