import React, { useEffect, useState } from 'react';
import { Minus, Plus, Sun } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext';

type TextScale = 'compact' | 'standard' | 'large';
const scales: TextScale[] = ['compact', 'standard', 'large'];

export const GovernmentUtilityBar: React.FC = () => {
  const { language, setLanguage, t } = useLanguage();
  const [scale, setScale] = useState<TextScale>(() => {
    const saved = window.localStorage.getItem('apaar-ui-scale');
    return scales.includes(saved as TextScale) ? (saved as TextScale) : 'standard';
  });
  const [brightTheme, setBrightTheme] = useState(() => window.localStorage.getItem('apaar-theme') === 'bright');
  const [today] = useState(() => new Intl.DateTimeFormat('en-IN', {
    day: 'numeric', month: 'long', year: 'numeric', timeZone: 'Asia/Kolkata',
  }).format(new Date()));

  useEffect(() => {
    document.documentElement.dataset.textScale = scale;
    window.localStorage.setItem('apaar-ui-scale', scale);
  }, [scale]);
  useEffect(() => {
    document.documentElement.dataset.theme = brightTheme ? 'bright' : 'standard';
    window.localStorage.setItem('apaar-theme', brightTheme ? 'bright' : 'standard');
  }, [brightTheme]);

  const changeScale = (direction: -1 | 1) => setScale((current) =>
    scales[Math.max(0, Math.min(scales.length - 1, scales.indexOf(current) + direction))]);

  return (
    <>
      <a className="skip-to-content" href="#main-content">{t('skip')}</a>
      <div className="govt-utility-bar no-print">
        <div className="govt-utility-inner">
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="font-semibold">APAAR GARUDA NETRA</span>
            <span className="hidden sm:inline text-slate-400" aria-hidden="true">|</span>
            <span className="text-slate-600">{t('serviceDescription')}</span>
          </div>
          <div className="flex items-center gap-3 sm:gap-4">
            <span className="hidden md:inline text-slate-600">{today}</span>
            <div className="flex items-center gap-1" role="group" aria-label="Text size controls">
              <span className="hidden sm:inline mr-1 text-slate-600">{t('textSize')}</span>
              <button className="govt-utility-button" type="button" onClick={() => changeScale(-1)} aria-label={t('decreaseText')} title={t('decreaseText')}><Minus className="h-3.5 w-3.5" /></button>
              <button className="govt-utility-button" type="button" onClick={() => setScale('standard')} aria-label={t('resetText')} title={t('resetText')}>A</button>
              <button className="govt-utility-button" type="button" onClick={() => changeScale(1)} aria-label={t('increaseText')} title={t('increaseText')}><Plus className="h-3.5 w-3.5" /></button>
            </div>
            <label className="sr-only" htmlFor="language-select">{t('language')}</label>
            <select id="language-select" className="govt-language-select" value={language} onChange={(event) => setLanguage(event.target.value as 'en' | 'hi' | 'mr')} aria-label={t('language')}>
              <option value="en">English</option><option value="hi">हिन्दी</option><option value="mr">मराठी</option>
            </select>
            <button className={`govt-utility-button inline-flex items-center gap-1.5 ${brightTheme ? 'is-active' : ''}`} type="button" aria-pressed={brightTheme} onClick={() => setBrightTheme((value) => !value)} title={t('brightTheme')}>
              <Sun className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">{t('brightTheme')}</span>
            </button>
          </div>
        </div>
      </div>
      <div className="govt-tricolour-rule" aria-hidden="true"><span /><span /><span /></div>
    </>
  );
};
