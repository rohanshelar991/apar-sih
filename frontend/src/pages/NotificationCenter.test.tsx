import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationCenter } from './NotificationCenter';
import { verificationWorkflowApi } from '../api/client';

vi.mock('../api/client', () => ({
  verificationWorkflowApi: {
    notifications: vi.fn(),
    markRead: vi.fn(),
  },
}));

describe('NotificationCenter', () => {
  beforeEach(() => {
    vi.mocked(verificationWorkflowApi.notifications).mockResolvedValue([{
      id: 31,
      title: 'Verification approved',
      message: 'Request #25 was approved.',
      type: 'VERIFICATION_APPROVED',
      createdAt: '2026-10-30T10:00:00',
    }] as never);
    vi.mocked(verificationWorkflowApi.markRead).mockResolvedValue({} as never);
  });

  it('loads the signed-in notification and marks it read', async () => {
    render(<NotificationCenter />);
    expect(await screen.findByText('Verification approved')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Mark as read' }));
    await waitFor(() => expect(verificationWorkflowApi.markRead).toHaveBeenCalledWith(31));
    expect(screen.queryByRole('button', { name: 'Mark as read' })).not.toBeInTheDocument();
  });
});
