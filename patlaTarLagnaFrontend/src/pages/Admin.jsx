import React, { useState, useEffect } from 'react';
import { Container, Grid, Box, Typography, Card, CardContent, Divider, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, Tabs, Tab, Alert } from '@mui/material';
import { adminService } from '../services/api';

const Admin = () => {
  const [tabValue, setTabValue] = useState(0);
  const [stats, setStats] = useState(null);
  const [verifications, setVerifications] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    setLoading(true);
    setError('');
    try {
      const statsRes = await adminService.getStats();
      if (statsRes.data) setStats(statsRes.data);

      const verRes = await adminService.getPendingVerifications();
      if (verRes.data) setVerifications(verRes.data);

      const repRes = await adminService.getReports();
      if (repRes.data) setReports(repRes.data);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch administrative data. Ensure you are authorized.');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (profileId) => {
    try {
      await adminService.approveProfile(profileId);
      alert('Profile approved and verified.');
      fetchAdminData();
    } catch (err) {
      alert('Could not approve profile.');
    }
  };

  const handleResolveReport = async (reportId, action) => {
    if (!window.confirm(`Are you sure you want to resolve this report with action: ${action}?`)) return;
    try {
      await adminService.resolveReport(reportId, action);
      alert('Report resolved.');
      fetchAdminData();
    } catch (err) {
      alert('Could not resolve report.');
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 10, textAlign: 'center' }}>
        <Typography variant="h5">RETRIEVING ADMIN CONSOLE METRICS...</Typography>
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

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        <Typography variant="h3" sx={{ mb: 4, letterSpacing: '0.05em' }}>
          ADMINISTRATOR CONTROL PANEL
        </Typography>

        {/* Stats Grid */}
        <Grid container spacing={3} sx={{ mb: 6 }}>
          <Grid item xs={12} sm={4}>
            <Card sx={{ border: '1px solid #000000', textAlign: 'center', py: 2 }}>
              <CardContent>
                <Typography variant="h6" sx={{ color: '#555555', mb: 1 }}>TOTAL REGISTERED USERS</Typography>
                <Typography variant="h3">{stats?.totalUsers || 0}</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card sx={{ border: '1px solid #000000', textAlign: 'center', py: 2 }}>
              <CardContent>
                <Typography variant="h6" sx={{ color: '#555555', mb: 1 }}>PENDING VERIFICATIONS</Typography>
                <Typography variant="h3">{stats?.pendingVerifications || 0}</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Card sx={{ border: '1px solid #000000', textAlign: 'center', py: 2 }}>
              <CardContent>
                <Typography variant="h6" sx={{ color: '#555555', mb: 1 }}>TOTAL ACTIVE REPORTS</Typography>
                <Typography variant="h3">{stats?.totalReports || 0}</Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Tabs value={tabValue} onChange={(e, val) => setTabValue(val)} centered sx={{ mb: 4 }}>
          <Tab label={`Verification Requests (${verifications.length})`} />
          <Tab label={`Reported Violations (${reports.length})`} />
        </Tabs>

        {/* Tab 0: Verification Approvals */}
        {tabValue === 0 && (
          <Box>
            {verifications.length === 0 ? (
              <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
                No pending profile verification requests.
              </Typography>
            ) : (
              <TableContainer component={Paper} sx={{ border: '1px solid rgba(0,0,0,0.15)' }}>
                <Table>
                  <TableHead sx={{ backgroundColor: 'rgba(0, 0, 0, 0.03)' }}>
                    <TableRow>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Name</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Age/Gender</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Location</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Occupation</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {verifications.map((row) => (
                      <TableRow key={row.id}>
                        <TableCell sx={{ color: '#000000' }}>{row.name}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.age} / {row.gender}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.city}, {row.state}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.occupation}</TableCell>
                        <TableCell>
                          <Button
                            onClick={() => handleApprove(row.id)}
                            variant="contained"
                            size="small"
                            sx={{ fontSize: '0.65rem', py: 0.5 }}
                          >
                            APPROVE & VERIFY
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        )}

        {/* Tab 1: Reported Violations */}
        {tabValue === 1 && (
          <Box>
            {reports.length === 0 ? (
              <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
                No reported violations.
              </Typography>
            ) : (
              <TableContainer component={Paper} sx={{ border: '1px solid rgba(0,0,0,0.15)' }}>
                <Table>
                  <TableHead sx={{ backgroundColor: 'rgba(0, 0, 0, 0.03)' }}>
                    <TableRow>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Reporter</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Reported User</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Reason</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Details</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Status</TableCell>
                      <TableCell sx={{ color: '#000000', fontWeight: 'bold' }}>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {reports.map((row) => (
                      <TableRow key={row.id}>
                        <TableCell sx={{ color: '#000000' }}>{row.reporter?.email}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.reported?.email}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.reason}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.details || 'No details provided'}</TableCell>
                        <TableCell sx={{ color: '#000000' }}>{row.status}</TableCell>
                        <TableCell>
                          {row.status === 'PENDING' ? (
                            <Box sx={{ display: 'flex', gap: 1 }}>
                              <Button
                                onClick={() => handleResolveReport(row.id, 'DISMISS')}
                                variant="outlined"
                                size="small"
                                sx={{ fontSize: '0.65rem', py: 0.5 }}
                              >
                                DISMISS
                              </Button>
                              <Button
                                onClick={() => handleResolveReport(row.id, 'SUSPEND')}
                                variant="contained"
                                size="small"
                                sx={{ fontSize: '0.65rem', py: 0.5, backgroundColor: '#000000', borderColor: '#000000', color: '#ffffff', '&:hover': { backgroundColor: '#555555', borderColor: '#555555' } }}
                              >
                                SUSPEND USER
                              </Button>
                            </Box>
                          ) : (
                            <Typography variant="caption" sx={{ color: '#555555' }}>RESOLVED</Typography>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default Admin;
