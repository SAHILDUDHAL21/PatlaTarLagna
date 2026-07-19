import React, { useState, useEffect } from 'react';
import { Container, Grid, Box, Typography, Card, CardContent, Button, Divider, Alert, Badge } from '@mui/material';
import { Link } from 'react-router-dom';
import { profileService, searchService, matchingService } from '../services/api';

const Dashboard = () => {
  const [profile, setProfile] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [visitors, setVisitors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // 1. Fetch own profile
      const profRes = await profileService.getMyProfile();
      if (profRes.data) {
        setProfile(profRes.data);

        // 2. Fetch recommendations (Opposite gender search)
        const targetGender = profRes.data.gender === 'MALE' ? 'FEMALE' : 'MALE';
        const recRes = await searchService.search({
          gender: targetGender,
          page: 0,
          size: 6,
        });
        if (recRes.data && recRes.data.content) {
          // Fetch compatibility percentages for these profiles
          const withComp = await Promise.all(
            recRes.data.content.map(async (p) => {
              try {
                const compRes = await matchingService.getCompatibility(p.userId);
                return { ...p, compatibility: compRes.data || 70.0 };
              } catch {
                return { ...p, compatibility: 70.0 };
              }
            })
          );
          // Sort by compatibility descending
          setRecommendations(withComp.sort((a, b) => b.compatibility - a.compatibility));
        }
      }

      // 3. Fetch Profile Visitors
      const visRes = await profileService.getVisitors();
      if (visRes.data) {
        setVisitors(visRes.data);
      }

    } catch (err) {
      console.error(err);
      setError('Could not retrieve dashboard information. Ensure profile is set up.');
    } finally {
      setLoading(false);
    }
  };

  const handleSendInterest = async (receiverId) => {
    try {
      await matchingService.sendInterest(receiverId);
      alert('Interest sent successfully!');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to send interest.');
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 10, textAlign: 'center' }}>
        <Typography variant="h5">RETRIEVING DASHBOARD METRICS...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      {error ? (
        <Box sx={{ mt: 4, textAlign: 'center' }}>
          <Alert severity="warning" variant="outlined" sx={{ color: '#000000', borderColor: '#000000', '& .MuiAlert-icon': { color: '#000000' }, mb: 4 }}>
            {error}
          </Alert>
          <Button component={Link} to="/profile" variant="contained">
            CREATE OR COMPLETE YOUR PROFILE
          </Button>
        </Box>
      ) : (
        <>
          {/* Hero Branding & Verification Section */}
          <Box sx={{ mb: 6, display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', md: 'center' }, border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
            <Box>
              <Typography variant="h2" sx={{ mb: 1 }}>
                Welcome, {profile?.name}
              </Typography>
              <Typography variant="body1" sx={{ color: '#555555' }}>
                Your matrimonial journey of pure compatibility starts here.
              </Typography>
            </Box>
            <Box sx={{ mt: { xs: 2, md: 0 } }}>
              {profile?.verified ? (
                <Box sx={{ border: '2px solid #000000', px: 3, py: 1, backgroundColor: '#000000', color: '#ffffff', fontWeight: 'bold', fontSize: '0.9rem', letterSpacing: '0.05em' }}>
                  VERIFIED PROFILE
                </Box>
              ) : (
                <Box sx={{ border: '1px solid rgba(0, 0, 0, 0.4)', px: 3, py: 1, color: '#555555', fontSize: '0.9rem', letterSpacing: '0.05em' }}>
                  PENDING VERIFICATION
                </Box>
              )}
            </Box>
          </Box>

          <Grid container spacing={4}>
            {/* Recommendations Grid */}
            <Grid item xs={12} md={8}>
              <Typography variant="h4" sx={{ mb: 3 }}>
                RECOMMENDED MATCHES
              </Typography>
              <Divider sx={{ mb: 4 }} />

              {recommendations.length === 0 ? (
                <Typography variant="body1" sx={{ color: '#555555' }}>
                  No recommendations found matching your preferences yet. Try updating your partner preferences.
                </Typography>
              ) : (
                <Grid container spacing={3}>
                  {recommendations.map((rec) => {
                    const mainPhoto = rec.photos?.find(p => p.main)?.photoUrl || 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=600';
                    return (
                      <Grid item xs={12} sm={6} key={rec.id}>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                          <Box
                            sx={{
                              height: 250,
                              backgroundImage: `url(${mainPhoto.startsWith('/api') ? 'http://localhost:8080' + mainPhoto : mainPhoto})`,
                              backgroundSize: 'cover',
                              backgroundPosition: 'center',
                              position: 'relative'
                            }}
                          >
                            <Box sx={{ position: 'absolute', top: 12, right: 12, backgroundColor: '#000000', border: '1px solid #000000', px: 1.5, py: 0.5, color: '#ffffff' }}>
                              <Typography variant="caption" sx={{ fontWeight: 'bold' }}>
                                {rec.compatibility}% MATCH
                              </Typography>
                            </Box>
                          </Box>
                          <CardContent sx={{ flexGrow: 1, p: 3 }}>
                            <Typography variant="h5" sx={{ mb: 1 }}>
                              {rec.name}, {rec.age}
                            </Typography>
                            <Typography variant="body2" sx={{ color: '#555555', mb: 2 }}>
                              {rec.occupation} • {rec.city}, {rec.state}
                            </Typography>
                            <Box sx={{ display: 'flex', gap: 2 }}>
                              <Button
                                component={Link}
                                to={`/user/${rec.userId}`}
                                variant="outlined"
                                fullWidth
                                sx={{ fontSize: '0.75rem', py: 0.8 }}
                              >
                                VIEW
                              </Button>
                              <Button
                                onClick={() => handleSendInterest(rec.userId)}
                                variant="contained"
                                fullWidth
                                sx={{ fontSize: '0.75rem', py: 0.8 }}
                              >
                                CONNECT
                              </Button>
                            </Box>
                          </CardContent>
                        </Card>
                      </Grid>
                    );
                  })}
                </Grid>
              )}
            </Grid>

            {/* Profile Visitors Panel */}
            <Grid item xs={12} md={4}>
              <Typography variant="h4" sx={{ mb: 3 }}>
                PROFILE VISITORS
              </Typography>
              <Divider sx={{ mb: 4 }} />

              {visitors.length === 0 ? (
                <Box sx={{ border: '1px dashed rgba(0, 0, 0, 0.2)', p: 4, textAlign: 'center' }}>
                  <Typography variant="body2" sx={{ color: '#555555' }}>
                    No recent profile visitors.
                  </Typography>
                </Box>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                  {visitors.map((visitor) => {
                    const mainPhoto = visitor.photos?.find(p => p.main)?.photoUrl || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600';
                    return (
                      <Card key={visitor.id} sx={{ display: 'flex', p: 2, alignItems: 'center', gap: 2 }}>
                        <Box
                          sx={{
                            width: 60,
                            height: 60,
                            backgroundImage: `url(${mainPhoto.startsWith('/api') ? 'http://localhost:8080' + mainPhoto : mainPhoto})`,
                            backgroundSize: 'cover',
                            backgroundPosition: 'center',
                            border: '1px solid #000000'
                          }}
                        />
                        <Box sx={{ flexGrow: 1 }}>
                          <Typography variant="h6" sx={{ fontSize: '1rem' }}>
                            {visitor.name}
                          </Typography>
                          <Typography variant="caption" sx={{ color: '#555555' }}>
                            {visitor.age} yrs • {visitor.city}
                          </Typography>
                        </Box>
                        <Button
                          component={Link}
                          to={`/user/${visitor.userId}`}
                          variant="outlined"
                          sx={{ py: 0.4, px: 2, fontSize: '0.65rem' }}
                        >
                          VIEW
                        </Button>
                      </Card>
                    );
                  })}
                </Box>
              )}
            </Grid>
          </Grid>
        </>
      )}
    </Container>
  );
};

export default Dashboard;
