import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout } from './components/Layout';
import { Login } from './pages/Login';
const InstitutionOverview = lazy(() => import('./pages/institution/Overview').then((m) => ({ default: m.InstitutionOverview })));
const UploadStudents = lazy(() => import('./pages/institution/UploadStudents').then((m) => ({ default: m.UploadStudents })));
const ValidationIssues = lazy(() => import('./pages/institution/ValidationIssues').then((m) => ({ default: m.ValidationIssues })));
const StudentsDirectory = lazy(() => import('./pages/institution/StudentsDirectory').then((m) => ({ default: m.StudentsDirectory })));
const StudentForm = lazy(() => import('./pages/institution/StudentForm').then((m) => ({ default: m.StudentForm })));
const RegulatorDashboard = lazy(() => import('./pages/regulator/Dashboard').then((m) => ({ default: m.RegulatorDashboard })));
const AnomalyCenter = lazy(() => import('./pages/regulator/AnomalyCenter').then((m) => ({ default: m.AnomalyCenter })));
const AlertsFeed = lazy(() => import('./pages/regulator/AlertsFeed').then((m) => ({ default: m.AlertsFeed })));
const Reports = lazy(() => import('./pages/regulator/Reports').then((m) => ({ default: m.Reports })));
const AuditLogs = lazy(() => import('./pages/regulator/AuditLogs').then((m) => ({ default: m.AuditLogs })));
const VerifierPage = lazy(() => import('./pages/verifier/VerifierPage').then((m) => ({ default: m.VerifierPage })));
const VerifyDetail = lazy(() => import('./pages/verifier/VerifyDetail').then((m) => ({ default: m.VerifyDetail })));
const HelpGuide = lazy(() => import('./pages/HelpGuide').then((m) => ({ default: m.HelpGuide })));
const DemoPlayer = lazy(() => import('./pages/DemoPlayer').then((m) => ({ default: m.DemoPlayer })));
const VerificationQueue = lazy(() => import('./pages/VerificationQueue').then((m) => ({ default: m.VerificationQueue })));
const NotificationCenter = lazy(() => import('./pages/NotificationCenter').then((m) => ({ default: m.NotificationCenter })));
const StudentPortal = lazy(() => import('./pages/student/StudentPortal').then((m) => ({ default: m.StudentPortal })));

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
    if (user.role === 'ROLE_SUPER_ADMIN') return <Navigate to="/regulator/dashboard" replace />;
    if (user.role === 'ROLE_STUDENT') return <Navigate to="/student/portal" replace />;
    if (user.role === 'ROLE_VERIFICATION_OFFICER') return <Navigate to="/verification/requests" replace />;
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
  if (user.role === 'ROLE_AICTE_REGULATOR' || user.role === 'ROLE_SUPER_ADMIN') {
    return <Navigate to="/regulator/dashboard" replace />;
  }
  if (user.role === 'ROLE_INSTITUTION_ADMIN') {
    return <Navigate to="/institution/overview" replace />;
  }
  if (user.role === 'ROLE_STUDENT') return <Navigate to="/student/portal" replace />;
  if (user.role === 'ROLE_VERIFICATION_OFFICER') return <Navigate to="/verification/requests" replace />;
  return <Navigate to="/verify" replace />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Suspense fallback={<div className="min-h-[50vh] grid place-items-center text-sm text-slate-500" role="status">Loading workspace…</div>}>
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

            <Route path="/verification/requests" element={<ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN','ROLE_AUTHORIZED_VERIFIER','ROLE_VERIFICATION_OFFICER','ROLE_AICTE_REGULATOR','ROLE_SUPER_ADMIN']}><VerificationQueue /></ProtectedRoute>} />
            <Route path="/notifications" element={<ProtectedRoute allowedRoles={['ROLE_INSTITUTION_ADMIN','ROLE_AUTHORIZED_VERIFIER','ROLE_VERIFICATION_OFFICER','ROLE_AICTE_REGULATOR','ROLE_SUPER_ADMIN','ROLE_STUDENT']}><NotificationCenter /></ProtectedRoute>} />
            <Route path="/student/portal" element={<ProtectedRoute allowedRoles={['ROLE_STUDENT']}><StudentPortal /></ProtectedRoute>} />

            {/* General Help Guide */}
            <Route path="/help" element={<HelpGuide />} />
          </Route>

          {/* Catch-all redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
