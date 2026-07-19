import React, { useState, useEffect } from 'react';
import { Container, Grid, Box, Typography, TextField, Button, Card, CardContent, Divider, Select, MenuItem, InputLabel, FormControl } from '@mui/material';
import { Link } from 'react-router-dom';
import { searchService, matchingService } from '../services/api';

const Search = () => {
  const [filters, setFilters] = useState({
    gender: '',
    religion: '',
    caste: '',
    minAge: '',
    maxAge: '',
    minHeight: '',
    maxHeight: '',
    education: '',
    occupation: '',
    minSalary: '',
    maxSalary: '',
    city: '',
    state: '',
    country: '',
  });

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    try {
      const cleanParams = {};
      Object.keys(filters).forEach((key) => {
        if (filters[key] !== '') {
          cleanParams[key] = filters[key];
        }
      });

      const res = await searchService.search(cleanParams);
      if (res.data && res.data.content) {
        // Fetch compatibility score for each result
        const withComp = await Promise.all(
          res.data.content.map(async (p) => {
            try {
              const compRes = await matchingService.getCompatibility(p.userId);
              return { ...p, compatibility: compRes.data || 70.0 };
            } catch {
              return { ...p, compatibility: 70.0 };
            }
          })
        );
        // Sort by compatibility descending
        setResults(withComp.sort((a, b) => b.compatibility - a.compatibility));
      }
    } catch (err) {
      console.error(err);
      alert('Search failed. Check your parameters.');
    } finally {
      setLoading(false);
    }
  };

  // Run a default search on page load (fetching opposite gender profiles)
  useEffect(() => {
    handleSearch();
  }, []);

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        <Typography variant="h3" sx={{ mb: 4, letterSpacing: '0.05em' }}>
          SEARCH PROFILES
        </Typography>

        {/* Filter Form */}
        <Box component="form" onSubmit={handleSearch} sx={{ mb: 6 }}>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={3}>
              <FormControl fullWidth>
                <InputLabel>Gender</InputLabel>
                <Select name="gender" value={filters.gender} label="Gender" onChange={handleFilterChange}>
                  <MenuItem value="">Any Gender</MenuItem>
                  <MenuItem value="MALE">Male</MenuItem>
                  <MenuItem value="FEMALE">Female</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="minAge" label="Min Age" type="number" value={filters.minAge} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="maxAge" label="Max Age" type="number" value={filters.maxAge} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="religion" label="Religion" value={filters.religion} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="caste" label="Caste" value={filters.caste} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="minHeight" label="Min Height (cm)" type="number" value={filters.minHeight} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="maxHeight" label="Max Height (cm)" type="number" value={filters.maxHeight} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField fullWidth name="occupation" label="Occupation" value={filters.occupation} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField fullWidth name="city" label="City" value={filters.city} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField fullWidth name="state" label="State" value={filters.state} onChange={handleFilterChange} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField fullWidth name="country" label="Country" value={filters.country} onChange={handleFilterChange} />
            </Grid>
          </Grid>
          <Button type="submit" variant="contained" fullWidth sx={{ mt: 4, py: 1.5 }}>
            {loading ? 'APPLYING FILTERS...' : 'SEARCH PROFILES'}
          </Button>
        </Box>

        <Divider sx={{ mb: 5 }} />

        {/* Results Grid */}
        <Typography variant="h4" sx={{ mb: 4 }}>
          MATCH RESULTS ({results.length})
        </Typography>

        {results.length === 0 ? (
          <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
            No matching profiles found. Try broadening your filter options.
          </Typography>
        ) : (
          <Grid container spacing={4}>
            {results.map((profile) => {
              const mainPhoto = profile.photos?.find(p => p.main)?.photoUrl || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=600';
              return (
                <Grid item xs={12} sm={6} md={4} key={profile.id}>
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
                          {profile.compatibility}% MATCH
                        </Typography>
                      </Box>
                    </Box>
                    <CardContent sx={{ flexGrow: 1, p: 3 }}>
                      <Typography variant="h5" sx={{ mb: 1 }}>
                        {profile.name}, {profile.age}
                      </Typography>
                      <Typography variant="body2" sx={{ color: '#555555', mb: 2 }}>
                        {profile.occupation} • {profile.city}, {profile.state}
                      </Typography>
                      <Button
                        component={Link}
                        to={`/user/${profile.userId}`}
                        variant="contained"
                        fullWidth
                        sx={{ fontSize: '0.75rem', py: 1 }}
                      >
                        VIEW FULL PROFILE
                      </Button>
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

export default Search;
