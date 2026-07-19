import React, { useState, useEffect } from 'react';
import { Container, Box, Typography, Grid, Card, CardContent, Button, Divider } from '@mui/material';
import { Link } from 'react-router-dom';
import { matchingService } from '../services/api';

const MutualMatches = () => {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMatches();
  }, []);

  const fetchMatches = async () => {
    setLoading(true);
    try {
      const res = await matchingService.getMutualMatches();
      if (res.data) setMatches(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 10, textAlign: 'center' }}>
        <Typography variant="h5">RETRIEVING MUTUAL CONNECTIONS...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        <Typography variant="h3" sx={{ mb: 4, letterSpacing: '0.05em' }}>
          MUTUAL MATCHES
        </Typography>
        <Typography variant="body2" sx={{ color: '#555555', mb: 4 }}>
          Profiles where both you and the other user have expressed mutual interest. You can initiate a private conversation with these matches.
        </Typography>

        {matches.length === 0 ? (
          <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
            No mutual connections found yet. Continue expressing interest to start matches!
          </Typography>
        ) : (
          <Grid container spacing={3}>
            {matches.map((item) => {
              const photoUrl = item.photos?.find(p => p.main)?.photoUrl || 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=600';
              return (
                <Grid item xs={12} sm={6} md={4} key={item.id}>
                  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                    <Box
                      sx={{
                        height: 200,
                        backgroundImage: `url(${photoUrl.startsWith('/api') ? 'http://localhost:8080' + photoUrl : photoUrl})`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                        borderBottom: '1px solid rgba(0, 0, 0, 0.12)'
                      }}
                    />
                    <CardContent sx={{ flexGrow: 1, p: 3 }}>
                      <Typography variant="h5" sx={{ mb: 1 }}>
                        {item.name}, {item.age}
                      </Typography>
                      <Typography variant="body2" sx={{ color: '#555555', mb: 3 }}>
                        {item.occupation} • {item.city}, {item.state}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 2 }}>
                        <Button
                          component={Link}
                          to={`/user/${item.userId}`}
                          variant="outlined"
                          fullWidth
                          sx={{ fontSize: '0.75rem', py: 1 }}
                        >
                          VIEW PROFILE
                        </Button>
                        <Button
                          component={Link}
                          to="/chat"
                          variant="contained"
                          fullWidth
                          sx={{ fontSize: '0.75rem', py: 1 }}
                        >
                          MESSAGE
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              );
            })}
          </Grid>
        )}
      </Box>
    </Container>
  );
};

export default MutualMatches;
