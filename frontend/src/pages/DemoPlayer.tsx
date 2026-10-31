import React, { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Play,
  Pause,
  Download,
  ArrowLeft,
  CheckCircle2,
  Clock,
  Shield,
  Layers,
  Sparkles,
  Award,
  Maximize2,
  Github,
  ExternalLink,
  Image as ImageIcon
} from 'lucide-react';
import { Logo } from '../components/Logo';

interface Chapter {
  start: number;
  end: number;
  timeStr: string;
  title: string;
  description: string;
  role: string;
}

const CHAPTERS: Chapter[] = [
  {
    start: 0,
    end: 27.74,
    timeStr: '0:00',
    title: 'Platform Overview & Role-Based Login',
    description: 'SIH 2026 Problem Statement 26263 intro, simulated adapter compliance badge, 3 role demo cards, and logging in as BMSCE (INST001).',
    role: 'Institution Admin'
  },
  {
    start: 27.74,
    end: 50.71,
    timeStr: '0:28',
    title: 'Institution Intake Health Monitoring',
    description: 'KPI overview (372 students, 95.8% B.Tech CSE capacity: 230/240 seats), navigating to batch upload.',
    role: 'Institution Admin'
  },
  {
    start: 50.71,
    end: 85.37,
    timeStr: '0:51',
    title: 'CSV Upload & 9-Rule Engine (15 Anomalies)',
    description: 'PapaParse preview, asynchronous batch processing, detecting in-file duplicates, format errors, and pushing CSE into INTAKE_EXCEEDED (108.7%).',
    role: 'Validation Engine'
  },
  {
    start: 85.37,
    end: 113.57,
    timeStr: '1:26',
    title: 'Inline Correction & Revalidation',
    description: 'Editing student Rahul Sharma directly in the modal, real-time regex check, saving & revalidating to green Valid, audit history logged.',
    role: 'Institution Admin'
  },
  {
    start: 113.57,
    end: 145.49,
    timeStr: '1:54',
    title: 'AICTE Regulator Dashboard & Anomaly Center',
    description: 'Nationwide oversight, Approved vs Actual discipline charts, BMSCE red EXCEEDED badge, reviewing & escalating cross-institution duplicate.',
    role: 'AICTE Regulator'
  },
  {
    start: 145.49,
    end: 178.75,
    timeStr: '2:25',
    title: 'Compliance Reports, QR & GitHub Prototype',
    description: 'Exporting PDF/CSV regulatory reports, generating consent-backed student verification token, verifying permitted fields, and GitHub repository prototype link.',
    role: 'Authorized Verifier'
  }
];

export const DemoPlayer: React.FC = () => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(178.75);
  const [activeChapter, setActiveChapter] = useState(0);

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
    } else {
      videoRef.current.play();
    }
  };

  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    const cur = videoRef.current.currentTime;
    setCurrentTime(cur);

    const chIdx = CHAPTERS.findIndex(ch => cur >= ch.start && cur < ch.end);
    if (chIdx !== -1) {
      setActiveChapter(chIdx);
    }
  };

  const seekToChapter = (start: number) => {
    if (!videoRef.current) return;
    videoRef.current.currentTime = start;
    videoRef.current.play();
    setIsPlaying(true);
  };

  const formatTime = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Top Header */}
      <header className="border-b border-slate-800/80 bg-slate-900/80 backdrop-blur-md px-6 py-3.5 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link
              to="/login"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-400 hover:text-white px-2.5 py-1.5 rounded-lg hover:bg-slate-800 transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back to App</span>
            </Link>
            <div className="h-4 w-px bg-slate-800" />
            <Logo size="sm" />
          </div>

          <div className="flex items-center gap-2 sm:gap-3">
            <span className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Award className="w-3.5 h-3.5" />
              SIH 2026 &bull; Problem Statement 26263 &bull; Team AuthenIQ
            </span>
            <a
              href="/thumbnail.jpg"
              download="APAAR_Connect_YouTube_Thumbnail.jpg"
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors"
            >
              <ImageIcon className="w-4 h-4 text-amber-400" />
              <span className="hidden sm:inline">Download Thumbnail</span>
              <span className="sm:hidden">Thumbnail</span>
            </a>
            <a
              href="/demo_video.mp4"
              download="APAAR_Connect_Demonstration_Video.mp4"
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-primary hover:bg-primary/90 text-white shadow-sm transition-colors"
            >
              <Download className="w-4 h-4" />
              <span>Download MP4</span>
            </a>
          </div>
        </div>
      </header>

      {/* Main Theater Container */}
      <main className="max-w-7xl mx-auto w-full px-4 sm:px-6 py-6 flex-1 flex flex-col gap-6">
        {/* Title & Badge */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">
                Official Project Walkthrough
              </span>
              <span className="text-xs text-slate-400 font-medium flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-slate-500" />
                Duration: 02:58 min &bull; Full HD 1080p &bull; Human Voice Narration
              </span>
            </div>
            <h1 className="text-2xl font-bold text-white tracking-tight">
              APAAR GARUDA NETRA End-to-End System Demonstration
            </h1>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs px-2.5 py-1 rounded-md bg-slate-800/80 text-slate-300 border border-slate-700/60 font-mono">
              Status: All Acceptance Criteria Passed
            </span>
          </div>
        </div>

        {/* Video & Chapter Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Video Player (8 cols) */}
          <div className="lg:col-span-8 flex flex-col gap-3">
            <div className="relative rounded-2xl overflow-hidden bg-black border border-slate-800 shadow-2xl aspect-video group">
              <video
                ref={videoRef}
                src="/demo_video.mp4"
                className="w-full h-full object-contain cursor-pointer"
                onClick={togglePlay}
                onPlay={() => setIsPlaying(true)}
                onPause={() => setIsPlaying(false)}
                onTimeUpdate={handleTimeUpdate}
                onLoadedMetadata={(e) => setDuration(e.currentTarget.duration || 172.35)}
                controls
                playsInline
                autoPlay
              />
            </div>

            {/* Quick Seek Highlights */}
            <div className="flex flex-wrap items-center justify-between gap-2 px-1 text-xs text-slate-400">
              <div className="flex items-center gap-2">
                <span className="font-semibold text-slate-300">Quick Jump:</span>
                <button
                  onClick={() => seekToChapter(0)}
                  className="px-2 py-1 rounded bg-slate-800/60 hover:bg-slate-700 text-slate-300 transition-colors"
                >
                  0:00 Intro
                </button>
                <button
                  onClick={() => seekToChapter(51.22)}
                  className="px-2 py-1 rounded bg-slate-800/60 hover:bg-slate-700 text-slate-300 transition-colors"
                >
                  0:51 Validation Engine
                </button>
                <button
                  onClick={() => seekToChapter(86.79)}
                  className="px-2 py-1 rounded bg-slate-800/60 hover:bg-slate-700 text-slate-300 transition-colors"
                >
                  1:26 Inline Correction
                </button>
                <button
                  onClick={() => seekToChapter(114.25)}
                  className="px-2 py-1 rounded bg-slate-800/60 hover:bg-slate-700 text-slate-300 transition-colors"
                >
                  1:54 Regulator Dashboard
                </button>
                <button
                  onClick={() => seekToChapter(145.88)}
                  className="px-2 py-1 rounded bg-slate-800/60 hover:bg-slate-700 text-slate-300 transition-colors"
                >
                  2:25 QR Verification
                </button>
              </div>

              <div className="font-mono text-slate-400">
                {formatTime(currentTime)} / {formatTime(duration)}
              </div>
            </div>
          </div>

          {/* Chapter Timeline (4 cols) */}
          <div className="lg:col-span-4 flex flex-col gap-3">
            <div className="flex items-center justify-between px-1">
              <h2 className="text-sm font-bold text-slate-200 flex items-center gap-2">
                <Layers className="w-4 h-4 text-primary" />
                Demonstration Chapters
              </h2>
              <span className="text-[11px] text-slate-500 font-mono">6 Sections</span>
            </div>

            <div className="flex flex-col gap-2 max-h-[500px] overflow-y-auto pr-1">
              {CHAPTERS.map((ch, idx) => {
                const isActive = activeChapter === idx;
                return (
                  <button
                    key={idx}
                    onClick={() => seekToChapter(ch.start)}
                    className={`text-left p-3.5 rounded-xl border transition-all flex flex-col gap-1.5 ${
                      isActive
                        ? 'bg-blue-950/40 border-blue-500/50 shadow-md ring-1 ring-blue-500/30'
                        : 'bg-slate-900/60 border-slate-800/80 hover:bg-slate-800/50 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-[11px] font-mono font-bold text-amber-400 flex items-center gap-1">
                        <Clock className="w-3 h-3 text-amber-400/80" />
                        {ch.timeStr}
                      </span>
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700/60">
                        {ch.role}
                      </span>
                    </div>

                    <h4 className={`text-xs font-bold ${isActive ? 'text-blue-300' : 'text-slate-200'}`}>
                      {ch.title}
                    </h4>

                    <p className="text-[11px] text-slate-400 line-clamp-2 leading-relaxed">
                      {ch.description}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* GitHub Repository Link Banner */}
        <div className="p-4 rounded-xl bg-gradient-to-r from-slate-900 via-indigo-950/60 to-slate-900 border border-indigo-500/30 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              <Github className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-white flex items-center gap-2">
                Prototype Working Code Uploaded to GitHub
                <span className="px-1.5 py-0.2 rounded text-[10px] font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  Open Source
                </span>
              </h4>
              <p className="text-[11px] text-slate-400 mt-0.5">
                Complete React 18 + Spring Boot 3 + 9-Rule Validation Engine repository by Team AuthenIQ
              </p>
            </div>
          </div>
          <a
            href="https://github.com/abhishekbagi262/APAAR-ID---AuthenIQ"
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition-colors shadow-sm"
          >
            <Github className="w-4 h-4" />
            <span>View GitHub Repository</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>

        {/* Feature Highlights Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-slate-800/80">
          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800 flex flex-col gap-1.5">
            <div className="flex items-center gap-2 text-primary font-bold text-xs">
              <CheckCircle2 className="w-4 h-4" />
              9-Rule Asynchronous Validation Engine
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              Detects in-file duplicates, cross-institutional concurrent enrolments, format anomalies, mandatory field omissions, and programme intake exceedances.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800 flex flex-col gap-1.5">
            <div className="flex items-center gap-2 text-amber-400 font-bold text-xs">
              <Shield className="w-4 h-4" />
              AICTE Regulatory Oversight & Anomaly Center
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              Aggregated national intake charts, real-time alerts, automated exceedance highlighting, anomaly escalation workflows, and instant PDF/CSV compliance report generation.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800 flex flex-col gap-1.5">
            <div className="flex items-center gap-2 text-emerald-400 font-bold text-xs">
              <Sparkles className="w-4 h-4" />
              Privacy-Preserving QR Verification
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              Zero leakage of Aadhaar or unconsented PII. Time-limited tokens allow authorized verifiers and employers to confirm permitted student credentials with confidence.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 bg-slate-900/60 px-6 py-4 text-center text-xs text-slate-500">
        <p>
          Smart India Hackathon 2026 &bull; Problem Statement 26263: APAAR ID-Based Student Data Management and Verification Platform
        </p>
        <p className="mt-0.5 text-[11px] text-slate-600">
          Built by Team AuthenIQ &bull; Prototype uses a Simulated APAAR Verification Adapter with Synthetic Data
        </p>
      </footer>
    </div>
  );
};
