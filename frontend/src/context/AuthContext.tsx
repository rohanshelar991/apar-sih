import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, DemoAccount, Role } from '../types';
import { authApi } from '../api/client';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, pass: string) => Promise<User>;
  logout: () => void;
  demoAccounts: DemoAccount[];
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Existing public demo personas remain visible while the backend is starting.
// These shortcuts still use the normal authenticated login endpoint.
const existingDemoAccounts: DemoAccount[] = [
  { role: 'ROLE_INSTITUTION_ADMIN', title: 'Institution Administrator', institution: 'BMS College of Engineering (INST001)', email: 'admin@bmsce.ac.in', password: 'Admin@123', description: 'Uploads student batch CSVs, reviews validation errors, corrects flagged records, exports directories.' },
  { role: 'ROLE_AICTE_REGULATOR', title: 'AICTE National Regulator', institution: 'AICTE Regulation Bureau', email: 'regulator@aicte-india.org', password: 'Regulator@123', description: 'Cross-institutional oversight, intake compliance charts, anomaly center, duplicate escalations, PDF reports.' },
  { role: 'ROLE_AUTHORIZED_VERIFIER', title: 'Authorized Verifier / Employer', institution: 'Tata Consultancy Services (TCS)', email: 'verifier@tcs.com', password: 'Verifier@123', description: 'Verifies candidate authenticity via QR code or verification token with permitted fields data minimization.' },
];

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('apaar_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('apaar_token');
  });
  const [demoAccounts, setDemoAccounts] = useState<DemoAccount[]>(existingDemoAccounts);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    authApi.getDemoAccounts()
      .then((data) => setDemoAccounts(data))
      .catch((err) => console.error('Failed to load demo accounts', err));
  }, []);

  const login = async (email: string, pass: string): Promise<User> => {
    setLoading(true);
    try {
      const resp = await authApi.login(email, pass);
      const userData: User = {
        email: resp.email,
        fullName: resp.fullName,
        role: resp.role as Role,
        institutionId: resp.institutionId,
        institutionCode: resp.institutionCode,
        institutionName: resp.institutionName,
      };
      setToken(resp.token);
      setUser(userData);
      localStorage.setItem('apaar_token', resp.token);
      localStorage.setItem('apaar_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('apaar_token');
    localStorage.removeItem('apaar_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        login,
        logout,
        demoAccounts,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
