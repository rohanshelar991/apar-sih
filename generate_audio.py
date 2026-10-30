import asyncio
import os
import subprocess
import glob
import edge_tts

VOICE = "en-IN-PrabhatNeural"

scripts = {
    "scene1": """Welcome. Team AuthenIQ presents APAAR-Connect for Smart India Hackathon 2026, Problem Statement 26263: AICTE Student Data Management and Verification.
Our prototype uses a simulated APAAR verification adapter with synthetic data.
The system provides role-based access for institutions, regulators, and verifiers.
Let us log in as BMS College of Engineering.""",

    "scene2": """In the Institution portal, real-time KPI cards display enrolment health and intake utilization.
Currently, B.Tech Computer Science has 230 students enrolled against 240 approved seats, operating at 95.8 percent capacity.
Let us now upload a new batch of 60 admission records.""",

    "scene3": """Institutions upload student records via CSV.
PapaParse immediately provides a client-side preview, verifying required columns.
Submitting triggers our asynchronous 9-rule validation engine.
In seconds, the engine flags 15 anomalies across the batch, including in-file duplicates and invalid formats.
Crucially, adding these 31 CSE records pushes enrolment to 261 students, triggering an INTAKE_EXCEEDED violation at 108.7 percent capacity.""",

    "scene4": """Instead of rejecting entire batches, APAAR-Connect enables inline corrections.
For student Rahul Sharma, flagged with an invalid format, the admin clicks Edit Record.
We enter the corrected 12-digit APAAR ID with live regex validation.
Upon saving, the engine instantly re-evaluates and marks the record Valid.
Every edit is permanently recorded in student history and audit logs.""",

    "scene5": """Switching to the AICTE Regulator role provides nationwide oversight.
The dashboard displays aggregate intake capacity and discipline-wise charts.
In the Intake Utilization monitor, BMS College of Engineering is immediately flagged with a red EXCEEDED badge for Computer Science.
In the Anomaly Center, our cross-institutional engine detects a student enrolled in two colleges simultaneously.
Regulators can review conflicting records and escalate for inquiry.""",

    "scene6": """Regulators can export full compliance reports in PDF and CSV with a single click.
Finally, our verification flow allows students to generate time-limited, consent-backed QR tokens.
Verifiers scan the token to verify permitted credentials without exposing Aadhaar or unconsented data.
Our complete prototype code and working implementation are uploaded to GitHub, with the repository link attached for you to view.
APAAR-Connect by Team AuthenIQ brings automated compliance and trust to higher education.
Thank you."""
}

async def generate_single(name, text):
    out_path = f"video_assets/audio/{name}.mp3"
    for attempt in range(4):
        try:
            print(f"Generating {name} (attempt {attempt+1})...")
            comm = edge_tts.Communicate(text, VOICE, rate="+11%")
            await comm.save(out_path)
            print(f"Successfully saved {out_path}")
            return
        except Exception as e:
            print(f"Attempt {attempt+1} failed for {name}: {e}")
            await asyncio.sleep(2.0)
    raise RuntimeError(f"Failed to generate {name} after 4 attempts")

async def main():
    os.makedirs("video_assets/audio", exist_ok=True)
    for name, text in scripts.items():
        await generate_single(name, text)
        await asyncio.sleep(0.8)

    ffmpeg = r"C:\Users\CHETHAN LAKSHMAN\AppData\Local\Programs\Python\Python311\Lib\site-packages\imageio_ffmpeg\binaries\ffmpeg-win-x86_64-v7.1.exe"
    total = 0.0
    print("\n--- Audio Durations ---")
    for f in sorted(glob.glob("video_assets/audio/scene*.mp3")):
        res = subprocess.run([ffmpeg, "-i", f], stderr=subprocess.PIPE, text=True)
        for line in res.stderr.split("\n"):
            if "Duration" in line:
                parts = line.strip().split(",")[0].split("Duration: ")[1].split(":")
                dur = float(parts[0])*3600 + float(parts[1])*60 + float(parts[2])
                total += dur
                print(f"{f}: {dur:.2f}s")
                break

    print(f"\nTOTAL DURATION: {total:.2f}s ({int(total//60)}m {int(total%60):02d}s)")

if __name__ == "__main__":
    asyncio.run(main())
