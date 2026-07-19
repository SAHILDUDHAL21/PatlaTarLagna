import React, { useState, useEffect, useRef } from 'react';
import { Container, Grid, Box, Typography, Card, TextField, Button, List, ListItem, ListItemText, ListItemAvatar, Avatar, Divider } from '@mui/material';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { matchingService, chatService, authService } from '../services/api';

const Chat = () => {
  const currentUser = authService.getCurrentUser();
  const [matches, setMatches] = useState([]);
  const [selectedMatch, setSelectedMatch] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const messagesEndRef = useRef(null);

  // Load mutual matches on page load
  useEffect(() => {
    fetchMatches();
  }, []);

  const fetchMatches = async () => {
    try {
      const res = await matchingService.getMutualMatches();
      if (res.data) {
        setMatches(res.data);
        if (res.data.length > 0) {
          setSelectedMatch(res.data[0]);
        }
      }
    } catch (err) {
      console.error(err);
    }
  };

  // Fetch history and connect to WebSocket whenever selected match changes
  useEffect(() => {
    if (!selectedMatch) return;

    fetchHistory(selectedMatch.userId);

    // Initialize SockJS + STOMP
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null; // Suppress debug logs in console

    stompClient.connect({}, () => {
      stompClient.subscribe('/user/queue/messages', (payload) => {
        const incomingMessage = JSON.parse(payload.body);
        // Only append if message belongs to current conversation
        if (incomingMessage.senderId === selectedMatch.userId) {
          setMessages((prev) => [...prev, incomingMessage]);
          scrollToBottom();
        }
      });
    }, (err) => {
      console.error('STOMP connection error:', err);
    });

    return () => {
      if (stompClient && stompClient.connected) {
        stompClient.disconnect();
      }
    };
  }, [selectedMatch]);

  const fetchHistory = async (userId) => {
    try {
      const res = await chatService.getHistory(userId);
      if (res.data) {
        setMessages(res.data);
        setTimeout(scrollToBottom, 50);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!inputText.trim() || !selectedMatch) return;

    try {
      const res = await chatService.sendMessage(selectedMatch.userId, inputText);
      if (res.data) {
        setMessages((prev) => [...prev, res.data]);
        setInputText('');
        setTimeout(scrollToBottom, 50);
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to send message.');
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 10 }} className="animate-fade-in">
      <Box sx={{ border: '1px solid #000000', backgroundColor: '#ffffff', height: '75vh', display: 'flex', flexDirection: 'column' }}>
        <Grid container sx={{ height: '100%' }}>
          
          {/* Matches List Sidebar */}
          <Grid item xs={12} sm={4} sx={{ borderRight: '1px solid rgba(0, 0, 0, 0.12)', height: '100%', overflowY: 'auto' }}>
            <Box sx={{ p: 3 }}>
              <Typography variant="h5" sx={{ mb: 2, letterSpacing: '0.05em' }}>
                CONVERSATIONS
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Box>
            
            {matches.length === 0 ? (
              <Box sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="body2" sx={{ color: '#555555' }}>
                  No mutual matches found. Accept interests to start messaging!
                </Typography>
              </Box>
            ) : (
              <List sx={{ p: 0 }}>
                {matches.map((match) => {
                  const isSelected = selectedMatch && selectedMatch.userId === match.userId;
                  const photoUrl = match.photos?.find(p => p.main)?.photoUrl || 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=600';
                  return (
                    <ListItem
                      button
                      key={match.id}
                      onClick={() => setSelectedMatch(match)}
                      sx={{
                        backgroundColor: isSelected ? 'rgba(0, 0, 0, 0.05)' : 'transparent',
                        borderLeft: isSelected ? '4px solid #000000' : '4px solid transparent',
                        py: 2,
                        '&:hover': { backgroundColor: 'rgba(0, 0, 0, 0.02)' }
                      }}
                    >
                      <ListItemAvatar>
                        <Avatar
                          src={photoUrl.startsWith('/api') ? 'http://localhost:8080' + photoUrl : photoUrl}
                          sx={{ borderRadius: 0, border: '1px solid #000000', width: 48, height: 48, mr: 1 }}
                        />
                      </ListItemAvatar>
                      <ListItemText
                        primary={match.name}
                        secondary={match.city}
                        primaryTypographyProps={{ fontWeight: isSelected ? 'bold' : 'normal' }}
                        secondaryTypographyProps={{ color: '#555555' }}
                      />
                    </ListItem>
                  );
                })}
              </List>
            )}
          </Grid>

          {/* Main Chat Box */}
          <Grid item xs={12} sm={8} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            {selectedMatch ? (
              <>
                {/* Chat Partner Header */}
                <Box sx={{ p: 3, borderBottom: '1px solid rgba(0, 0, 0, 0.12)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="h5">{selectedMatch.name}</Typography>
                  <Typography variant="caption" sx={{ color: '#555555' }}>
                    {selectedMatch.city}, {selectedMatch.state}
                  </Typography>
                </Box>

                {/* Messages Log */}
                <Box sx={{ flexGrow: 1, p: 3, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {messages.length === 0 ? (
                    <Box sx={{ my: 'auto', mx: 'auto', textAlign: 'center' }}>
                      <Typography variant="body2" sx={{ color: '#555555', fontStyle: 'italic' }}>
                        This is the beginning of your conversation. Write a polite message!
                      </Typography>
                    </Box>
                  ) : (
                    messages.map((msg) => {
                      const isMe = msg.senderName !== selectedMatch.name;
                      return (
                        <Box
                          key={msg.id}
                          sx={{
                            alignSelf: isMe ? 'flex-end' : 'flex-start',
                            maxWidth: '70%',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: isMe ? 'flex-end' : 'flex-start'
                          }}
                        >
                          <Box
                            sx={{
                              backgroundColor: isMe ? '#000000' : '#ffffff',
                              color: isMe ? '#ffffff' : '#000000',
                              border: isMe ? '1px solid #000000' : '1px solid rgba(0, 0, 0, 0.15)',
                              p: 2,
                              fontSize: '0.9rem',
                              lineHeight: 1.4
                            }}
                          >
                            {msg.content}
                          </Box>
                          <Typography variant="caption" sx={{ color: '#888888', mt: 0.5, fontSize: '0.65rem' }}>
                            {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </Typography>
                        </Box>
                      );
                    })
                  )}
                  <div ref={messagesEndRef} />
                </Box>

                {/* Chat Input Field */}
                <Box component="form" onSubmit={handleSend} sx={{ p: 2, borderTop: '1px solid rgba(0, 0, 0, 0.12)', display: 'flex', gap: 2 }}>
                  <TextField
                    fullWidth
                    placeholder="Type your message here..."
                    variant="outlined"
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                  />
                  <Button type="submit" variant="contained" sx={{ px: 4 }}>
                    SEND
                  </Button>
                </Box>
              </>
            ) : (
              <Box sx={{ m: 'auto', textAlign: 'center' }}>
                <Typography variant="h5" sx={{ color: '#555555' }}>
                  SELECT A CONVERSATION TO START MESSAGING
                </Typography>
              </Box>
            )}
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default Chat;
