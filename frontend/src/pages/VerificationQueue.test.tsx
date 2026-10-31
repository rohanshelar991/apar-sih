import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { VerificationQueue } from './VerificationQueue';
import { verificationWorkflowApi } from '../api/client';

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({ user: { role: 'ROLE_AUTHORIZED_VERIFIER' } }),
}));
vi.mock('../api/client', () => ({
  verificationWorkflowApi: {
    queue: vi.fn(),
    history: vi.fn(),
    decide: vi.fn(),
  },
}));

const pending = {
  id: 25,
  studentId: 8,
  studentName: 'Fictional Student',
  maskedApaarId: '20XX XXXX 0012',
  institutionName: 'Example Institute',
  programme: 'Computer Science',
  status: 'PENDING',
  submittedBy: 'admin@example.test',
  submittedAt: '2026-10-30T10:00:00',
};

describe('VerificationQueue', () => {
  beforeEach(() => {
    vi.mocked(verificationWorkflowApi.queue).mockResolvedValue([pending] as never);
    vi.mocked(verificationWorkflowApi.decide).mockResolvedValue({} as never);
  });

  it('renders queued case data and records an approval', async () => {
    render(<VerificationQueue />);
    expect(await screen.findByText('Fictional Student')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Approve record' }));
    await waitFor(() => expect(verificationWorkflowApi.decide).toHaveBeenCalledWith(25, 'APPROVED', undefined));
    expect(verificationWorkflowApi.queue).toHaveBeenCalledTimes(2);
  });

  it('requires a rejection reason before making the API request', async () => {
    render(<VerificationQueue />);
    await screen.findByText('Fictional Student');
    fireEvent.click(screen.getByRole('button', { name: 'Reject' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Add a rejection reason');
    expect(verificationWorkflowApi.decide).not.toHaveBeenCalled();
    fireEvent.change(screen.getByLabelText('Reviewer remarks (required for rejection)'), { target: { value: 'Record mismatch' } });
    fireEvent.click(screen.getByRole('button', { name: 'Reject' }));
    await waitFor(() => expect(verificationWorkflowApi.decide).toHaveBeenCalledWith(25, 'REJECTED', 'Record mismatch'));
  });
});
