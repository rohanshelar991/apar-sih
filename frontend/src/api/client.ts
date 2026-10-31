import axios from 'axios';
import {
  User, DemoAccount, Institution, Programme, Student, ValidationIssue,
  UploadBatch, UploadResult, InstitutionStats, RegulatorKpi, ProgrammeIntake,
  CategoryBreakdown, YearTrend, Anomaly, AuditLog, PublicVerification
} from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || (import.meta.env.DEV ? '/api' : undefined),
  // Render cold starts can exceed a normal short request timeout. No automatic
  // retries: uploads and other writes must never be submitted twice silently.
  timeout: 120_000,
});

// Intercept requests to inject JWT Bearer token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('apaar_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Intercept responses to handle 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const isPublicVerify = window.location.pathname.startsWith('/verify');
      const isLogin = window.location.pathname === '/login';
      if (!isPublicVerify && !isLogin) {
        localStorage.removeItem('apaar_token');
        localStorage.removeItem('apaar_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: async (email: string, password: string) => {
    const res = await api.post('/auth/login', { email, password });
    return res.data;
  },
  getCurrentUser: async () => {
    const res = await api.get<User>('/auth/me');
    return res.data;
  },
  getDemoAccounts: async () => {
    const res = await api.get<DemoAccount[]>('/auth/demo-accounts');
    return res.data;
  },
};

export const institutionApi = {
  getCurrent: async () => {
    const res = await api.get<Institution>('/institution/current');
    return res.data;
  },
  getAll: async () => {
    const res = await api.get<Institution[]>('/institution/all');
    return res.data;
  },
  getProgrammes: async (institutionId?: number) => {
    const res = await api.get<Programme[]>('/institution/programmes', {
      params: { institutionId },
    });
    return res.data;
  },
  getStats: async (institutionId?: number) => {
    const res = await api.get<InstitutionStats>('/institution/stats', {
      params: { institutionId },
    });
    return res.data;
  },
};

export const studentApi = {
  getStudents: async (params: {
    institutionId?: number;
    programmeId?: number;
    category?: string;
    status?: string;
    year?: number;
    search?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await api.get<{ content: Student[]; totalElements: number; totalPages: number }>('/students', {
      params,
    });
    return res.data;
  },
  getStudentById: async (id: number) => {
    const res = await api.get<Student>(`/students/${id}`);
    return res.data;
  },
  createStudent: async (studentData: any) => {
    const res = await api.post<Student>('/students', studentData);
    return res.data;
  },
  updateStudent: async (id: number, studentData: any) => {
    const res = await api.put<Student>(`/students/${id}`, studentData);
    return res.data;
  },
  exportCsv: async (params: any) => {
    const res = await api.get('/students/export', {
      params,
      responseType: 'blob',
    });
    return res.data;
  },
};

export const uploadApi = {
  uploadCsv: async (file: File, institutionId?: number) => {
    const formData = new FormData();
    formData.append('file', file);
    if (institutionId) {
      formData.append('institutionId', institutionId.toString());
    }
    const res = await api.post<UploadResult>('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },
  getBatches: async (institutionId?: number, page = 0, size = 10) => {
    const res = await api.get<{ content: UploadBatch[]; totalElements: number }>('/upload/batches', {
      params: { institutionId, page, size },
    });
    return res.data;
  },
};

export const validationApi = {
  getIssues: async (params: {
    institutionId?: number;
    status?: string;
    severity?: string;
    ruleCode?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await api.get<{ content: ValidationIssue[]; totalElements: number; totalPages: number }>('/validation/issues', {
      params,
    });
    return res.data;
  },
  resolveIssue: async (id: number, data: {
    apaarId?: string;
    name?: string;
    dob?: string;
    programmeId?: number;
    admissionYear?: number;
    category?: string;
    resolutionNote?: string;
  }) => {
    const res = await api.post<ValidationIssue>(`/validation/issues/${id}/resolve`, data);
    return res.data;
  },
};

export const regulatorApi = {
  getKpis: async () => {
    const res = await api.get<RegulatorKpi>('/regulator/kpis');
    return res.data;
  },
  getProgrammeIntakes: async (institutionId?: number, academicYear?: number) => {
    const res = await api.get<ProgrammeIntake[]>('/regulator/programmes-intake', {
      params: { institutionId, academicYear },
    });
    return res.data;
  },
  getCategoryBreakdown: async () => {
    const res = await api.get<CategoryBreakdown[]>('/regulator/category-breakdown');
    return res.data;
  },
  getYearTrends: async () => {
    const res = await api.get<YearTrend[]>('/regulator/year-trends');
    return res.data;
  },
  getAnomalies: async () => {
    const res = await api.get<Anomaly[]>('/regulator/anomalies');
    return res.data;
  },
  performAnomalyAction: async (id: number, action: string, note?: string) => {
    const res = await api.post(`/regulator/anomalies/${id}/action`, { action, note });
    return res.data;
  },
  getAlerts: async () => {
    const res = await api.get<any[]>('/regulator/alerts');
    return res.data;
  },
};

export const reportApi = {
  downloadCsv: async (institutionId?: number) => {
    const res = await api.get('/reports/summary/csv', {
      params: { institutionId },
      responseType: 'blob',
    });
    return res.data;
  },
  downloadPdf: async (institutionId?: number) => {
    const res = await api.get('/reports/summary/pdf', {
      params: { institutionId },
      responseType: 'blob',
    });
    return res.data;
  },
};

export const verifyApi = {
  createToken: async (studentId: number, allowedFields: string, expiryHours = 72) => {
    const res = await api.post<{
      token: string;
      verificationUrl: string;
      expiresAt: string;
      allowedFields: string;
    }>('/tokens/generate', {
      studentId,
      allowedFields,
      expiryHours,
    });
    return res.data;
  },
  verifyToken: async (token: string) => {
    const res = await api.get<PublicVerification>(`/verify/${token}`);
    return res.data;
  },
};

export const auditApi = {
  getLogs: async (params: {
    actor?: string;
    role?: string;
    action?: string;
    entity?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await api.get<{ content: AuditLog[]; totalElements: number; totalPages: number }>('/audit/logs', {
      params,
    });
    return res.data;
  },
  exportCsv: async (params?: any) => {
    const res = await api.get('/audit/export', {
      params,
      responseType: 'blob',
    });
    return res.data;
  },
};

export default api;
