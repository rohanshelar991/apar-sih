export type Role = 'ROLE_INSTITUTION_ADMIN' | 'ROLE_AICTE_REGULATOR' | 'ROLE_AUTHORIZED_VERIFIER';

export interface User {
  email: string;
  fullName: string;
  role: Role;
  institutionId?: number;
  institutionCode?: string;
  institutionName?: string;
}

export interface DemoAccount {
  role: Role;
  title: string;
  institution: string;
  email: string;
  password: string;
  description: string;
}

export interface Institution {
  id: number;
  code: string;
  name: string;
  state: string;
  city: string;
  status: string;
}

export interface Programme {
  id: number;
  name: string;
  code: string;
  academicYear: number;
  approvedIntake: number;
  supernumeraryQuota: number;
  totalPermittedIntake?: number;
  institution?: Institution;
}

export interface ValidationIssue {
  id: number;
  studentId?: number;
  studentName?: string;
  apaarId?: string;
  maskedApaarId?: string;
  batchId?: number;
  ruleCode: string;
  severity: 'ERROR' | 'WARNING' | 'INFO';
  message: string;
  suggestedFix?: string;
  status: 'OPEN' | 'RESOLVED' | 'UNDER_REVIEW' | 'DISMISSED' | 'ESCALATED';
  resolvedBy?: string;
  resolvedAt?: string;
  createdAt: string;
}

export interface Student {
  id: number;
  apaarId: string;
  maskedApaarId: string;
  name: string;
  dob: string;
  gender: string;
  programmeId: number;
  programmeName: string;
  programmeCode: string;
  institutionId: number;
  institutionCode: string;
  institutionName: string;
  admissionYear: number;
  category: string;
  email?: string;
  phone?: string;
  verificationStatus: 'VALID' | 'PENDING' | 'DUPLICATE' | 'ERROR' | 'VERIFIED';
  consentGiven: boolean;
  createdAt: string;
  updatedAt: string;
  issues?: ValidationIssue[];
}

export interface UploadBatch {
  id: number;
  institutionId: number;
  institutionName: string;
  filename: string;
  totalRows: number;
  validRows: number;
  errorRows: number;
  duplicateRows: number;
  uploadedBy: string;
  uploadedAt: string;
  status: string;
}

export interface UploadResult {
  batchId: number;
  filename: string;
  totalRows: number;
  validRows: number;
  errorRows: number;
  duplicateRows: number;
  status: string;
  issues: ValidationIssue[];
}

export interface InstitutionStats {
  totalStudents: number;
  validStudents: number;
  pendingStudents: number;
  duplicateStudents: number;
  errorStudents: number;
  approvedIntake: number;
  supernumeraryQuota: number;
  totalPermittedIntake: number;
  utilizationPercent: number;
}

export interface RegulatorKpi {
  totalInstitutions: number;
  approvedIntake: number;
  actualEnrolment: number;
  supernumerarySeats: number;
  openExceptions: number;
  intakeExceededProgrammes: number;
}

export interface ProgrammeIntake {
  programmeId: number;
  programmeCode: string;
  programmeName: string;
  institutionId: number;
  institutionCode: string;
  institutionName: string;
  approvedIntake: number;
  supernumeraryQuota: number;
  totalPermitted: number;
  actualEnrolment: number;
  utilizationPercent: number;
  status: 'OK' | 'WARNING' | 'EXCEEDED';
}

export interface CategoryBreakdown {
  category: string;
  count: number;
  percentage: number;
}

export interface YearTrend {
  year: number;
  count: number;
}

export interface Anomaly {
  id: number;
  type: string;
  title: string;
  description: string;
  institutionCode: string;
  institutionName: string;
  programmeName: string;
  severity: string;
  createdAt: string;
  status: string;
  studentId?: number;
  apaarId?: string;
  maskedApaarId?: string;
}

export interface AuditLog {
  id: number;
  actor: string;
  role: string;
  action: string;
  entity: string;
  entityId: string;
  ip: string;
  timestamp: string;
  details: string;
}

export interface PublicVerification {
  valid: boolean;
  error?: string;
  token?: string;
  studentName?: string;
  maskedApaarId?: string;
  dob?: string;
  gender?: string;
  institutionCode?: string;
  institutionName?: string;
  programmeName?: string;
  admissionYear?: number;
  verificationStatus?: string;
  verifiedAt?: string;
  permittedFields?: string[];
  issuer?: string;
}
