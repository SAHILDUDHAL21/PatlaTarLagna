import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Container, Box, Typography, TextField, Button, Alert } from '@mui/material';
import { authService } from '../services/api';

const ResetPassword = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const prefilledEmail = location.state?.email || '';

  const [email, setEmail] = useState(prefilledEmail);
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await authService.resetPassword(email, otp, newPassword);
      setSuccess('Password updated successfully! Redirecting to login.');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Password reset failed. Check OTP.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="xs" sx={{ mt: 10 }}>
      <Box
        className="animate-fade-in"
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          border: '1px solid #000000',
          p: 4,
          backgroundColor: '#ffffff',
        }}
      >
        <Typography variant="h4" component="h1" sx={{ mb: 1, letterSpacing: '0.05em' }}>
          RESET PASSWORD
        </Typography>
        <Typography variant="body2" sx={{ mb: 3, color: '#555555', textAlign: 'center' }}>
          Input OTP code and your new credentials.
        </Typography>

        {error && (
          <Alert severity="error" variant="outlined" sx={{ width: '100%', mb: 2, borderRadius: 0, color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" variant="outlined" sx={{ width: '100%', mb: 2, borderRadius: 0, color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
            {success}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Email Address"
            name="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={loading || !!prefilledEmail}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="otp"
            label="OTP Code"
            id="otp"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            disabled={loading}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="newPassword"
            label="New Password"
            type="password"
            id="newPassword"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            disabled={loading}
            sx={{ mb: 3 }}
          />

          <Button type="submit" fullWidth variant="contained" disabled={loading}>
            {loading ? 'RESETTING...' : 'UPDATE PASSWORD'}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default ResetPassword;
