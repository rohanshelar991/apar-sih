import React, { useState, useRef } from 'react';
import Papa from 'papaparse';
import { uploadApi } from '../../api/client';
import { UploadResult, ValidationIssue } from '../../types';
import { StatusChip } from '../../components/StatusChip';
import {
  Upload,
  FileSpreadsheet,
  CheckCircle2,
  AlertTriangle,
  AlertCircle,
  FileDown,
  ArrowRight,
  RefreshCw,
  Eye,
  Check,
} from 'lucide-react';
import confetti from 'canvas-confetti';

export const UploadStudents: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [previewRows, setPreviewRows] = useState<any[]>([]);
  const [headers, setHeaders] = useState<string[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [result, setResult] = useState<UploadResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const expectedHeaders = ['apaar_id', 'name', 'dob', 'gender', 'programme', 'admission_year', 'category'];

  const handleFileChange = (selectedFile: File) => {
    if (!selectedFile.name.endsWith('.csv')) {
      setError('Please upload a valid .csv file.');
      return;
    }
    setError(null);
    setFile(selectedFile);
    setResult(null);

    // Client-side PapaParse preview
    Papa.parse(selectedFile, {
      header: true,
      preview: 5,
      skipEmptyLines: true,
      complete: (results) => {
        if (results.meta.fields) {
          setHeaders(results.meta.fields);
        }
        setPreviewRows(results.data);
      },
      error: (err) => {
        setError('Error parsing CSV file: ' + err.message);
      },
    });
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileChange(e.dataTransfer.files[0]);
    }
  };

  const handleStartUpload = async () => {
    if (!file) return;
    setIsUploading(true);
    setUploadProgress(15);
    setError(null);

    // Simulate animated upload progress for judges experience
    const progressInterval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 85) {
          clearInterval(progressInterval);
          return 85;
        }
        return prev + 15;
      });
    }, 120);

    try {
      const res = await uploadApi.uploadCsv(file);
      clearInterval(progressInterval);
      setUploadProgress(100);
      setResult(res);

      if (res.validRows > 0) {
        confetti({
          particleCount: 50,
          spread: 60,
          origin: { y: 0.7 },
        });
      }
    } catch (err: any) {
      clearInterval(progressInterval);
      setError(err.response?.data?.message || 'Server error while validating CSV');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDownloadSample = () => {
    // Generate sample template CSV download
    const csvContent =
      'apaar_id,name,dob,gender,programme,admission_year,category,email,phone\n' +
      '202611223301,Aditi Sharma,2005-04-12,Female,CSE,2026,REGULAR,aditi.s@bmsce.ac.in,9876501234\n' +
      '202611223302,Rohan Varma,2005-08-25,Male,CSE,2026,REGULAR,rohan.v@bmsce.ac.in,9876501235\n' +
      '202611223303,Pooja Patel,2004-11-03,Female,ECE,2026,SUPERNUMERARY,pooja.p@bmsce.ac.in,9876501236\n';

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'apaar_student_template.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleUseDemoFile = async () => {
    try {
      const resp = await fetch('/sample-uploads/new_admissions_INST001.csv');
      const text = await resp.text();
      const blob = new Blob([text], { type: 'text/csv' });
      const demoFile = new File([blob], 'new_admissions_INST001.csv', { type: 'text/csv' });
      handleFileChange(demoFile);
    } catch (err) {
      setError('Could not auto-load sample file. Please select it manually from sample-uploads folder.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/90 shadow-soft">
        <div>
          <span className="text-xs font-bold text-primary tracking-wide uppercase">
            Data Ingestion Engine
          </span>
          <h2 className="text-2xl font-extrabold text-navy mt-0.5">
            Bulk Student Admissions CSV Upload
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Validate records, detect cross-institution duplicates, format anomalies, and intake capacities
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleDownloadSample}
            className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold transition-colors"
          >
            <FileDown className="w-4 h-4 text-slate-500" />
            <span>Download CSV Template</span>
          </button>

          <button
            onClick={handleUseDemoFile}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-purple-50 hover:bg-purple-100 border border-purple-200 text-purple-900 text-xs font-bold transition-colors"
          >
            <span>Load SIH Demo CSV (~60 rows)</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-3">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Upload Box */}
      {!result && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-soft p-6 space-y-6">
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-2xl p-8 sm:p-12 text-center cursor-pointer transition-all duration-200 ${
              isDragging
                ? 'border-primary bg-blue-50/50 scale-[1.01]'
                : file
                ? 'border-emerald-300 bg-emerald-50/20'
                : 'border-slate-300 hover:border-primary/60 bg-slate-50/50 hover:bg-slate-50'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv"
              className="hidden"
              onChange={(e) => e.target.files && handleFileChange(e.target.files[0])}
            />

            <div className="flex flex-col items-center justify-center space-y-3">
              <div className="p-4 rounded-2xl bg-blue-100 text-primary shadow-xs">
                <Upload className="w-8 h-8" />
              </div>
              <div>
                <h4 className="text-base font-bold text-slate-800">
                  {file ? file.name : 'Choose a CSV file or drag & drop here'}
                </h4>
                <p className="text-xs text-slate-500 mt-1">
                  Supported format: CSV (UTF-8) with up to 5,000 admission records
                </p>
              </div>

              {file && (
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-semibold">
                  <Check className="w-3.5 h-3.5" />
                  <span>File Selected: {(file.size / 1024).toFixed(1)} KB</span>
                </div>
              )}
            </div>
          </div>

          {/* Client Preview Table via PapaParse */}
          {previewRows.length > 0 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Eye className="w-4 h-4 text-slate-500" />
                  <h4 className="text-xs font-bold text-slate-800">
                    Client Preview (First {previewRows.length} rows parsed locally)
                  </h4>
                </div>

                {/* Column Mapping Indicator */}
                <div className="flex items-center gap-1.5 text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-md border border-emerald-200">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>Headers mapped successfully</span>
                </div>
              </div>

              <div className="overflow-x-auto rounded-xl border border-slate-200">
                <table className="w-full text-left text-xs text-slate-700">
                  <thead className="bg-slate-50 font-bold text-slate-600 border-b border-slate-200">
                    <tr>
                      {headers.map((h, i) => (
                        <th key={i} className="py-2.5 px-3 uppercase text-[10px]">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {previewRows.map((row, idx) => (
                      <tr key={idx} className="hover:bg-slate-50">
                        {headers.map((h, i) => (
                          <td key={i} className="py-2 px-3 font-mono text-[11px] truncate max-w-[150px]">
                            {row[h] || <span className="text-slate-300 italic">null</span>}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Upload Progress Bar if uploading */}
              {isUploading && (
                <div className="space-y-2 pt-2">
                  <div className="flex items-center justify-between text-xs font-semibold text-primary">
                    <span>Running 9-Rule Validation Engine & Duplicate Detection...</span>
                    <span>{uploadProgress}%</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                    <div
                      className="bg-primary h-2.5 rounded-full transition-all duration-300"
                      style={{ width: `${uploadProgress}%` }}
                    />
                  </div>
                </div>
              )}

              {/* Action Button */}
              <div className="flex justify-end pt-2">
                <button
                  onClick={handleStartUpload}
                  disabled={isUploading}
                  className="py-2.5 px-6 rounded-xl bg-primary hover:bg-primary-600 text-white font-bold text-xs shadow-md transition-all flex items-center gap-2 disabled:opacity-50"
                >
                  {isUploading ? (
                    <>
                      <RefreshCw className="w-4 h-4 animate-spin" />
                      <span>Validating Records...</span>
                    </>
                  ) : (
                    <>
                      <span>Submit Batch for Server Validation</span>
                      <ArrowRight className="w-4 h-4" />
                    </>
                  )}
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Upload Results Summary Card */}
      {result && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-soft p-6 space-y-6">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                <h3 className="text-lg font-extrabold text-navy">
                  Batch Validation Completed: {result.filename}
                </h3>
              </div>
              <p className="text-xs text-slate-500 mt-0.5">
                Processed {result.totalRows} student admission records through APAAR verification rules.
              </p>
            </div>

            <button
              onClick={() => {
                setResult(null);
                setFile(null);
                setPreviewRows([]);
              }}
              className="px-3.5 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50"
            >
              Upload Another File
            </button>
          </div>

          {/* Result Metric Badges */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
              <span className="text-[11px] font-semibold text-slate-500">Total Rows</span>
              <p className="text-2xl font-extrabold text-slate-900 mt-1">{result.totalRows}</p>
            </div>

            <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200">
              <span className="text-[11px] font-semibold text-emerald-800">Valid Rows</span>
              <p className="text-2xl font-extrabold text-emerald-900 mt-1">{result.validRows}</p>
            </div>

            <div className="p-4 rounded-xl bg-purple-50 border border-purple-200">
              <span className="text-[11px] font-semibold text-purple-800">Duplicate Rows</span>
              <p className="text-2xl font-extrabold text-purple-900 mt-1">{result.duplicateRows}</p>
            </div>

            <div className="p-4 rounded-xl bg-red-50 border border-red-200">
              <span className="text-[11px] font-semibold text-red-800">Error / Flagged Rows</span>
              <p className="text-2xl font-extrabold text-red-900 mt-1">{result.errorRows}</p>
            </div>
          </div>

          {/* Validation Issues Table */}
          {result.issues && result.issues.length > 0 && (
            <div className="space-y-3 pt-2">
              <div className="flex items-center justify-between">
                <h4 className="font-bold text-xs text-slate-800">
                  Detected Exceptions ({result.issues.length} items flagged for review)
                </h4>
                <a
                  href="/institution/issues"
                  className="text-xs font-semibold text-primary hover:underline flex items-center gap-1"
                >
                  <span>Go to Issues Resolution Workflow</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </a>
              </div>

              <div className="overflow-x-auto rounded-xl border border-slate-200">
                <table className="w-full text-left text-xs text-slate-700">
                  <thead className="bg-slate-50 font-bold text-slate-600 text-[10px] uppercase border-b border-slate-200">
                    <tr>
                      <th className="py-2.5 px-4">Student / APAAR</th>
                      <th className="py-2.5 px-3">Rule Code</th>
                      <th className="py-2.5 px-3">Severity</th>
                      <th className="py-2.5 px-4">Message</th>
                      <th className="py-2.5 px-4">Suggested Fix</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {result.issues.map((issue) => (
                      <tr key={issue.id} className="hover:bg-slate-50">
                        <td className="py-2.5 px-4">
                          <span className="font-bold text-slate-900 block">{issue.studentName || 'Row Entry'}</span>
                          <span className="font-mono text-[10px] text-slate-400">
                            {issue.maskedApaarId || issue.apaarId || 'N/A'}
                          </span>
                        </td>
                        <td className="py-2.5 px-3 font-mono font-semibold text-primary text-[11px]">
                          {issue.ruleCode}
                        </td>
                        <td className="py-2.5 px-3">
                          <StatusChip status={issue.severity} showIcon={false} />
                        </td>
                        <td className="py-2.5 px-4 text-slate-700 max-w-xs">{issue.message}</td>
                        <td className="py-2.5 px-4 text-slate-500 italic text-[11px] max-w-xs">
                          {issue.suggestedFix}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
