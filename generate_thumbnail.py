import os
import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1280, 720

# 1. Base Canvas & Deep Tech Gradient Background
img = Image.new("RGBA", (W, H), (11, 19, 43, 255))
draw = ImageDraw.Draw(img)

# Vertical + diagonal gradient
for y in range(H):
    ratio = y / float(H)
    r = int(10 + ratio * 15)
    g = int(18 + ratio * 24)
    b = int(40 + ratio * 45)
    draw.line([(0, y), (W, y)], fill=(r, g, b, 255))

# Subtle tech grid lines
grid_overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
g_draw = ImageDraw.Draw(grid_overlay)
for x in range(0, W, 40):
    g_draw.line([(x, 0), (x, H)], fill=(0, 112, 192, 18), width=1)
for y in range(0, H, 40):
    g_draw.line([(0, y), (W, y)], fill=(0, 112, 192, 18), width=1)

# Glowing radial cyan flare on right side
flare = Image.new("RGBA", (W, H), (0, 0, 0, 0))
f_draw = ImageDraw.Draw(flare)
f_draw.ellipse([700, 80, 1240, 620], fill=(0, 112, 192, 65))
f_draw.ellipse([820, 180, 1120, 480], fill=(0, 210, 255, 45))
flare = flare.filter(ImageFilter.GaussianBlur(radius=60))

img = Image.alpha_composite(img, grid_overlay)
img = Image.alpha_composite(img, flare)

# 2. Right Side Showcase Cards (Actual System Screenshots)
# Load dashboard chart
dash_path = "video_assets/frames/10_regulator_dashboard.png"
if os.path.exists(dash_path):
    dash_img = Image.open(dash_path).convert("RGBA")
    # Crop central chart & cards area
    chart_crop = dash_img.crop((240, 180, 1400, 860))
    chart_crop = chart_crop.resize((580, 340), Image.Resampling.LANCZOS)
    
    # Add rounded corners & border
    mask = Image.new("L", (580, 340), 0)
    m_draw = ImageDraw.Draw(mask)
    m_draw.rounded_rectangle([0, 0, 580, 340], radius=16, fill=255)
    
    # Shadow
    shadow = Image.new("RGBA", (610, 370), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.rounded_rectangle([15, 15, 595, 355], radius=18, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=10))
    
    img.paste(shadow, (660, 95), shadow)
    img.paste(chart_crop, (675, 110), mask)
    
    # Border stroke
    b_draw = ImageDraw.Draw(img)
    b_draw.rounded_rectangle([675, 110, 1255, 450], radius=16, outline=(0, 180, 255, 180), width=2)

# Load QR Verified Card
badge_path = "video_assets/frames/15_verified_badge.png"
if os.path.exists(badge_path):
    badge_img = Image.open(badge_path).convert("RGBA")
    # Crop the verified student card
    card_crop = badge_img.crop((700, 300, 1220, 820))
    card_crop = card_crop.resize((320, 320), Image.Resampling.LANCZOS)
    
    # Mask & shadow
    c_mask = Image.new("L", (320, 320), 0)
    cm_draw = ImageDraw.Draw(c_mask)
    cm_draw.rounded_rectangle([0, 0, 320, 320], radius=18, fill=255)
    
    c_shadow = Image.new("RGBA", (350, 350), (0, 0, 0, 0))
    cs_draw = ImageDraw.Draw(c_shadow)
    cs_draw.rounded_rectangle([15, 15, 335, 335], radius=20, fill=(0, 0, 0, 200))
    c_shadow = c_shadow.filter(ImageFilter.GaussianBlur(radius=12))
    
    img.paste(c_shadow, (635, 350), c_shadow)
    img.paste(card_crop, (650, 365), c_mask)
    
    b_draw = ImageDraw.Draw(img)
    b_draw.rounded_rectangle([650, 365, 970, 685], radius=18, outline=(46, 213, 115, 230), width=3)

# Floating Exceedance Alert Pill
alert_pill = Image.new("RGBA", (330, 80), (0, 0, 0, 0))
ap_draw = ImageDraw.Draw(alert_pill)
ap_draw.rounded_rectangle([0, 0, 330, 80], radius=14, fill=(220, 38, 38, 240), outline=(255, 120, 120, 255), width=2)

try:
    font_alert_h = ImageFont.truetype("segoeuib.ttf", 16)
    font_alert_sub = ImageFont.truetype("segoeuib.ttf", 14)
except:
    font_alert_h = ImageFont.load_default()
    font_alert_sub = font_alert_h

ap_draw.text((20, 14), "INTAKE EXCEEDED: 108.7%", fill=(255, 255, 255), font=font_alert_h)
ap_draw.text((20, 42), "BMSCE Computer Science (261/240)", fill=(255, 220, 220), font=font_alert_sub)

alert_shadow = Image.new("RGBA", (360, 110), (0, 0, 0, 0))
as_draw = ImageDraw.Draw(alert_shadow)
as_draw.rounded_rectangle([15, 15, 345, 95], radius=16, fill=(220, 38, 38, 100))
alert_shadow = alert_shadow.filter(ImageFilter.GaussianBlur(radius=8))

img.paste(alert_shadow, (905, 470), alert_shadow)
img.paste(alert_pill, (920, 485), alert_pill)

# 3. Left Side Typography & Branding
draw = ImageDraw.Draw(img)

# Fonts
try:
    font_pill = ImageFont.truetype("segoeuib.ttf", 15)
    font_giant = ImageFont.truetype("impact.ttf", 76)
    font_sub = ImageFont.truetype("segoeuib.ttf", 22)
    font_badge = ImageFont.truetype("segoeuib.ttf", 15)
    font_team = ImageFont.truetype("impact.ttf", 28)
    font_yt = ImageFont.truetype("segoeuib.ttf", 14)
except:
    font_pill = ImageFont.load_default()
    font_giant = font_pill
    font_sub = font_pill
    font_badge = font_pill
    font_team = font_pill
    font_yt = font_pill

# Top SIH Pill Badge
pill_w = 440
pill_h = 36
draw.rounded_rectangle([60, 45, 60 + pill_w, 45 + pill_h], radius=18, fill=(242, 161, 0, 230), outline=(255, 215, 0), width=1)
draw.text((78, 52), "SMART INDIA HACKATHON 2026 • PS 26263", fill=(15, 23, 42), font=font_pill)

# Giant Headline
draw.text((60, 100), "APAAR-CONNECT", fill=(255, 255, 255), font=font_giant)
# Cyan glow accent under title
draw.text((62, 180), "AICTE STUDENT DATA PLATFORM", fill=(0, 210, 255), font=font_sub)

# Subtitle / Description
draw.text((60, 216), "Automated Intake Compliance & Verification", fill=(180, 205, 235), font=font_sub)

# Feature Badges
badges = [
    (" 9-Rule Automated Validation Engine", (46, 213, 115)),
    (" Real-Time Intake Exceedance Alerts", (255, 75, 75)),
    (" Privacy-Preserving QR Token Verify", (168, 85, 247)),
    (" One-Click AICTE Compliance PDF/CSV", (0, 170, 255))
]

by = 270
for b_text, color in badges:
    # Pill background
    draw.rounded_rectangle([60, by, 510, by + 34], radius=8, fill=(18, 30, 58, 220), outline=(color[0], color[1], color[2], 120), width=1)
    # Indicator dot
    draw.ellipse([75, by + 12, 85, by + 22], fill=color)
    draw.text((96, by + 7), b_text, fill=(240, 245, 255), font=font_badge)
    by += 44

# Bottom Team Branding Box
team_box_y = 465
draw.rounded_rectangle([60, team_box_y, 550, team_box_y + 110], radius=14, fill=(15, 25, 50, 240), outline=(0, 112, 192, 180), width=2)

draw.text((80, team_box_y + 14), "TEAM AUTHENIQ", fill=(242, 161, 0), font=font_team)
draw.text((80, team_box_y + 54), "Full Working Prototype • SIH 2026 Solution", fill=(220, 235, 255), font=font_badge)
draw.text((80, team_box_y + 78), "GitHub Repository Working Code Included", fill=(0, 210, 255), font=font_yt)

# Bottom YouTube Meta Pill
yt_y = 600
draw.rounded_rectangle([60, yt_y, 380, yt_y + 36], radius=18, fill=(220, 38, 38, 230), outline=(255, 100, 100), width=1)
draw.text((80, yt_y + 8), "▶  WATCH FULL DEMO  (02:58 MIN)", fill=(255, 255, 255), font=font_pill)

# Save high-res thumbnails
out_path = "APAAR_Connect_YouTube_Thumbnail.png"
img_rgb = img.convert("RGB")
img_rgb.save(out_path, quality=95)
img_rgb.save("frontend/public/thumbnail.png", quality=95)

# Also save 1080p Full HD version
img_1080p = img_rgb.resize((1920, 1080), Image.Resampling.LANCZOS)
img_1080p.save("APAAR_Connect_YouTube_Thumbnail_1080p.png", quality=95)

print("YouTube Thumbnails generated successfully!")
