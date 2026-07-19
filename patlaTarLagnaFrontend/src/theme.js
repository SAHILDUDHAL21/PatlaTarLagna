import { createTheme } from '@mui/material/styles';

const monoTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#000000',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#000000',
      contrastText: '#ffffff',
    },
    background: {
      default: '#ffffff',
      paper: '#ffffff',
    },
    text: {
      primary: '#000000',
      secondary: '#555555',
    },
    divider: 'rgba(0, 0, 0, 0.12)',
  },
  typography: {
    fontFamily: "'Outfit', sans-serif",
    h1: {
      fontFamily: "'Playfair Display', serif",
      fontWeight: 700,
    },
    h2: {
      fontFamily: "'Playfair Display', serif",
      fontWeight: 700,
    },
    h3: {
      fontFamily: "'Playfair Display', serif",
      fontWeight: 700,
    },
    h4: {
      fontFamily: "'Playfair Display', serif",
      fontWeight: 700,
    },
    h5: {
      fontFamily: "'Playfair Display', serif",
      fontWeight: 600,
    },
    h6: {
      fontFamily: "'Playfair Display', serif",
      fontWeight: 600,
    },
    button: {
      textTransform: 'uppercase',
      letterSpacing: '0.1em',
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 0,
          border: '1px solid #000000',
          color: '#000000',
          backgroundColor: '#ffffff',
          transition: 'all 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
          padding: '10px 24px',
          '&:hover': {
            backgroundColor: '#000000',
            color: '#ffffff',
            borderColor: '#000000',
          },
        },
        contained: {
          backgroundColor: '#000000',
          color: '#ffffff',
          border: '1px solid #000000',
          '&:hover': {
            backgroundColor: '#ffffff',
            color: '#000000',
            borderColor: '#000000',
          },
        },
        outlined: {
          borderColor: 'rgba(0, 0, 0, 0.3)',
          '&:hover': {
            backgroundColor: '#000000',
            color: '#ffffff',
            borderColor: '#000000',
          },
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& label.Mui-focused': {
            color: '#000000',
          },
          '& .MuiInput-underline:after': {
            borderBottomColor: '#000000',
          },
          '& .MuiOutlinedInput-root': {
            borderRadius: 0,
            '& fieldset': {
              borderColor: 'rgba(0, 0, 0, 0.15)',
            },
            '&:hover fieldset': {
              borderColor: 'rgba(0, 0, 0, 0.5)',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#000000',
            },
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          borderRadius: 0,
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: 'rgba(0, 0, 0, 0.15)',
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: 'rgba(0, 0, 0, 0.5)',
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: '#000000',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 0,
          border: '1px solid rgba(0, 0, 0, 0.12)',
          backgroundColor: '#ffffff',
          backgroundImage: 'none',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 0,
          backgroundColor: '#ffffff',
          backgroundImage: 'none',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
          backgroundColor: '#ffffff',
          boxShadow: 'none',
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: '#000000',
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          fontFamily: "'Outfit', sans-serif",
          fontSize: '0.9rem',
          letterSpacing: '0.05em',
          color: '#555555',
          '&.Mui-selected': {
            color: '#000000',
          },
        },
      },
    },
  },
});

export default monoTheme;
