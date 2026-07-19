import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Grid, Box, Typography, Button, Divider, Alert, Card, CardContent, Dialog, DialogTitle, DialogContent, DialogActions, TextField } from '@mui/material';
import { profileService, matchingService } from '../services/api';

const UserProfile = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [compatibility, setCompatibility] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [interestSent, setInterestSent] = useState(false);

  // Report Modal state
  const [reportOpen, setReportOpen] = useState(false);
  const [reportReason, setReportReason] = useState('');
  const [reportDetails, setReportDetails] = useState('');

  useEffect(() => {
    fetchUserProfile();
  }, [userId]);

  const fetchUserProfile = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await profileService.getUserProfile(userId);
      if (res.data) {
        setProfile(res.data);
      }

      const compRes = await matchingService.getCompatibility(userId);
      if (compRes.data !== undefined) {
        setCompatibility(compRes.data);
      }

      // Check if interest has already been sent
      const sentRes = await matchingService.getSentInterests();
      if (sentRes.data) {
        const alreadySent = sentRes.data.some(interest => interest.receiverId === parseInt(userId, 10));
        setInterestSent(alreadySent);
      }

    } catch (err) {
      console.error(err);
      setError('Could not retrieve profile. Profile may not exist or is suspended.');
    } finally {
      setLoading(false);
    }
  };

  const handleSendInterest = async () => {
    try {
      await matchingService.sendInterest(userId);
      setInterestSent(true);
      alert('Interest expressed successfully!');
    } catch (err) {
      alert(err.response?.data?.message || 'Could not send interest.');
    }
  };

  const handleBlockUser = async () => {
    if (!window.confirm('Are you sure you want to block this user? They will not be able to see you or contact you.')) return;
    try {
      await matchingService.blockUser(userId);
      alert('User has been blocked.');
      navigate('/');
    } catch (err) {
      alert(err.response?.data?.message || 'Could not block user.');
    }
  };

  const handleReportUserSubmit = async () => {
    if (!reportReason) {
      alert('Please provide a reason for the report.');
      return;
    }
    try {
      await matchingService.reportUser(userId, reportReason, reportDetails);
      alert('User has been reported to administrators.');
      setReportOpen(false);
      setReportReason('');
      setReportDetails('');
    } catch (err) {
      alert(err.response?.data?.message || 'Could not submit report.');
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 10, textAlign: 'center' }}>
        <Typography variant="h5">RETRIEVING MATRIMONIAL PROFILE...</Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container sx={{ mt: 5 }}>
        <Alert severity="error" variant="outlined" sx={{ color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' } }}>
          {error}
        </Alert>
      </Container>
    );
  }

  const mainPhoto = profile?.photos?.find(p => p.main)?.photoUrl || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600';

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        
        {/* Header Summary */}
        <Grid container spacing={4} sx={{ mb: 6 }}>
          <Grid item xs={12} sm={4}>
            <Box
              sx={{
                height: 350,
                backgroundImage: `url(${mainPhoto.startsWith('/api') ? 'http://localhost:8080' + mainPhoto : mainPhoto})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                border: '1px solid #000000'
              }}
            />
          </Grid>
          <Grid item xs={12} sm={8} sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h2">{profile?.name}</Typography>
                <Box sx={{ border: '1px solid #000000', px: 2, py: 0.5, backgroundColor: '#000000', color: '#ffffff', fontWeight: 'bold' }}>
                  {compatibility}% COMPATIBILITY
                </Box>
              </Box>
              <Typography variant="h6" sx={{ color: '#555555', mb: 3 }}>
                {profile?.occupation} • {profile?.city}, {profile?.state}, {profile?.country}
              </Typography>
              <Typography variant="body1" sx={{ fontStyle: 'italic', mb: 4 }}>
                "{profile?.aboutMe}"
              </Typography>
            </Box>

            {/* Profile Action Buttons */}
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Button
                onClick={handleSendInterest}
                variant="contained"
                disabled={interestSent}
                sx={{ flexGrow: 1, py: 1.5 }}
              >
                {interestSent ? 'INTEREST EXPRESSED' : 'EXPRESS INTEREST'}
              </Button>
              <Button onClick={handleBlockUser} variant="outlined" sx={{ py: 1.5 }}>
                BLOCK
              </Button>
              <Button onClick={() => setReportOpen(true)} variant="outlined" sx={{ py: 1.5, borderColor: '#000000', color: '#000000', '&:hover': { backgroundColor: '#000000', color: '#ffffff', borderColor: '#000000' } }}>
                REPORT
              </Button>
            </Box>
          </Grid>
        </Grid>

        <Divider sx={{ mb: 5 }} />

        {/* Detailed Information Tabs / Grids */}
        <Grid container spacing={4}>
          <Grid item xs={12} md={6}>
            <Typography variant="h4" sx={{ mb: 3 }}>
              PERSONAL CHARACTERISTICS
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Age:</Typography></Grid><Grid item xs={6}><Typography>{profile?.age} years</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Gender:</Typography></Grid><Grid item xs={6}><Typography>{profile?.gender}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Marital Status:</Typography></Grid><Grid item xs={6}><Typography>{profile?.maritalStatus?.replace('_', ' ')}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Religion:</Typography></Grid><Grid item xs={6}><Typography>{profile?.religion}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Caste:</Typography></Grid><Grid item xs={6}><Typography>{profile?.caste} {profile?.subCaste ? `(${profile.subCaste})` : ''}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Mother Tongue:</Typography></Grid><Grid item xs={6}><Typography>{profile?.motherTongue}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Height / Weight:</Typography></Grid><Grid item xs={6}><Typography>{profile?.height} cm / {profile?.weight} kg</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Lifestyle:</Typography></Grid><Grid item xs={6}><Typography>{profile?.lifestyle}</Typography></Grid></Grid>
            </Box>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography variant="h4" sx={{ mb: 3 }}>
              PROFESSIONAL BACKGROUND
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mb: 4 }}>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Education:</Typography></Grid><Grid item xs={6}><Typography>{profile?.education}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Occupation:</Typography></Grid><Grid item xs={6}><Typography>{profile?.occupation}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Annual Income:</Typography></Grid><Grid item xs={6}><Typography>₹ {profile?.annualIncome?.toLocaleString()}</Typography></Grid></Grid>
            </Box>

            <Typography variant="h4" sx={{ mb: 3 }}>
              ADDITIONAL DETAILS
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Hobbies:</Typography></Grid><Grid item xs={6}><Typography>{profile?.hobbies || 'None shared'}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Family Details:</Typography></Grid><Grid item xs={6}><Typography>{profile?.familyDetails || 'None shared'}</Typography></Grid></Grid>
              <Grid container><Grid item xs={6}><Typography sx={{ color: '#555555' }}>Horoscope / Gotra:</Typography></Grid><Grid item xs={6}><Typography>{profile?.horoscope || 'None shared'}</Typography></Grid></Grid>
            </Box>
          </Grid>
        </Grid>

        <Divider sx={{ my: 5 }} />

        {/* Gallery */}
        <Typography variant="h4" sx={{ mb: 3 }}>
          PHOTO GALLERY
        </Typography>
        <Grid container spacing={3}>
          {profile?.photos && profile.photos.length > 0 ? (
            profile.photos.map((photo) => (
              <Grid item xs={6} sm={3} key={photo.id}>
                <Card
                  sx={{
                    height: 200,
                    backgroundImage: `url(http://localhost:8080${photo.photoUrl})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    cursor: 'pointer',
                    '&:hover': { border: '2px solid #000000' }
                  }}
                  onClick={() => window.open(`http://localhost:8080${photo.photoUrl}`, '_blank')}
                />
              </Grid>
            ))
          ) : (
            <Grid item xs={12}>
              <Typography variant="body2" sx={{ color: '#555555' }}>
                No additional photos shared by this user.
              </Typography>
            </Grid>
          )}
        </Grid>
      </Box>

      {/* Report User Dialog */}
      <Dialog open={reportOpen} onClose={() => setReportOpen(false)} sx={{ '& .MuiPaper-root': { border: '1px solid #000000' } }}>
        <DialogTitle sx={{ fontFamily: "'Playfair Display', serif", fontWeight: 'bold' }}>
          REPORT USER PROFILE
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 3, color: '#555555' }}>
            Flag this profile for review by the administrators. Please supply a reason.
          </Typography>
          <TextField
            autoFocus
            margin="dense"
            label="Reason for Report"
            fullWidth
            required
            variant="outlined"
            value={reportReason}
            onChange={(e) => setReportReason(e.target.value)}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Additional Details (Optional)"
            fullWidth
            multiline
            rows={3}
            variant="outlined"
            value={reportDetails}
            onChange={(e) => setReportDetails(e.target.value)}
          />
        </DialogContent>
        <DialogActions sx={{ p: 3 }}>
          <Button onClick={() => setReportOpen(false)} variant="outlined">
            CANCEL
          </Button>
          <Button onClick={handleReportUserSubmit} variant="contained" sx={{ color: '#ffffff', backgroundColor: '#000000' }}>
            SUBMIT REPORT
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default UserProfile;
