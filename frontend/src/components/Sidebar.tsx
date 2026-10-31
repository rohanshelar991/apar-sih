import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Logo } from './Logo';
import {
  LayoutDashboard,
  Upload,
  AlertTriangle,
  Users,
  UserPlus,
  ShieldAlert,
  FileText,
  History,
  QrCode,
  HelpCircle,
  Bell,
  CheckCircle,
  LogOut,
  Building
} from 'lucide-react';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isRegulator = user?.role === 'ROLE_AICTE_REGULATOR';
  const isInstAdmin = user?.role === 'ROLE_INSTITUTION_ADMIN';
  const isSuperAdmin = user?.role === 'ROLE_SUPER_ADMIN';
  const isStudent = user?.role === 'ROLE_STUDENT';
  const isVerifier = user?.role === 'ROLE_AUTHORIZED_VERIFIER' || user?.role === 'ROLE_VERIFICATION_OFFICER';

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-3 min-h-11 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
      isActive
        ? 'bg-white/10 text-white border-l-2 border-gold font-semibold'
        : 'text-[#C4CDDC] hover:text-white hover:bg-white/5'
    }`;

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          onClick={onClose}
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-xs lg:hidden"
        />
      )}

      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 flex w-64 flex-col garuda-sidebar border-r border-navy bg-navy transition-transform duration-200 ease-in-out lg:static lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        } no-print`}
      >
        {/* Logo header */}
        <div className="flex h-24 items-center px-5 border-b border-white/10">
          <Logo size="sm" showSubtitle={false} />
        </div>

        {/* Navigation list */}
        <div className="flex-1 overflow-y-auto px-4 py-4 space-y-6">
          {/* Institution Menu */}
          {(isInstAdmin || isRegulator || isSuperAdmin) && (
            <div>
              <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-[#AAB6C9]">
                Institution Portal
              </div>
              <div className="space-y-1">
                <NavLink to="/institution/overview" className={linkClass} onClick={onClose}>
                  <LayoutDashboard className="w-4 h-4 flex-shrink-0" />
                  <span>Overview</span>
                </NavLink>

                <NavLink to="/institution/upload" className={linkClass} onClick={onClose}>
                  <Upload className="w-4 h-4 flex-shrink-0" />
                  <span>Upload Admissions</span>
                </NavLink>

                <NavLink to="/institution/issues" className={linkClass} onClick={onClose}>
                  <AlertTriangle className="w-4 h-4 flex-shrink-0 text-amber-500" />
                  <div className="flex items-center justify-between flex-1">
                    <span>Validation Issues</span>
                    <span className="text-[10px] bg-amber-100 text-amber-800 font-bold px-1.5 py-0.5 rounded-full">
                      Live
                    </span>
                  </div>
                </NavLink>

                <NavLink to="/institution/students" className={linkClass} onClick={onClose}>
                  <Users className="w-4 h-4 flex-shrink-0" />
                  <span>Students Directory</span>
                </NavLink>

                <NavLink to="/verification/requests" className={linkClass} onClick={onClose}>
                  <CheckCircle className="w-4 h-4 flex-shrink-0" />
                  <span>Verification Requests</span>
                </NavLink>

                <NavLink to="/notifications" className={linkClass} onClick={onClose}>
                  <Bell className="w-4 h-4 flex-shrink-0" />
                  <span>Notifications</span>
                </NavLink>

                <NavLink to="/institution/students/new" className={linkClass} onClick={onClose}>
                  <UserPlus className="w-4 h-4 flex-shrink-0" />
                  <span>New Student Entry</span>
                </NavLink>
              </div>
            </div>
          )}

          {/* AICTE Regulator Menu */}
          {(isRegulator || isSuperAdmin) && (
            <div>
              <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-[#AAB6C9] flex items-center justify-between">
                <span>AICTE Regulation</span>
                <span className="text-[9px] bg-purple-100 text-purple-700 px-1 py-0.2 rounded font-semibold">Bureau</span>
              </div>
              <div className="space-y-1">
                <NavLink to="/regulator/dashboard" className={linkClass} onClick={onClose}>
                  <Building className="w-4 h-4 flex-shrink-0" />
                  <span>National Dashboard</span>
                </NavLink>

                <NavLink to="/regulator/anomalies" className={linkClass} onClick={onClose}>
                  <ShieldAlert className="w-4 h-4 flex-shrink-0 text-red-500" />
                  <div className="flex items-center justify-between flex-1">
                    <span>Anomaly Center</span>
                    <span className="text-[10px] bg-red-100 text-red-700 font-bold px-1.5 py-0.5 rounded-full">
                      Alert
                    </span>
                  </div>
                </NavLink>

                <NavLink to="/regulator/alerts" className={linkClass} onClick={onClose}>
                  <Bell className="w-4 h-4 flex-shrink-0" />
                  <span>Alerts Feed</span>
                </NavLink>

                <NavLink to="/regulator/reports" className={linkClass} onClick={onClose}>
                  <FileText className="w-4 h-4 flex-shrink-0" />
                  <span>Summary Reports</span>
                </NavLink>

                <NavLink to="/regulator/audit" className={linkClass} onClick={onClose}>
                  <History className="w-4 h-4 flex-shrink-0" />
                  <span>Audit Trail</span>
                </NavLink>
              </div>
            </div>
          )}

          {isVerifier && (
            <div><div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-[#AAB6C9]">Review Workspace</div><div className="space-y-1">
              <NavLink to="/verification/requests" className={linkClass} onClick={onClose}><CheckCircle className="w-4 h-4"/><span>Verification Requests</span></NavLink>
              <NavLink to="/notifications" className={linkClass} onClick={onClose}><Bell className="w-4 h-4"/><span>Notifications</span></NavLink>
            </div></div>
          )}

          {isStudent && <div><div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-[#AAB6C9]">Student Portal</div><div className="space-y-1">
            <NavLink to="/student/portal" className={linkClass} onClick={onClose}><Users className="w-4 h-4"/><span>My Academic Profile</span></NavLink>
            <NavLink to="/notifications" className={linkClass} onClick={onClose}><Bell className="w-4 h-4"/><span>Notifications</span></NavLink>
          </div></div>}

          {/* Verifier Menu */}
          <div>
            <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-[#AAB6C9]">
              Public / Verifier Flow
            </div>
            <div className="space-y-1">
              <NavLink to="/verify" className={linkClass} onClick={onClose}>
                <QrCode className="w-4 h-4 flex-shrink-0 text-emerald-600" />
                <span>Verify Credential</span>
              </NavLink>

              <NavLink to="/help" className={linkClass} onClick={onClose}>
                <HelpCircle className="w-4 h-4 flex-shrink-0 text-[#AAB6C9]" />
                <span>Guide & Demo Walkthrough</span>
              </NavLink>
            </div>
          </div>
        </div>

        {/* User Card in Footer */}
        <div className="p-4 border-t border-white/10 bg-white/5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 overflow-hidden">
              <div className="w-8 h-8 rounded-full bg-white/10 text-white flex items-center justify-center font-bold text-xs flex-shrink-0">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
              <div className="truncate">
                <p className="text-xs font-semibold text-white truncate">{user?.fullName}</p>
                <p className="text-[10px] text-[#AAB6C9] truncate">{user?.email}</p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              title="Logout"
              className="p-1.5 text-[#AAB6C9] hover:text-red-600 hover:bg-white/10 rounded-lg transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>
    </>
  );
};
