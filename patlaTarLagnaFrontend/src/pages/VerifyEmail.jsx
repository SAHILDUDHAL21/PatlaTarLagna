import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Container, Box, Typography, TextField, Button, Alert } from '@mui/material';
import { authService } from '../services/api';

const VerifyEmail = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const prefilledEmail = location.state?.email || '';

  const [email, setEmail] = useState(prefilledEmail);
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await authService.verifyEmail(email, otp);
      setSuccess('Email verified successfully! You can now log in.');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'OTP verification failed. Check code.');
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
        <Typography variant="h3" component="h1" sx={{ mb: 1, letterSpacing: '0.05em' }}>
          VERIFY EMAIL
        </Typography>
        <Typography variant="body2" sx={{ mb: 3, color: '#555555', textAlign: 'center' }}>
          Enter the 6-digit OTP code sent to your registered email.
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
            label="OTP Verification Code"
            id="otp"
            autoFocus
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            disabled={loading}
            sx={{ mb: 3 }}
          />

          <Button type="submit" fullWidth variant="contained" disabled={loading}>
            {loading ? 'VERIFYING...' : 'VERIFY EMAIL'}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default VerifyEmail;
