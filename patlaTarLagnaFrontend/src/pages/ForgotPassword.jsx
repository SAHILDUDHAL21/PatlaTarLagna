import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Box, Typography, TextField, Button, Alert, Link as MuiLink } from '@mui/material';
import { authService } from '../services/api';

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await authService.forgotPassword(email);
      setSuccess('OTP verification code has been dispatched to your email.');
      setTimeout(() => {
        navigate('/reset-password', { state: { email } });
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to dispatch reset request.');
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
          FORGOT PASSWORD
        </Typography>
        <Typography variant="body2" sx={{ mb: 3, color: '#555555', textAlign: 'center' }}>
          Enter your email to receive a recovery code.
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
            disabled={loading}
            sx={{ mb: 3 }}
          />

          <Button type="submit" fullWidth variant="contained" disabled={loading} sx={{ mb: 2 }}>
            {loading ? 'REQUESTING...' : 'DISPATCH CODE'}
          </Button>

          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <MuiLink component={Link} to="/login" variant="body2" sx={{ color: '#555555', '&:hover': { color: '#000000' } }}>
              Back to Login
            </MuiLink>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};

export default ForgotPassword;
