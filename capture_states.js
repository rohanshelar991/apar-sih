const puppeteer = require('./frontend/node_modules/puppeteer-core');
const path = require('path');
const os = require('os');
const fs = require('fs');

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

(async () => {
  const outputDir = path.join(__dirname, 'video_assets', 'frames');
  fs.mkdirSync(outputDir, { recursive: true });

  const profileDir = path.join(os.tmpdir(), 'edge_capture_' + Date.now());
  fs.mkdirSync(profileDir, { recursive: true });

  console.log('Launching Edge browser at 1920x1080...');
  const browser = await puppeteer.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    headless: 'new',
    userDataDir: profileDir,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1920,1080', '--disable-gpu']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1920, height: 1080, deviceScaleFactor: 1 });

  // 1. Login Page
  console.log('1. Loading Login Page...');
  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
  await sleep(1200);
  await page.screenshot({ path: path.join(outputDir, '01_login.png') });

  // 1b. Scroll to Prototype Badge
  console.log('1b. Capturing footer disclaimer badge...');
  await page.evaluate(() => window.scrollTo({ top: 300, behavior: 'smooth' }));
  await sleep(600);
  await page.screenshot({ path: path.join(outputDir, '01b_login_badge.png') });
  await page.evaluate(() => window.scrollTo({ top: 0, behavior: 'smooth' }));
  await sleep(400);

  // 2. Click Demo BMSCE Card
  console.log('2. Clicking BMS College Demo card...');
  await page.evaluate(() => {
    const btns = Array.from(document.querySelectorAll('button'));
    const btn = btns.find(b => b.textContent && (b.textContent.includes('BMS College') || b.textContent.includes('INST001')));
    if (btn) btn.click();
  });

  await sleep(2500);

  // 3. Institution Overview Dashboard
  console.log('3. Capturing Institution Overview...');
  await page.screenshot({ path: path.join(outputDir, '02_inst_overview.png') });

  // 4. Navigate to Upload Students
  console.log('4. Navigating to Upload page...');
  await page.goto('http://localhost:5173/institution/upload', { waitUntil: 'networkidle2' });
  await sleep(1500);
  await page.screenshot({ path: path.join(outputDir, '03_upload_page.png') });

  // 5. Select sample CSV file
  console.log('5. Selecting sample CSV...');
  const sampleCsvPath = path.resolve(__dirname, 'sample-uploads', 'new_admissions_INST001.csv');
  const fileInput = await page.$('input[type="file"]');
  if (fileInput) {
    await fileInput.uploadFile(sampleCsvPath);
    await sleep(1500);
    await page.screenshot({ path: path.join(outputDir, '04_upload_preview.png') });

    // Submit batch
    console.log('Submitting batch...');
    await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button'));
      const btn = btns.find(b => b.textContent && (b.textContent.includes('Process') || b.textContent.includes('Submit') || b.textContent.includes('Upload')));
      if (btn) btn.click();
    });

    await sleep(1200);
    await page.screenshot({ path: path.join(outputDir, '05_upload_progress.png') });
    await sleep(2500);
  }

  // 6. Validation Issues Page
  console.log('6. Navigating to Validation Issues...');
  await page.goto('http://localhost:5173/institution/issues', { waitUntil: 'networkidle2' });
  await sleep(2000);
  await page.screenshot({ path: path.join(outputDir, '06_validation_issues.png') });

  // 7. Expand first issue
  console.log('7. Expanding violation row...');
  await page.evaluate(() => {
    const row = document.querySelector('table tbody tr');
    if (row) row.click();
  });
  await sleep(800);
  await page.screenshot({ path: path.join(outputDir, '07_issue_expanded.png') });

  // 8. Open Edit Record Modal
  console.log('8. Opening inline edit modal...');
  await page.evaluate(() => {
    const btns = Array.from(document.querySelectorAll('button'));
    const btn = btns.find(b => b.textContent && b.textContent.includes('Edit'));
    if (btn) btn.click();
  });
  await sleep(1200);
  await page.screenshot({ path: path.join(outputDir, '08_edit_modal.png') });

  // Type corrected APAAR ID
  const apaarInput = await page.$('input[placeholder*="APAAR"], input[name="apaarId"], input[type="text"]');
  if (apaarInput) {
    await apaarInput.click({ clickCount: 3 });
    await apaarInput.type('9901 2345 6789', { delay: 40 });
    await sleep(600);
    await page.screenshot({ path: path.join(outputDir, '08b_edit_corrected.png') });

    // Click Save
    await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button'));
      const btn = btns.find(b => b.textContent && (b.textContent.includes('Save') || b.textContent.includes('Update') || b.textContent.includes('Revalidate')));
      if (btn) btn.click();
    });
    await sleep(1500);
    await page.screenshot({ path: path.join(outputDir, '09_issue_resolved.png') });
  }

  // 9. Switch to AICTE Regulator
  console.log('9. Switching to AICTE Regulator...');
  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
  await sleep(1000);
  
  await page.evaluate(() => {
    const btns = Array.from(document.querySelectorAll('button'));
    const btn = btns.find(b => b.textContent && (b.textContent.includes('Regulator') || b.textContent.includes('AICTE')));
    if (btn) btn.click();
  });
  await sleep(2500);

  // 10. Regulator Dashboard
  console.log('10. Capturing Regulator Dashboard...');
  await page.screenshot({ path: path.join(outputDir, '10_regulator_dashboard.png') });

  // Scroll to intake utilization table
  await page.evaluate(() => window.scrollTo({ top: 400, behavior: 'smooth' }));
  await sleep(800);
  await page.screenshot({ path: path.join(outputDir, '10b_regulator_exceeded.png') });
  await page.evaluate(() => window.scrollTo({ top: 0, behavior: 'smooth' }));

  // 11. Anomaly Center
  console.log('11. Capturing Anomaly Center...');
  await page.goto('http://localhost:5173/regulator/anomalies', { waitUntil: 'networkidle2' });
  await sleep(2000);
  await page.screenshot({ path: path.join(outputDir, '11_anomaly_center.png') });

  // 12. Reports Page
  console.log('12. Capturing Reports Page...');
  await page.goto('http://localhost:5173/regulator/reports', { waitUntil: 'networkidle2' });
  await sleep(1500);
  await page.screenshot({ path: path.join(outputDir, '12_regulatory_reports.png') });

  // 13. Audit Logs
  console.log('13. Capturing Audit Logs...');
  await page.goto('http://localhost:5173/regulator/audit-logs', { waitUntil: 'networkidle2' });
  await sleep(1500);
  await page.screenshot({ path: path.join(outputDir, '13_audit_logs.png') });

  // 14. Verifier Portal
  console.log('14. Capturing Verifier Portal...');
  await page.goto('http://localhost:5173/verify', { waitUntil: 'networkidle2' });
  await sleep(1500);
  await page.screenshot({ path: path.join(outputDir, '14_verifier_portal.png') });

  // 15. Verify with Demo Student
  console.log('15. Verifying Student Record...');
  // Find a verified token or open first student
  await page.goto('http://localhost:5173/verify/demo-token-1', { waitUntil: 'networkidle2' });
  await sleep(1500);
  await page.screenshot({ path: path.join(outputDir, '15_verified_badge.png') });

  await browser.close();
  fs.rmSync(profileDir, { recursive: true, force: true });
  console.log('ALL SCREENSHOTS CAPTURED SUCCESSFULLY!');
})();
