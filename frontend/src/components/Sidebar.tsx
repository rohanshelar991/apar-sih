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
  const isVerifier = user?.role === 'ROLE_AUTHORIZED_VERIFIER';

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
      isActive
        ? 'bg-primary text-white shadow-sm font-semibold'
        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80'
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
        className={`fixed top-0 bottom-0 left-0 z-50 flex w-64 flex-col border-r border-slate-200 bg-white transition-transform duration-200 ease-in-out lg:static lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        } no-print`}
      >
        {/* Logo header */}
        <div className="flex h-16 items-center px-6 border-b border-slate-100">
          <Logo size="sm" />
        </div>

        {/* Navigation list */}
        <div className="flex-1 overflow-y-auto px-4 py-4 space-y-6">
          {/* Institution Menu */}
          {(isInstAdmin || isRegulator) && (
            <div>
              <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
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

                <NavLink to="/institution/students/new" className={linkClass} onClick={onClose}>
                  <UserPlus className="w-4 h-4 flex-shrink-0" />
                  <span>New Student Entry</span>
                </NavLink>
              </div>
            </div>
          )}

          {/* AICTE Regulator Menu */}
          {isRegulator && (
            <div>
              <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-slate-400 flex items-center justify-between">
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

          {/* Verifier Menu */}
          <div>
            <div className="px-3 mb-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
              Public / Verifier Flow
            </div>
            <div className="space-y-1">
              <NavLink to="/verify" className={linkClass} onClick={onClose}>
                <QrCode className="w-4 h-4 flex-shrink-0 text-emerald-600" />
                <span>Verify Credential</span>
              </NavLink>

              <NavLink to="/help" className={linkClass} onClick={onClose}>
                <HelpCircle className="w-4 h-4 flex-shrink-0 text-slate-400" />
                <span>Guide & Demo Walkthrough</span>
              </NavLink>
            </div>
          </div>
        </div>

        {/* User Card in Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50/50">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 overflow-hidden">
              <div className="w-8 h-8 rounded-full bg-navy text-white flex items-center justify-center font-bold text-xs flex-shrink-0">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
              <div className="truncate">
                <p className="text-xs font-semibold text-slate-800 truncate">{user?.fullName}</p>
                <p className="text-[10px] text-slate-400 truncate">{user?.email}</p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              title="Logout"
              className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>
    </>
  );
};
