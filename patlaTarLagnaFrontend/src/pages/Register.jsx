import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Container, Box, Typography, TextField, Button, Alert, Grid, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { authService } from '../services/api';

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
    age: '',
    gender: 'MALE',
    religion: '',
    caste: '',
    motherTongue: '',
    height: '',
    weight: '',
    education: '',
    occupation: '',
    annualIncome: '',
    maritalStatus: 'NEVER_MARRIED',
    city: '',
    state: '',
    country: 'India',
    aboutMe: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const payload = {
        ...formData,
        age: parseInt(formData.age, 10),
        height: parseFloat(formData.height),
        weight: parseFloat(formData.weight),
        annualIncome: parseFloat(formData.annualIncome),
      };

      await authService.register(payload);
      setSuccess('Account created successfully! Check your email for OTP verification.');
      setTimeout(() => {
        navigate('/verify-email', { state: { email: formData.email } });
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ mt: 5, mb: 10 }}>
      <Box
        className="animate-fade-in"
        sx={{
          border: '1px solid #000000',
          p: 5,
          backgroundColor: '#ffffff',
        }}
      >
        <Typography variant="h3" align="center" sx={{ mb: 1, letterSpacing: '0.05em' }}>
          CREATE PROFILE
        </Typography>
        <Typography variant="body2" align="center" sx={{ mb: 4, color: '#555555' }}>
          Join PatlaTarLagna to discover compatible life partners.
        </Typography>

        {error && (
          <Alert severity="error" variant="outlined" sx={{ mb: 3, borderRadius: 0, color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" variant="outlined" sx={{ mb: 3, borderRadius: 0, color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
            {success}
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <Typography variant="h6" sx={{ borderBottom: '1px solid rgba(0, 0, 0, 0.15)', pb: 1, mb: 3 }}>
            1. Account Details
          </Typography>
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={6}>
              <TextField required fullWidth name="email" label="Email Address" type="email" value={formData.email} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField required fullWidth name="password" label="Password" type="password" value={formData.password} onChange={handleChange} />
            </Grid>
          </Grid>

          <Typography variant="h6" sx={{ borderBottom: '1px solid rgba(0, 0, 0, 0.15)', pb: 1, mb: 3 }}>
            2. Personal details
          </Typography>
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={6}>
              <TextField required fullWidth name="name" label="Full Name" value={formData.name} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField required fullWidth name="age" label="Age" type="number" value={formData.age} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <FormControl fullWidth>
                <InputLabel>Gender</InputLabel>
                <Select name="gender" value={formData.gender} label="Gender" onChange={handleChange}>
                  <MenuItem value="MALE">Male</MenuItem>
                  <MenuItem value="FEMALE">Female</MenuItem>
                  <MenuItem value="OTHER">Other</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="religion" label="Religion" value={formData.religion} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="caste" label="Caste" value={formData.caste} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="motherTongue" label="Mother Tongue" value={formData.motherTongue} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField required fullWidth name="height" label="Height (cm)" type="number" value={formData.height} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField required fullWidth name="weight" label="Weight (kg)" type="number" value={formData.weight} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Marital Status</InputLabel>
                <Select name="maritalStatus" value={formData.maritalStatus} label="Marital Status" onChange={handleChange}>
                  <MenuItem value="NEVER_MARRIED">Never Married</MenuItem>
                  <MenuItem value="DIVORCED">Divorced</MenuItem>
                  <MenuItem value="WIDOWED">Widowed</MenuItem>
                  <MenuItem value="AWAITING_DIVORCE">Awaiting Divorce</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>

          <Typography variant="h6" sx={{ borderBottom: '1px solid rgba(0, 0, 0, 0.15)', pb: 1, mb: 3 }}>
            3. Professional Details
          </Typography>
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="education" label="Highest Education" value={formData.education} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="occupation" label="Occupation" value={formData.occupation} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="annualIncome" label="Annual Income (INR)" type="number" value={formData.annualIncome} onChange={handleChange} />
            </Grid>
          </Grid>

          <Typography variant="h6" sx={{ borderBottom: '1px solid rgba(0, 0, 0, 0.15)', pb: 1, mb: 3 }}>
            4. Location Details & Bio
          </Typography>
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="city" label="City" value={formData.city} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="state" label="State" value={formData.state} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField required fullWidth name="country" label="Country" value={formData.country} onChange={handleChange} />
            </Grid>
            <Grid item xs={12}>
              <TextField required fullWidth multiline rows={4} name="aboutMe" label="About Me / Partner Preferences description" value={formData.aboutMe} onChange={handleChange} />
            </Grid>
          </Grid>

          <Button type="submit" fullWidth variant="contained" disabled={loading} sx={{ py: 1.5, mb: 3 }}>
            {loading ? 'SUBMITTING REGISTRATION...' : 'REGISTER & VERIFY'}
          </Button>

          <Typography align="center" variant="body2" sx={{ color: '#555555' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#000000', textDecoration: 'underline' }}>
              Sign in here
            </Link>
          </Typography>
        </form>
      </Box>
    </Container>
  );
};

export default Register;
