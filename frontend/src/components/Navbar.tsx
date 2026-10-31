import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Building2, Shield, LogOut, HelpCircle, Bell, Play, Menu, Landmark } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

interface NavbarProps {
  onToggleSidebar?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onToggleSidebar }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadge = (role?: string) => {
    if (role === 'ROLE_AICTE_REGULATOR') {
      return {
        label: 'AICTE Regulator',
        color: 'bg-purple-100 text-purple-800 border-purple-200',
        icon: <Shield className="w-3.5 h-3.5 text-purple-600" />,
      };
    }
    if (role === 'ROLE_INSTITUTION_ADMIN') {
      return {
        label: 'Institution Admin',
        color: 'bg-blue-100 text-blue-800 border-blue-200',
        icon: <Building2 className="w-3.5 h-3.5 text-blue-600" />,
      };
    }
    if (role === 'ROLE_SUPER_ADMIN') return { label: 'Super administrator', color: 'bg-slate-100 text-slate-800 border-slate-300', icon: <Shield className="w-3.5 h-3.5 text-slate-700" /> };
    if (role === 'ROLE_STUDENT') return { label: 'Student', color: 'bg-emerald-50 text-emerald-800 border-emerald-200', icon: <Building2 className="w-3.5 h-3.5 text-emerald-700" /> };
    if (role === 'ROLE_VERIFICATION_OFFICER') return { label: 'Verification officer', color: 'bg-amber-50 text-amber-900 border-amber-200', icon: <Shield className="w-3.5 h-3.5 text-amber-700" /> };
    return {
      label: 'Authorized Verifier',
      color: 'bg-emerald-100 text-emerald-800 border-emerald-200',
      icon: <Shield className="w-3.5 h-3.5 text-emerald-600" />,
    };
  };

  const roleInfo = getRoleBadge(user?.role);

  return (
    <header className="sticky top-0 z-30 flex min-h-16 w-full items-center justify-between border-b border-slate-200 bg-white px-4 sm:px-6 shadow-sm no-print">
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSidebar}
          className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 lg:hidden"
          aria-label="Toggle Navigation"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Institution / Regulator Context Indicator */}
        <div className="flex items-center gap-2">
          {user?.institutionName ? (
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-50 border border-slate-200">
              <Building2 className="w-4 h-4 text-primary" />
              <div className="leading-none">
                <span className="text-xs font-bold text-navy block">{user.institutionName}</span>
                <span className="text-[10px] text-slate-500 font-mono font-medium">{user.institutionCode}</span>
              </div>
            </div>
          ) : (
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-primary-50 border border-primary-100">
              <Landmark className="w-4 h-4 text-primary" />
              <div className="leading-none">
                <span className="text-xs font-bold text-navy block">Academic Oversight Workspace</span>
                <span className="text-[10px] text-slate-600 font-medium">Role-authorized access</span>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="flex items-center gap-3">
        <Link to="/notifications" aria-label="Notifications" title="Notifications" className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 hover:text-primary">
          <Bell className="w-4 h-4" />
        </Link>
        {/* Watch Demo Video Link */}
        <Link
          to="/demo"
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-gold-light text-gold-dark border border-[#E8D8B7] font-bold text-xs shadow-xs hover:opacity-90 transition-opacity"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span className="hidden sm:inline">Watch Demo Video</span>
          <span className="sm:hidden">Video</span>
        </Link>

        {/* Help Link */}
        <Link
          to="/help"
          className="hidden sm:flex items-center gap-1.5 text-xs font-medium text-slate-600 hover:text-primary px-2.5 py-1.5 rounded-lg hover:bg-slate-100 transition-colors"
        >
          <HelpCircle className="w-4 h-4 text-slate-400" />
          <span>How It Works</span>
        </Link>

        {/* Role Badge */}
        <span className={`hidden sm:inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${roleInfo.color}`}>
          {roleInfo.icon}
          {roleInfo.label}
        </span>

        {/* User Profile & Logout */}
        <div className="flex items-center gap-2 border-l border-slate-200 pl-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-navy to-primary text-white flex items-center justify-center font-bold text-xs shadow-sm">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div className="hidden md:block leading-tight text-left">
            <span className="text-xs font-bold text-slate-800 block truncate max-w-[140px]">{user?.fullName}</span>
            <span className="text-[10px] text-slate-400 truncate max-w-[140px] block">{user?.email}</span>
          </div>

          <button
            onClick={handleLogout}
            title="Log Out"
            className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors ml-1"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
