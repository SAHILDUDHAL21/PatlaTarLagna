import React, { useState, useEffect } from 'react';
import { Container, Tabs, Tab, Box, Typography, TextField, Button, Alert, Grid, FormControl, InputLabel, Select, MenuItem, Card } from '@mui/material';
import { profileService, preferenceService } from '../services/api';

const MyProfile = () => {
  const [tabValue, setTabValue] = useState(0);
  const [profile, setProfile] = useState({
    name: '',
    age: '',
    gender: 'MALE',
    religion: '',
    caste: '',
    subCaste: '',
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
    hobbies: '',
    lifestyle: 'VEGETARIAN',
    familyDetails: '',
    horoscope: '',
    photos: []
  });

  const [preference, setPreference] = useState({
    preferredAgeMin: '',
    preferredAgeMax: '',
    preferredHeightMin: '',
    preferredHeightMax: '',
    religion: '',
    caste: '',
    education: '',
    occupation: '',
    incomeMin: '',
    incomeMax: '',
    city: '',
    state: '',
    country: 'India',
    lifestyle: 'VEGETARIAN'
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [photoError, setPhotoError] = useState('');
  const [photoSuccess, setPhotoSuccess] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [isMainPhoto, setIsMainPhoto] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchProfileAndPrefs();
  }, []);

  const fetchProfileAndPrefs = async () => {
    setLoading(true);
    try {
      const profRes = await profileService.getMyProfile();
      if (profRes.data) {
        setProfile(profRes.data);
      }

      const prefRes = await preferenceService.getMyPreference();
      if (prefRes.data) {
        setPreference(prefRes.data);
      }
    } catch (err) {
      console.error(err);
      setError('Could not retrieve profile settings. You can create a new profile below.');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfile((prev) => ({ ...prev, [name]: value }));
  };

  const handlePrefChange = (e) => {
    const { name, value } = e.target;
    setPreference((prev) => ({ ...prev, [name]: value }));
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        ...profile,
        age: parseInt(profile.age, 10),
        height: parseFloat(profile.height),
        weight: parseFloat(profile.weight),
        annualIncome: parseFloat(profile.annualIncome)
      };
      
      let res;
      try {
        res = await profileService.updateProfile(payload);
      } catch {
        res = await profileService.createProfile(payload);
      }
      setSuccess('Profile details saved successfully!');
      if (res.data) setProfile(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save profile.');
    }
  };

  const handlePrefSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        ...preference,
        preferredAgeMin: preference.preferredAgeMin ? parseInt(preference.preferredAgeMin, 10) : null,
        preferredAgeMax: preference.preferredAgeMax ? parseInt(preference.preferredAgeMax, 10) : null,
        preferredHeightMin: preference.preferredHeightMin ? parseFloat(preference.preferredHeightMin) : null,
        preferredHeightMax: preference.preferredHeightMax ? parseFloat(preference.preferredHeightMax) : null,
        incomeMin: preference.incomeMin ? parseFloat(preference.incomeMin) : null,
        incomeMax: preference.incomeMax ? parseFloat(preference.incomeMax) : null
      };

      const res = await preferenceService.updateMyPreference(payload);
      setSuccess('Partner preferences saved successfully!');
      if (res.data) setPreference(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save preferences.');
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handlePhotoUpload = async (e) => {
    e.preventDefault();
    setPhotoError('');
    setPhotoSuccess('');
    if (!selectedFile) {
      setPhotoError('Please select a file to upload first.');
      return;
    }

    try {
      await profileService.uploadPhoto(selectedFile, isMainPhoto);
      setPhotoSuccess('Photo uploaded successfully!');
      setSelectedFile(null);
      setIsMainPhoto(false);
      // Reload profile to refresh photo list
      const profRes = await profileService.getMyProfile();
      if (profRes.data) setProfile(profRes.data);
    } catch (err) {
      setPhotoError(err.response?.data?.message || 'Failed to upload photo.');
    }
  };

  const handleDeletePhoto = async (photoId) => {
    setPhotoError('');
    setPhotoSuccess('');
    if (!window.confirm('Are you sure you want to delete this photo?')) return;

    try {
      await profileService.deletePhoto(photoId);
      setPhotoSuccess('Photo deleted successfully.');
      const profRes = await profileService.getMyProfile();
      if (profRes.data) setProfile(profRes.data);
    } catch (err) {
      setPhotoError(err.response?.data?.message || 'Failed to delete photo.');
    }
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        <Typography variant="h3" align="center" sx={{ mb: 4, letterSpacing: '0.05em' }}>
          PROFILE SETTINGS
        </Typography>

        <Tabs value={tabValue} onChange={(e, val) => setTabValue(val)} centered sx={{ mb: 4 }}>
          <Tab label="Profile Details" />
          <Tab label="Photos" />
          <Tab label="Partner Preferences" />
        </Tabs>

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

        {/* Tab 0: Profile Details Form */}
        {tabValue === 0 && (
          <form onSubmit={handleProfileSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField required fullWidth name="name" label="Full Name" value={profile.name || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField required fullWidth name="age" label="Age" type="number" value={profile.age || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={3}>
                <FormControl fullWidth>
                  <InputLabel>Gender</InputLabel>
                  <Select name="gender" value={profile.gender || 'MALE'} label="Gender" onChange={handleProfileChange}>
                    <MenuItem value="MALE">Male</MenuItem>
                    <MenuItem value="FEMALE">Female</MenuItem>
                    <MenuItem value="OTHER">Other</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="religion" label="Religion" value={profile.religion || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="caste" label="Caste" value={profile.caste || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField fullWidth name="subCaste" label="Sub-Caste" value={profile.subCaste || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="motherTongue" label="Mother Tongue" value={profile.motherTongue || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="height" label="Height (cm)" type="number" value={profile.height || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="weight" label="Weight (kg)" type="number" value={profile.weight || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Marital Status</InputLabel>
                  <Select name="maritalStatus" value={profile.maritalStatus || 'NEVER_MARRIED'} label="Marital Status" onChange={handleProfileChange}>
                    <MenuItem value="NEVER_MARRIED">Never Married</MenuItem>
                    <MenuItem value="DIVORCED">Divorced</MenuItem>
                    <MenuItem value="WIDOWED">Widowed</MenuItem>
                    <MenuItem value="AWAITING_DIVORCE">Awaiting Divorce</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Lifestyle</InputLabel>
                  <Select name="lifestyle" value={profile.lifestyle || 'VEGETARIAN'} label="Lifestyle" onChange={handleProfileChange}>
                    <MenuItem value="VEGETARIAN">Vegetarian</MenuItem>
                    <MenuItem value="NON_VEGETARIAN">Non-Vegetarian</MenuItem>
                    <MenuItem value="EGGETARIAN">Eggetarian</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="education" label="Highest Education" value={profile.education || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="occupation" label="Occupation" value={profile.occupation || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="annualIncome" label="Annual Income (INR)" type="number" value={profile.annualIncome || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="city" label="City" value={profile.city || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="state" label="State" value={profile.state || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField required fullWidth name="country" label="Country" value={profile.country || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth multiline rows={2} name="hobbies" label="Hobbies" value={profile.hobbies || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth multiline rows={2} name="familyDetails" label="Family Background Details" value={profile.familyDetails || ''} onChange={handleProfileChange} />
              </Grid>
              <Grid item xs={12}>
                <TextField required fullWidth multiline rows={4} name="aboutMe" label="About Me" value={profile.aboutMe || ''} onChange={handleProfileChange} />
              </Grid>
            </Grid>
            <Button type="submit" variant="contained" fullWidth sx={{ mt: 4 }}>
              SAVE PROFILE DETAILS
            </Button>
          </form>
        )}

        {/* Tab 1: Manage Photos */}
        {tabValue === 1 && (
          <Box>
            {photoError && (
              <Alert severity="error" variant="outlined" sx={{ mb: 3, borderRadius: 0, color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
                {photoError}
              </Alert>
            )}
            {photoSuccess && (
              <Alert severity="success" variant="outlined" sx={{ mb: 3, borderRadius: 0, color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
                {photoSuccess}
              </Alert>
            )}

            <Typography variant="h5" sx={{ mb: 2 }}>
              Upload New Photo
            </Typography>
            <Box component="form" onSubmit={handlePhotoUpload} sx={{ mb: 5, p: 3, border: '1px dashed #000000', textAlign: 'center' }}>
              <input type="file" accept="image/*" onChange={handleFileChange} style={{ display: 'block', margin: '0 auto 20px auto' }} />
              
              <FormControl sx={{ mb: 2, display: 'flex', flexDirection: 'row', justifyContent: 'center', gap: 1, alignItems: 'center' }}>
                <input type="checkbox" id="mainCheck" checked={isMainPhoto} onChange={(e) => setIsMainPhoto(e.target.checked)} />
                <label htmlFor="mainCheck">Set as primary profile photo</label>
              </FormControl>

              <Button type="submit" variant="contained" sx={{ px: 4 }}>
                UPLOAD
              </Button>
            </Box>

            <Typography variant="h5" sx={{ mb: 2 }}>
              Current Profile Photos (Max 5)
            </Typography>
            <Grid container spacing={3}>
              {profile.photos && profile.photos.length > 0 ? (
                profile.photos.map((photo) => (
                  <Grid item xs={6} sm={4} key={photo.id}>
                    <Card sx={{ position: 'relative' }}>
                      <Box
                        sx={{
                          height: 150,
                          backgroundImage: `url(http://localhost:8080${photo.photoUrl})`,
                          backgroundSize: 'cover',
                          backgroundPosition: 'center',
                        }}
                      />
                      <Box sx={{ p: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="caption" sx={{ color: photo.main ? '#000000' : '#888888', fontWeight: photo.main ? 'bold' : 'normal' }}>
                          {photo.main ? 'MAIN PHOTO' : 'SECONDARY'}
                        </Typography>
                        <Button
                          onClick={() => handleDeletePhoto(photo.id)}
                          variant="text"
                          sx={{ fontSize: '0.65rem', py: 0.2, px: 1, border: 'none', color: '#ff4444', '&:hover': { backgroundColor: 'transparent' } }}
                        >
                          DELETE
                        </Button>
                      </Box>
                    </Card>
                  </Grid>
                ))
              ) : (
                <Grid item xs={12}>
                  <Typography variant="body2" sx={{ color: '#555555', textAlign: 'center' }}>
                    No photos uploaded yet. Add at least one photo.
                  </Typography>
                </Grid>
              )}
            </Grid>
          </Box>
        )}

        {/* Tab 2: Partner Preferences Form */}
        {tabValue === 2 && (
          <form onSubmit={handlePrefSubmit}>
            <Typography variant="h6" sx={{ mb: 2, borderBottom: '1px solid rgba(0,0,0,0.15)', pb: 1 }}>
              What are you looking for in a partner?
            </Typography>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="preferredAgeMin" label="Minimum Age Preference" type="number" value={preference.preferredAgeMin || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="preferredAgeMax" label="Maximum Age Preference" type="number" value={preference.preferredAgeMax || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="preferredHeightMin" label="Min Height Preference (cm)" type="number" value={preference.preferredHeightMin || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="preferredHeightMax" label="Max Height Preference (cm)" type="number" value={preference.preferredHeightMax || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="religion" label="Religion Preference" value={preference.religion || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="caste" label="Caste Preference" value={preference.caste || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="education" label="Education Preference keyword" value={preference.education || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="occupation" label="Occupation Preference keyword" value={preference.occupation || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="incomeMin" label="Min Annual Income (INR)" type="number" value={preference.incomeMin || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField fullWidth name="incomeMax" label="Max Annual Income (INR)" type="number" value={preference.incomeMax || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField fullWidth name="city" label="City Preference" value={preference.city || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField fullWidth name="state" label="State Preference" value={preference.state || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField fullWidth name="country" label="Country Preference" value={preference.country || ''} onChange={handlePrefChange} />
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>Lifestyle Preference</InputLabel>
                  <Select name="lifestyle" value={preference.lifestyle || 'VEGETARIAN'} label="Lifestyle Preference" onChange={handlePrefChange}>
                    <MenuItem value="VEGETARIAN">Vegetarian</MenuItem>
                    <MenuItem value="NON_VEGETARIAN">Non-Vegetarian</MenuItem>
                    <MenuItem value="EGGETARIAN">Eggetarian</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
            <Button type="submit" variant="contained" fullWidth sx={{ mt: 4 }}>
              SAVE PARTNER PREFERENCES
            </Button>
          </form>
        )}
      </Box>
    </Container>
  );
};

export default MyProfile;
