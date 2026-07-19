import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Badge, Box, Container } from '@mui/material';
import { authService, notificationService } from '../services/api';

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = authService.getCurrentUser();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (user) {
      fetchNotifications();
      const interval = setInterval(fetchNotifications, 15000); // poll notifications
      return () => clearInterval(interval);
    }
  }, []);

  const fetchNotifications = async () => {
    try {
      const res = await notificationService.getUnreadNotifications();
      if (res.data) {
        setUnreadCount(res.data.length);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <AppBar position="sticky">
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ justifyContent: 'space-between' }}>
          <Typography
            variant="h5"
            component={Link}
            to="/"
            sx={{
              fontWeight: 700,
              letterSpacing: '0.15em',
              color: '#000000',
              fontFamily: "'Playfair Display', serif",
              textDecoration: 'none',
              display: 'flex',
              alignItems: 'center'
            }}
          >
            PATLATARLAGNA
          </Typography>

          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button
              component={Link}
              to="/search"
              variant={location.pathname === '/search' ? 'contained' : 'text'}
              sx={{ py: 0.5, px: 2, fontSize: '0.75rem' }}
            >
              Search
            </Button>
            <Button
              component={Link}
              to="/interests"
              variant={location.pathname === '/interests' ? 'contained' : 'text'}
              sx={{ py: 0.5, px: 2, fontSize: '0.75rem' }}
            >
              Interests
            </Button>
            <Button
              component={Link}
              to="/matches"
              variant={location.pathname === '/matches' ? 'contained' : 'text'}
              sx={{ py: 0.5, px: 2, fontSize: '0.75rem' }}
            >
              Mutual Matches
            </Button>
            <Button
              component={Link}
              to="/chat"
              variant={location.pathname === '/chat' ? 'contained' : 'text'}
              sx={{ py: 0.5, px: 2, fontSize: '0.75rem' }}
            >
              Chat
            </Button>
            <Button
              component={Link}
              to="/profile"
              variant={location.pathname === '/profile' ? 'contained' : 'text'}
              sx={{ py: 0.5, px: 2, fontSize: '0.75rem' }}
            >
              My Profile
            </Button>
            {authService.isAdmin() && (
              <Button
                component={Link}
                to="/admin"
                variant={location.pathname === '/admin' ? 'contained' : 'text'}
                sx={{ py: 0.5, px: 2, fontSize: '0.75rem' }}
              >
                Admin
              </Button>
            )}
          </Box>

          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <Badge
              badgeContent={unreadCount}
              sx={{
                '& .MuiBadge-badge': {
                  color: '#ffffff',
                  backgroundColor: '#000000',
                  fontWeight: 'bold',
                  fontFamily: "'Outfit', sans-serif"
                }
              }}
            >
              <Button
                component={Link}
                to="/notifications"
                variant="text"
                sx={{ py: 0.5, px: 1.5, fontSize: '0.75rem', borderColor: 'transparent' }}
              >
                Notifications
              </Button>
            </Badge>
            <Button
              onClick={handleLogout}
              variant="text"
              sx={{ py: 0.5, px: 1.5, fontSize: '0.75rem', borderColor: 'transparent', '&:hover': { backgroundColor: 'rgba(0,0,0,0.05)' } }}
            >
              Logout
            </Button>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default Navbar;
