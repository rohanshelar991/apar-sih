import os
import cv2
import math
import numpy as np
import subprocess
from PIL import Image, ImageDraw, ImageFont

FFMPEG_EXE = r"C:\Users\CHETHAN LAKSHMAN\AppData\Local\Programs\Python\Python311\Lib\site-packages\imageio_ffmpeg\binaries\ffmpeg-win-x86_64-v7.1.exe"
AUDIO_PATH = "video_assets/full_voiceover.mp3"
OUTPUT_VIDEO = "APAAR_Connect_Demo_Video.mp4"

WIDTH = 1920
HEIGHT = 1080
FPS = 25

# Load state screenshots
frames_dir = "video_assets/frames"
def load_img(name):
    p = os.path.join(frames_dir, name)
    if not os.path.exists(p):
        print(f"Warning: {p} not found, using blank frame")
        return np.ones((HEIGHT, WIDTH, 3), dtype=np.uint8) * 240
    img = cv2.imread(p)
    if img.shape[0] != HEIGHT or img.shape[1] != WIDTH:
        img = cv2.resize(img, (WIDTH, HEIGHT), interpolation=cv2.INTER_AREA)
    return img

states = {
    "login": load_img("01_login.png"),
    "login_badge": load_img("01b_login_badge.png"),
    "inst_overview": load_img("02_inst_overview.png"),
    "upload_page": load_img("03_upload_page.png"),
    "upload_preview": load_img("04_upload_preview.png"),
    "upload_progress": load_img("05_upload_progress.png"),
    "validation_issues": load_img("06_validation_issues.png"),
    "issue_expanded": load_img("07_issue_expanded.png"),
    "edit_modal": load_img("08_edit_modal.png"),
    "regulator_dash": load_img("10_regulator_dashboard.png"),
    "regulator_exceeded": load_img("10b_regulator_exceeded.png"),
    "anomaly_center": load_img("11_anomaly_center.png"),
    "reports": load_img("12_regulatory_reports.png"),
    "audit_logs": load_img("13_audit_logs.png"),
    "verifier_portal": load_img("14_verifier_portal.png"),
    "verified_badge": load_img("15_verified_badge.png")
}

# Load authentic Windows 11 cursor
cursor_img = cv2.imread("video_assets/win11_cursor.png", cv2.IMREAD_UNCHANGED)
if cursor_img is None or cursor_img.shape[2] != 4:
    # Fallback to white RGBA cursor
    cursor_img = np.zeros((32, 32, 4), dtype=np.uint8)
    cursor_img[:, :, :3] = 255
    cursor_img[:, :, 3] = 255

cursor_h, cursor_w = cursor_img.shape[:2]
cursor_rgb = cursor_img[:, :, :3].astype(np.float32)
cursor_alpha = (cursor_img[:, :, 3].astype(np.float32) / 255.0)[:, :, np.newaxis]

# Scene Durations:
# scene1: 27.74s (0.00 to 27.74)
# scene2: 22.97s (27.74 to 50.71)
# scene3: 34.66s (50.71 to 85.37)
# scene4: 28.20s (85.37 to 113.57)
# scene5: 31.92s (113.57 to 145.49)
# scene6: 33.26s (145.49 to 178.75)
TOTAL_DURATION = 178.75
TOTAL_FRAMES = int(TOTAL_DURATION * FPS)

chapters = [
    (0.00, 27.74, "1. LOGIN & ROLE-BASED ACCESS", "BMS College of Engineering Demo Login"),
    (27.74, 50.71, "2. INSTITUTION INTAKE MONITORING", "Enrolment Health & 95.8% B.Tech CSE Capacity"),
    (50.71, 85.37, "3. 9-RULE VALIDATION ENGINE", "Batch Processing: 15 Exceptions & Intake Exceedance"),
    (85.37, 113.57, "4. INLINE CORRECTION WORKFLOW", "Audit-Logged Resolution & Real-Time Revalidation"),
    (113.57, 145.49, "5. AICTE REGULATOR DASHBOARD", "Discipline Analytics, Exceeded Warnings & Anomaly Center"),
    (145.49, 178.75, "6. REGULATORY REPORTS & QR VERIFICATION", "One-Click Compliance Export, QR Flow & GitHub Code Link")
]

subtitles = [
    (0.0, 7.5, "Welcome. Team AuthenIQ presents APAAR GARUDA NETRA for Smart India Hackathon 2026, Problem Statement 26263."),
    (7.5, 14.5, "Our prototype uses a simulated APAAR verification adapter with synthetic data, as shown in the footer."),
    (14.5, 21.0, "The system provides role-based access for institutions, regulators, and verifiers."),
    (21.0, 27.74, "Let us log in as BMS College of Engineering (INST001)."),
    
    (27.74, 35.0, "In the Institution portal, real-time KPI cards display enrolment health and intake utilization."),
    (35.0, 43.0, "Currently, B.Tech Computer Science has 230 students enrolled against 240 approved seats—operating at 95.8% capacity."),
    (43.0, 50.71, "Let us now upload a new batch of 60 admission records."),
    
    (50.71, 57.5, "Institutions upload student records via CSV."),
    (57.5, 64.5, "PapaParse immediately provides a client-side preview, verifying required columns."),
    (64.5, 71.5, "Submitting triggers our asynchronous 9-rule validation engine."),
    (71.5, 78.5, "In seconds, the engine flags 15 anomalies across the batch, including in-file duplicates and invalid formats."),
    (78.5, 85.37, "Crucially, adding these 31 CSE records pushes enrolment to 261 students, triggering an INTAKE_EXCEEDED violation at 108.7% capacity."),
    
    (85.37, 92.5, "Instead of rejecting entire batches, APAAR GARUDA NETRA enables inline corrections."),
    (92.5, 99.5, "For student Rahul Sharma, flagged with an invalid format, the admin clicks Edit Record."),
    (99.5, 106.0, "We enter the corrected 12-digit APAAR ID with live regex validation."),
    (106.0, 113.57, "Upon saving, the engine instantly re-evaluates and marks the record Valid. Every edit is logged in the audit trail."),
    
    (113.57, 120.5, "Switching to the AICTE Regulator role provides nationwide oversight."),
    (120.5, 128.0, "The dashboard displays aggregate intake capacity and discipline-wise charts across all approved institutions."),
    (128.0, 135.5, "In the Intake Utilization monitor, BMS College of Engineering is immediately flagged with a red EXCEEDED badge for Computer Science."),
    (135.5, 145.49, "In the Anomaly Center, our cross-institutional engine detects a student enrolled in two colleges simultaneously. Regulators can escalate for inquiry."),
    
    (145.49, 153.0, "Regulators can export full compliance reports in PDF and CSV with a single click."),
    (153.0, 160.5, "Finally, our verification flow allows students to generate time-limited, consent-backed QR tokens."),
    (160.5, 167.5, "Verifiers scan the token to verify permitted credentials without exposing Aadhaar or unconsented data."),
    (167.5, 178.75, "Our complete prototype working is uploaded to GitHub, with the repository link attached for you to view. APAAR GARUDA NETRA by Team AuthenIQ brings automated compliance and trust to higher education. Thank you.")
]

# State switch intervals: (start_t, end_t, state_key)
state_intervals = [
    (0.00, 7.50, "login"),
    (7.50, 14.50, "login_badge"),
    (14.50, 27.74, "login"),
    (27.74, 50.71, "inst_overview"),
    (50.71, 57.50, "upload_page"),
    (57.50, 64.50, "upload_preview"),
    (64.50, 71.50, "upload_progress"),
    (71.50, 78.50, "validation_issues"),
    (78.50, 85.37, "issue_expanded"),
    (85.37, 92.50, "validation_issues"),
    (92.50, 105.00, "edit_modal"),
    (105.00, 113.57, "validation_issues"),
    (113.57, 128.00, "regulator_dash"),
    (128.00, 135.50, "regulator_exceeded"),
    (135.50, 145.49, "anomaly_center"),
    (145.49, 153.00, "reports"),
    (153.00, 162.00, "verifier_portal"),
    (162.00, 178.75, "verified_badge")
]

# Realistic Human Waypoint Script (timestamp, target_x, target_y, is_click)
# Represents continuous, living human hand movements with pauses, reading gestures, and authentic clicks
human_waypoints = [
    # Scene 1: Login & Intro
    (0.0, 850, 220, False),         # Hovering near title
    (3.5, 870, 230, False),         # Tiny hand drift
    (5.0, 960, 960, False),         # Moving down towards disclaimer badge
    (7.5, 960, 960, False),         # Reading badge
    (12.0, 975, 965, False),        # Hovering on badge
    (14.5, 650, 480, False),        # Moving up to demo cards
    (16.5, 340, 480, False),        # Hovering over AICTE Regulator card
    (18.5, 480, 480, False),        # Hovering over Verifier card
    (20.5, 560, 480, False),        # Settling on BMSCE card
    (23.5, 560, 480, True),         # CLICK BMSCE card!
    (25.0, 562, 482, False),        # Post-click dwell
    (27.74, 562, 482, False),
    
    # Scene 2: Institution Overview (Pointing out numbers like a human presenter)
    (29.0, 280, 240, False),        # Gesturing to Total Students card (372)
    (31.5, 500, 240, False),        # Gesturing to Valid card (367)
    (33.5, 940, 240, False),        # Gesturing to Intake Utilization (95.8%)
    (36.0, 750, 460, False),        # Moving down to B.Tech CSE row
    (39.0, 820, 460, False),        # Reading 230 / 240 seats
    (42.0, 750, 460, False),        # Small hover
    (45.0, 120, 260, False),        # Moving to sidebar "Upload Batch"
    (48.5, 120, 260, False),        # Hovering on button
    (49.2, 120, 260, True),         # CLICK Upload Batch!
    (50.71, 122, 262, False),
    
    # Scene 3: Bulk Upload & 9-Rule Engine
    (52.5, 960, 440, False),        # Centering on dropzone
    (55.0, 965, 445, False),        # Small hover
    (57.5, 620, 420, False),        # Moving to preview table: APAAR ID column
    (60.0, 840, 420, False),        # Moving to Name / DOB
    (62.5, 1100, 420, False),       # Moving to Programme & Category
    (64.5, 1450, 680, False),       # Moving to "Process Batch" button
    (65.5, 1450, 680, True),        # CLICK Process Batch!
    (67.5, 960, 400, False),        # Moving up to progress bar
    (70.0, 960, 400, False),        # Reading "15 issues detected"
    (73.0, 780, 370, False),        # Moving to first violation row
    (76.5, 780, 370, False),        # Reading rule description
    (79.5, 780, 370, True),         # CLICK row to expand!
    (81.5, 860, 460, False),        # Hovering on INTAKE_EXCEEDED (108.7%)
    (84.0, 860, 520, False),        # Hovering on DUPLICATE_IN_FILE
    (85.37, 860, 520, False),
    
    # Scene 4: Inline Record Correction
    (87.5, 1540, 370, False),       # Moving across to "Edit Record" button
    (91.5, 1540, 370, False),       # Hovering on Edit button
    (93.0, 1540, 370, True),        # CLICK Edit Record!
    (94.5, 960, 470, False),        # Moving into APAAR ID input box
    (95.5, 960, 470, True),         # CLICK input field!
    (98.0, 1020, 470, False),       # Simulating typing correction (9901 2345 6789)
    (101.5, 1020, 470, False),      # Seeing live regex check turn green
    (103.5, 1150, 680, False),      # Moving down to "Save & Revalidate" button
    (104.5, 1150, 680, True),       # CLICK Save & Revalidate!
    (107.0, 780, 370, False),       # Returning to table, seeing green "Valid" chip
    (110.5, 780, 370, False),       # Dwell on resolved state
    (113.57, 780, 370, False),
    
    # Scene 5: Regulator Dashboard & Anomaly Center
    (115.0, 280, 200, False),       # Moving to Regulator KPIs: 10 Institutions
    (117.5, 580, 200, False),       # 11,200 Approved Seats
    (120.0, 880, 200, False),       # 3,663 Enrolments
    (123.0, 650, 480, False),       # Moving over discipline bar chart (CSE, ECE, ME)
    (126.0, 720, 480, False),       # Chart inspection
    (129.0, 1200, 480, False),      # Moving to BMSCE red EXCEEDED (108.7%) badge
    (132.5, 1200, 480, False),      # Hovering on exceedance badge
    (134.5, 120, 335, False),       # Moving to sidebar "Anomaly Center"
    (135.5, 120, 335, True),        # CLICK Anomaly Center!
    (138.0, 960, 360, False),       # Looking at DUPLICATE_CROSS_INSTITUTION conflict
    (141.0, 1100, 360, False),      # Hovering over conflict details
    (142.5, 1480, 360, False),      # Moving to "Escalate" button
    (143.2, 1480, 360, True),       # CLICK Escalate!
    (145.49, 1482, 362, False),
    
    # Scene 6: Reports, QR Verification & GitHub Close
    (146.5, 120, 375, False),       # Moving to Reports menu
    (147.2, 120, 375, True),        # CLICK Reports!
    (148.5, 520, 340, False),       # Moving to "Download Summary Report PDF"
    (149.2, 520, 340, True),        # CLICK Download PDF!
    (152.0, 120, 415, False),       # Moving to Verifier link
    (154.5, 960, 520, False),       # Moving to Verified Student Card
    (157.5, 880, 520, False),       # Pointing to QR Code
    (160.5, 1040, 520, False),      # Pointing to Masked APAAR ID (12XX XXXX 4567)
    (164.0, 960, 560, False),       # Hovering on green "Verified" checkmark
    (168.0, 960, 600, False),       # Gentle resting motion
    (174.0, 965, 605, False),       # Natural breathing tremor
    (178.75, 965, 605, False)
]

def min_jerk(tau):
    """Biologically validated minimum-jerk human trajectory curve"""
    tau = min(1.0, max(0.0, tau))
    return 10.0 * (tau**3) - 15.0 * (tau**4) + 6.0 * (tau**5)

def get_human_cursor(t):
    """Computes continuous, natural human cursor position with wrist arcs and physiological tremor"""
    # Find current waypoint segment
    w_idx = 0
    for i in range(len(human_waypoints) - 1):
        if human_waypoints[i][0] <= t < human_waypoints[i+1][0]:
            w_idx = i
            break
        elif t >= human_waypoints[-1][0]:
            w_idx = len(human_waypoints) - 2

    t0, x0, y0, _ = human_waypoints[w_idx]
    t1, x1, y1, _ = human_waypoints[w_idx + 1]

    dt = max(0.001, t1 - t0)
    tau = (t - t0) / dt
    s = min_jerk(tau)

    dx = x1 - x0
    dy = y1 - y0
    dist = math.hypot(dx, dy)

    # Biological wrist arc (humans naturally curve perpendicular to trajectory)
    arc_x = 0.0
    arc_y = 0.0
    if dist > 30.0:
        drift_amp = min(35.0, dist * 0.09)
        nx = -dy / dist
        ny = dx / dist
        arc = drift_amp * math.sin(math.pi * s)
        arc_x = nx * arc
        arc_y = ny * arc

    # Physiological tremor (0.4–0.7px continuous micro-motion)
    tremor_x = 0.55 * math.sin(t * 11.3) + 0.35 * math.cos(t * 17.7)
    tremor_y = 0.50 * math.cos(t * 13.1) + 0.30 * math.sin(t * 19.3)

    cx = int(x0 + dx * s + arc_x + tremor_x)
    cy = int(y0 + dy * s + arc_y + tremor_y)

    # Check click ripple
    click_ring = 0
    for (wt, wx, wy, is_click) in human_waypoints:
        if is_click and 0 <= (t - wt) < 0.40:
            delta = t - wt
            click_ring = int(6 + delta * 65)
            break

    return cx, cy, click_ring

def overlay_cursor_alpha(frame, x, y, click_ring=0):
    """Blends the authentic Windows 11 cursor with subpixel alpha and drop shadow"""
    # Draw soft, native OS click pulse if active
    if click_ring > 0:
        cv2.circle(frame, (x, y), click_ring, (255, 185, 0), 2, cv2.LINE_AA)
        cv2.circle(frame, (x, y), max(1, click_ring - 4), (255, 235, 120), 1, cv2.LINE_AA)

    # Cursor boundary clipping
    x1, y1 = max(0, x), max(0, y)
    x2, y2 = min(WIDTH, x + cursor_w), min(HEIGHT, y + cursor_h)

    c_x1 = x1 - x
    c_y1 = y1 - y
    c_x2 = c_x1 + (x2 - x1)
    c_y2 = c_y1 + (y2 - y1)

    if x2 > x1 and y2 > y1:
        roi = frame[y1:y2, x1:x2].astype(np.float32)
        c_sub_rgb = cursor_rgb[c_y1:c_y2, c_x1:c_x2]
        c_sub_a = cursor_alpha[c_y1:c_y2, c_x1:c_x2]

        blended = roi * (1.0 - c_sub_a) + c_sub_rgb * c_sub_a
        frame[y1:y2, x1:x2] = blended.astype(np.uint8)

def draw_hud(img, t):
    curr_ch = "APAAR GARUDA NETRA • SMART INDIA HACKATHON 2026"
    sub_ch = "Team AuthenIQ • Problem Statement 26263"
    for start, end, ch_title, ch_sub in chapters:
        if start <= t < end:
            curr_ch = ch_title
            sub_ch = ch_sub
            break

    curr_sub = ""
    for start, end, text in subtitles:
        if start <= t < end:
            curr_sub = text
            break

    hud_h = 100
    hud_y = HEIGHT - hud_h - 18
    hud_w = WIDTH - 80
    hud_x = 40

    overlay = img.copy()
    cv2.rectangle(overlay, (hud_x, hud_y), (hud_x + hud_w, hud_y + hud_h), (16, 22, 34), -1)
    cv2.rectangle(overlay, (hud_x, hud_y), (hud_x + hud_w, hud_y + hud_h), (55, 75, 110), 2)
    cv2.rectangle(overlay, (hud_x, hud_y), (hud_x + 8, hud_y + hud_h), (242, 161, 0), -1)
    cv2.addWeighted(overlay, 0.92, img, 0.08, 0, img)

    pil_img = Image.fromarray(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    draw = ImageDraw.Draw(pil_img)

    try:
        font_title = ImageFont.truetype("arialbd.ttf", 20)
        font_sub = ImageFont.truetype("arial.ttf", 15)
        font_caption = ImageFont.truetype("arialbd.ttf", 18)
    except:
        font_title = ImageFont.load_default()
        font_sub = font_title
        font_caption = font_title

    draw.text((hud_x + 24, hud_y + 12), curr_ch, fill=(242, 161, 0), font=font_title)
    draw.text((hud_x + 24, hud_y + 38), sub_ch, fill=(160, 185, 220), font=font_sub)

    if curr_sub:
        draw.text((hud_x + 24, hud_y + 64), f"\" {curr_sub} \"", fill=(255, 255, 255), font=font_caption)

    badge_text = "SIH 2026 | Team AuthenIQ"
    draw.text((hud_x + hud_w - 240, hud_y + 14), badge_text, fill=(200, 220, 255), font=font_sub)
    timer_text = f"{int(t//60):02d}:{int(t%60):02d} / 02:58"
    draw.text((hud_x + hud_w - 180, hud_y + 38), timer_text, fill=(255, 255, 255), font=font_title)

    return cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

def main():
    print(f"Starting video rendering for Team AuthenIQ to {OUTPUT_VIDEO}...")
    print(f"Total duration: {TOTAL_DURATION}s | Frames: {TOTAL_FRAMES} | FPS: {FPS}")

    cmd = [
        FFMPEG_EXE, "-y",
        "-f", "rawvideo",
        "-vcodec", "rawvideo",
        "-s", f"{WIDTH}x{HEIGHT}",
        "-pix_fmt", "bgr24",
        "-r", str(FPS),
        "-i", "-",
        "-i", AUDIO_PATH,
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-preset", "fast",
        "-crf", "19",
        "-c:a", "aac",
        "-b:a", "192k",
        "-shortest",
        OUTPUT_VIDEO
    ]

    proc = subprocess.Popen(cmd, stdin=subprocess.PIPE, stderr=subprocess.DEVNULL)

    for f_idx in range(TOTAL_FRAMES):
        t = f_idx / float(FPS)

        # Active state image
        active_state = "login"
        for s_start, s_end, s_key in state_intervals:
            if s_start <= t < s_end:
                active_state = s_key
                break

        base_img = states.get(active_state, states["login"]).copy()

        # Compute continuous human cursor position & clicks
        cx, cy, click_ring = get_human_cursor(t)

        # Alpha-blend authentic Windows 11 cursor
        overlay_cursor_alpha(base_img, cx, cy, click_ring)

        # Draw HUD overlays and subtitles
        final_frame = draw_hud(base_img, t)

        proc.stdin.write(final_frame.tobytes())

        if f_idx % 250 == 0:
            print(f"Rendered {f_idx}/{TOTAL_FRAMES} frames ({t:.1f}s / {TOTAL_DURATION:.1f}s)...")

    proc.stdin.close()
    proc.wait()
    print("DEMONSTRATION VIDEO RENDERING FOR TEAM AUTHENIQ COMPLETE!")

if __name__ == "__main__":
    main()
