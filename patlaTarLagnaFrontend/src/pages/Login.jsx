import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Container, Box, Typography, TextField, Button, Alert, Link as MuiLink } from '@mui/material';
import { authService } from '../services/api';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const res = await authService.login(email, password);
      setSuccess('Login successful!');
      setTimeout(() => {
        navigate('/');
      }, 1000);
    } catch (err) {
      setError(
        err.response?.data?.message || 'Login failed. Please verify credentials.'
      );
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
        <Typography
          variant="h3"
          component="h1"
          sx={{ mb: 1, letterSpacing: '0.05em' }}
        >
          LOG IN
        </Typography>
        <Typography
          variant="body2"
          sx={{ mb: 3, color: '#555555', textAlign: 'center' }}
        >
          Enter credentials to access your matrimonial profile.
        </Typography>

        {error && (
          <Alert
            severity="error"
            variant="outlined"
            sx={{
              width: '100%',
              mb: 2,
              borderRadius: 0,
              color: '#000000',
              borderColor: '#000000',
              '& .MuiAlert-icon': { color: '#000000' },
            }}
          >
            {error}
          </Alert>
        )}

        {success && (
          <Alert
            severity="success"
            variant="outlined"
            sx={{
              width: '100%',
              mb: 2,
              borderRadius: 0,
              color: '#000000',
              borderColor: '#000000',
              '& .MuiAlert-icon': { color: '#000000' },
            }}
          >
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
            autoComplete="email"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={loading}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
            sx={{ mb: 3 }}
          />

          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={loading}
            sx={{ mb: 2 }}
          >
            {loading ? 'AUTHENTICATING...' : 'SIGN IN'}
          </Button>

          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
            <MuiLink
              component={Link}
              to="/forgot-password"
              variant="body2"
              sx={{ color: '#555555', '&:hover': { color: '#000000' } }}
            >
              Forgot Password?
            </MuiLink>
            <MuiLink
              component={Link}
              to="/register"
              variant="body2"
              sx={{ color: '#555555', '&:hover': { color: '#000000' } }}
            >
              Register Account
            </MuiLink>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};

export default Login;
