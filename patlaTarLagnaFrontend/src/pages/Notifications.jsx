import React, { useState, useEffect } from 'react';
import { Container, Box, Typography, List, ListItem, ListItemText, Button, Divider, Alert } from '@mui/material';
import { notificationService } from '../services/api';

const Notifications = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationService.getNotifications();
      if (res.data) {
        // Sort notifications by createdAt descending if field exists, otherwise keep response order
        setNotifications(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 10, textAlign: 'center' }}>
        <Typography variant="h5">RETRIEVING ACCOUNT NOTIFICATIONS...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', p: 4, backgroundColor: '#ffffff' }}>
        <Typography variant="h3" sx={{ mb: 4, letterSpacing: '0.05em' }}>
          NOTIFICATIONS
        </Typography>

        {notifications.length === 0 ? (
          <Typography variant="body1" sx={{ color: '#555555', textAlign: 'center', py: 4 }}>
            No notifications yet.
          </Typography>
        ) : (
          <List>
            {notifications.map((notif, index) => (
              <React.Fragment key={notif.id}>
                {index > 0 && <Divider sx={{ borderColor: 'rgba(0,0,0,0.1)' }} />}
                <ListItem
                  sx={{
                    py: 2,
                    backgroundColor: notif.read ? 'transparent' : 'rgba(0, 0, 0, 0.03)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderLeft: notif.read ? '3px solid transparent' : '3px solid #000000',
                    pl: 2
                  }}
                >
                  <ListItemText
                    primary={notif.message}
                    secondary={notif.type}
                    primaryTypographyProps={{
                      fontWeight: notif.read ? 'normal' : 'bold',
                      color: '#000000'
                    }}
                    secondaryTypographyProps={{
                      color: '#555555',
                      fontSize: '0.75rem'
                    }}
                  />
                  {!notif.read && (
                    <Button
                      onClick={() => handleMarkAsRead(notif.id)}
                      variant="outlined"
                      size="small"
                      sx={{ fontSize: '0.65rem', py: 0.5 }}
                    >
                      MARK READ
                    </Button>
                  )}
                </ListItem>
              </React.Fragment>
            ))}
          </List>
        )}
      </Box>
    </Container>
  );
};

export default Notifications;
