import React, { useState, useEffect } from 'react';
import { Container, Box, Typography, Tabs, Tab, Grid, Card, CardContent, Button, Divider, Alert } from '@mui/material';
import { Link } from 'react-router-dom';
import { matchingService } from '../services/api';

const Interests = () => {
  const [tabValue, setTabValue] = useState(0);
  const [received, setReceived] = useState([]);
  const [sent, setSent] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchInterests();
  }, []);

  const fetchInterests = async () => {
    setLoading(true);
    try {
      const recRes = await matchingService.getReceivedInterests();
      if (recRes.data) setReceived(recRes.data);

      const sentRes = await matchingService.getSentInterests();
      if (sentRes.data) setSent(sentRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (interestId) => {
    try {
      await matchingService.acceptInterest(interestId);
      alert('Interest accepted! You can now chat in Mutual Matches.');
      fetchInterests();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not accept interest.');
    }
  };

  const handleReject = async (interestId) => {
    if (!window.confirm('Are you sure you want to reject this interest?')) return;
    try {
      await matchingService.rejectInterest(interestId);
      alert('Interest rejected.');
      fetchInterests();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not reject interest.');
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 10, textAlign: 'center' }}>
        <Typography variant="h5">RETRIEVING INTEREST ACTIVITY...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        <Typography variant="h3" sx={{ mb: 4, letterSpacing: '0.05em' }}>
          INTERESTS CENTER
        </Typography>

        <Tabs value={tabValue} onChange={(e, val) => setTabValue(val)} centered sx={{ mb: 4 }}>
          <Tab label={`Received Requests (${received.length})`} />
          <Tab label={`Sent Requests (${sent.length})`} />
        </Tabs>

        {/* Tab 0: Received Interests */}
        {tabValue === 0 && (
          <Box>
            {received.length === 0 ? (
              <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
                No received interest requests.
              </Typography>
            ) : (
              <Grid container spacing={3}>
                {received.map((item) => {
                  const photoUrl = item.senderPhoto ? `http://localhost:8080${item.senderPhoto}` : 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600';
                  return (
                    <Grid item xs={12} sm={6} key={item.id}>
                      <Card sx={{ p: 2 }}>
                        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
                          <Box
                            sx={{
                              width: 80,
                              height: 80,
                              backgroundImage: `url(${photoUrl})`,
                              backgroundSize: 'cover',
                              backgroundPosition: 'center',
                              border: '1px solid #000000'
                            }}
                          />
                          <Box>
                            <Typography variant="h5">{item.senderName}</Typography>
                            <Typography variant="body2" sx={{ color: '#555555' }}>
                              Status: {item.status}
                            </Typography>
                          </Box>
                        </Box>
                        {item.status === 'PENDING' ? (
                          <Box sx={{ display: 'flex', gap: 2 }}>
                            <Button
                              onClick={() => handleAccept(item.id)}
                              variant="contained"
                              fullWidth
                              sx={{ fontSize: '0.75rem' }}
                            >
                              ACCEPT
                            </Button>
                            <Button
                              onClick={() => handleReject(item.id)}
                              variant="outlined"
                              fullWidth
                              sx={{ fontSize: '0.75rem', borderColor: '#000000', color: '#000000', '&:hover': { backgroundColor: '#000000', color: '#ffffff', borderColor: '#000000' } }}
                            >
                              DECLINE
                            </Button>
                          </Box>
                        ) : (
                          <Typography variant="body2" sx={{ fontStyle: 'italic', color: '#555555' }}>
                            You have already {item.status.toLowerCase()} this request.
                          </Typography>
                        )}
                      </Card>
                    </Grid>
                  );
                })}
              </Grid>
            )}
          </Box>
        )}

        {/* Tab 1: Sent Interests */}
        {tabValue === 1 && (
          <Box>
            {sent.length === 0 ? (
              <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
                No sent interest requests.
              </Typography>
            ) : (
              <Grid container spacing={3}>
                {sent.map((item) => {
                  const photoUrl = item.receiverPhoto ? `http://localhost:8080${item.receiverPhoto}` : 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=600';
                  return (
                    <Grid item xs={12} sm={6} key={item.id}>
                      <Card sx={{ p: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
                        <Box
                          sx={{
                            width: 80,
                            height: 80,
                            backgroundImage: `url(${photoUrl})`,
                            backgroundSize: 'cover',
                            backgroundPosition: 'center',
                            border: '1px solid #000000'
                          }}
                        />
                        <Box>
                          <Typography variant="h5">{item.receiverName}</Typography>
                          <Typography variant="body2" sx={{ color: '#555555', mb: 1 }}>
                            Status: {item.status}
                          </Typography>
                          <Button
                            component={Link}
                            to={`/user/${item.receiverId}`}
                            variant="outlined"
                            sx={{ fontSize: '0.65rem', py: 0.5, px: 2 }}
                          >
                            VIEW PROFILE
                          </Button>
                        </Box>
                      </Card>
                    </Grid>
                  );
                })}
              </Grid>
            )}
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default Interests;
